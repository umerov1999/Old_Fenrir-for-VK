package dev.ragnarok.fenrir.domain.impl;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.response.DonateCheckResponse;
import dev.ragnarok.fenrir.domain.IDonateCheckInteractor;
import io.reactivex.rxjava3.core.Single;

public class DonateCheckInteractor implements IDonateCheckInteractor {

    private final INetworker networker;

    public DonateCheckInteractor(INetworker networker) {
        this.networker = networker;
    }

    @Override
    public Single<DonateCheckResponse> check() {
        return networker.donateCheckApi()
                .check()
                .map(out -> out);
    }
}
