package dev.ragnarok.fenrir;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.umerov.rlottie.RLottieImageView;

import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;

public class CheckDonate {
    public static final Integer[] donatedUsers = {572488303};

    public static boolean isFullVersion(Context context) {
        if (!Utils.isValueAssigned(Settings.get().accounts().getCurrent(), donatedUsers) && !Utils.isValueAssigned(Settings.get().accounts().getCurrent(), Utils.donate_users) && !Constants.IS_DONATE) {
            MaterialAlertDialogBuilder dlgAlert = new MaterialAlertDialogBuilder(context);

            View view = LayoutInflater.from(context).inflate(R.layout.donate_alert, null);
            view.findViewById(R.id.item_donate).setOnClickListener(v -> LinkHelper.openLinkInBrowser(context, "https://play.google.com/store/apps/details?id=dev.ragnarok.fenrir_full"));
            RLottieImageView anim = view.findViewById(R.id.lottie_animation);
            anim.setAutoRepeat(true);
            anim.setAnimation(R.raw.google_store, Utils.dp(200), Utils.dp(200));
            anim.playAnimation();

            dlgAlert.setTitle(R.string.info);
            dlgAlert.setIcon(R.drawable.client_round);
            dlgAlert.setCancelable(true);
            dlgAlert.setView(view);
            dlgAlert.show();
            return false;
        }
        return true;
    }

    public static void isDonated(Activity context, int account_id) {
        Utils.donate_users.clear();
        Utils.donate_users.addAll(Settings.get().other().getDonates());

        //noinspection ResultOfMethodCallIgnored
        InteractorFactory.createDebugToolInteractor().call_debugger()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> {
                    if (!Utils.isEmpty(t.donates)) {
                        Utils.donate_users.clear();
                        Utils.donate_users.addAll(t.donates);
                        Settings.get().other().registerDonatesId(Utils.donate_users);
                    }
                    MaterialAlertDialogBuilder dlgAlert = new MaterialAlertDialogBuilder(context);
                    boolean isDon = Utils.isValueAssigned(account_id, donatedUsers) || Utils.isValueAssigned(account_id, Utils.donate_users);
                    View view = LayoutInflater.from(context).inflate(R.layout.is_donate_alert, null);
                    ((TextView) view.findViewById(R.id.item_status)).setText(isDon ? R.string.button_yes : R.string.button_no);
                    RLottieImageView anim = view.findViewById(R.id.lottie_animation);
                    anim.setAutoRepeat(true);
                    anim.setAnimation(isDon ? R.raw.is_donated : R.raw.is_not_donated, Utils.dp(200), Utils.dp(200));
                    anim.playAnimation();

                    dlgAlert.setTitle(R.string.info);
                    dlgAlert.setIcon(R.drawable.client_round);
                    dlgAlert.setCancelable(true);
                    dlgAlert.setView(view);
                    dlgAlert.show();
                }, e -> Utils.showErrorInAdapter(context, e));
    }

    public static void UpdateDonateList(Activity context) {
        Utils.donate_users.clear();
        Utils.donate_users.addAll(Settings.get().other().getDonates());

        //noinspection ResultOfMethodCallIgnored
        InteractorFactory.createDebugToolInteractor().call_debugger()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> {
                    if (!Utils.isEmpty(t.donates)) {
                        Utils.donate_users.clear();
                        Utils.donate_users.addAll(t.donates);
                        Settings.get().other().registerDonatesId(Utils.donate_users);
                    }
                }, e -> Utils.showErrorInAdapter(context, e));
    }
}
