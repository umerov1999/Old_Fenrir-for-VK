package dev.ragnarok.fenrir.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VkApiAttachments {

    public static final String TYPE_PHOTO = "photo";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_DOC = "doc";
    public static final String TYPE_POST = "wall";
    public static final String TYPE_FAVE_POST = "post";
    public static final String TYPE_LINK = "link";
    public static final String TYPE_NOTE = "note";
    public static final String TYPE_APP = "app";
    public static final String TYPE_AUDIO_PLAYLIST = "audio_playlist";
    public static final String TYPE_POLL = "poll";
    public static final String TYPE_WIKI_PAGE = "page";
    public static final String TYPE_ARTICLE = "article";
    public static final String TYPE_STORY = "story";
    public static final String TYPE_GRAFFITI = "graffiti";
    public static final String TYPE_CALL = "call";
    public static final String TYPE_ALBUM = "album";
    public static final String TYPE_STICKER = "sticker";
    public static final String TYPE_GIFT = "gift";

    public ArrayList<Entry> entries;

    public VkApiAttachments() {
        entries = new ArrayList<>(1);
    }

    public VkApiAttachments(int initialSize) {
        entries = new ArrayList<>(initialSize);
    }

    public List<Entry> entryList() {
        return Collections.unmodifiableList(entries);
    }

    public void append(VKApiAttachment attachment) {
        if (entries == null) {
            entries = new ArrayList<>(1);
        }

        entries.add(new Entry(attachment.getType(), attachment));
    }

    public void clear() {
        if (entries != null) {
            entries.clear();
        }
    }

    public void append(List<? extends VKApiAttachment> data) {
        for (VKApiAttachment attachment : data) {
            append(attachment);
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean nonEmpty() {
        return size() > 0;
    }

    public int size() {
        return entries == null ? 0 : entries.size();
    }

    public static class Entry {

        public final String type;
        public final VKApiAttachment attachment;

        public Entry(String type, VKApiAttachment attachment) {
            this.type = type;
            this.attachment = attachment;
        }
    }
}
