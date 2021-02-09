package dev.ragnarok.fenrir.domain;

import java.util.List;

import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.Video;
import io.reactivex.rxjava3.core.Single;

public interface ILocalServerInteractor {
    Single<List<Video>> getVideos(int offset, int count);

    Single<List<Audio>> getAudios(int offset, int count);

    Single<List<Audio>> getDiscography(int offset, int count);

    Single<List<Video>> searchVideos(String q, int offset, int count);

    Single<List<Audio>> searchAudios(String q, int offset, int count);

    Single<List<Audio>> searchDiscography(String q, int offset, int count);

    Single<Integer> update_time(String hash);
}
