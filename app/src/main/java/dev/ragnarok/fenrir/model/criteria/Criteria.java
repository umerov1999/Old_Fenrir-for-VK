package dev.ragnarok.fenrir.model.criteria;

import org.jetbrains.annotations.NotNull;

public class Criteria implements Cloneable {

    @NotNull
    @Override
    public Criteria clone() throws CloneNotSupportedException {
        return (Criteria) super.clone();
    }
}
