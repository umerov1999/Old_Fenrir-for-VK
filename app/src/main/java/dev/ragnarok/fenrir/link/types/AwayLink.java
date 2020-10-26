package dev.ragnarok.fenrir.link.types;

import org.jetbrains.annotations.NotNull;

public class AwayLink extends AbsLink {

    public final String link;

    public AwayLink(String link) {
        super(EXTERNAL_LINK);
        this.link = link;
    }

    @NotNull
    @Override
    public String toString() {
        return "AwayLink{" +
                "link='" + link + '\'' +
                '}';
    }
}
