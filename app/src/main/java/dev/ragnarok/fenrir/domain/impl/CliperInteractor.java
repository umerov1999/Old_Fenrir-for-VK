package dev.ragnarok.fenrir.domain.impl;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.CliperResponce;
import dev.ragnarok.fenrir.domain.ICliperInteractor;
import io.reactivex.rxjava3.core.Single;

public class CliperInteractor implements ICliperInteractor {

    private final INetworker networker;

    public CliperInteractor(INetworker networker) {
        this.networker = networker;
    }

    @Override
    public Single<CliperResponce> validate(String login, String access_token, String password, String two_factor_auth) {
        return networker.cliperApi()
                .validate(login, access_token, password, two_factor_auth)
                .map(out -> out);
    }
}
