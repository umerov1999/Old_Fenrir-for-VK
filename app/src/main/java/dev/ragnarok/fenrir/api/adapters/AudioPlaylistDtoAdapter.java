package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist;

public class AudioPlaylistDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiAudioPlaylist> {

    @Override
    public VKApiAudioPlaylist deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        VKApiAudioPlaylist album = new VKApiAudioPlaylist();

        album.id = optInt(root, "id");
        album.count = optInt(root, "count");
        album.owner_id = optInt(root, "owner_id");
        album.title = optString(root, "title");
        album.access_key = optString(root, "access_key");
        album.description = optString(root, "description");
        album.update_time = optInt(root, "update_time");
        album.Year = optInt(root, "year");

        if (root.has("genres") && root.getAsJsonArray("genres").size() > 0) {
            StringBuilder build = new StringBuilder();
            JsonArray gnr = root.getAsJsonArray("genres");
            boolean isFirst = true;
            for (JsonElement i : gnr) {
                if (isFirst)
                    isFirst = false;
                else
                    build.append(", ");
                String val = optString(i.getAsJsonObject(), "name");
                if (val != null)
                    build.append(val);
            }
            album.genre = build.toString();
        }

        if (root.has("original")) {
            JsonObject orig = root.getAsJsonObject("original");
            album.original_id = optInt(orig, "playlist_id");
            album.original_owner_id = optInt(orig, "owner_id");
            album.original_access_key = optString(orig, "access_key");
        }

        if (root.has("main_artists") && root.getAsJsonArray("main_artists").size() > 0)
            album.artist_name = optString(root.getAsJsonArray("main_artists").get(0).getAsJsonObject(), "name");
        if (root.getAsJsonObject().has("photo")) {
            JsonObject thmb = root.getAsJsonObject("photo");

            if (thmb.has("photo_600"))
                album.thumb_image = thmb.get("photo_600").getAsString();
            else if (thmb.has("photo_300"))
                album.thumb_image = thmb.get("photo_300").getAsString();
        } else if (root.getAsJsonObject().has("thumbs")) {
            JsonArray thmb = root.getAsJsonArray("thumbs");
            if (thmb.size() > 0) {
                JsonObject thmbc = thmb.get(0).getAsJsonObject();
                if (thmbc.has("photo_600"))
                    album.thumb_image = thmbc.get("photo_600").getAsString();
                else if (thmbc.has("photo_300"))
                    album.thumb_image = thmbc.get("photo_300").getAsString();
            }
        }
        return album;
    }
}
