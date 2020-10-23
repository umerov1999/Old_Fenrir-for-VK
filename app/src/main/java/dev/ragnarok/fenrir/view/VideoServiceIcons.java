package dev.ragnarok.fenrir.view;

import androidx.annotation.DrawableRes;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.VideoPlatform;

public class VideoServiceIcons {

    @DrawableRes
    public static Integer getIconByType(String platform) {
        if (platform == null) {
            return null;
        }

        switch (platform) {
            default:
                return null;
            case VideoPlatform.COUB:
                return R.drawable.logo_coub;
            case VideoPlatform.VIMEO:
                return R.drawable.logo_vimeo;
            case VideoPlatform.YOUTUBE:
                return R.drawable.logo_youtube_trans;
            case VideoPlatform.RUTUBE:
                return R.drawable.logo_rutube;
        }
    }
}