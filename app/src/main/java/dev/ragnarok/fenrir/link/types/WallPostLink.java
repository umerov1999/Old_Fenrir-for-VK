package dev.ragnarok.fenrir.link.types;

import org.jetbrains.annotations.NotNull;

public class WallPostLink extends AbsLink {

    public final int ownerId;
    public final int postId;

    public WallPostLink(int ownerId, int postId) {
        super(WALL_POST);
        this.ownerId = ownerId;
        this.postId = postId;
    }

    @NotNull
    @Override
    public String toString() {
        return "WallPostLink{" +
                "ownerId=" + ownerId +
                ", postId=" + postId +
                '}';
    }
}
