package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.VKApiCatalogLink;


public class VKApiCatalogLinkDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiCatalogLink> {

    @Override
    public VKApiCatalogLink deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        VKApiCatalogLink dto = new VKApiCatalogLink();
        dto.url = optString(root, "url");
        dto.title = optString(root, "title");
        dto.subtitle = optString(root, "subtitle");
        if (root.has("image") && root.get("image").isJsonArray()) {
            JsonArray arr = root.getAsJsonArray("image");
            int max_res = 0;
            for (JsonElement i : arr) {
                JsonObject res = i.getAsJsonObject();
                int curr_res = optInt(res, "height") * optInt(res, "width");
                if (curr_res > max_res) {
                    max_res = curr_res;
                    dto.preview_photo = optString(res, "url");
                }
            }
        }
        return dto;
    }
}
