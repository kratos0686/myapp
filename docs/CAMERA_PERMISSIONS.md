# Camera Permission Setup Guide

This guide explains how camera permissions are handled in the Room Scanner app and how to implement camera functionality.

## Overview

The app requires multiple permissions for full functionality:
- **CAMERA**: Required for 3D scanning and photo capture
- **WRITE_EXTERNAL_STORAGE**: Save scan data and exports
- **READ_EXTERNAL_STORAGE**: Read previously saved scans
- **RECORD_AUDIO**: Voice guidance feature
- **ACCESS_FINE_LOCATION**: AR features and location tagging

## Permission Handling in MainActivity

The `MainActivity` class already includes comprehensive permission handling:

### Permissions Declaration

```java
private static final int PERMISSION_REQUEST_CODE = 1001;
private static final String[] REQUIRED_PERMISSIONS = new String[]{
    Manifest.permission.CAMERA,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.ACCESS_FINE_LOCATION
};
```

### Check All Permissions

```java
private boolean checkAllPermissions() {
    for (String permission : REQUIRED_PERMISSIONS) {
        if (ContextCompat.checkSelfPermission(this, permission) 
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
    }
    return true;
}
```

### Request Permissions

```java
private void requestPermissions() {
    ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
}
```

### Handle Permission Results

```java
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    
    if (requestCode == PERMISSION_REQUEST_CODE) {
        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        
        if (allGranted) {
            Toast.makeText(this, "All permissions granted! Ready to scan.", Toast.LENGTH_SHORT).show();
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "Some permissions denied. App functionality may be limited.",
                    Snackbar.LENGTH_LONG).show();
        }
    }
}
```

## Manifest Permissions

Permissions are declared in `AndroidManifest.xml`:

```xml
<!-- Camera and scanning permissions -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="true" />
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />

<!-- Storage permissions -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
                 android:maxSdkVersion="28" />

<!-- Audio for voice guidance -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- Location for AR features -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Internet for cloud sync -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- ARCore requirements -->
<uses-feature android:name="android.hardware.camera.ar" android:required="false" />
```

## CameraX Integration

### Step 1: Add CameraX Dependencies

Dependencies are already included in `app/build.gradle`:

```gradle
// CameraX
def camerax_version = "1.3.1"
implementation "androidx.camera:camera-core:${camerax_version}"
implementation "androidx.camera:camera-camera2:${camerax_version}"
implementation "androidx.camera:camera-lifecycle:${camerax_version}"
implementation "androidx.camera:camera-view:${camerax_version}"
```

### Step 2: Create Camera Preview Layout

Add to your scanning activity layout:

```xml
<androidx.camera.view.PreviewView
    android:id="@+id/previewView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### Step 3: Initialize Camera

```java
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;

public class ScanningActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ImageCapture imageCapture;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        
        previewView = findViewById(R.id.previewView);
        
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }
    
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
            ProcessCameraProvider.getInstance(this);
        
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (Exception e) {
                Log.e("CameraX", "Failed to start camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        // Preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        
        // Image capture use case
        imageCapture = new ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build();
        
        // Select back camera
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        
        try {
            // Unbind all use cases before rebinding
            cameraProvider.unbindAll();
            
            // Bind use cases to camera
            Camera camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture);
                
            // Enable tap to focus
            setupTapToFocus(camera);
            
        } catch (Exception e) {
            Log.e("CameraX", "Failed to bind use cases", e);
        }
    }
    
    private void setupTapToFocus(Camera camera) {
        previewView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                MeteringPointFactory factory = previewView.getMeteringPointFactory();
                MeteringPoint point = factory.createPoint(event.getX(), event.getY());
                
                FocusMeteringAction action = new FocusMeteringAction.Builder(point)
                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                    .build();
                    
                camera.getCameraControl().startFocusAndMetering(action);
                return true;
            }
            return false;
        });
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, 
            new String[]{Manifest.permission.CAMERA}, 
            CAMERA_PERMISSION_REQUEST_CODE);
    }
}
```

### Step 4: Capture Images

```java
private void capturePhoto() {
    if (imageCapture == null) return;
    
    // Create output file
    File photoFile = new File(
        getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "scan_" + System.currentTimeMillis() + ".jpg"
    );
    
    ImageCapture.OutputFileOptions outputOptions = 
        new ImageCapture.OutputFileOptions.Builder(photoFile).build();
    
    // Capture image
    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
        new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                String msg = "Photo captured: " + photoFile.getAbsolutePath();
                Toast.makeText(ScanningActivity.this, msg, Toast.LENGTH_SHORT).show();
                Log.d("CameraX", msg);
            }
            
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraX", "Photo capture failed", exception);
                Toast.makeText(ScanningActivity.this, 
                    "Failed to capture photo", Toast.LENGTH_SHORT).show();
            }
        }
    );
}
```

## ARCore Integration for 3D Scanning

### Step 1: Check ARCore Availability

```java
import com.google.ar.core.ArCoreApk;

