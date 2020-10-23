package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.VkApiProfileInfoResponce;

public class ProfileInfoResponceDtoAdapter extends AbsAdapter implements JsonDeserializer<VkApiProfileInfoResponce> {

    @Override
    public VkApiProfileInfoResponce deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        VkApiProfileInfoResponce info = new VkApiProfileInfoResponce();
        if (root.has("name_request")) {
            info.status = 2;
        } else {
            info.status = optInt(root, "changed", 0) == 1 ? 1 : 0;
        }
        return info;
    }
}
