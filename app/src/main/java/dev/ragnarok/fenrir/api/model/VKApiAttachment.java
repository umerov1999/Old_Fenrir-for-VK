package dev.ragnarok.fenrir.api.model;

/**
 * An abstract class for all attachments
 */
public interface VKApiAttachment {

    String TYPE_PHOTO = "photo";
    String TYPE_VIDEO = "video";
    String TYPE_AUDIO = "audio";
    String TYPE_DOC = "doc";
    String TYPE_POST = "wall";
    String TYPE_WALL_REPLY = "wall_reply";
    String TYPE_POSTED_PHOTO = "posted_photo";
    String TYPE_LINK = "link";
    String TYPE_ARTICLE = "article";
    String TYPE_STORY = "story";
    String TYPE_CALL = "call";
    String TYPE_NOT_SUPPORT = "not_support";
    String TYPE_NOTE = "note";
    String TYPE_APP = "app";
    String TYPE_POLL = "poll";
    String TYPE_WIKI_PAGE = "page";
    String TYPE_ALBUM = "album";
    String TYPE_STICKER = "sticker";
    String TYPE_AUDIO_MESSAGE = "audio_message";
    String TYPE_GIFT = "gift";
    String TYPE_GRAFFITY = "graffiti";
    String TYPE_AUDIO_PLAYLIST = "audio_playlist";

    /**
     * @return type of this attachment
     */
    String getType();
}
