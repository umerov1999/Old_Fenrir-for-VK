package dev.ragnarok.fenrir.link.types;

import org.jetbrains.annotations.NotNull;

public class OwnerLink extends AbsLink {

    public final int ownerId;

    public OwnerLink(int id) {
        super(id > 0 ? PROFILE : GROUP);
        ownerId = id;
    }

    @NotNull
    @Override
    public String toString() {
        return "OwnerLink{" +
                "ownerId=" + ownerId +
                '}';
    }
}
