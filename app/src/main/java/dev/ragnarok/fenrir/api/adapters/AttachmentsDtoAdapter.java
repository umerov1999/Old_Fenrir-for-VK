package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import dev.ragnarok.fenrir.api.model.VKApiArticle;
import dev.ragnarok.fenrir.api.model.VKApiAttachment;
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist;
import dev.ragnarok.fenrir.api.model.VKApiCall;
import dev.ragnarok.fenrir.api.model.VKApiGiftItem;
import dev.ragnarok.fenrir.api.model.VKApiGraffiti;
import dev.ragnarok.fenrir.api.model.VKApiLink;
import dev.ragnarok.fenrir.api.model.VKApiNotSupported;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiPhotoAlbum;
import dev.ragnarok.fenrir.api.model.VKApiPoll;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.api.model.VKApiSticker;
import dev.ragnarok.fenrir.api.model.VKApiStory;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.VKApiWallReply;
import dev.ragnarok.fenrir.api.model.VKApiWikiPage;
import dev.ragnarok.fenrir.api.model.VkApiAttachments;
import dev.ragnarok.fenrir.api.model.VkApiAudioMessage;
import dev.ragnarok.fenrir.api.model.VkApiDoc;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;

public class AttachmentsDtoAdapter extends AbsAdapter implements JsonDeserializer<VkApiAttachments> {

    @Override
    public VkApiAttachments deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        VkApiAttachments dto = new VkApiAttachments();

        dto.entries = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            JsonObject o = array.get(i).getAsJsonObject();

            String type = optString(o, "type");
            VKApiAttachment attachment = parse(type, o, context);

            if (Objects.nonNull(attachment)) {
                dto.entries.add(new VkApiAttachments.Entry(type, attachment));
            }
        }

        return dto;
    }

    private VKApiAttachment parse(String type, JsonObject root, JsonDeserializationContext context) {
        JsonElement o = root.get(type);

        //{"type":"photos_list","photos_list":["406536042_456239026"]}

        if (VkApiAttachments.TYPE_PHOTO.equals(type)) {
            return context.deserialize(o, VKApiPhoto.class);
        } else if (VkApiAttachments.TYPE_VIDEO.equals(type)) {
            return context.deserialize(o, VKApiVideo.class);
        } else if (VkApiAttachments.TYPE_AUDIO.equals(type)) {
            return context.deserialize(o, VKApiAudio.class);
        } else if (VkApiAttachments.TYPE_DOC.equals(type)) {
            return context.deserialize(o, VkApiDoc.class);
        } else if (VkApiAttachments.TYPE_POST.equals(type) || VkApiAttachments.TYPE_FAVE_POST.equals(type)) {
            return context.deserialize(o, VKApiPost.class);
            //} else if (VkApiAttachments.TYPE_POSTED_PHOTO.equals(type)) {
            //    return context.deserialize(o, VKApiPostedPhoto.class);
        } else if (VkApiAttachments.TYPE_LINK.equals(type)) {
            return context.deserialize(o, VKApiLink.class);
            //} else if (VkApiAttachments.TYPE_NOTE.equals(type)) {
            //    return context.deserialize(o, VKApiNote.class);
            //} else if (VkApiAttachments.TYPE_APP.equals(type)) {
            //    return context.deserialize(o, VKApiApplicationContent.class);
        } else if (VkApiAttachments.TYPE_ARTICLE.equals(type)) {
            return context.deserialize(o, VKApiArticle.class);
        } else if (VkApiAttachments.TYPE_POLL.equals(type)) {
            return context.deserialize(o, VKApiPoll.class);
        } else if (VkApiAttachments.TYPE_WIKI_PAGE.equals(type)) {
            return context.deserialize(o, VKApiWikiPage.class);
        } else if (VkApiAttachments.TYPE_ALBUM.equals(type)) {
            return context.deserialize(o, VKApiPhotoAlbum.class);
        } else if (VkApiAttachments.TYPE_STICKER.equals(type)) {
            return context.deserialize(o, VKApiSticker.class);
        } else if (VKApiAttachment.TYPE_AUDIO_MESSAGE.equals(type)) {
            return context.deserialize(o, VkApiAudioMessage.class);
        } else if (VKApiAttachment.TYPE_GIFT.equals(type)) {
            return context.deserialize(o, VKApiGiftItem.class);
        } else if (VKApiAttachment.TYPE_GRAFFITY.equals(type)) {
            return context.deserialize(o, VKApiGraffiti.class);
        } else if (VKApiAttachment.TYPE_STORY.equals(type)) {
            return context.deserialize(o, VKApiStory.class);
        } else if (VKApiAttachment.TYPE_CALL.equals(type)) {
            return context.deserialize(o, VKApiCall.class);
        } else if (VKApiAttachment.TYPE_AUDIO_PLAYLIST.equals(type)) {
            return context.deserialize(o, VKApiAudioPlaylist.class);
        } else if (VKApiAttachment.TYPE_WALL_REPLY.equals(type)) {
            return context.deserialize(o, VKApiWallReply.class);
        } else if (!Utils.isValueAssigned(type, VKApiAttachment.IGNORE_ATTACHMENTS)) {
            return new VKApiNotSupported(type, o.toString());
        } else {
            return null;
        }
    }
}
