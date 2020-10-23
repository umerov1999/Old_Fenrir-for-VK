package ealvatag.tag.images;

import java.io.File;
import java.io.IOException;

/**
 * Represents artwork in a format independent  way
 */
public interface Artwork {
    byte[] getBinaryData();

    Artwork setBinaryData(byte[] binaryData);

    String getMimeType();

    Artwork setMimeType(String mimeType);

    String getDescription();

    Artwork setDescription(String description);

    int getHeight();

    Artwork setHeight(int height);

    int getWidth();

    Artwork setWidth(int width);

    boolean isLinked();

    Artwork setLinked(boolean linked);

    String getImageUrl();

    Artwork setImageUrl(String imageUrl);

    int getPictureType();

    Artwork setPictureType(int pictureType);

    Artwork setFromFile(File file) throws IOException;
}
