package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;

import dev.ragnarok.fenrir.api.model.VKApiAudio;

public class AudioDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiAudio> {

    @Override
    public VKApiAudio deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        VKApiAudio dto = new VKApiAudio();
        dto.id = optInt(root, "id");
        dto.owner_id = optInt(root, "owner_id");
        dto.artist = optString(root, "artist");
        dto.title = optString(root, "title");
        dto.duration = optInt(root, "duration");
        dto.url = optString(root, "url");
        dto.lyrics_id = optInt(root, "lyrics_id");
        dto.genre_id = optInt(root, "genre_id");
        dto.access_key = optString(root, "access_key");
        dto.isHq = optBoolean(root, "is_hq");
        if (root.has("main_artists") && root.size() > 0) {
            JsonArray arr = root.getAsJsonArray("main_artists");
            dto.main_artists = new HashMap<>();
            for (JsonElement i : arr) {
                String name = optString(i.getAsJsonObject(), "name");
                String id = optString(i.getAsJsonObject(), "id");
                dto.main_artists.put(id, name);
            }
        }

        if (root.has("album")) {
            JsonObject thmb = root.getAsJsonObject("album");
            dto.album_id = optInt(thmb, "id");
            dto.album_owner_id = optInt(thmb, "owner_id");
            dto.album_access_key = optString(thmb, "access_key");
            dto.album_title = optString(thmb, "title");

            if (thmb.has("thumb")) {
                thmb = thmb.getAsJsonObject("thumb");
                if (thmb.has("photo_135"))
                    dto.thumb_image_little = thmb.get("photo_135").getAsString();
                else if (thmb.has("photo_68"))
                    dto.thumb_image_little = thmb.get("photo_68").getAsString();
                else if (thmb.has("photo_34"))
                    dto.thumb_image_little = thmb.get("photo_34").getAsString();

                if (thmb.has("photo_1200")) {
                    dto.thumb_image_very_big = thmb.get("photo_1200").getAsString();
                }
                if (thmb.has("photo_600")) {
                    dto.thumb_image_big = thmb.get("photo_600").getAsString();
                    if (dto.thumb_image_very_big == null)
                        dto.thumb_image_very_big = thmb.get("photo_600").getAsString();
                } else if (thmb.has("photo_300")) {
                    dto.thumb_image_big = thmb.get("photo_300").getAsString();
                    if (dto.thumb_image_very_big == null)
                        dto.thumb_image_very_big = thmb.get("photo_300").getAsString();
                }
            }
        }

        return dto;
    }
}
