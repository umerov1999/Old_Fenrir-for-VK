package dev.ragnarok.fenrir.model.menu;

import androidx.annotation.DrawableRes;

import dev.ragnarok.fenrir.model.Icon;
import dev.ragnarok.fenrir.model.Text;

public class AdvancedItem {

    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_COPY_DETAILS_ONLY = 1;
    private final int key;
    private final int type;
    private final Text title;
    private Icon icon;
    private Text subtitle;

    private Section section;

    private Object tag;

    public AdvancedItem(int key, Text title) {
        this(key, TYPE_DEFAULT, title);
    }

    public AdvancedItem(int key, int type, Text title) {
        this.key = key;
        this.type = type;
        this.title = title;
    }

    public Object getTag() {
        return tag;
    }

    public AdvancedItem setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    public int getType() {
        return type;
    }

    public int getKey() {
        return key;
    }

    public Text getSubtitle() {
        return subtitle;
    }

    public AdvancedItem setSubtitle(Text subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public Section getSection() {
        return section;
    }

    public AdvancedItem setSection(Section section) {
        this.section = section;
        return this;
    }

    public Icon getIcon() {
        return icon;
    }

    public AdvancedItem setIcon(Icon icon) {
        this.icon = icon;
        return this;
    }

    public AdvancedItem setIcon(@DrawableRes int iconRes) {
        icon = Icon.fromResources(iconRes);
        return this;
    }

    public AdvancedItem setIcon(String remoteUrl) {
        icon = Icon.fromUrl(remoteUrl);
        return this;
    }

    public Text getTitle() {
        return title;
    }
}
