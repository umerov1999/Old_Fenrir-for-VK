package dev.ragnarok.fenrir.api.model.upload;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

public class UploadAudioDto {

    @SerializedName("server")
    public String server;

    @SerializedName("audio")
    public String audio;

    @SerializedName("hash")
    public String hash;

    @NotNull
    @Override
    public String toString() {
        return "UploadAudioDto{" +
                "server='" + server + '\'' +
                ", audio='" + audio + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
