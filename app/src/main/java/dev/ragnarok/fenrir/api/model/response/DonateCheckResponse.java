package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DonateCheckResponse {
    @SerializedName("donates")
    public List<Integer> donates;
}
