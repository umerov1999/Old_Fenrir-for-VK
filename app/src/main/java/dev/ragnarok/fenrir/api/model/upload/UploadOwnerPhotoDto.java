package dev.ragnarok.fenrir.api.model.upload;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

public class UploadOwnerPhotoDto {

    @SerializedName("server")
    public String server;

    @SerializedName("photo")
    public String photo;

    @SerializedName("hash")
    public String hash;

    @NotNull
    @Override
    public String toString() {
        return "UploadOwnerPhotoDto{" +
                "server=" + server +
                ", photo='" + photo + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
