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
}
