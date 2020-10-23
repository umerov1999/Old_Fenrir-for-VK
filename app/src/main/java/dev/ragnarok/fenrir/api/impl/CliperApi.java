package dev.ragnarok.fenrir.api.impl;

import dev.ragnarok.fenrir.api.ICliperSeviceProvider;
import dev.ragnarok.fenrir.api.interfaces.ICliperApi;
import dev.ragnarok.fenrir.api.model.CliperResponce;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.Exceptions;

class CliperApi implements ICliperApi {

    private final ICliperSeviceProvider service;

    CliperApi(ICliperSeviceProvider service) {
        this.service = service;
    }

    static CliperResponce extractRawWithErrorHandling(CliperResponce response) {
        if (Objects.nonNull(response.error)) {
            throw Exceptions.propagate(new Exception(response.error));
        }

        if (Utils.isEmpty(response.status) || !response.status.equals("success")) {
            throw Exceptions.propagate(new Exception("Not Success"));
        }

        return response;
    }

    @Override
    public Single<CliperResponce> validate(String login, String access_token, String password, String two_factor_auth) {
        return service.provideCliperService()
                .flatMap(service -> service.validate(login, access_token, password, two_factor_auth)
                        .map(CliperApi::extractRawWithErrorHandling));
    }
}
