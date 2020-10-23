package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class CliperResponce {
    @SerializedName("status")
    public String status;

    @SerializedName("error")
    public String error;
}
