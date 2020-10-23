package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiOwner;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.response.LikesListResponse;

public class LikesListAdapter extends AbsAdapter implements JsonDeserializer<LikesListResponse> {

    @Override
    public LikesListResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        LikesListResponse response = new LikesListResponse();
        JsonObject root = json.getAsJsonObject();

        response.count = optInt(root, "count");

        if (root.has("items")) {
            JsonArray itemsArray = root.getAsJsonArray("items");
            response.owners = new ArrayList<>(itemsArray.size());

            for (int i = 0; i < itemsArray.size(); i++) {
                JsonObject itemRoot = itemsArray.get(i).getAsJsonObject();
                String type = optString(itemRoot, "type");

                VKApiOwner owner = null;
                if ("profile".equals(type)) {
                    owner = context.deserialize(itemRoot, VKApiUser.class);
                } else if ("group".equals(type) || "page".equals(type)) {
                    owner = context.deserialize(itemRoot, VKApiCommunity.class);
                }

                if (owner != null) {
                    response.owners.add(owner);
                }
            }
        }

        return response;
    }
}
