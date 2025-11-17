# Quick Start Guide

## Getting Started with Room Scanner

This guide will help you quickly set up and run the Room Scanner app.

## Prerequisites

- **Android Studio** Arctic Fox or later
- **Android Device** with ARCore support ([check here](https://developers.google.com/ar/devices))
- **Android 7.0+** (API level 24 or higher)
- **Google Account** for Firebase

## 5-Minute Setup

### Step 1: Clone and Open Project

```bash
git clone https://github.com/kratos0686/Android2.0roomscanner.git
cd Android2.0roomscanner
```

Open in Android Studio.

### Step 2: Firebase Setup (Required)

1. Create Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add Android app with package name: `com.roomscanner.app`
3. Download `google-services.json`
4. Place in `app/` directory (rename from `google-services.json.example`)

### Step 3: Build and Run

```bash
./gradlew build
./gradlew installDebug
```

Or use Android Studio's Run button.

## What's Included

### ✅ Room Database Integration
- **Location**: `app/src/main/java/com/roomscanner/app/data/`
- **Files**: 
  - `entity/ScanEntity.kt` - Scan data model
  - `entity/NoteEntity.kt` - Note data model
  - `dao/ScanDao.kt` - Scan data access
  - `dao/NoteDao.kt` - Note data access
  - `AppDatabase.kt` - Database configuration
  - `repository/ScanRepository.kt` - Repository layer

**Features**:
- Offline-first architecture
- Coroutines and RxJava support
- Foreign key relationships
- Automatic sync flags

### ✅ ARCore Integration
- **Location**: `app/src/main/java/com/roomscanner/app/arcore/`
- **Files**:
  - `ARCoreSessionManager.kt` - Session management
  - `ScanDataProcessor.kt` - Data extraction

**Features**:
- 3D room scanning
- Plane detection (floors, walls, ceilings)
- Room dimension extraction
- Point cloud capture

### ✅ ML Kit & TensorFlow Lite
- **Location**: `app/src/main/java/com/roomscanner/app/ml/`
- **Files**:
  - `MLKitDetector.kt` - Object detection and labeling
  - `TFLiteModelRunner.kt` - Custom model inference

**Features**:
- Object detection
- Image labeling
- Damage detection (custom models)
- Material classification

### ✅ Firebase Integration
- **Location**: `app/src/main/java/com/roomscanner/app/firebase/`
- **Files**:
  - `FirestoreSync.kt` - Database sync
  - `CloudFunctionsClient.kt` - Serverless functions
  - `StorageManager.kt` - File uploads

**Features**:
- Cloud database sync
- File storage
- Serverless processing
- Report generation

### ✅ Jetpack Compose UI
- **Location**: `app/src/main/java/com/roomscanner/app/`
- **Files**:
  - `MainActivity.kt` - Main UI
  - `ui/theme/Theme.kt` - App theming

**Features**:
- Material Design 3
- Bottom navigation
- Floating action button
- Modern, reactive UI

## Code Examples

### Save a Scan

```kotlin
val repository = ScanRepository(scanDao, noteDao)

lifecycleScope.launch {
    val scan = ScanEntity(
        roomName = "Living Room",
        length = 5.0f,
        width = 4.0f,
        height = 2.5f
    )
    val scanId = repository.insertScan(scan)
}
```

### Add a Note

```kotlin
lifecycleScope.launch {
    val note = NoteEntity(
        scanId = scanId,
        title = "Damage Found",
        content = "Water damage on north wall",
        category = "damage"
    )
    repository.insertNote(note)
}
```

### Detect Objects with ML Kit

```kotlin
val mlKit = MLKitDetector(context)

lifecycleScope.launch {
    val objects = mlKit.detectObjects(bitmap)
    objects.forEach { obj ->
        println("${obj.label}: ${obj.confidence}")
    }
}
```

### Scan Room with ARCore

```kotlin
val sessionManager = ARCoreSessionManager(context)
val session = sessionManager.createSession()

val frame = session.update()
val dimensions = dataProcessor.extractRoomDimensions(frame)
```

### Sync to Firebase

```kotlin
val firestoreSync = FirestoreSync()

lifecycleScope.launch {
    val firebaseId = firestoreSync.uploadScan(scan)
    repository.markScanAsSynced(scan.id, firebaseId)
}
```

## Testing the App

### 1. Test Room Database

```kotlin
// In a ViewModel or Repository
lifecycleScope.launch {
    // Insert test scan
    val scan = ScanEntity(
        roomName = "Test Room",
        length = 3.0f,
        width = 4.0f,
        height = 2.5f
    )
    val id = repository.insertScan(scan)
    
    // Verify insertion
    val retrieved = repository.getScanById(id)
    println("Saved scan: ${retrieved?.roomName}")
}
```

### 2. Test ML Kit (requires real device)

```kotlin
val mlKit = MLKitDetector(context)
val testBitmap = BitmapFactory.decodeResource(resources, R.drawable.test_image)

lifecycleScope.launch {
    val objects = mlKit.detectObjects(testBitmap)
    println("Detected ${objects.size} objects")
}
```

### 3. Test ARCore (requires ARCore-compatible device)

Run the app on a physical device and check ARCore availability:

```kotlin
val sessionManager = ARCoreSessionManager(this)
if (sessionManager.checkARCoreAvailability()) {
    println("ARCore is available!")
} else {
    println("ARCore not available")
}
```

## Next Steps

### 1. Add Custom ML Models

See [TFLITE_DEPLOYMENT.md](TFLITE_DEPLOYMENT.md) for:
- Converting PyTorch/TensorFlow models to TFLite
- Optimizing models with quantization
- Deploying models via Firebase

### 2. Set Up Cloud Functions

See [FIREBASE_SETUP.md](FIREBASE_SETUP.md) for:
- Deploying Cloud Functions
- Setting up triggers
- Implementing custom processing

### 3. Customize UI

Edit `MainActivity.kt` to:
- Add more screens
- Customize navigation
- Add data visualization

### 4. Add Authentication

Implement Firebase Authentication:

```kotlin
val auth = Firebase.auth

// Sign in anonymously for testing
auth.signInAnonymously()
    .addOnSuccessListener { result ->
        println("Signed in: ${result.user?.uid}")
    }
```

## Common Issues

### "google-services.json not found"

**Solution**: Download from Firebase Console and place in `app/` directory.

### "ARCore not available"

**Solution**: Check device compatibility at https://developers.google.com/ar/devices

### "ML Kit fails to detect objects"

**Solution**: Ensure:
- Image quality is good
- Sufficient lighting
- Objects are visible and in focus

### Build errors

**Solution**: 
```bash
./gradlew clean
./gradlew build
```

## Documentation

For detailed guides, see:

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System design
- **[ROOM_DATABASE.md](ROOM_DATABASE.md)** - Database guide
- **[FIREBASE_SETUP.md](FIREBASE_SETUP.md)** - Firebase configuration
- **[ARCORE_INTEGRATION.md](ARCORE_INTEGRATION.md)** - 3D scanning
- **[TFLITE_DEPLOYMENT.md](TFLITE_DEPLOYMENT.md)** - ML model deployment
- **[ML_INTEGRATION.md](ML_INTEGRATION.md)** - ML Kit usage

## Support

- **Issues**: Open on GitHub
- **Questions**: Check documentation first
- **Contributions**: Pull requests welcome!

## Project Structure Summary

```
Android2.0roomscanner/
├── app/
│   ├── src/main/
│   │   ├── java/com/roomscanner/app/
│   │   │   ├── data/          # Room Database
│   │   │   ├── arcore/        # ARCore integration
│   │   │   ├── ml/            # ML Kit & TFLite
│   │   │   ├── firebase/      # Firebase services
│   │   │   ├── ui/            # Compose UI
│   │   │   └── MainActivity.kt
│   │   ├── res/               # Resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts       # App dependencies
├── firebase/
│   └── functions/             # Cloud Functions
├── docs/                      # Documentation
├── build.gradle.kts           # Project build file
└── settings.gradle.kts        # Project settings
```

## License

MIT License - See repository for details.

---

**Ready to build?** Start with Firebase setup, then run the app!
