package io.github.plastix.forage.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Utility class for handling Android Marshmallow runtime permissions.
 */
public class PermissionUtils {

    /**
     * Checks if specified permissions are granted and requests them if not.
     *
     * @param activity    Activity to request permissions from.
     * @param requestCode
     * @param permissions
     */
    public static void requestPermissions(Activity activity, int requestCode, String... permissions) {
        if (!hasPermissions(activity, permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    /**
     * Returns whether the application has access to the specified permissions.
     *
     * @param context     Required context.
     * @param permissions List of permissions to check.
     * @return True if all are granted, else false.
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String perm : permissions) {
            if (!hasPermission(context, perm)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the application has access to a specific permission.
     *
     * @param permission Permission to check.
     */
    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Returns whether all permissions have been granted based on the specified permission results.
     *
     * @param grantResults Permission grant results.
     * @return True if all permissions have been granted, else false.
     */
    public static boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        // If the request is canceled the results array will be empty
        if (grantResults.length == 0) {
            return false;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

}
