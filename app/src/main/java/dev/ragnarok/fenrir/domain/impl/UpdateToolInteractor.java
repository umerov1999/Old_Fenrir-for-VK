package dev.ragnarok.fenrir.domain.impl;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.response.UpdateToolResponse;
import dev.ragnarok.fenrir.domain.IUpdateToolInteractor;
import io.reactivex.rxjava3.core.Single;

public class UpdateToolInteractor implements IUpdateToolInteractor {

    private final INetworker networker;

    public UpdateToolInteractor(INetworker networker) {
        this.networker = networker;
    }

    @Override
    public Single<UpdateToolResponse> get_update_info() {
        return networker.updateToolApi()
                .get_update_info()
                .map(out -> out);
    }
}
