package dev.ragnarok.fenrir.api.interfaces;

import dev.ragnarok.fenrir.api.model.response.DonateCheckResponse;
import io.reactivex.rxjava3.core.Single;

public interface IDonateCheckApi {
    Single<DonateCheckResponse> check();
}
