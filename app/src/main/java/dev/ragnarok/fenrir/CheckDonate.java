package dev.ragnarok.fenrir;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class CheckDonate {
    public static final Integer[] donatedUsers = {572488303, 365089125,
            164736208,
            87731802,
            633896460,
            244271565,
    };

    public static boolean isFullVersion(@NonNull Context context) {
        if (!Constants.IS_DONATE && !Utils.isOneElementAssigned(Settings.get().accounts().getRegistered(), donatedUsers)) {
            View view = LayoutInflater.from(context).inflate(R.layout.donate_alert, null);
            view.findViewById(R.id.item_donate).setOnClickListener(v -> LinkHelper.openLinkInBrowser(context, "https://play.google.com/store/apps/details?id=dev.ragnarok.fenrir_full"));
            RLottieImageView anim = view.findViewById(R.id.lottie_animation);
            anim.setAutoRepeat(true);
            anim.fromRes(R.raw.google_store, Utils.dp(200), Utils.dp(200));
            anim.playAnimation();

            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.info)
                    .setIcon(R.drawable.client_round)
                    .setCancelable(true)
                    .setView(view)
                    .show();
            return false;
        }
        return true;
    }
}
