package dev.ragnarok.fenrir.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

import java.util.Map;

import dev.ragnarok.fenrir.R;

public class AppPerms {
    public static boolean hasWriteStoragePermision(@NonNull Context context) {
        if (!Utils.hasMarshmallow()) return true;
        int hasWritePermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return hasWritePermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasReadWriteStoragePermision(@NonNull Context context) {
        if (!Utils.hasMarshmallow()) return true;
        int hasWritePermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int hasReadPermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return hasWritePermission == PackageManager.PERMISSION_GRANTED && hasReadPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasReadStoragePermision(@NonNull Context context) {
        if (!Utils.hasMarshmallow()) return true;
        int hasWritePermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return hasWritePermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasCameraPermision(@NonNull Context context) {
        if (!Utils.hasMarshmallow()) return true;
        int hasCameraPermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA);
        int hasWritePermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int hasReadPermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return hasCameraPermission == PackageManager.PERMISSION_GRANTED && hasWritePermission == PackageManager.PERMISSION_GRANTED && hasReadPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static doRequestPermissions requestPermissions(@NonNull Fragment fragment, @NonNull String[] permissions, @NonNull onPermissionsGranted callback) {
        ActivityResultLauncher<String[]> request = fragment.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if (Utils.checkValues(result.values())) {
                    callback.granted();
                } else {
                    Utils.showRedTopToast(fragment.requireActivity(), R.string.not_permitted);
                }
            }
        });
        return new doRequestPermissions() {
            @Override
            public void launch() {
                request.launch(permissions);
            }
        };
    }

    public interface doRequestPermissions {
        void launch();
    }

    public interface onPermissionsGranted {
        void granted();
    }
}
