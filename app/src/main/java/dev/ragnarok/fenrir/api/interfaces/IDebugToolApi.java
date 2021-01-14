package dev.ragnarok.fenrir.api.interfaces;

import dev.ragnarok.fenrir.api.model.response.DebugToolResponse;
import io.reactivex.rxjava3.core.Single;

public interface IDebugToolApi {
    Single<DebugToolResponse> call_debugger();
}
