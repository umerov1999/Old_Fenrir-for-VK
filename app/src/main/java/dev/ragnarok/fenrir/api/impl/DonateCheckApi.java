package dev.ragnarok.fenrir.api.impl;

import dev.ragnarok.fenrir.api.IDonateCheckSeviceProvider;
import dev.ragnarok.fenrir.api.interfaces.IDonateCheckApi;
import dev.ragnarok.fenrir.api.model.response.DonateCheckResponse;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.Exceptions;

class DonateCheckApi implements IDonateCheckApi {

    private final IDonateCheckSeviceProvider service;

    DonateCheckApi(IDonateCheckSeviceProvider service) {
        this.service = service;
    }

    static DonateCheckResponse extractRawWithErrorHandling(DonateCheckResponse response) {
        if (Utils.isEmpty(response.donates)) {
            throw Exceptions.propagate(new Exception("Error get donates from repo"));
        }
        return response;
    }

    @Override
    public Single<DonateCheckResponse> check() {
        return service.provideDonateCheckService()
                .flatMap(service -> service.check()
                        .map(DonateCheckApi::extractRawWithErrorHandling));
    }
}
