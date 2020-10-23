package dev.ragnarok.fenrir.model;

import org.jetbrains.annotations.NotNull;

public class DraftMessage {

    private final int id;

    private final String body;

    private int attachmentsCount;

    public DraftMessage(int id, String body) {
        this.body = body;
        this.id = id;
    }

    public int getAttachmentsCount() {
        return attachmentsCount;
    }

    public void setAttachmentsCount(int attachmentsCount) {
        this.attachmentsCount = attachmentsCount;
    }

    public int getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    @NotNull
    @Override
    public String toString() {
        return "id=" + getId() + ", body='" + getBody() + '\'' + ", count=" + attachmentsCount;
    }
}
