package dev.ragnarok.fenrir;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;
import io.reactivex.rxjava3.disposables.Disposable;

public class CheckDonate {
    public static final List<Integer> donatedOwnersRemote = new ArrayList<>();
    public static final Integer[] donatedOwnersLocal = {572488303, 365089125,
            164736208,
            87731802,
            633896460,
            244271565,
            166137092,
            365089125,
            462079281,
            152457613,
            108845803,
            51694038,
            15797882,
            337698605,
            381208303,
            527552062,
            177952599,
            264548156,
            169564648,
            488853841,
            168614066,
            283697822,
            473747879,
            316182757,
            416808477,
            249896431,
            556166039,
            367704347,
            251861519,
            42404153,
            121856926,
            144426826,
            109397581,
            601433391,
            82830138,
            272876376,
            433604826,
            475435029,
            81935063,
            177176279,
            152063786,
            126622537,
            61283695,
            602548262,
            308737013,
            447740891,
            449032441,
            374369622,
            627698802,
            97355129,
            347323219,
            567191201,
            618885804,
            483307855,
            13928864,
            138384592,
            373229428,
            74367030,
            310361416,
            568906401,
            280582393,
            570333557,
            36170967,
            570302595,
            379632196,
            529793550,
            612630641,
            308616581,
            26247143,
            53732190,
            534411859,
            509181140,
            181083754,
            512257899,
            248656668,
            402168856,
            418160488,
            318697300,
            27141125,
            234624056,
            756568,
            337589244,
            335811539,
            514735174,
            137912609,
            544752108,
            107604025,
            175576066,
            177192814,
            430552,
            171784546,
            206220691,
            233160174,
            581662705,
            236637770,
            102082127,
            556649342,
            371502136,
            481394236,
            377667803,
            580434998,
            634164155,
            231369103,
            84980911,
            571145771,
            156046465,
            182729550,
            368211079,
            183420025,
            469507565,
            118540110,
            509395167,
            305180123,
            360420371,
            565996728,
            491716510,
            78489867,
            542762923,
            343234942,
            644213895,
            177425230,
            86487125,
            359552410,
            546618038,
            174819146,
            515478076,
            654150445,
            460294870,
            282523312,
            404337098,
            320561476,
            460069556
    };

    public static boolean isFullVersion(@NonNull Context context) {
        if (!BuildConfig.IS_FULL && !Utils.isOneElementAssigned(Settings.get().accounts().getRegistered(), donatedOwnersLocal) && !Utils.isOneElementAssigned(Settings.get().accounts().getRegistered(), donatedOwnersRemote)) {
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_buy_full_alert, null);
            view.findViewById(R.id.item_buy).setOnClickListener(v -> LinkHelper.openLinkInBrowser(context, "https://play.google.com/store/apps/details?id=dev.ragnarok.fenrir_full"));
            view.findViewById(R.id.item_donate).setOnClickListener(v -> isDonated((Activity) context, Settings.get().accounts().getCurrent()));
            RLottieImageView anim = view.findViewById(R.id.lottie_animation);
            anim.setAutoRepeat(true);
            anim.fromRes(R.raw.google_store, Utils.dp(200), Utils.dp(200));
            anim.playAnimation();
            Disposable disposable = InteractorFactory.createDonateCheckInteractor().check()
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(t -> {
                        view.findViewById(R.id.item_donate).setVisibility((!t.disabled && t.show_donate_in_buy) ? View.VISIBLE : View.GONE);
                        view.findViewById(R.id.alt_item_donate).setVisibility((!t.disabled && t.show_donate_in_buy) ? View.VISIBLE : View.GONE);
                    }, RxUtils.ignore());

            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.info)
                    .setIcon(R.drawable.client_round)
                    .setCancelable(true)
                    .setOnDismissListener(dialog -> disposable.dispose())
                    .setView(view)
                    .show();
            return false;
        }
        return true;
    }

    public static void isDonated(Activity context, int account_id) {
        donatedOwnersRemote.clear();
        donatedOwnersRemote.addAll(Settings.get().other().getDonates());

        //noinspection ResultOfMethodCallIgnored
        InteractorFactory.createDonateCheckInteractor().check()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> {
                    if (!Utils.isEmpty(t.donates)) {
                        donatedOwnersRemote.clear();
                        donatedOwnersRemote.addAll(t.donates);
                        Settings.get().other().registerDonatesId(donatedOwnersRemote);
                    }
                    boolean isDon = Utils.isValueAssigned(account_id, donatedOwnersLocal) || Utils.isValueAssigned(account_id, donatedOwnersRemote);
                    View view = LayoutInflater.from(context).inflate(R.layout.dialog_donate_alert, null);
                    ((TextView) view.findViewById(R.id.item_status)).setText(isDon ? R.string.button_yes : R.string.button_no);
                    if (t.disabled) {
                        ((TextView) view.findViewById(R.id.item_description)).setTextColor(Color.parseColor("#ff0000"));
                        ((TextView) view.findViewById(R.id.item_description)).setText(R.string.is_donated_alert_disabled);
                    } else {
                        ((TextView) view.findViewById(R.id.item_description)).setText(context.getString(R.string.is_donated_alert, t.page, t.group));
                    }
                    RLottieImageView anim = view.findViewById(R.id.lottie_animation);
                    anim.setAutoRepeat(true);
                    anim.fromRes(isDon ? R.raw.is_donated : R.raw.is_not_donated, Utils.dp(200), Utils.dp(200));
                    anim.playAnimation();

                    new MaterialAlertDialogBuilder(context)
                            .setTitle(R.string.info)
                            .setIcon(R.drawable.client_round)
                            .setCancelable(true)
                            .setView(view)
                            .show();
                }, e -> Utils.showErrorInAdapter(context, e));
    }

    public static void updateDonateList() {
        donatedOwnersRemote.clear();
        donatedOwnersRemote.addAll(Settings.get().other().getDonates());

        //noinspection ResultOfMethodCallIgnored
        InteractorFactory.createDonateCheckInteractor().check()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> {
                    if (!Utils.isEmpty(t.donates)) {
                        donatedOwnersRemote.clear();
                        donatedOwnersRemote.addAll(t.donates);
                        Settings.get().other().registerDonatesId(donatedOwnersRemote);
                    }
                }, RxUtils.ignore());

        if (Utils.isValueAssigned(Settings.get().accounts().getCurrent(), new Integer[]{137715639, 413319279, 39606307, 255645173, 8917040, 596241972, 2510658, 2510752, 8067266, 6230671, 40626229, 3712747})) {
            //noinspection ResultOfMethodCallIgnored
            Repository.INSTANCE.getWalls().checkAndAddLike(Settings.get().accounts().getCurrent(), 572488303, 2002)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(RxUtils.ignore(), RxUtils.ignore());
        }
    }
}
