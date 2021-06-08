package dev.ragnarok.fenrir;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class CheckDonate {
    public static final List<Integer> donatedUsersRemote = new ArrayList<>();
    public static final Integer[] donatedUsersLocal = {572488303, 365089125,
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
            118540110
    };

    public static boolean isFullVersion(@NonNull Context context) {
        if (!Constants.IS_DONATE && !Utils.isOneElementAssigned(Settings.get().accounts().getRegistered(), donatedUsersLocal) && !Utils.isOneElementAssigned(Settings.get().accounts().getRegistered(), donatedUsersRemote)) {
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

    public static void isDonated(Activity context, int account_id) {
        donatedUsersRemote.clear();
        donatedUsersRemote.addAll(Settings.get().other().getDonates());

        //noinspection ResultOfMethodCallIgnored
        InteractorFactory.createDonateCheckInteractor().check()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> {
                    if (!Utils.isEmpty(t.donates)) {
                        donatedUsersRemote.clear();
                        donatedUsersRemote.addAll(t.donates);
                        Settings.get().other().registerDonatesId(donatedUsersRemote);
                    }
                    boolean isDon = Utils.isValueAssigned(account_id, donatedUsersLocal) || Utils.isValueAssigned(account_id, donatedUsersRemote);
                    View view = LayoutInflater.from(context).inflate(R.layout.is_donate_alert, null);
                    ((TextView) view.findViewById(R.id.item_status)).setText(isDon ? R.string.button_yes : R.string.button_no);
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
        donatedUsersRemote.clear();
        donatedUsersRemote.addAll(Settings.get().other().getDonates());

        //noinspection ResultOfMethodCallIgnored
        InteractorFactory.createDonateCheckInteractor().check()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> {
                    if (!Utils.isEmpty(t.donates)) {
                        donatedUsersRemote.clear();
                        donatedUsersRemote.addAll(t.donates);
                        Settings.get().other().registerDonatesId(donatedUsersRemote);
                    }
                }, RxUtils.ignore());
    }
}
