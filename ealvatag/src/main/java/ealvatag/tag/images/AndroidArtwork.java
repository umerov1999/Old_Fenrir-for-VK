package ealvatag.tag.images;

import java.io.File;
import java.io.IOException;

public class AndroidArtwork extends AbstractArtwork {

    AndroidArtwork() {
    }

    static AndroidArtwork createArtworkFromFile(File file) throws IOException {
        AndroidArtwork artwork = new AndroidArtwork();
        artwork.setFromFile(file);
        return artwork;
    }

    static AndroidArtwork createLinkedArtworkFromURL(String url) {
        AndroidArtwork artwork = new AndroidArtwork();
        artwork.setImageUrl(url);
        return artwork;
    }

    public boolean setImageFromData() {
        return true;
    }

    public Object getImage() {
        return null;
    }

}
