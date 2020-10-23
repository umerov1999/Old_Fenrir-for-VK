package dev.ragnarok.fenrir.link.types;

import org.jetbrains.annotations.NotNull;

public class PollLink extends AbsLink {

    public int ownerId;
    public int Id;

    public PollLink(int ownerId, int Id) {
        super(POLL);
        this.Id = Id;
        this.ownerId = ownerId;
    }

    @NotNull
    @Override
    public String toString() {
        return "PollLink{" +
                "ownerId=" + ownerId +
                ", Id=" + Id +
                '}';
    }
}
