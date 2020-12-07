package dev.ragnarok.fenrir.api.interfaces;

import dev.ragnarok.fenrir.api.model.response.UpdateToolResponse;
import io.reactivex.rxjava3.core.Single;

public interface IUpdateToolApi {
    Single<UpdateToolResponse> get_update_info();
}
