package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.response.DebugToolResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;

public interface IDebugToolService {

    @GET("current_version.json")
    Single<DebugToolResponse> call_debugger();
}
