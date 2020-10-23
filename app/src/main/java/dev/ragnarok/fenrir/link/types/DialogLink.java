package dev.ragnarok.fenrir.link.types;

import org.jetbrains.annotations.NotNull;

public class DialogLink extends AbsLink {

    public int peerId;

    public DialogLink(int peerId) {
        super(DIALOG);
        this.peerId = peerId;
    }

    @NotNull
    @Override
    public String toString() {
        return "DialogLink{" +
                "peerId=" + peerId +
                '}';
    }
}
