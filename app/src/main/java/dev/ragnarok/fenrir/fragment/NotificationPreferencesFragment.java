package dev.ragnarok.fenrir.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Logger;

public class NotificationPreferencesFragment extends PreferenceFragmentCompat {

    public static final int REQUEST_CODE_RINGTONE = 116;
    private static final String TAG = NotificationPreferencesFragment.class.getSimpleName();
    private Ringtone current;
    private int selection;

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.notication_settings);

        findPreference("notif_sound").setOnPreferenceClickListener(preference -> {
            showAlertDialog();
            return true;
        });
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(view.findViewById(R.id.toolbar));
    }

    @Override
    protected void finalize() throws Throwable {
        Logger.d(TAG, "finalize");
        super.finalize();
    }

    private void stopRingtoneIfExist() {
        if (current != null && current.isPlaying()) {
            current.stop();
        }
    }

    private void showAlertDialog() {
        Map<String, String> ringrones = getNotifications();

        Set<String> keys = ringrones.keySet();
        String[] array = keys.toArray(new String[0]);

        String selectionKey = getKeyByValue(ringrones, Settings.get()
                .notifications()
                .getNotificationRingtone());

        selection = Arrays.asList(array).indexOf(selectionKey);

        new MaterialAlertDialogBuilder(requireActivity()).setSingleChoiceItems(array, selection, (dialog, which) -> {
            selection = which;
            stopRingtoneIfExist();
            String title = array[which];
            String uri = ringrones.get(title);
            Ringtone r = RingtoneManager.getRingtone(requireActivity(), Uri.parse(uri));
            current = r;
            r.play();
        }).setPositiveButton("OK", (dialog, which) -> {
            if (selection == -1) {
                Toast.makeText(requireActivity(), R.string.ringtone_not_selected, Toast.LENGTH_SHORT).show();
            } else {
                String title = array[selection];
                Settings.get()
                        .notifications()
                        .setNotificationRingtoneUri(ringrones.get(title));
                stopRingtoneIfExist();
            }
        })
                .setNegativeButton(R.string.cancel, (dialog, which) -> stopRingtoneIfExist())
                .setNeutralButton(R.string.ringtone_custom, (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/audio");
                    startActivityForResult(intent, REQUEST_CODE_RINGTONE);
                }).setOnDismissListener(dialog -> stopRingtoneIfExist()).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRingtoneIfExist();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RINGTONE) {
            if (resultCode == Activity.RESULT_OK) {
                String uri = data.getData().getPath();
                Settings.get()
                        .notifications()
                        .setNotificationRingtoneUri(uri);
            }
        }
    }

    public Map<String, String> getNotifications() {
        RingtoneManager manager = new RingtoneManager(requireActivity());
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);
        Cursor cursor = manager.getCursor();
        Map<String, String> list = new HashMap<>();
        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            Uri notificationUri = manager.getRingtoneUri(cursor.getPosition());
            list.put(notificationTitle, notificationUri.toString());
        }

        list.put(getString(R.string.ringtone_vk), Settings.get()
                .notifications()
                .getDefNotificationRingtone());
        return list;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.settings);
            actionBar.setSubtitle(R.string.notif_setting_title);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_SETTINGS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }
}