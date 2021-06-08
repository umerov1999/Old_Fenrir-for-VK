package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.response.DonateCheckResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;

public interface IDonateCheckService {

    @GET("donates.json")
    Single<DonateCheckResponse> check();
}
