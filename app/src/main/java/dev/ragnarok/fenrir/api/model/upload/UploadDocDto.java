package dev.ragnarok.fenrir.api.model.upload;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

public class UploadDocDto {

    @SerializedName("file")
    public String file;

    @NotNull
    @Override
    public String toString() {
        return "UploadDocDto{" +
                "file='" + file + '\'' +
                '}';
    }
}
