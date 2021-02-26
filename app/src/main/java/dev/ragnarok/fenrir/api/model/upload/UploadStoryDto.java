package dev.ragnarok.fenrir.api.model.upload;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

public class UploadStoryDto {

    @SerializedName("upload_result")
    public String upload_result;

    @NotNull
    @Override
    public String toString() {
        return "UploadStoryDto{" +
                "upload_result='" + upload_result + '\'' +
                '}';
    }
}
