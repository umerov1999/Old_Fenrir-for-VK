package dev.ragnarok.fenrir.domain.impl;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.VKApiSticker;
import dev.ragnarok.fenrir.api.model.VKApiStickerSet;
import dev.ragnarok.fenrir.api.model.VkApiStickersKeywords;
import dev.ragnarok.fenrir.db.interfaces.IStickersStorage;
import dev.ragnarok.fenrir.db.model.entity.StickerSetEntity;
import dev.ragnarok.fenrir.db.model.entity.StickersKeywordsEntity;
import dev.ragnarok.fenrir.domain.IStickersInteractor;
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity;
import dev.ragnarok.fenrir.domain.mappers.Entity2Model;
import dev.ragnarok.fenrir.model.StickerSet;
import dev.ragnarok.fenrir.model.StickersKeywords;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

import static dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAll;
import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;


public class StickersInteractor implements IStickersInteractor {

    private final INetworker networker;
    private final IStickersStorage storage;

    public StickersInteractor(INetworker networker, IStickersStorage storage) {
        this.networker = networker;
        this.storage = storage;
    }

    @Override
    public Completable getAndStore(int accountId) {
        return networker.vkDefault(accountId)
                .store()
                .getStickers()
                .flatMapCompletable(items -> {
                    List<VKApiStickerSet.Product> list = listEmptyIfNull(items.sticker_pack.items);

                    if (Settings.get().ui().isStickers_by_new()) {
                        Collections.reverse(list);
                    }

                    StickerSetEntity temp = new StickerSetEntity(-1).setTitle("recent")
                            .setStickers(mapAll(listEmptyIfNull(listEmptyIfNull(items.recent.items)), Dto2Entity::mapSticker)).setActive(true).setPurchased(true);
                    List<StickerSetEntity> ret = mapAll(list, Dto2Entity::mapStikerSet);
                    ret.add(temp);
                    if (Settings.get().other().isHint_stickers()) {
                        return storage.store(accountId, ret).andThen(getStickersKeywordsAndStore(accountId, items));
                    } else {
                        return storage.store(accountId, ret);
                    }
                });
    }

    private List<StickersKeywordsEntity> generateKeywords(@NonNull List<List<VKApiSticker>> s, @NonNull List<List<String>> w) {
        List<StickersKeywordsEntity> ret = new ArrayList<>(w.size());
        for (int i = 0; i < w.size(); i++) {
            if (Utils.isEmpty(w.get(i))) {
                continue;
            }
            ret.add(new StickersKeywordsEntity(w.get(i), mapAll(listEmptyIfNull(s.get(i)), Dto2Entity::mapSticker)));
        }
        return ret;
    }

    private Completable getStickersKeywordsAndStore(int accountId, VkApiStickersKeywords items) {
        List<List<VKApiSticker>> s = listEmptyIfNull(items.words_stickers);
        List<List<String>> w = listEmptyIfNull(items.keywords);
        List<StickersKeywordsEntity> temp = new ArrayList<>();
        if (Utils.isEmpty(w) || Utils.isEmpty(s) || w.size() != s.size()) {
            return storage.storeKeyWords(accountId, temp);
        }

        temp.addAll(generateKeywords(s, w));
        return storage.storeKeyWords(accountId, temp);
    }

    @Override
    public Single<List<StickerSet>> getStickers(int accountId) {
        return storage.getPurchasedAndActive(accountId)
                .map(entities -> mapAll(entities, Entity2Model::map));
    }

    @Override
    public Single<List<StickersKeywords>> getKeywordsStickers(int accountId) {
        return storage.getKeywordsStickers(accountId)
                .map(entities -> mapAll(entities, Entity2Model::map));
    }
}