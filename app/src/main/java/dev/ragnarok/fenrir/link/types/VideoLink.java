package dev.ragnarok.fenrir.link.types;

import org.jetbrains.annotations.NotNull;

public class VideoLink extends AbsLink {

    public int ownerId;
    public int videoId;

    public VideoLink(int ownerId, int videoId) {
        super(VIDEO);
        this.videoId = videoId;
        this.ownerId = ownerId;
    }

    @NotNull
    @Override
    public String toString() {
        return "VideoLink{" +
                "ownerId=" + ownerId +
                ", videoId=" + videoId +
                '}';
    }
}
