package ealvatag.tag.images;

import java.io.File;
import java.io.IOException;

import ealvatag.tag.TagOptionSingleton;

/**
 * Get appropriate Artwork class
 */
public class ArtworkFactory {

    public static Artwork getNew() {
        return TagOptionSingleton.getInstance().isAndroid() ? new AndroidArtwork()
                : new StandardArtwork();
    }

    public static Artwork createArtworkFromFile(File file) throws IOException {
        return TagOptionSingleton.getInstance().isAndroid() ? AndroidArtwork.createArtworkFromFile(file)
                : StandardArtwork.createArtworkFromFile(file);
    }

    public static Artwork createLinkedArtworkFromURL(String link) {
        return TagOptionSingleton.getInstance().isAndroid() ? AndroidArtwork.createLinkedArtworkFromURL(link)
                : StandardArtwork.createLinkedArtworkFromURL(link);
    }
}
