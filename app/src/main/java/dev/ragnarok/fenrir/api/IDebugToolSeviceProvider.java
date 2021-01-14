package dev.ragnarok.fenrir.api;

import dev.ragnarok.fenrir.api.services.IDebugToolService;
import io.reactivex.rxjava3.core.Single;

public interface IDebugToolSeviceProvider {
    Single<IDebugToolService> provideDebugToolService();
}
