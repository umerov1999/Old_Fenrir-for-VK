package dev.ragnarok.fenrir.api.interfaces;

import dev.ragnarok.fenrir.api.model.AudioCoverAmazon;
import io.reactivex.rxjava3.core.Single;

public interface IAudioCoverApi {
    Single<AudioCoverAmazon> getAudioCover(String track, String artist);
}
