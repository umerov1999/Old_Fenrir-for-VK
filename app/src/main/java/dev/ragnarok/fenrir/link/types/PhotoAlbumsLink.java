package dev.ragnarok.fenrir.link.types;

import org.jetbrains.annotations.NotNull;

public class PhotoAlbumsLink extends AbsLink {

    public final int ownerId;

    public PhotoAlbumsLink(int ownerId) {
        super(ALBUMS);
        this.ownerId = ownerId;
    }

    @NotNull
    @Override
    public String toString() {
        return "PhotoAlbumsLink{" +
                "ownerId=" + ownerId +
                '}';
    }
}
