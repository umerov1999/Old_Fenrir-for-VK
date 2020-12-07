package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdateToolResponse {
    @SerializedName("app_id")
    public String app_id;

    @SerializedName("apk_version")
    public int apk_version;

    @SerializedName("changes")
    public String changes;

    @SerializedName("donates")
    public List<Integer> donates;

    @SerializedName("add_likes_to_girl")
    public GirlInfo add_likes_to_girl;

    public static final class GirlInfo {
        @SerializedName("enabled")
        public boolean enabled;

        @SerializedName("type")
        public String type;

        @SerializedName("owner_id")
        public int owner_id;

        @SerializedName("item_id")
        public int item_id;
    }
}
