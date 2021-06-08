package dev.ragnarok.fenrir.domain;

import dev.ragnarok.fenrir.api.model.response.DonateCheckResponse;
import io.reactivex.rxjava3.core.Single;

public interface IDonateCheckInteractor {
    Single<DonateCheckResponse> check();
}
