package dev.ragnarok.fenrir.mvp.view;

import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.mvp.core.IMvpView;


public interface IAudioDuplicateView extends IMvpView, IErrorView {
    void displayData(Audio new_audio, Audio old_audio);

    void setOldBitrate(Long bitrate);

    void setNewBitrate(Long bitrate);

    void updateShowBitrate(boolean needShow);
}
