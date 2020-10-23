package dev.ragnarok.fenrir.link.internal;

import org.jetbrains.annotations.NotNull;

public class AbsInternalLink {

    public int start;
    public int end;

    public String targetLine;

    @NotNull
    @Override
    public String toString() {
        return "AbsInternalLink{" +
                "start=" + start +
                ", end=" + end +
                ", targetLine='" + targetLine + '\'' +
                '}';
    }
}
