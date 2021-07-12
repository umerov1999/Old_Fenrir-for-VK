package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DonateCheckResponse {
    @SerializedName("disabled")
    public boolean disabled;
    @SerializedName("show_donate_in_buy")
    public boolean show_donate_in_buy;
    @SerializedName("page")
    public int page;
    @SerializedName("group")
    public int group;
    @SerializedName("donates")
    public List<Integer> donates;
}
