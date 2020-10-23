package dev.ragnarok.fenrir.link.types;

import org.jetbrains.annotations.NotNull;

public class DomainLink extends AbsLink {

    public String fullLink;
    public String domain;

    public DomainLink(String fullLink, String domain) {
        super(DOMAIN);
        this.domain = domain;
        this.fullLink = fullLink;
    }

    @NotNull
    @Override
    public String toString() {
        return "DomainLink{" +
                "fullLink='" + fullLink + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }
}
