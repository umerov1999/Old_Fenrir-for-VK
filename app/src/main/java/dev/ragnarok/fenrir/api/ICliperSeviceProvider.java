package dev.ragnarok.fenrir.api;

import dev.ragnarok.fenrir.api.services.ICliperService;
import io.reactivex.rxjava3.core.Single;

public interface ICliperSeviceProvider {
    Single<ICliperService> provideCliperService();
}