private boolean checkARCoreAvailability() {
    ArCoreApk.Availability availability = ArCoreApk.getInstance()
        .checkAvailability(this);
        
    if (availability.isSupported()) {
        return true;
    } else {
        Toast.makeText(this, "ARCore not supported on this device", 
            Toast.LENGTH_LONG).show();
        return false;
    }
}
```

### Step 2: Request ARCore Installation

```java
private void requestARCoreInstallation() {
    try {
        switch (ArCoreApk.getInstance().requestInstall(this, true)) {
            case INSTALLED:
                // ARCore is installed
                startARSession();
                break;
            case INSTALL_REQUESTED:
                // ARCore installation requested
                // Will return to onResume() after installation
                break;
        }
    } catch (Exception e) {
        Log.e("ARCore", "ARCore installation failed", e);
    }
}
```

### Step 3: Create AR Session

```java
import com.google.ar.core.Session;
import com.google.ar.core.Config;

private Session arSession;

private void startARSession() {
    try {
        arSession = new Session(this);
        
        Config config = new Config(arSession);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        config.setFocusMode(Config.FocusMode.AUTO);
        arSession.configure(config);
        
        arSession.resume();
        Log.d("ARCore", "AR session started");
        
    } catch (Exception e) {
        Log.e("ARCore", "Failed to create AR session", e);
    }
}
```

## Permission Request Flow

### User Flow Diagram

```
1. User launches app
   ↓
2. App checks permissions
   ↓
3. Missing permissions? → Show explanation dialog
   ↓
4. Request permissions
   ↓
5. User grants/denies
   ↓
6a. All granted → Enable all features
6b. Some denied → Show limited functionality message
   ↓
7. User can retry from Settings button
```

### Handling Permission Denial

```java
private void handlePermissionDenied(String permission) {
    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
        // Show explanation and request again
        new AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage(getPermissionRationale(permission))
            .setPositiveButton("Grant", (dialog, which) -> requestPermissions())
            .setNegativeButton("Cancel", null)
            .show();
    } else {
        // User selected "Don't ask again"
        showSettingsDialog();
    }
}

private String getPermissionRationale(String permission) {
    switch (permission) {
        case Manifest.permission.CAMERA:
            return "Camera permission is required for 3D scanning";
        case Manifest.permission.WRITE_EXTERNAL_STORAGE:
            return "Storage permission is required to save scan data";
        case Manifest.permission.RECORD_AUDIO:
            return "Audio permission is required for voice guidance";
        case Manifest.permission.ACCESS_FINE_LOCATION:
            return "Location permission is required for AR features";
        default:
            return "This permission is required for app functionality";
    }
}

private void showSettingsDialog() {
    new AlertDialog.Builder(this)
        .setTitle("Permissions Required")
        .setMessage("Please grant permissions in Settings to use all features")
        .setPositiveButton("Open Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        })
        .setNegativeButton("Cancel", null)
        .show();
}
```

## Best Practices

### 1. Request Permissions at the Right Time

- Don't request all permissions at app launch
- Request permissions when the user tries to use a feature
- Provide context before requesting permissions

### 2. Handle Permission States

```java
public enum PermissionState {
    GRANTED,           // Permission is granted
    DENIED,            // Permission is denied but can be requested again
    PERMANENTLY_DENIED // User selected "Don't ask again"
}

private PermissionState getPermissionState(String permission) {
    if (ContextCompat.checkSelfPermission(this, permission) 
            == PackageManager.PERMISSION_GRANTED) {
        return PermissionState.GRANTED;
    } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
        return PermissionState.DENIED;
    } else {
        return PermissionState.PERMANENTLY_DENIED;
    }
}
```

### 3. Graceful Degradation

```java
private void initializeFeatures() {
    // Camera-dependent features
    if (hasPermission(Manifest.permission.CAMERA)) {
        enableScanning();
    } else {
        disableScanning();
        showCameraPermissionRequired();
    }
    
    // Storage-dependent features
    if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        enableExport();
    } else {
        disableExport();
    }
    
    // Optional features
    if (hasPermission(Manifest.permission.RECORD_AUDIO)) {
        enableVoiceGuidance();
    }
}
```

### 4. Android 11+ Storage Changes

For Android 11 (API 30) and above, use scoped storage:

```java
// No WRITE_EXTERNAL_STORAGE permission needed for app-specific directories
File appDir = getExternalFilesDir(null);
File scanFile = new File(appDir, "scans/scan_" + System.currentTimeMillis() + ".obj");
```

## Testing Permissions

### Test on Different Android Versions

- **Android 6.0 (API 23)**: Runtime permissions introduced
- **Android 10 (API 29)**: Scoped storage, location permission changes
- **Android 11 (API 30)**: Storage permissions simplified
- **Android 12 (API 31)**: Approximate location option
- **Android 13 (API 33)**: Granular media permissions

### Test Permission Scenarios

1. ✅ Grant all permissions
2. ❌ Deny camera permission
3. ❌ Deny storage permission
4. ⚠️ Deny with "Don't ask again"
5. 🔄 Revoke permission after granting
6. 🔙 Return from Settings after granting

## Troubleshooting

### Permission not being requested

- Check `AndroidManifest.xml` has permission declared
- Verify targetSdkVersion >= 23
- Ensure you're testing on a device with API 23+

### Permission always denied

- Check if user selected "Don't ask again"
- Guide user to app Settings to manually grant
- Clear app data and test again

### Camera not working after permission granted

- Restart camera initialization after permission granted
- Check for camera hardware availability
- Verify no other app is using the camera

## Additional Resources

- [Android Permissions Overview](https://developer.android.com/guide/topics/permissions/overview)
- [CameraX Documentation](https://developer.android.com/training/camerax)
- [ARCore Documentation](https://developers.google.com/ar)
- [Request Runtime Permissions](https://developer.android.com/training/permissions/requesting)
