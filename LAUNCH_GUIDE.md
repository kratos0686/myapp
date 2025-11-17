# Room Scanner App - Launch Guide

This guide explains how to launch and run the Room Scanner Android application.

## Prerequisites

Before you can launch the app, ensure you have:

1. **Android Studio** - Arctic Fox (2020.3.1) or later
   - Download from: https://developer.android.com/studio

2. **Java Development Kit (JDK)** - Version 17
   - Android Studio includes a JDK, or download from: https://adoptium.net/

3. **Android SDK** - API Level 26 or higher
   - Installed via Android Studio SDK Manager

4. **Android Device or Emulator**
   - Physical device with ARCore support (recommended for full functionality)
   - OR Android Emulator with API Level 26+ 
   - Check ARCore device compatibility: https://developers.google.com/ar/devices

## Setup Steps

### 1. Clone the Repository (if not already done)

```bash
git clone https://github.com/kratos0686/Android2.0roomscanner.git
cd Android2.0roomscanner
```

### 2. Configure Firebase (Required)

The app includes a placeholder `google-services.json` file. For full functionality, you need to:

1. Create a Firebase project:
   - Go to https://console.firebase.google.com/
   - Click "Add project" or use an existing project
   - Follow the setup wizard

2. Add an Android app to your Firebase project:
   - Package name: `com.roomscanner.app`
   - Download the `google-services.json` file
   - Replace `app/google-services.json` with your downloaded file

**Note:** The app will compile with the placeholder file, but Firebase features (Firestore, Storage, Cloud Functions) will not work until you use a real configuration.

For detailed Firebase setup, see: [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md)

### 3. Open Project in Android Studio

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `Android2.0roomscanner` directory
4. Click "OK"

Android Studio will:
- Sync Gradle dependencies (this may take several minutes)
- Index the project files
- Download any missing SDK components

### 4. Sync Gradle

If Gradle doesn't sync automatically:

1. Click "File" → "Sync Project with Gradle Files"
2. Wait for the sync to complete
3. Check the "Build" panel for any errors

### 5. Configure Android SDK

If you see SDK-related errors:

1. Open "Tools" → "SDK Manager"
2. Ensure these are installed:
   - Android SDK Build-Tools 34.0.0 or later
   - Android SDK Platform 34 (API Level 34)
   - Android SDK Platform-Tools
   - Google Play services
3. Click "Apply" to install missing components

## Building the App

### Command Line Build

Using the Gradle wrapper:

```bash
# On macOS/Linux:
./gradlew assembleDebug

# On Windows:
gradlew.bat assembleDebug
```

The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Android Studio Build

1. In Android Studio, click "Build" → "Make Project" (Ctrl+F9 / Cmd+F9)
2. Wait for the build to complete
3. Check the "Build" panel for success/errors

## Running the App

### Option 1: Run on Physical Device (Recommended)

1. Enable Developer Options on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Return to Settings → Developer Options
   - Enable "USB Debugging"

2. Connect device via USB cable

3. In Android Studio:
   - Click the "Run" button (green play icon) or press Shift+F10
   - Select your device from the list
   - Click "OK"

### Option 2: Run on Emulator

1. Open AVD Manager:
   - Click "Tools" → "AVD Manager"
   - Click "Create Virtual Device"
   
2. Choose a device definition (e.g., Pixel 5)

3. Select a system image (API Level 26+, x86_64 recommended)

4. Click "Finish"

5. Start the emulator and run the app

**Note:** ARCore features may not work on emulators. For full 3D scanning functionality, use a physical device.

### Option 3: Install APK Manually

After building:

```bash
# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# Or drag and drop the APK onto the emulator
```

## Verifying the Launch

Once the app launches, you should see:

1. **Main Screen** with three tabs:
   - 🏠 Scans
   - 📝 Notes
   - ⚙️ Settings

2. **Features List** on the Scans screen:
   - ✓ ARCore 3D scanning
   - ✓ ML Kit object detection
   - ✓ Offline storage with Room DB
   - ✓ Firebase cloud sync
   - ✓ RxJava reactive updates

3. **Camera Button** (floating action button) on the Scans tab

