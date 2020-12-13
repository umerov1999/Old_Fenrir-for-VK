package dev.ragnarok.fenrir.domain;

import android.content.Context;

import java.util.List;

import dev.ragnarok.fenrir.model.StickerSet;
import dev.ragnarok.fenrir.model.StickersKeywords;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public interface IStickersInteractor {
    Completable getAndStore(int accountId);

    Single<List<StickerSet>> getStickers(int accountId);

    Single<List<StickersKeywords>> getKeywordsStickers(int accountId);

    Completable PlaceToStickerCache(Context context);
}
