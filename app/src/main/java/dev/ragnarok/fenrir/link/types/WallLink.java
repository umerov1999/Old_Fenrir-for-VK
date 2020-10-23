package dev.ragnarok.fenrir.link.types;

import org.jetbrains.annotations.NotNull;

public class WallLink extends AbsLink {

    public int ownerId;

    public WallLink(int ownerId) {
        super(WALL);
        this.ownerId = ownerId;
    }

    @NotNull
    @Override
    public String toString() {
        return "WallLink{" +
                "ownerId=" + ownerId +
                '}';
    }
}
