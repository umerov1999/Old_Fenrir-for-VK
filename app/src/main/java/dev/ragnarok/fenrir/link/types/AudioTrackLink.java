package dev.ragnarok.fenrir.link.types;

import org.jetbrains.annotations.NotNull;

public class AudioTrackLink extends AbsLink {

    public final int ownerId;
    public final int trackId;

    public AudioTrackLink(int ownerId, int trackId) {
        super(AUDIO_TRACK);
        this.trackId = trackId;
        this.ownerId = ownerId;
    }

    @NotNull
    @Override
    public String toString() {
        return "AudioTrackLink{" +
                "ownerId=" + ownerId +
                ", trackId=" + trackId +
                '}';
    }
}
