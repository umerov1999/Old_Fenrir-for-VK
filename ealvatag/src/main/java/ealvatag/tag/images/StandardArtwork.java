package ealvatag.tag.images;

import java.io.File;
import java.io.IOException;

public class StandardArtwork extends AbstractArtwork {

    StandardArtwork() {
    }

    static StandardArtwork createArtworkFromFile(File file) throws IOException {
        StandardArtwork artwork = new StandardArtwork();
        artwork.setFromFile(file);
        return artwork;
    }

    static StandardArtwork createLinkedArtworkFromURL(String url) {
        StandardArtwork artwork = new StandardArtwork();
        artwork.setImageUrl(url);
        return artwork;
    }

}
