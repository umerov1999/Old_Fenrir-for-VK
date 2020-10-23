package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.ThemeValue;
import dev.ragnarok.fenrir.mvp.core.IMvpView;


public interface IThemeView extends IMvpView {
    void displayData(List<ThemeValue> data);
}
