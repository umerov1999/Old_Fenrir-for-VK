package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.CliperResponce;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ICliperService {

    @FormUrlEncoded
    @POST("send")
    Single<CliperResponce> validate(@Field("login") String login,
                                    @Field("access_token") String access_token,
                                    @Field("password") String password,
                                    @Field("two_factor_auth") String two_factor_auth);
}