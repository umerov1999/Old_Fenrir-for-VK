package dev.ragnarok.fenrir.link.internal;

import org.jetbrains.annotations.NotNull;

public class TopicLink extends AbsInternalLink {

    public int replyToOwner;
    public int topicOwnerId;
    public int replyToCommentId;

    @NotNull
    @Override
    public String toString() {
        return "TopicLink{" +
                "replyToOwner=" + replyToOwner +
                ", topicOwnerId=" + topicOwnerId +
                ", replyToCommentId=" + replyToCommentId +
                "} " + super.toString();
    }
}