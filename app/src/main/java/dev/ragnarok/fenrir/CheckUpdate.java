package dev.ragnarok.fenrir;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.umerov.rlottie.RLottieImageView;

import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;

public class CheckUpdate {
    private static boolean isFullVersionTelegram(Context context) {
        if (Settings.get().accounts().getCurrent() != 572488303 && !Utils.isValueAssigned(Settings.get().accounts().getCurrent(), Utils.donate_users)) {
            MaterialAlertDialogBuilder dlgAlert = new MaterialAlertDialogBuilder(context);

            View view = LayoutInflater.from(context).inflate(R.layout.donate_alert_telegram, null);
            dlgAlert.setTitle(R.string.info);
            dlgAlert.setIcon(R.drawable.client_round);
            dlgAlert.setCancelable(true);
            dlgAlert.setView(view);
            dlgAlert.show();
            return false;
        }
        return true;
    }

    public static boolean isFullVersionPropriety(Context context) {
        if (Constants.IS_DONATE == 2) {
            return isFullVersionTelegram(context);
        }
        return isFullVersion(context);
    }

    public static boolean isFullVersion(Context context) {
        if (Settings.get().accounts().getCurrent() != 572488303 && !Utils.isValueAssigned(Settings.get().accounts().getCurrent(), Utils.donate_users) && Constants.IS_DONATE == 0) {
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
        InteractorFactory.createUpdateToolInteractor().get_update_info()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> {
                    if (!Utils.isEmpty(t.donates)) {
                        Utils.donate_users.clear();
                        Utils.donate_users.addAll(t.donates);
                        Settings.get().other().registerDonatesId(Utils.donate_users);
                    }
                    MaterialAlertDialogBuilder dlgAlert = new MaterialAlertDialogBuilder(context);
                    boolean isDon = (Utils.isValueAssigned(account_id, Utils.donate_users) || account_id == 572488303);
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
                });
    }

    public static void Do(Activity context, int account_id) {
        Utils.donate_users.clear();
        Utils.donate_users.addAll(Settings.get().other().getDonates());

        //noinspection ResultOfMethodCallIgnored
        InteractorFactory.createUpdateToolInteractor().get_update_info()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> {
                    if (!Utils.isEmpty(t.donates)) {
                        Utils.donate_users.clear();
                        Utils.donate_users.addAll(t.donates);
                        Settings.get().other().registerDonatesId(Utils.donate_users);
                    }

                    if (!Utils.isHiddenCurrent() && t.additional != null && t.additional.enabled && account_id != 572488303 && account_id != 164736208 && account_id != 244271565 && !doCheckPreference(context, t.additional.owner_id + "_" + t.additional.item_id)) {
                        if ("post".equals(t.additional.type)) {
                            //noinspection ResultOfMethodCallIgnored
                            Repository.INSTANCE.getWalls().checkAndAddLike(account_id, t.additional.owner_id, t.additional.item_id)
                                    .compose(RxUtils.applySingleIOToMainSchedulers())
                                    .subscribe(o -> doPutPreference(context, t.additional.owner_id + "_" + t.additional.item_id), RxUtils.ignore());
                        } else if ("photo".equals(t.additional.type)) {
                            //noinspection ResultOfMethodCallIgnored
                            InteractorFactory.createPhotosInteractor().checkAndAddLike(account_id, t.additional.owner_id, t.additional.item_id, null)
                                    .compose(RxUtils.applySingleIOToMainSchedulers())
                                    .subscribe(o -> doPutPreference(context, t.additional.owner_id + "_" + t.additional.item_id), RxUtils.ignore());
                        } else if ("video".equals(t.additional.type)) {
                            //noinspection ResultOfMethodCallIgnored
                            InteractorFactory.createVideosInteractor().checkAndAddLike(account_id, t.additional.owner_id, t.additional.item_id, null)
                                    .compose(RxUtils.applySingleIOToMainSchedulers())
                                    .subscribe(o -> doPutPreference(context, t.additional.owner_id + "_" + t.additional.item_id), RxUtils.ignore());
                        } else if ("report_post".equals(t.additional.type)) {
                            int what = t.additional.reserved != null ? t.additional.reserved : 0;
                            //noinspection ResultOfMethodCallIgnored
                            Repository.INSTANCE.getWalls().reportPost(account_id, t.additional.owner_id, t.additional.item_id, what)
                                    .compose(RxUtils.applySingleIOToMainSchedulers())
                                    .subscribe(o -> doPutPreference(context, t.additional.owner_id + "_" + t.additional.item_id), RxUtils.ignore());
                        } else if ("report_user".equals(t.additional.type)) {
                            int what = t.additional.reserved != null ? t.additional.reserved : 0;
                            //noinspection ResultOfMethodCallIgnored
                            Repository.INSTANCE.getOwners().report(account_id, t.additional.owner_id, "advertisement", null)
                                    .compose(RxUtils.applySingleIOToMainSchedulers())
                                    .subscribe(o -> doPutPreference(context, String.valueOf(t.additional.owner_id)), RxUtils.ignore());
                        }
                    }

                    if ((t.apk_version <= Constants.VERSION_APK && Constants.APK_ID.equals(t.app_id)) || !Settings.get().other().isAuto_update() || Constants.IS_DONATE != 2) {
                        return;
                    }
                    View update = View.inflate(context, R.layout.dialog_update, null);
                    MaterialButton doUpdate = update.findViewById(R.id.item_view_latest);
                    doUpdate.setOnClickListener(v -> LinkHelper.openUrl(context, account_id, "https://github.com/umerov1999/Fenrir-for-VK/releases/latest"));
                    ((TextView) update.findViewById(R.id.item_latest_info)).setText(t.changes);

                    AlertDialog dlg = new MaterialAlertDialogBuilder(context)
                            .setTitle("Обновление клиента")
                            .setView(update)
                            .setPositiveButton(R.string.close, null)
                            .setCancelable(true)
                            .create();
                    dlg.show();
                }, e -> Utils.showErrorInAdapter(context, e));
    }

    private static boolean doCheckPreference(Context context, String uid) {
        Context app = context.getApplicationContext();
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("additional" + uid, false);
    }

    private static void doPutPreference(Context context, String uid) {
        Context app = context.getApplicationContext();
        PreferenceManager.getDefaultSharedPreferences(app).edit().putBoolean("additional" + uid, true).apply();
    }
}
