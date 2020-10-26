package dev.ragnarok.fenrir.link.types;

import org.jetbrains.annotations.NotNull;

public class DocLink extends AbsLink {

    public final int ownerId;
    public final int docId;

    public DocLink(int ownerId, int docId) {
        super(DOC);
        this.docId = docId;
        this.ownerId = ownerId;
    }

    @NotNull
    @Override
    public String toString() {
        return "DocLink{" +
                "ownerId=" + ownerId +
                ", docId=" + docId +
                '}';
    }
}
