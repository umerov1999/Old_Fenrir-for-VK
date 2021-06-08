package dev.ragnarok.fenrir.api;

import dev.ragnarok.fenrir.api.services.IDonateCheckService;
import io.reactivex.rxjava3.core.Single;

public interface IDonateCheckSeviceProvider {
    Single<IDonateCheckService> provideDonateCheckService();
}
