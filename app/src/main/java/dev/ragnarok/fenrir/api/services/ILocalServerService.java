package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ILocalServerService {

    @FormUrlEncoded
    @POST("audio.get")
    Single<BaseResponse<Items<VKApiAudio>>> getAudios(@Field("offset") Integer offset,
                                                      @Field("count") Integer count);

    @FormUrlEncoded
    @POST("discography.get")
    Single<BaseResponse<Items<VKApiAudio>>> getDiscography(@Field("offset") Integer offset,
                                                           @Field("count") Integer count);

    @FormUrlEncoded
    @POST("video.get")
    Single<BaseResponse<Items<VKApiVideo>>> getVideos(@Field("offset") Integer offset,
                                                      @Field("count") Integer count);

    @FormUrlEncoded
    @POST("audio.search")
    Single<BaseResponse<Items<VKApiAudio>>> searchAudios(@Field("q") String query,
                                                         @Field("offset") Integer offset,
                                                         @Field("count") Integer count);

    @FormUrlEncoded
    @POST("discography.search")
    Single<BaseResponse<Items<VKApiAudio>>> searchDiscography(@Field("q") String query,
                                                              @Field("offset") Integer offset,
                                                              @Field("count") Integer count);

    @FormUrlEncoded
    @POST("video.search")
    Single<BaseResponse<Items<VKApiVideo>>> searchVideos(@Field("q") String query,
                                                         @Field("offset") Integer offset,
                                                         @Field("count") Integer count);

    @FormUrlEncoded
    @POST("update_time")
    Single<BaseResponse<Integer>> update_time(@Field("hash") String hash);
}
