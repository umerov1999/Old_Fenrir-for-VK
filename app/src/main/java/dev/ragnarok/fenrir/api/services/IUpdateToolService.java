package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.response.UpdateToolResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;

public interface IUpdateToolService {

    @GET("current_version.json")
    Single<UpdateToolResponse> get_update_info();
}