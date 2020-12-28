package dev.ragnarok.fenrir.mvp.presenter;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.AttachmenEntry;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.reflect.OnGuiCreated;
import dev.ragnarok.fenrir.mvp.view.IBaseAttachmentsEditView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.IUploadManager;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.util.FileUtil;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Predicate;

import static dev.ragnarok.fenrir.util.AppPerms.hasCameraPermision;
import static dev.ragnarok.fenrir.util.AppPerms.hasReadStoragePermision;
import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.findInfoByPredicate;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOfMultiple;


public abstract class AbsAttachmentsEditPresenter<V extends IBaseAttachmentsEditView>
        extends AccountDependencyPresenter<V> {

    private static final String SAVE_DATA = "save_data";
    private static final String SAVE_TIMER = "save_timer";
    private static final String SAVE_BODY = "save_body";
    private static final String SAVE_CURRENT_PHOTO_CAMERA_URI = "save_current_photo_camera_uri";
    final IUploadManager uploadManager;
    private final ArrayList<AttachmenEntry> data;
    private String textBody;
    private Uri currentPhotoCameraUri;
    private Long timerValue;

    AbsAttachmentsEditPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        uploadManager = Injection.provideUploadManager();

        if (nonNull(savedInstanceState)) {
            currentPhotoCameraUri = savedInstanceState.getParcelable(SAVE_CURRENT_PHOTO_CAMERA_URI);
            textBody = savedInstanceState.getString(SAVE_BODY);
            timerValue = savedInstanceState.containsKey(SAVE_TIMER) ? savedInstanceState.getLong(SAVE_TIMER) : null;
        }

        data = new ArrayList<>();
        if (nonNull(savedInstanceState)) {
            ArrayList<AttachmenEntry> savedEntries = savedInstanceState.getParcelableArrayList(SAVE_DATA);
            if (nonEmpty(savedEntries)) {
                data.addAll(savedEntries);
            }
        }
    }

    static List<AttachmenEntry> createFrom(List<Upload> objects) {
        List<AttachmenEntry> data = new ArrayList<>(objects.size());
        for (Upload object : objects) {
            data.add(new AttachmenEntry(true, object));
        }

        return data;
    }

    static List<AttachmenEntry> createFrom(List<Pair<Integer, AbsModel>> pairs, boolean canDelete) {
        List<AttachmenEntry> data = new ArrayList<>(pairs.size());
        for (Pair<Integer, AbsModel> pair : pairs) {
            data.add(new AttachmenEntry(canDelete, pair.getSecond()).setOptionalId(pair.getFirst()));
        }
        return data;
    }

    Long getTimerValue() {
        return timerValue;
    }

    void setTimerValue(Long timerValue) {
        this.timerValue = timerValue;
        resolveTimerInfoView();
    }

    ArrayList<AttachmenEntry> getData() {
        return data;
    }

    @OnGuiCreated
    void resolveTimerInfoView() {
        if (isGuiReady()) {
            getView().setTimerValue(timerValue);
        }
    }

    @OnGuiCreated
    void resolveTextView() {
        if (isGuiReady()) {
            getView().setTextBody(textBody);
        }
    }

    String getTextBody() {
        return textBody;
    }

    void setTextBody(String body) {
        textBody = body;
        resolveTextView();
    }

    ArrayList<AttachmenEntry> getNeedParcelSavingEntries() {
        return new ArrayList<>(0);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_CURRENT_PHOTO_CAMERA_URI, currentPhotoCameraUri);
        outState.putParcelableArrayList(SAVE_DATA, getNeedParcelSavingEntries());
        outState.putString(SAVE_BODY, textBody);
        if (nonNull(timerValue)) {
            outState.putLong(SAVE_TIMER, timerValue);
        }
    }

    void onUploadProgressUpdate(List<IUploadManager.IProgressUpdate> updates) {
        for (IUploadManager.IProgressUpdate update : updates) {
            Predicate<AttachmenEntry> predicate = entry -> entry.getAttachment() instanceof Upload
                    && ((Upload) entry.getAttachment()).getId() == update.getId();

            Pair<Integer, AttachmenEntry> info = findInfoByPredicate(getData(), predicate);

            if (nonNull(info)) {
                AttachmenEntry entry = info.getSecond();

                Upload object = (Upload) entry.getAttachment();
                if (object.getStatus() != Upload.STATUS_UPLOADING) {
                    continue;
                }

                callView(v -> v.updateProgressAtIndex(entry.getId(), update.getProgress()));
            }
        }
    }

    void onUploadObjectRemovedFromQueue(int[] ids) {
        for (int id : ids) {
            int index = findUploadIndexById(id);
            if (index != -1) {
                manuallyRemoveElement(index);
            }
        }
    }

    void onUploadQueueUpdates(List<Upload> updates, Predicate<Upload> predicate) {
        int startSize = data.size();
        int count = 0;

        for (Upload u : updates) {
            if (predicate.test(u)) {
                data.add(new AttachmenEntry(true, u));
                count++;
            }
        }

        if (count > 0) {
            safelyNotifyItemsAdded(startSize, count);
        }
    }

    void safelyNotifyItemsAdded(int position, int count) {
        if (isGuiReady()) {
            getView().notifyItemRangeInsert(position, count);
        }
    }

    List<AttachmenEntry> combine(List<AttachmenEntry> first, List<AttachmenEntry> second) {
        List<AttachmenEntry> data = new ArrayList<>(safeCountOfMultiple(first, second));
        data.addAll(first);
        data.addAll(second);
        return data;
    }

    void onUploadStatusUpdate(Upload update) {
        int index = findUploadIndexById(update.getId());
        if (index != -1) {
            safeNotifyDataSetChanged();
        }
    }

    @Override
    public void onGuiCreated(@NonNull V viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayInitialModels(data);
    }

    public final void fireRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        if (attachment.getAttachment() instanceof Upload) {
            Upload upload = (Upload) attachment.getAttachment();
            uploadManager.cancel(upload.getId());
            return;
        }

        onAttachmentRemoveClick(index, attachment);
    }

    void safelyNotifyItemRemoved(int position) {
        if (isGuiReady()) {
            getView().notifyItemRemoved(position);
        }
    }

    void onAttachmentRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        throw new UnsupportedOperationException();
    }

    void manuallyRemoveElement(int index) {
        data.remove(index);
        safelyNotifyItemRemoved(index);

        //safeNotifyDataSetChanged();
    }

    public final void fireTitleClick(int index, @NonNull AttachmenEntry attachment) {

    }

    private int getMaxCountOfAttachments() {
        return 10;
    }

    private boolean canAttachMore() {
        return data.size() < getMaxCountOfAttachments();
    }

    private int getMaxFutureAttachmentCount() {
        int count = data.size() - getMaxCountOfAttachments();
        return Math.max(count, 0);
    }

    public final void firePhotoFromVkChoose() {
        getView().openAddVkPhotosWindow(getMaxFutureAttachmentCount(), getAccountId(), getAccountId());
    }

    private boolean checkAbilityToAttachMore() {
        if (canAttachMore()) {
            return true;
        } else {
            safeShowError(getView(), R.string.reached_maximum_count_of_attachments);
            return false;
        }
    }

    public final void firePhotoFromLocalGalleryChoose() {
        if (!hasReadStoragePermision(getApplicationContext())) {
            getView().requestReadExternalStoragePermission();
            return;
        }

        getView().openAddPhotoFromGalleryWindow(getMaxFutureAttachmentCount());
    }

    public final void firePhotoFromCameraChoose() {
        if (!hasCameraPermision(getApplicationContext())) {
            getView().requestCameraPermission();
            return;
        }

        createImageFromCamera();
    }

    private void createImageFromCamera() {
        try {
            File photoFile = FileUtil.createImageFile();
            currentPhotoCameraUri = FileUtil.getExportedUriForFile(getApplicationContext(), photoFile);

            getView().openCamera(currentPhotoCameraUri);
        } catch (IOException e) {
            safeShowError(getView(), e.getMessage());
        }
    }

    public final void firePhotoMaked() {
        getView().notifySystemAboutNewPhoto(currentPhotoCameraUri);

        LocalPhoto makedPhoto = new LocalPhoto().setFullImageUri(currentPhotoCameraUri);
        doUploadPhotos(Collections.singletonList(makedPhoto));
    }

    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        throw new UnsupportedOperationException();
    }

    private void doUploadPhotos(List<LocalPhoto> photos) {
        Integer size = Settings.get()
                .main()
                .getUploadImageSize();

        if (isNull(size)) {
            getView().displaySelectUploadPhotoSizeDialog(photos);
        } else {
            doUploadPhotos(photos, size);
        }
    }

    public final void firePhotosFromGallerySelected(ArrayList<LocalPhoto> photos) {
        doUploadPhotos(photos);
    }

    public final void fireButtonPhotoClick() {
        if (checkAbilityToAttachMore()) {
            getView().displayChoosePhotoTypeDialog();
        }
    }

    public final void fireButtonAudioClick() {
        if (checkAbilityToAttachMore()) {
            getView().openAddAudiosWindow(getMaxFutureAttachmentCount(), getAccountId());
        }
    }

    public final void fireButtonVideoClick() {
        if (checkAbilityToAttachMore()) {
            getView().openAddVideosWindow(getMaxFutureAttachmentCount(), getAccountId());
        }
    }

    public final void fireButtonDocClick() {
        if (checkAbilityToAttachMore()) {
            getView().openAddDocumentsWindow(getMaxFutureAttachmentCount(), getAccountId());
        }
    }

    protected void onPollCreateClick() {
        throw new UnsupportedOperationException();
    }

    protected void onTimerClick() {
        throw new UnsupportedOperationException();
    }

    public final void fireButtonPollClick() {
        onPollCreateClick();
    }

    public final void fireButtonTimerClick() {
        onTimerClick();
    }

    protected void onModelsAdded(List<? extends AbsModel> models) {
        for (AbsModel model : models) {
            data.add(new AttachmenEntry(true, model));
        }

        safeNotifyDataSetChanged();
    }

    public final void fireAttachmentsSelected(@NonNull ArrayList<? extends AbsModel> attachments) {
        onModelsAdded(attachments);
    }

    public void fireUploadPhotoSizeSelected(@NonNull List<LocalPhoto> photos, int size) {
        doUploadPhotos(photos, size);
    }

    public final void firePollCreated(@NonNull Poll poll) {
        onModelsAdded(Collections.singletonList(poll));
    }

    protected void safeNotifyDataSetChanged() {
        if (isGuiReady()) {
            getView().notifyDataSetChanged();
        }
    }

    public final void fireTextChanged(CharSequence s) {
        textBody = isNull(s) ? null : s.toString();
    }

    public final void fireVkPhotosSelected(@NonNull ArrayList<Photo> photos) {
        onModelsAdded(photos);
    }

    public void fireCameraPermissionResolved() {
        if (hasCameraPermision(getApplicationContext())) {
            createImageFromCamera();
        }
    }

    public void fireReadStoragePermissionResolved() {
        if (hasReadStoragePermision(getApplicationContext())) {
            getView().openAddPhotoFromGalleryWindow(getMaxFutureAttachmentCount());
        }
    }

    boolean hasUploads() {
        for (AttachmenEntry entry : data) {
            if (entry.getAttachment() instanceof Upload) {
                return true;
            }
        }

        return false;
    }

    int findUploadIndexById(int id) {
        for (int i = 0; i < data.size(); i++) {
            AttachmenEntry item = data.get(i);
            if (item.getAttachment() instanceof Upload && ((Upload) item.getAttachment()).getId() == id) {
                return i;
            }
        }

        return -1;
    }

    public void fireTimerTimeSelected(long unixtime) {
        throw new UnsupportedOperationException();
    }
}
