# Setup Guide

This guide helps you set up and run the Room Scanner Android app.

## Prerequisites

1. **Android Studio**: Hedgehog (2023.1.1) or newer
2. **JDK**: Version 17 or higher
3. **Android SDK**: 
   - Minimum SDK: 26 (Android 8.0)
   - Target SDK: 34 (Android 14)
4. **ARCore Device**: Physical device that supports ARCore ([Check compatibility](https://developers.google.com/ar/devices))
5. **Firebase Account**: For cloud features (free tier works)

## Step-by-Step Setup

### 1. Clone the Repository

```bash
git clone https://github.com/kratos0686/Android2.0roomscanner.git
cd Android2.0roomscanner
```

### 2. Configure Firebase

#### Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Add project"
3. Enter project name: "Room Scanner" (or your choice)
4. Follow the setup wizard

#### Add Android App to Firebase

1. In Firebase Console, click "Add app" → Android
2. Enter package name: `com.roomscanner.app`
3. Enter app nickname: "Room Scanner"
4. Download `google-services.json`
5. Move it to `app/` directory

```bash
# Place the file here:
# Android2.0roomscanner/app/google-services.json
```

#### Enable Firebase Services

In Firebase Console:

1. **Firestore Database**:
   - Go to Firestore Database
   - Click "Create database"
   - Start in test mode (or production mode with rules)
   - Choose your region

2. **Storage**:
   - Go to Storage
   - Click "Get started"
   - Start in test mode
   - Choose your region

3. **Authentication** (Optional):
   - Go to Authentication
   - Click "Get started"
   - Enable Email/Password or Google Sign-In

### 3. Open in Android Studio

1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the cloned repository
4. Click "OK"

### 4. Sync Gradle

Android Studio will automatically start syncing Gradle. If not:

1. Click "File" → "Sync Project with Gradle Files"
2. Wait for sync to complete (first time may take a few minutes)

### 5. Connect ARCore-Compatible Device

1. Enable Developer Options on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → Developer Options

2. Enable USB Debugging

3. Connect device via USB

4. Allow USB debugging when prompted

5. Verify device is recognized:
   ```bash
   adb devices
   ```

### 6. Install ARCore (if not already installed)

ARCore will be automatically installed when you first run the app. Alternatively:

1. Open Google Play Store on device
2. Search "Google Play Services for AR"
3. Install/Update

### 7. Run the App

1. In Android Studio, select your device from the device dropdown
2. Click the green "Run" button (or press Shift+F10)
3. The app will build and install on your device

## Troubleshooting

### Gradle Sync Issues

**Problem**: Gradle sync fails with dependency resolution errors

**Solution**:
```bash
# Clear Gradle cache
./gradlew clean
./gradlew --stop

# In Android Studio:
# File → Invalidate Caches → Invalidate and Restart
```

### ARCore Installation Failed

**Problem**: App crashes with "ARCore not installed" error

**Solution**:
1. Check device compatibility: https://developers.google.com/ar/devices
2. Install ARCore from Play Store manually
3. Restart device and try again

### Firebase Connection Issues

**Problem**: Firebase services not working

**Solution**:
1. Verify `google-services.json` is in `app/` directory
2. Check package name matches: `com.roomscanner.app`
3. Verify Firebase services are enabled in console
4. Rebuild the project

### ML Kit Download Issues

**Problem**: ML Kit models not downloading

**Solution**:
1. Ensure device has internet connection
2. ML Kit downloads models on first use
3. Wait a few seconds after app launch
4. Check Logcat for download status

### Build Errors

**Problem**: Compilation errors

**Solution**:
```bash
# Clean and rebuild
./gradlew clean build

# Check Gradle version
./gradlew --version

# Update Gradle wrapper if needed
./gradlew wrapper --gradle-version=8.2
```

## Development Setup

### Run Unit Tests

```bash
./gradlew test
```

### Run Instrumented Tests

```bash
# Make sure device is connected
./gradlew connectedAndroidTest
```

### Check Code Style

```bash
./gradlew ktlintCheck
```

### Generate APK

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing)
./gradlew assembleRelease
```

## Configuration Options

### Changing Package Name

If you want to use a different package name:

1. Update `app/build.gradle.kts`:
   ```kotlin
   android {
       namespace = "com.yourcompany.roomscanner"
       defaultConfig {
           applicationId = "com.yourcompany.roomscanner"
       }
   }
   ```

2. Refactor package in Android Studio:
   - Right-click package → Refactor → Rename

3. Update Firebase configuration with new package name

### Adjusting Minimum SDK

To support older devices (with limited features):

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        minSdk = 24  // Change from 26
    }
}
```

Note: ARCore requires SDK 24+

### Enabling ProGuard for Release

```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

## Next Steps

After successful setup:

1. Read [ARCHITECTURE.md](ARCHITECTURE.md) to understand the codebase
2. Check [SAMPLE_INTEGRATION.md](SAMPLE_INTEGRATION.md) for integration examples
3. Explore the UI screens in `app/src/main/java/com/roomscanner/app/ui/`
4. Test ARCore scanning functionality
5. Try ML Kit damage detection with sample images
6. Customize the app for your use case

## Support

For issues:
1. Check [Troubleshooting](#troubleshooting) section
2. Review [GitHub Issues](https://github.com/kratos0686/Android2.0roomscanner/issues)
3. Create a new issue with detailed description

## Resources

- [ARCore Documentation](https://developers.google.com/ar)
- [ML Kit Documentation](https://developers.google.com/ml-kit)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Jetpack Compose Guide](https://developer.android.com/jetpack/compose)
- [Firebase Documentation](https://firebase.google.com/docs)
- [RxJava Documentation](https://github.com/ReactiveX/RxJava)
