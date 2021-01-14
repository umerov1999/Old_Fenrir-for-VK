package dev.ragnarok.fenrir.api.impl;

import dev.ragnarok.fenrir.api.IDebugToolSeviceProvider;
import dev.ragnarok.fenrir.api.interfaces.IDebugToolApi;
import dev.ragnarok.fenrir.api.model.response.DebugToolResponse;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.Exceptions;

class DebugToolApi implements IDebugToolApi {

    private final IDebugToolSeviceProvider service;

    DebugToolApi(IDebugToolSeviceProvider service) {
        this.service = service;
    }

    static DebugToolResponse extractRawWithErrorHandling(DebugToolResponse response) {
        if (Utils.isEmpty(response.donates)) {
            throw Exceptions.propagate(new Exception("Error get donates from repo"));
        }
        return response;
    }

    @Override
    public Single<DebugToolResponse> call_debugger() {
        return service.provideDebugToolService()
                .flatMap(service -> service.call_debugger()
                        .map(DebugToolApi::extractRawWithErrorHandling));
    }
}