## Troubleshooting

### Build Errors

**Error:** "SDK location not found"
- **Solution:** Create `local.properties` file with:
  ```
  sdk.dir=/path/to/Android/Sdk
  ```

**Error:** "Plugin [id: 'com.android.application'] was not found"
- **Solution:** Ensure you're building from Android Studio or have Android SDK installed

**Error:** "google-services.json is missing"
- **Solution:** The placeholder file should work for compilation. For full Firebase functionality, add your own configuration.

### Runtime Errors

**Issue:** Camera permission denied
- **Solution:** Grant camera permission in device Settings → Apps → Room Scanner → Permissions

**Issue:** ARCore not available
- **Solution:** Check if your device supports ARCore: https://developers.google.com/ar/devices
- Install Google Play Services for AR from Play Store

**Issue:** Firebase features not working
- **Solution:** Replace the placeholder `google-services.json` with your actual Firebase configuration

### Gradle Issues

**Issue:** Gradle sync fails
```bash
# Clean and rebuild:
./gradlew clean
./gradlew build --refresh-dependencies
```

**Issue:** Gradle daemon issues
```bash
# Stop all Gradle daemons:
./gradlew --stop
```

## Testing the App

### Quick Test Checklist

- [ ] App launches without crashing
- [ ] Three tabs are visible and clickable
- [ ] Camera button appears on Scans tab
- [ ] UI responds to interactions
- [ ] No immediate crashes or errors

### Testing Room Database

The app includes Room database for offline storage. To test:

1. Navigate to Scans tab
2. Create a test scan (if ARCore available)
3. Data persists across app restarts

### Testing ARCore (Physical Device Only)

1. Tap the camera button
2. Grant camera permissions when prompted
3. Point camera at a flat surface
4. Look for plane detection indicators

### Testing ML Kit

ML Kit features work on both emulators and devices:

1. Capture or select an image
2. Object detection runs automatically
3. Results displayed with confidence scores

## Next Steps

After successfully launching:

1. **Explore the Codebase**
   - See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
   - Review [docs/QUICK_START.md](docs/QUICK_START.md)

2. **Configure Firebase** 
   - Complete Firebase setup for cloud features
   - See [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md)

3. **Deploy ML Models**
   - Add custom TFLite models
   - See [docs/TFLITE_DEPLOYMENT.md](docs/TFLITE_DEPLOYMENT.md)

4. **Customize the App**
   - Modify UI in `MainActivity.kt`
   - Add new features as needed

## Project Structure

```
Android2.0roomscanner/
├── app/                           # Android app module
│   ├── src/main/
│   │   ├── java/com/roomscanner/app/
│   │   │   ├── data/              # Room Database
│   │   │   ├── arcore/            # ARCore integration
│   │   │   ├── ml/                # ML Kit & TFLite
│   │   │   ├── firebase/          # Firebase services
│   │   │   ├── ui/                # Compose UI
│   │   │   └── MainActivity.kt    # Main entry point
│   │   ├── res/                   # Resources
│   │   └── AndroidManifest.xml    # App manifest
│   ├── build.gradle.kts           # App dependencies
│   └── google-services.json       # Firebase config
├── gradle/                        # Gradle wrapper
├── docs/                          # Documentation
├── gradlew / gradlew.bat          # Gradle wrapper scripts
├── build.gradle.kts               # Project build config
├── settings.gradle.kts            # Project settings
└── README.md                      # Main documentation
```

## Support

For issues or questions:

1. Check existing documentation in `docs/` directory
2. Review troubleshooting section above
3. Open an issue on GitHub
4. Consult Android/ARCore/Firebase official documentation

## Resources

- **Android Developer Docs:** https://developer.android.com/
- **ARCore Documentation:** https://developers.google.com/ar
- **Firebase Documentation:** https://firebase.google.com/docs
- **Jetpack Compose:** https://developer.android.com/jetpack/compose
- **Room Database:** https://developer.android.com/training/data-storage/room
- **ML Kit:** https://developers.google.com/ml-kit

---

**Ready to launch!** Follow the steps above to get your Room Scanner app running.
