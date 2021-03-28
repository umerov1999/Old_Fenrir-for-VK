package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import dev.ragnarok.fenrir.model.AnswerVKOfficial;
import dev.ragnarok.fenrir.model.AnswerVKOfficialList;

public class AnswerVKOfficialDtoAdapter extends AbsAdapter implements JsonDeserializer<AnswerVKOfficialList> {
    private static final String TAG = AnswerVKOfficialDtoAdapter.class.getSimpleName();

    @Override
    public AnswerVKOfficialList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        AnswerVKOfficialList dtolist = new AnswerVKOfficialList();
        JsonObject root = json.getAsJsonObject();

        dtolist.items = new ArrayList<>();
        dtolist.fields = new ArrayList<>();

        if (hasArray(root, "profiles")) {
            JsonArray temp = root.getAsJsonArray("profiles");
            for (JsonElement i : temp) {
                if (!checkObject(i)) {
                    continue;
                }
                JsonObject obj = i.getAsJsonObject();
                int id = optInt(obj, "id");
                if (obj.has("photo_200")) {
                    String url = optString(obj, "photo_200");
                    dtolist.fields.add(new AnswerVKOfficialList.AnswerField(id, url));
                } else if (obj.has("photo_200_orig")) {
                    String url = optString(obj, "photo_200_orig");
                    dtolist.fields.add(new AnswerVKOfficialList.AnswerField(id, url));
                }
            }
        }
        if (hasArray(root, "groups")) {
            JsonArray temp = root.getAsJsonArray("groups");
            for (JsonElement i : temp) {
                if (!checkObject(i)) {
                    continue;
                }
                JsonObject obj = i.getAsJsonObject();
                int id = optInt(obj, "id") * -1;
                if (obj.has("photo_200")) {
                    String url = optString(obj, "photo_200");
                    dtolist.fields.add(new AnswerVKOfficialList.AnswerField(id, url));
                } else if (obj.has("photo_200_orig")) {
                    String url = optString(obj, "photo_200_orig");
                    dtolist.fields.add(new AnswerVKOfficialList.AnswerField(id, url));
                }
            }
        }

        if (!hasArray(root, "items"))
            return dtolist;

        for (JsonElement i : root.getAsJsonArray("items")) {
            if (!checkObject(i)) {
                continue;
            }
            JsonObject root_item = i.getAsJsonObject();
            AnswerVKOfficial dto = new AnswerVKOfficial();
            dto.iconType = optString(root_item, "icon_type");
            dto.header = optString(root_item, "header");
            if (dto.header != null) {
                dto.header = dto.header.replace("{date}", "").replaceAll("'''(((?!''').)*)'''", "<b>$1</b>").replaceAll("\\[vk(ontakte)?://[A-Za-z0-9/?=]+\\|([^]]+)]", "$2");
            }
            dto.text = optString(root_item, "text");
            if (dto.text != null)
                dto.text = dto.text.replace("{date}", "").replaceAll("'''(((?!''').)*)'''", "<b>$1</b>").replaceAll("\\[vk(ontakte)?://[A-Za-z0-9/?=]+\\|([^]]+)]", "$2");
            dto.footer = optString(root_item, "footer");
            if (dto.footer != null)
                dto.footer = dto.footer.replace("{date}", "").replaceAll("'''(((?!''').)*)'''", "<b>$1</b>").replaceAll("\\[vk(ontakte)?://[A-Za-z0-9/?=]+\\|([^]]+)]", "$2");
            dto.time = optLong(root_item, "date");
            dto.iconURL = optString(root_item, "icon_url");

            if (hasObject(root_item, "main_item")) {
                JsonObject main_item = root_item.get("main_item").getAsJsonObject();
                if (hasArray(main_item, "image_object")) {
                    JsonArray jsonPhotos2 = main_item.get("image_object").getAsJsonArray();
                    dto.iconURL = jsonPhotos2.get(jsonPhotos2.size() - 1).getAsJsonObject().get("url").getAsString();
                }
            }
            if (hasObject(root_item, "additional_item")) {
                JsonObject additional_item = root_item.get("additional_item").getAsJsonObject();
                if (hasArray(additional_item, "image_object")) {
                    JsonArray arrt = additional_item.getAsJsonArray("image_object");
                    dto.images = new ArrayList<>();
                    for (JsonElement s : arrt) {
                        if (!checkObject(s)) {
                            continue;
                        }
                        AnswerVKOfficial.ImageAdditional imgh = context.deserialize(s, AnswerVKOfficial.ImageAdditional.class);
                        if (imgh != null)
                            dto.images.add(imgh);
                    }
                }
            }
            dtolist.items.add(dto);
        }
        return dtolist;
    }
}
