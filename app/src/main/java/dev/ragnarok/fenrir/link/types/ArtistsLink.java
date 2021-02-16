package dev.ragnarok.fenrir.link.types;

import org.jetbrains.annotations.NotNull;

public class ArtistsLink extends AbsLink {

    public final String Id;

    public ArtistsLink(String Id) {
        super(ARTISTS);
        this.Id = Id;
    }

    @NotNull
    @Override
    public String toString() {
        return "ArtistsLink{" +
                "Id=" + Id +
                '}';
    }
}
