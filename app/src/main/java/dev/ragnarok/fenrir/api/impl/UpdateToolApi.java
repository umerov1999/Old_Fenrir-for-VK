package dev.ragnarok.fenrir.api.impl;

import dev.ragnarok.fenrir.api.IUpdateToolSeviceProvider;
import dev.ragnarok.fenrir.api.interfaces.IUpdateToolApi;
import dev.ragnarok.fenrir.api.model.response.UpdateToolResponse;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.Exceptions;

class UpdateToolApi implements IUpdateToolApi {

    private final IUpdateToolSeviceProvider service;

    UpdateToolApi(IUpdateToolSeviceProvider service) {
        this.service = service;
    }

    static UpdateToolResponse extractRawWithErrorHandling(UpdateToolResponse response) {
        if (Utils.isEmpty(response.app_id)) {
            throw Exceptions.propagate(new Exception("Error get update from repo"));
        }

        return response;
    }

    @Override
    public Single<UpdateToolResponse> get_update_info() {
        return service.provideUpdateToolService()
                .flatMap(service -> service.get_update_info()
                        .map(UpdateToolApi::extractRawWithErrorHandling));
    }
}
