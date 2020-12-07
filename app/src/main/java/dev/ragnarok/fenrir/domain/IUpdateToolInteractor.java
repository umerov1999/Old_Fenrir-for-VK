package dev.ragnarok.fenrir.domain;

import dev.ragnarok.fenrir.api.model.response.UpdateToolResponse;
import io.reactivex.rxjava3.core.Single;

public interface IUpdateToolInteractor {
    Single<UpdateToolResponse> get_update_info();
}
