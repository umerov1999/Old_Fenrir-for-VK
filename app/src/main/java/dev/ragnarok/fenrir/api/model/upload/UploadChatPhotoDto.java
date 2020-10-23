package dev.ragnarok.fenrir.api.model.upload;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

public class UploadChatPhotoDto {

    @SerializedName("response")
    public String response;

    @NotNull
    @Override
    public String toString() {
        return "UploadChatPhotoDto{" +
                "response='" + response + '\'' +
                '}';
    }
}
