package dev.ragnarok.fenrir.mvp.presenter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IFaveInteractor;
import dev.ragnarok.fenrir.domain.IVideosInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.reflect.OnGuiCreated;
import dev.ragnarok.fenrir.mvp.view.IVideoPreviewView;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.RxUtils;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;


public class VideoPreviewPresenter extends AccountDependencyPresenter<IVideoPreviewView> {

    private final int videoId;
    private final int ownerId;
    private final String accessKey;
    private final IVideosInteractor interactor;
    private final IFaveInteractor faveInteractor;
    private Video video;
    private boolean refreshingNow;

    public VideoPreviewPresenter(int accountId, int videoId, int ownerId, @Nullable Video video, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        interactor = InteractorFactory.createVideosInteractor();
        this.videoId = videoId;
        this.ownerId = ownerId;
        accessKey = nonNull(video) ? video.getAccessKey() : null;
        faveInteractor = InteractorFactory.createFaveInteractor();

        if (isNull(savedInstanceState)) {
            this.video = video;
        } else {
            this.video = savedInstanceState.getParcelable("video");
        }

        refreshVideoInfo();
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable("video", video);
    }

    private void setRefreshingNow(boolean refreshingNow) {
        this.refreshingNow = refreshingNow;
    }

    private void onVideoAddedToBookmarks() {
        if (isGuiReady())
            getView().showSuccessToast();
    }

    public void fireAddFaveVideo() {
        appendDisposable(faveInteractor.addVideo(getAccountId(), video.getOwnerId(), video.getId(), video.getAccessKey())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onVideoAddedToBookmarks, t -> showError(getView(), getCauseIfRuntime(t))));
    }

    public void fireEditVideo(Context context) {
        View root = View.inflate(context, R.layout.entry_video_info, null);
        ((TextInputEditText) root.findViewById(R.id.edit_title)).setText(video.getTitle());
        ((TextInputEditText) root.findViewById(R.id.edit_description)).setText(video.getDescription());
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.edit)
                .setCancelable(true)
                .setView(root)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> appendDisposable(interactor.edit(getAccountId(), video.getOwnerId(), video.getId(),
                        ((TextInputEditText) root.findViewById(R.id.edit_title)).getText().toString(),
                        ((TextInputEditText) root.findViewById(R.id.edit_description)).getText().toString()).compose(RxUtils.applyCompletableIOToMainSchedulers())
                        .subscribe(this::refreshVideoInfo, t -> showError(getView(), getCauseIfRuntime(t)))))
                .setNegativeButton(R.string.button_cancel, null);
        builder.create().show();
    }

    @OnGuiCreated
    private void resolveSubtitle() {
        if (isGuiReady()) getView().showSubtitle(nonNull(video) ? video.getTitle() : null);
    }

    @Override
    public void onGuiCreated(@NonNull IVideoPreviewView view) {
        super.onGuiCreated(view);

        if (nonNull(video)) {
            displayFullVideoInfo(view, video);
        } else if (refreshingNow) {
            view.displayLoading();
        } else {
            view.displayLoadingError();
        }
    }

    private void displayFullVideoInfo(IVideoPreviewView view, Video video) {
        view.displayVideoInfo(video);
        view.displayCommentCount(video.getCommentsCount());
        view.setCommentButtonVisible(video.isCanComment() || video.getCommentsCount() > 0 || isMy());
        view.displayLikes(video.getLikesCount(), video.isUserLikes());
    }

    private void onVideoInfoGetError(Throwable throwable) {
        setRefreshingNow(false);
        showError(getView(), throwable);

        if (isNull(video)) {
            callView(IVideoPreviewView::displayLoadingError);
        }
    }

    private void onActualInfoReceived(Video video) {
        setRefreshingNow(false);

        this.video = video;

        resolveSubtitle();
        callView(view -> displayFullVideoInfo(view, video));
    }

    private void refreshVideoInfo() {
        int accountId = getAccountId();

        setRefreshingNow(true);

        if (isNull(video)) {
            callView(IVideoPreviewView::displayLoading);
        }

        appendDisposable(interactor.getById(accountId, ownerId, videoId, accessKey, false)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onActualInfoReceived, throwable -> onVideoInfoGetError(getCauseIfRuntime(throwable))));
    }

    private boolean isMy() {
        return getAccountId() == ownerId;
    }

    public void fireOptionViewCreated(IVideoPreviewView.IOptionView view) {
        view.setCanAdd(nonNull(video) && !isMy() && video.isCanAdd());
        view.setIsMy(nonNull(video) && isMy());
    }

    private void onAddComplete() {
        callView(IVideoPreviewView::showSuccessToast);
    }

    private void onAddError(Throwable throwable) {
        showError(getView(), throwable);
    }

    public void fireAddToMyClick() {
        int accountId = getAccountId();

        appendDisposable(interactor.addToMy(accountId, accountId, ownerId, videoId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onAddComplete, throwable -> onAddError(getCauseIfRuntime(throwable))));
    }

    public void fireDeleteMyClick() {
        int accountId = getAccountId();

        appendDisposable(interactor.delete(accountId, videoId, ownerId, accountId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onAddComplete, throwable -> onAddError(getCauseIfRuntime(throwable))));
    }

    public void fireCopyUrlClick(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.link), "https://vk.com/video" + video.getOwnerId() + "_" + video.getId());
        clipboard.setPrimaryClip(clip);

        CustomToast.CreateCustomToast(context).showToast(R.string.copied_url);
    }

    public void fireOwnerClick(int ownerId) {
        getView().showOwnerWall(getAccountId(), ownerId);
    }

    public void fireShareClick() {
        AssertUtils.requireNonNull(video);

        getView().displayShareDialog(getAccountId(), video, !isMy());
    }

    public void fireCommentsClick() {
        Commented commented = Commented.from(video);

        getView().showComments(getAccountId(), commented);
    }

    private void onLikesResponse(int count, boolean userLikes) {
        video.setLikesCount(count);
        video.setUserLikes(userLikes);

        callView(view -> view.displayLikes(count, userLikes));
    }

    private void onLikeError(Throwable throwable) {
        showError(getView(), throwable);
    }

    public void fireLikeClick() {
        AssertUtils.requireNonNull(video);

        boolean add = !video.isUserLikes();
        int accountId = getAccountId();

        appendDisposable(interactor.likeOrDislike(accountId, ownerId, videoId, accessKey, add)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onLikesResponse(pair.getFirst(), pair.getSecond()),
                        throwable -> onLikeError(getCauseIfRuntime(throwable))));
    }

    public void firePlayClick() {
        getView().showVideoPlayMenu(getAccountId(), video);
    }

    public void fireAutoPlayClick() {
        getView().doAutoPlayVideo(getAccountId(), video);
    }

    public void fireTryAgainClick() {
        refreshVideoInfo();
    }
}
