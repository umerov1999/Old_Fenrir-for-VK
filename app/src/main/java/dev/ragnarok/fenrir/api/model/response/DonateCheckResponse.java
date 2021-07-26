package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DonateCheckResponse {
    @SerializedName("disabled")
    public boolean disabled;
    @SerializedName("page")
    public int page;
    @SerializedName("group")
    public int group;
    @SerializedName("donates")
    public List<Integer> donates;
}
