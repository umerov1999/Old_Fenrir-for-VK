package dev.ragnarok.fenrir.model;

import android.graphics.Color;

public class ThemeValue {

    public int color_primary;
    public int color_secondary;
    public String id;
    public String name;

    public ThemeValue(String color_primary, String color_secondary, String id, String name) {
        this.color_primary = Color.parseColor(color_primary);
        this.color_secondary = Color.parseColor(color_secondary);
        this.id = id;
        this.name = name;
    }
}
