package dev.ragnarok.fenrir.model;

import android.graphics.Color;

public class ThemeValue {

    public final int color_primary;
    public final int color_secondary;
    public final String id;
    public final String name;

    public ThemeValue(String color_primary, String color_secondary, String id, String name) {
        this.color_primary = Color.parseColor(color_primary);
        this.color_secondary = Color.parseColor(color_secondary);
        this.id = id;
        this.name = name;
    }
}
