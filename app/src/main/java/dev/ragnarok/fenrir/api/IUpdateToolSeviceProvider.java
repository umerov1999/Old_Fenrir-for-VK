package dev.ragnarok.fenrir.api;

import dev.ragnarok.fenrir.api.services.IUpdateToolService;
import io.reactivex.rxjava3.core.Single;

public interface IUpdateToolSeviceProvider {
    Single<IUpdateToolService> provideUpdateToolService();
}
