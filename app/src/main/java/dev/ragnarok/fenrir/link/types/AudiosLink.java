package dev.ragnarok.fenrir.link.types;

import org.jetbrains.annotations.NotNull;

public class AudiosLink extends AbsLink {

    public final int ownerId;

    public AudiosLink(int ownerId) {
        super(AUDIOS);
        this.ownerId = ownerId;
    }

    @NotNull
    @Override
    public String toString() {
        return "AudiosLink{" +
                "ownerId=" + ownerId +
                '}';
    }
}
