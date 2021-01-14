package dev.ragnarok.fenrir.domain.impl;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.response.DebugToolResponse;
import dev.ragnarok.fenrir.domain.IDebugToolInteractor;
import io.reactivex.rxjava3.core.Single;

public class DebugToolInteractor implements IDebugToolInteractor {

    private final INetworker networker;

    public DebugToolInteractor(INetworker networker) {
        this.networker = networker;
    }

    @Override
    public Single<DebugToolResponse> call_debugger() {
        return networker.debugToolApi()
                .call_debugger()
                .map(out -> out);
    }
}
