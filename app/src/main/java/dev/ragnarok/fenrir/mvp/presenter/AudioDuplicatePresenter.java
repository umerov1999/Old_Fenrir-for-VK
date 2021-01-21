package dev.ragnarok.fenrir.mvp.presenter;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;

import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.IdPair;
import dev.ragnarok.fenrir.mvp.presenter.base.RxSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudioDuplicateView;
import dev.ragnarok.fenrir.player.util.MusicUtils;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

import static dev.ragnarok.fenrir.player.util.MusicUtils.observeServiceBinding;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;


public class AudioDuplicatePresenter extends RxSupportPresenter<IAudioDuplicateView> {

    private final int accountId;

    private final Audio new_audio;
    private final Audio old_audio;
    private final IAudioInteractor mAudioInteractor = InteractorFactory.createAudioInteractor();
    private final Disposable mPlayerDisposable;
    private Long oldBitrate;
    private Long newBitrate;
    private boolean needShowBitrateButton = true;
    private Disposable audioListDisposable = Disposable.disposed();

    public AudioDuplicatePresenter(int accountId, Audio new_audio, Audio old_audio, @Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        this.accountId = accountId;
        this.new_audio = new_audio;
        this.old_audio = old_audio;
        mPlayerDisposable = observeServiceBinding()
                .compose(RxUtils.applyObservableIOToMainSchedulers())
                .subscribe(this::onServiceBindEvent);
    }

    private void getMp3AndBitrate() {
        if (Utils.isEmpty(new_audio.getUrl()) || new_audio.isHLS()) {
            audioListDisposable = mAudioInteractor.getByIdOld(accountId, Collections.singletonList(new IdPair(new_audio.getId(), new_audio.getOwnerId()))).compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(t -> getBitrate(t.get(0).getUrl()), e -> getBitrate(new_audio.getUrl()));
        } else {
            getBitrate(new_audio.getUrl());
        }
    }

    private Single<Long> doBitrate(String url) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(Audio.getMp3FromM3u8(url), new HashMap<>());
            String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            if (bitrate != null) {
                return Single.just(Long.parseLong(bitrate) / 1000);
            }
            return Single.error(new Throwable("Can't receipt bitrate "));
        } catch (RuntimeException e) {
            return Single.error(e);
        }
    }

    private void getBitrate(String url) {
        if (Utils.isEmpty(url)) {
            return;
        }
        audioListDisposable = doBitrate(url).compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(r -> {
                    newBitrate = r;
                    callView(o -> o.setNewBitrate(newBitrate));
                }, this::onDataGetError);
    }

    private Single<Long> doLocalBitrate(Context context, String url) {
        try {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.MediaColumns.DATA},
                    BaseColumns._ID + "=? ",
                    new String[]{Uri.parse(url).getLastPathSegment()}, null);
            if (cursor != null && cursor.moveToFirst()) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                String fl = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                retriever.setDataSource(fl);
                cursor.close();
                String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
                if (bitrate != null) {
                    return Single.just(Long.parseLong(bitrate) / 1000);
                }
                return Single.error(new Throwable("Can't receipt bitrate "));
            }
            return Single.error(new Throwable("Can't receipt bitrate "));
        } catch (RuntimeException e) {
            return Single.error(e);
        }
    }

    public void getBitrateAll(@NonNull Context context) {
        if (Utils.isEmpty(old_audio.getUrl())) {
            return;
        }
        needShowBitrateButton = false;
        callView(v -> v.updateShowBitrate(needShowBitrateButton));
        audioListDisposable = doLocalBitrate(context, old_audio.getUrl()).compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(r -> {
                            oldBitrate = r;
                            callView(o -> o.setOldBitrate(oldBitrate));
                            getMp3AndBitrate();
                        },
                        this::onDataGetError);
    }

    private void onServiceBindEvent(@MusicUtils.PlayerStatus int status) {
        switch (status) {
            case MusicUtils.PlayerStatus.UPDATE_TRACK_INFO:
            case MusicUtils.PlayerStatus.SERVICE_KILLED:
            case MusicUtils.PlayerStatus.UPDATE_PLAY_PAUSE:
                callView(v -> v.displayData(new_audio, old_audio));
                break;
            case MusicUtils.PlayerStatus.REPEATMODE_CHANGED:
            case MusicUtils.PlayerStatus.SHUFFLEMODE_CHANGED:
                break;
        }
    }

    @Override
    public void onDestroyed() {
        mPlayerDisposable.dispose();
        audioListDisposable.dispose();
        super.onDestroyed();
    }

    @Override
    public void onGuiCreated(@NonNull IAudioDuplicateView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(new_audio, old_audio);
        viewHost.setNewBitrate(newBitrate);
        viewHost.setOldBitrate(oldBitrate);
        viewHost.updateShowBitrate(needShowBitrateButton);
    }

    private void onDataGetError(Throwable t) {
        showError(getView(), getCauseIfRuntime(t));
    }
}
