package dev.ragnarok.fenrir.view.emoji;

import dev.ragnarok.fenrir.model.StickerSet;

public class StickerSection extends AbsSection {

    public StickerSet stickerSet;

    public StickerSection(StickerSet set) {
        super(TYPE_STICKER);
        stickerSet = set;
    }
}
