package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class AudioCoverAmazon {

    @SerializedName("image")
    public String image;

    @SerializedName("album")
    public String album;

    @SerializedName("message")
    public String message;
}
