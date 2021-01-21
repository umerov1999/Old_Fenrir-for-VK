package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Constants;

public class AbsAdapter {

    public static String optString(JsonObject json, String name) {
        return optString(json, name, null);
    }

    public static String optString(JsonObject json, String name, String fallback) {
        try {
            return json.has(name) ? json.get(name).getAsString() : fallback;
        } catch (ClassCastException | IllegalStateException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    public static boolean optIntAsBoolean(JsonObject json, String name) {
        return optInt(json, name) == 1;
    }

    public static boolean optBoolean(JsonObject json, String name) {
        try {
            return json.has(name) && json.get(name).getAsBoolean();
        } catch (ClassCastException | IllegalStateException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static int optInt(JsonObject json, String name) {
        return optInt(json, name, 0);
    }

    public static int optInt(JsonArray array, int index) {
        return optInt(array, index, 0);
    }

    public static int getFirstInt(JsonObject json, int fallback, String... names) {
        try {
            for (String name : names) {
                if (json.has(name)) {
                    return json.get(name).getAsInt();
                }
            }

            return fallback;
        } catch (ClassCastException | IllegalStateException | NumberFormatException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    public static long optLong(JsonArray array, int index) {
        return optLong(array, index, 0L);
    }

    public static long optLong(JsonArray array, int index, long fallback) {
        try {
            JsonElement opt = opt(array, index);
            return opt == null ? fallback : opt.getAsLong();
        } catch (ClassCastException | IllegalStateException | NumberFormatException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    public static int optInt(JsonArray array, int index, int fallback) {
        try {
            JsonElement opt = opt(array, index);
            return opt == null ? fallback : opt.getAsInt();
        } catch (ClassCastException | IllegalStateException | NumberFormatException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    public static JsonElement opt(JsonArray array, int index) {
        if (index < 0 || index >= array.size()) {
            return null;
        }

        return array.get(index);
    }

    public static String optString(JsonArray array, int index) {
        return optString(array, index, null);
    }

    public static String optString(JsonArray array, int index, String fallback) {
        try {
            JsonElement opt = opt(array, index);
            return opt == null ? fallback : opt.getAsString();
        } catch (ClassCastException | IllegalStateException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    public static int optInt(JsonObject json, String name, int fallback) {
        try {
            return json.has(name) ? json.get(name).getAsInt() : fallback;
        } catch (ClassCastException | IllegalStateException | NumberFormatException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    public static long optLong(JsonObject json, String name) {
        return optLong(json, name, 0L);
    }

    public static long optLong(JsonObject json, String name, long fallback) {
        try {
            return json.has(name) ? json.get(name).getAsLong() : fallback;
        } catch (ClassCastException | IllegalStateException | NumberFormatException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    protected static <T> List<T> parseArray(JsonArray array, Class<? extends T> type, JsonDeserializationContext context, List<T> fallback) {
        if (array == null) {
            return fallback;
        }

        try {
            List<T> list = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                list.add(context.deserialize(array.get(i), type));
            }

            return list;
        } catch (JsonParseException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    protected static String[] optStringArray(JsonObject root, String name, String[] fallback) {
        try {
            if (!root.has(name)) {
                return fallback;
            }

            JsonArray array = root.getAsJsonArray(name);
            if (array == null) {
                return fallback;
            }
            return parseStringArray(array);
        } catch (ClassCastException | IllegalStateException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    protected static int[] optIntArray(JsonObject root, String name, int[] fallback) {
        try {
            if (!root.has(name)) {
                return fallback;
            }

            JsonArray array = root.getAsJsonArray(name);
            if (array == null) {
                return fallback;
            }
            return parseIntArray(array);
        } catch (ClassCastException | IllegalStateException | NumberFormatException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    protected static int[] optIntArray(JsonArray array, int index, int[] fallback) {
        try {
            if (index < 0 || index >= array.size()) {
                return fallback;
            }

            JsonArray array_r = array.get(index).getAsJsonArray();
            if (array_r == null) {
                return fallback;
            }
            return parseIntArray(array_r);
        } catch (ClassCastException | IllegalStateException | NumberFormatException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    private static int[] parseIntArray(JsonArray array) {
        int[] list = new int[array.size()];
        for (int i = 0; i < array.size(); i++) {
            list[i] = array.get(i).getAsInt();
        }

        return list;
    }

    private static String[] parseStringArray(JsonArray array) {
        String[] list = new String[array.size()];
        for (int i = 0; i < array.size(); i++) {
            list[i] = array.get(i).getAsString();
        }

        return list;
    }
}
