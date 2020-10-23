package dev.ragnarok.fenrir;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.api.ProxyUtil;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CheckUpdate {
    public static void Do(Activity context, int account_id) {
        Utils.donate_users.clear();
        Utils.donate_users.addAll(Settings.get().other().getDonates());
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder().addHeader("User-Agent", Constants.USER_AGENT(Account_Types.BY_TYPE)).build();
                    return chain.proceed(request);
                });
        ProxyUtil.applyProxyConfig(builder, Injection.provideProxySettings().getActiveProxy());
        Request request = new Request.Builder()
                .url("https://raw.githubusercontent.com/umerov1999/Fenrir-for-VK/main/current_version.json").build();

        builder.build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call th, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call th, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        int APK_VERS = Constants.VERSION_APK;
                        String Chngs = "";
                        JSONObject obj = new JSONObject(Objects.requireNonNull(response.body()).string());
                        if (obj.has("apk_version"))
                            APK_VERS = obj.getInt("apk_version");
                        if (obj.has("changes"))
                            Chngs = obj.getString("changes");

                        String apk_id = "null";
                        if (obj.has("app_id"))
                            apk_id = obj.getString("app_id");

                        if (obj.has("donates")) {
                            Utils.donate_users.clear();
                            JSONArray arr = obj.getJSONArray("donates");
                            for (int i = 0; i < arr.length(); i++) {
                                Utils.donate_users.add(arr.getInt(i));
                            }
                            Settings.get().other().registerDonatesId(Utils.donate_users);

                        }
                        String Chenges_log = Chngs;

                        if ((APK_VERS <= Constants.VERSION_APK && Constants.APK_ID.equals(apk_id)) || !Settings.get().other().isAuto_update())
                            return;

                        Handler uiHandler = new Handler(context.getMainLooper());
                        uiHandler.post(() -> {
                            View update = View.inflate(context, R.layout.dialog_update, null);
                            MaterialButton doUpdate = update.findViewById(R.id.item_view_latest);
                            doUpdate.setOnClickListener(v -> LinkHelper.openUrl(context, account_id, "https://github.com/umerov1999/Fenrir-for-VK/releases/latest"));
                            ((TextView) update.findViewById(R.id.item_latest_info)).setText(Chenges_log);

                            AlertDialog dlg = new MaterialAlertDialogBuilder(context)
                                    .setTitle("Обновление клиента")
                                    .setView(update)
                                    .setPositiveButton(R.string.close, null)
                                    .setNegativeButton(R.string.do_donate, (dialog, which) -> {
                                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("response", "5599005042882048");
                                        clipboard.setPrimaryClip(clip);
                                        CustomToast.CreateCustomToast(context).setDuration(Toast.LENGTH_LONG).showToast(R.string.copied_card);
                                    })
                                    .setCancelable(true)
                                    .create();
                            dlg.show();
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static boolean isFullVersion(Context context) {
        if (!Utils.isValueAssigned(Settings.get().accounts().getCurrent(), Constants.DONATES_USERS) && Constants.IS_DONATE == 0) {
            MaterialAlertDialogBuilder dlgAlert = new MaterialAlertDialogBuilder(context);
            dlgAlert.setMessage(R.string.in_full_version);
            dlgAlert.setTitle(R.string.info);
            dlgAlert.setIcon(R.drawable.client_round);
            dlgAlert.setCancelable(true);
            dlgAlert.setPositiveButton(R.string.go_donate, (dialog, which) -> LinkHelper.openLinkInBrowser(context, "https://play.google.com/store/apps/details?id=dev.ragnarok.fenrir_full"));
            dlgAlert.show();
            return false;
        }
        return true;
    }
}
