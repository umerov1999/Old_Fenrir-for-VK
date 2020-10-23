package dev.ragnarok.fenrir.domain;

import java.util.List;

import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom;
import dev.ragnarok.fenrir.model.Gift;
import io.reactivex.rxjava3.core.Single;

public interface IGiftsInteractor {
    Single<List<Gift>> get(int userId, IntNextFrom start, int count);
}
