package com.roomscanner.app.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Utility class for managing app permissions
 * Simplifies permission checking and requesting across the app
 */
public class PermissionHelper {

    // Permission request codes
    public static final int REQUEST_CAMERA = 100;
    public static final int REQUEST_STORAGE = 101;
    public static final int REQUEST_AUDIO = 102;
    public static final int REQUEST_LOCATION = 103;
    public static final int REQUEST_ALL_PERMISSIONS = 1001;

    // Required permissions for various features
    public static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    public static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] AUDIO_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO
    };

    public static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static final String[] ALL_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    /**
     * Check if a single permission is granted
     */
    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if all permissions in an array are granted
     */
    public static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (!hasPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if camera permission is granted
     */
    public static boolean hasCameraPermission(Context context) {
        return hasPermission(context, Manifest.permission.CAMERA);
    }

    /**
     * Check if storage permissions are granted
     */
    public static boolean hasStoragePermission(Context context) {
        return hasPermissions(context, STORAGE_PERMISSIONS);
    }

    /**
     * Check if audio permission is granted
     */
    public static boolean hasAudioPermission(Context context) {
        return hasPermission(context, Manifest.permission.RECORD_AUDIO);
    }

    /**
     * Check if location permissions are granted
     */
    public static boolean hasLocationPermission(Context context) {
        return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * Check if all required permissions are granted
     */
    public static boolean hasAllPermissions(Context context) {
        return hasPermissions(context, ALL_PERMISSIONS);
    }

    /**
     * Request a single permission
     */
    public static void requestPermission(Activity activity, String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

    /**
     * Request multiple permissions
     */
    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * Request camera permission
     */
    public static void requestCameraPermission(Activity activity) {
        requestPermissions(activity, CAMERA_PERMISSIONS, REQUEST_CAMERA);
    }

    /**
     * Request storage permissions
     */
    public static void requestStoragePermission(Activity activity) {
        requestPermissions(activity, STORAGE_PERMISSIONS, REQUEST_STORAGE);
    }

    /**
     * Request audio permission
     */
    public static void requestAudioPermission(Activity activity) {
        requestPermissions(activity, AUDIO_PERMISSIONS, REQUEST_AUDIO);
    }

    /**
     * Request location permission
     */
    public static void requestLocationPermission(Activity activity) {
        requestPermissions(activity, LOCATION_PERMISSIONS, REQUEST_LOCATION);
    }

    /**
     * Request all required permissions
     */
    public static void requestAllPermissions(Activity activity) {
        requestPermissions(activity, ALL_PERMISSIONS, REQUEST_ALL_PERMISSIONS);
    }

    /**
     * Check if we should show permission rationale
     */
    public static boolean shouldShowRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * Check if any permission in array needs rationale
     */
    public static boolean shouldShowRationale(Activity activity, String[] permissions) {
        for (String permission : permissions) {
            if (shouldShowRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get permission state for a specific permission
     */
    public static PermissionState getPermissionState(Activity activity, String permission) {
        if (hasPermission(activity, permission)) {
            return PermissionState.GRANTED;
        } else if (shouldShowRationale(activity, permission)) {
            return PermissionState.DENIED;
        } else {
            return PermissionState.PERMANENTLY_DENIED;
        }
    }

    /**
     * Check if all permissions in the request results are granted
     */
    public static boolean areAllPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return grantResults.length > 0;
    }

    /**
     * Get array of denied permissions from request results
     */
    public static String[] getDeniedPermissions(String[] permissions, int[] grantResults) {
        int deniedCount = 0;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                deniedCount++;
            }
        }

        String[] deniedPermissions = new String[deniedCount];
        int index = 0;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions[index++] = permissions[i];
            }
        }
        return deniedPermissions;
    }

    /**
     * Open app settings so user can manually grant permissions
     */
    public static void openAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    /**
     * Get human-readable permission name
     */
    public static String getPermissionName(String permission) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                return "Camera";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Storage (Write)";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "Storage (Read)";
            case Manifest.permission.RECORD_AUDIO:
                return "Microphone";
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return "Location (Precise)";
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "Location (Approximate)";
            default:
                return permission.substring(permission.lastIndexOf('.') + 1);
        }
    }

    /**
     * Get permission rationale message
     */
    public static String getPermissionRationale(String permission) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                return "Camera permission is required for 3D scanning and capturing room photos.";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "Storage permission is required to save and load scan data, exports, and project files.";
            case Manifest.permission.RECORD_AUDIO:
                return "Microphone permission is required for voice-guided scanning instructions.";
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return "Location permission is required for AR features and geotagging scans.";
            default:
                return "This permission is required for proper app functionality.";
        }
    }

    /**
     * Permission state enum
     */
    public enum PermissionState {
        GRANTED,              // Permission is granted
        DENIED,               // Permission is denied but can be requested again
        PERMANENTLY_DENIED    // User selected "Don't ask again"
    }

    /**
     * Permission callback interface
     */
    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
        void onPermissionPermanentlyDenied();
    }
}
