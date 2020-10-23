package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class VkApiStickersKeywords {

    @SerializedName("words_stickers")
    public List<List<VKApiSticker>> words_stickers;

    @SerializedName("keywords")
    public List<List<String>> keywords;

    @SerializedName("recent")
    public Items<VKApiSticker> recent;

    @SerializedName("sticker_pack")
    public Items<VKApiStickerSet.Product> sticker_pack;

}
