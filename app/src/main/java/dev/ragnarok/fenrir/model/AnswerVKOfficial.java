package dev.ragnarok.fenrir.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.util.Utils;

public class AnswerVKOfficial {
    public String footer;
    public String header;
    public String text;
    public String iconURL;
    public String iconType;
    public Long time;
    public List<ImageAdditional> images;
    public List<Photo> attachments;

    public ImageAdditional getImage(int prefSize) {
        ImageAdditional result = null;
        if (Utils.isEmpty(images))
            return null;

        for (ImageAdditional image : images) {
            if (result == null) {
                result = image;
                continue;
            }
            if (Math.abs(image.calcAverageSize() - prefSize) < Math.abs(result.calcAverageSize() - prefSize)) {
                result = image;
            }
        }
        return result;
    }

    public static final class ImageAdditional {
        @SerializedName("url")
        public String url;
        @SerializedName("width")
        public int width;
        @SerializedName("height")
        public int height;

        private int calcAverageSize() {
            return (width + height) / 2;
        }
    }

    public static final class Attachment {
        @SerializedName("type")
        public String type;
        @SerializedName("object_id")
        public String object_id;
    }
}
