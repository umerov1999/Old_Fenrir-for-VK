package dev.ragnarok.fenrir.api;

import dev.ragnarok.fenrir.api.services.IAudioCoverService;
import io.reactivex.rxjava3.core.Single;

public interface IAudioCoverSeviceProvider {
    Single<IAudioCoverService> provideAudioCoverService();
}
