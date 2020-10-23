package dev.ragnarok.fenrir.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

import dev.ragnarok.fenrir.R;

public class AppPerms {

    public static final int REQUEST_PERMISSION_READ_EXTARNAL_STORAGE = 8365;
    public static final int REQUEST_PERMISSION_WRITE_STORAGE = 8364;

    public static boolean hasWriteStoragePermision(Context context) {
        if (!Utils.hasMarshmallow()) return true;
        int hasWritePermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return hasWritePermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasReadWriteStoragePermision(Context context) {
        if (!Utils.hasMarshmallow()) return true;
        int hasWritePermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int hasReadPermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return hasWritePermission == PackageManager.PERMISSION_GRANTED && hasReadPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasReadStoragePermision(Context context) {
        if (!Utils.hasMarshmallow()) return true;
        int hasWritePermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return hasWritePermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasCameraPermision(Context context) {
        if (!Utils.hasMarshmallow()) return true;
        int hasWritePermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA);
        return hasWritePermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestWriteStoragePermission(Activity activity) {
        if (Utils.hasMarshmallow()) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE_STORAGE);
        }
    }

    public static void requestReadWriteStoragePermission(Activity activity) {
        if (Utils.hasMarshmallow()) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE_STORAGE);
        }
    }

    public static void requestReadExternalStoragePermission(Activity activity) {
        if (Utils.hasMarshmallow()) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_READ_EXTARNAL_STORAGE);
        }
    }

    public static void requestReadExternalStoragePermission(@NonNull Fragment fragment, int requestCode) {
        if (Utils.hasMarshmallow()) {
            fragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
        }
    }

    public static void tryInterceptAppPermission(Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            int grantResult = grantResults[i];

            if (requestCode != REQUEST_PERMISSION_WRITE_STORAGE && requestCode != REQUEST_PERMISSION_READ_EXTARNAL_STORAGE) {
                continue;
            }

            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                CustomToast.CreateCustomToast(activity).showToast(R.string.permission_granted_text, permissions[i]);
            } else {
                CustomToast.CreateCustomToast(activity).showToastError(R.string.permission_is_not_granted_text, permissions[i]);
            }
        }
    }
}