package dev.ragnarok.fenrir.api.model.upload;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

public class UploadPhotoToAlbumDto {

    @SerializedName("server")
    public int server;

    @SerializedName("photos_list")
    public String photos_list;

    @SerializedName("aid")
    public int aid;

    @SerializedName("hash")
    public String hash;

    @NotNull
    @Override
    public String toString() {
        return "UploadPhotoToAlbumDto{" +
                "server=" + server +
                ", photos_list='" + photos_list + '\'' +
                ", aid=" + aid +
                ", hash='" + hash + '\'' +
                '}';
    }
}
