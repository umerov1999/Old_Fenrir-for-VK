package dev.ragnarok.fenrir.domain;

import dev.ragnarok.fenrir.api.model.response.DebugToolResponse;
import io.reactivex.rxjava3.core.Single;

public interface IDebugToolInteractor {
    Single<DebugToolResponse> call_debugger();
}
