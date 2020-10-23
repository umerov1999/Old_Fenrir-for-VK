package dev.ragnarok.fenrir.api.impl;

import dev.ragnarok.fenrir.api.IAudioCoverSeviceProvider;
import dev.ragnarok.fenrir.api.interfaces.IAudioCoverApi;
import dev.ragnarok.fenrir.api.model.AudioCoverAmazon;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.Exceptions;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

class AudioCoverApi implements IAudioCoverApi {

    private final IAudioCoverSeviceProvider service;

    AudioCoverApi(IAudioCoverSeviceProvider service) {
        this.service = service;
    }

    static AudioCoverAmazon extractRawWithErrorHandling(AudioCoverAmazon response) {
        if (nonNull(response.message)) {
            throw Exceptions.propagate(new Exception(response.message));
        }

        return response;
    }

    @Override
    public Single<AudioCoverAmazon> getAudioCover(String track, String artist) {
        return service.provideAudioCoverService()
                .flatMap(service -> service.getAudioCover(track, artist)
                        .map(AudioCoverApi::extractRawWithErrorHandling));
    }
}
