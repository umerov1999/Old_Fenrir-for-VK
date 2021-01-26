package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import io.reactivex.rxjava3.core.Single;

public interface ILocalServerApi {
    @CheckResult
    Single<Items<VKApiVideo>> getVideos(Integer offset, Integer count);

    @CheckResult
    Single<Items<VKApiAudio>> getAudios(Integer offset, Integer count);

    @CheckResult
    Single<Items<VKApiVideo>> searchVideos(String query, Integer offset, Integer count);

    @CheckResult
    Single<Items<VKApiAudio>> searchAudios(String query, Integer offset, Integer count);
}
