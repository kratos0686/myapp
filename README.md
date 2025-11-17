# Room Scanner - 3D Room Scanning Android App

A professional Android application for 3D room scanning with ARCore, ML-based damage detection, offline storage, and cloud synchronization.

## 🚀 Features

- **ARCore 3D Scanning**: Real-time room scanning with plane detection and measurements
- **ML Kit Analysis**: On-device AI for detecting damaged areas and estimating material types
- **Room Database**: Offline-first architecture with reactive data flow
- **Firebase Integration**: Cloud backup and synchronization for scans and notes
- **Jetpack Compose**: Modern Material Design 3 UI
- **RxJava Data Flow**: Reactive programming for seamless async operations
- **Drying Plan Generation**: AI-powered recommendations for water damage restoration

## 📱 Tech Stack

- **ARCore** - 3D spatial mapping and measurements
- **ML Kit & TensorFlow Lite** - On-device machine learning
- **Room Database** - Local data persistence with RxJava support
- **Firebase** (Firestore, Storage, Auth) - Cloud synchronization
- **Jetpack Compose** - Modern declarative UI
- **RxJava 3** - Reactive data streams
- **Kotlin Coroutines** - Async/await patterns
- **Material Design 3** - Latest design components

## 📖 Documentation

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Complete architecture overview and data flow
- **[SAMPLE_INTEGRATION.md](SAMPLE_INTEGRATION.md)** - Sample code for wiring up ARCore, Firebase, and RxJava

## 🏗️ Project Structure

```
app/src/main/java/com/roomscanner/app/
├── data/                      # Data layer (Room, Firebase, Repository)
├── domain/                    # Business logic (Use Cases)
├── ui/                        # Jetpack Compose UI screens
├── arcore/                    # ARCore integration
├── ml/                        # ML Kit integration
├── MainActivity.kt
└── RoomScannerApplication.kt
```

## 🛠️ Setup

### Prerequisites

1. Android Studio Hedgehog or newer
2. Android SDK 26+
3. ARCore-compatible device for testing
4. Firebase account (for cloud features)

### Firebase Configuration

1. Create a Firebase project at [firebase.google.com](https://firebase.google.com)
2. Add an Android app with package name: `com.roomscanner.app`
3. Download `google-services.json` and place in `app/` directory
4. Enable Firestore, Storage, and Authentication in Firebase Console

### Build

```bash
git clone https://github.com/kratos0686/Android2.0roomscanner.git
cd Android2.0roomscanner
./gradlew build
```

### Run

1. Connect an ARCore-compatible Android device
2. Open project in Android Studio
3. Sync Gradle files
4. Run the app

## 📚 Key Components

### ARCore Integration
- **File**: `app/src/main/java/com/roomscanner/app/arcore/ARCoreManager.kt`
- Features: Plane detection, point cloud collection, distance measurements

### ML Kit Analysis
- **File**: `app/src/main/java/com/roomscanner/app/ml/MLKitAnalyzer.kt`
- Features: Damage detection, material type estimation, confidence scoring

### Room Database
- **Files**: `app/src/main/java/com/roomscanner/app/data/`
- Features: Offline storage, reactive queries, type converters

### Firebase Sync
- **File**: `app/src/main/java/com/roomscanner/app/data/FirebaseSyncService.kt`
- Features: Background sync, conflict resolution, image uploads

## 💡 Usage Example

```kotlin
// Complete scan workflow with RxJava
val useCase = CreateRoomScanUseCase(repository, mlKitAnalyzer)
useCase.execute(roomName, dimensions, pointCloudData, capturedImages)
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        { scanId -> println("Scan saved: $scanId") },
        { error -> println("Error: ${error.message}") }
    )
```

## 🧪 Testing
# Room Scanner - Android 3D Room Scanning App

A modern Android application for 3D room scanning using ARCore, with offline storage, ML-powered analysis, and cloud synchronization.

## 🚀 Quick Start - Launch the App

**Ready to launch?** Follow these steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/kratos0686/Android2.0roomscanner.git
   cd Android2.0roomscanner
   ```

2. **Open in Android Studio:**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Build and Run:**
   - Click the Run button (▶️) or press Shift+F10
   - Select your Android device or emulator
   
**For detailed launch instructions, see [LAUNCH_GUIDE.md](LAUNCH_GUIDE.md)**

---

## Features

### 🏠 ARCore 3D Scanning
- Real-time 3D room scanning
- Automatic plane detection (floors, walls, ceilings)
- Room dimension extraction
- Point cloud data capture
- Mesh data generation

### 🗄️ Offline Storage with Room Database
- Local SQLite database for offline-first architecture
- Store scan data and job notes
- Support for Kotlin Coroutines and RxJava
- Automatic backup configuration
- Foreign key relationships and cascading deletes

### 🤖 ML Kit & TensorFlow Lite Integration
- Object detection in room scans
- Image labeling for material identification
- Custom TFLite models for damage detection
- GPU/NNAPI acceleration support
- On-device inference for privacy

### ☁️ Firebase Integration
- **Firestore**: Cloud database sync
- **Cloud Functions**: Serverless processing (report generation, cost estimation)
- **Storage**: Upload point clouds, mesh data, and images
- **ML Model Download**: Dynamic model updates

### 📝 Job Notes & Annotations
- Add notes to specific locations in scans
- Categorize notes (damage, measurements, materials)
- Offline storage with cloud sync
- Position tracking in 3D space

### 🎨 Modern UI with Jetpack Compose
- Material Design 3
- Reactive UI updates
- Navigation between screens
- Responsive layouts

### ⚡ Reactive Programming
- RxJava 3 support for reactive streams
- Kotlin Coroutines and Flow
- Seamless integration between both paradigms

## Architecture

The app follows a clean, layered architecture:

```
UI Layer (Compose) → ViewModel → Repository → Data Sources
                                              ├─ Room DB (Local)
                                              ├─ Firebase (Cloud)
                                              ├─ ML Kit
                                              └─ ARCore
```

See [ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed architecture documentation.

## Prerequisites

- Android Studio Arctic Fox or later
- Android device with ARCore support ([check compatibility](https://developers.google.com/ar/devices))
- Android 7.0 (API level 24) or higher
- Camera permissions
- Google account for Firebase setup

## Setup Instructions

> **📖 Comprehensive Launch Guide:** See [LAUNCH_GUIDE.md](LAUNCH_GUIDE.md) for detailed step-by-step instructions, troubleshooting, and device requirements.

### 1. Clone the Repository

```bash
git clone https://github.com/kratos0686/Android2.0roomscanner.git
cd Android2.0roomscanner
```

### 2. Firebase Setup

1. Create a Firebase project in [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app with package name `com.roomscanner.app`
3. Download `google-services.json` and place it in `app/` directory
4. Enable Firestore, Storage, and Cloud Functions

See [FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md) for detailed Firebase configuration.

### 3. Build the Project

```bash
./gradlew build
```

### 4. Run on Device

1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect device via USB
4. Run from Android Studio or:

```bash
./gradlew installDebug
```

## Project Structure

```
app/
├── src/main/java/com/roomscanner/app/
│   ├── data/
│   │   ├── entity/          # Room entities (ScanEntity, NoteEntity)
│   │   ├── dao/             # Data Access Objects
│   │   ├── repository/      # Repository layer
│   │   └── AppDatabase.kt   # Room database
│   ├── arcore/              # ARCore integration
│   │   ├── ARCoreSessionManager.kt
│   │   └── ScanDataProcessor.kt
│   ├── ml/                  # Machine Learning
│   │   ├── MLKitDetector.kt
│   │   └── TFLiteModelRunner.kt
│   ├── firebase/            # Firebase integration
│   │   ├── FirestoreSync.kt
│   │   ├── CloudFunctionsClient.kt
│   │   └── StorageManager.kt
│   ├── ui/                  # UI components
│   │   └── theme/
│   └── MainActivity.kt      # Main activity
├── src/main/res/
│   ├── values/
│   ├── xml/
│   └── ...
└── build.gradle.kts

docs/                        # Documentation
├── ARCHITECTURE.md          # Architecture overview
├── ROOM_DATABASE.md         # Room DB integration guide
├── FIREBASE_SETUP.md        # Firebase setup guide
├── ARCORE_INTEGRATION.md    # ARCore integration guide
└── TFLITE_DEPLOYMENT.md     # TFLite model deployment guide
```

## Key Components

### Room Database

Offline-first storage for scan data:

```kotlin
@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomName: String,
    val length: Float,
    val width: Float,
    val height: Float,
    val pointCloudPath: String?,
    val isSynced: Boolean = false
)
```

See [ROOM_DATABASE.md](docs/ROOM_DATABASE.md) for detailed usage.

### ARCore Integration

3D scanning with ARCore:

```kotlin
val sessionManager = ARCoreSessionManager(context)
val session = sessionManager.createSession()

val frame = session.update()
val dimensions = dataProcessor.extractRoomDimensions(frame)
```

See [ARCORE_INTEGRATION.md](docs/ARCORE_INTEGRATION.md) for detailed integration.

### ML Kit

Object detection and image labeling:

```kotlin
val mlKit = MLKitDetector(context)
val objects = mlKit.detectObjects(bitmap)
val labels = mlKit.labelImage(bitmap)
```

### TensorFlow Lite

Custom model inference:

```kotlin
val modelRunner = TFLiteModelRunner(context)
modelRunner.loadModel("damage_detector.tflite")
val damage = modelRunner.detectDamage(bitmap)
```

See [TFLITE_DEPLOYMENT.md](docs/TFLITE_DEPLOYMENT.md) for model deployment.

### Firebase Sync

Cloud synchronization:

```kotlin
val firestoreSync = FirestoreSync()
val firebaseId = firestoreSync.uploadScan(scan)

val cloudFunctions = CloudFunctionsClient()
val report = cloudFunctions.generatePDFReport(scanId)
```

See [FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md) for setup and usage.

## Usage Examples

### Creating a Scan

```kotlin
viewModelScope.launch {
    val scan = ScanEntity(
        roomName = "Living Room",
        length = 5.0f,
        width = 4.0f,
        height = 2.5f
    )
    val scanId = repository.insertScan(scan)
}
```

### Adding Notes

```kotlin
viewModelScope.launch {
    val note = NoteEntity(
        scanId = scanId,
        title = "Damage Found",
        content = "Water damage on north wall",
        category = "damage"
    )
    repository.insertNote(note)
}
```

### Observing Data with Flow

```kotlin
repository.getAllScans()
    .collect { scans ->
        // Update UI with scans
    }
```

### Observing Data with RxJava

```kotlin
repository.getAllScansRx()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe { scans ->
        // Update UI with scans
    }
```

## Documentation

Comprehensive guides are available in the `docs/` directory:

- **[Architecture Overview](docs/ARCHITECTURE.md)** - System design and component responsibilities
- **[Room Database Integration](docs/ROOM_DATABASE.md)** - Local storage guide
- **[Firebase Setup](docs/FIREBASE_SETUP.md)** - Cloud services configuration
- **[ARCore Integration](docs/ARCORE_INTEGRATION.md)** - 3D scanning implementation
- **[TFLite Deployment](docs/TFLITE_DEPLOYMENT.md)** - ML model deployment and optimization

## Dependencies

Key dependencies used in this project:

- **Jetpack Compose** - Modern UI toolkit
- **Room** - Local database
- **ARCore** - 3D scanning
- **ML Kit** - Object detection and labeling
- **TensorFlow Lite** - Custom ML models
- **Firebase** - Cloud services (Firestore, Functions, Storage)
- **RxJava 3** - Reactive programming
- **Kotlin Coroutines** - Asynchronous programming

See [build.gradle.kts](app/build.gradle.kts) for complete dependency list.

## Testing

Run tests:

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## 📄 License

See LICENSE file for details.

## 🤝 Contributing

Pull requests are welcome! Please open an issue first to discuss proposed changes.

## 📞 Support

For issues and questions, please open a GitHub issue.
# Instrumentation tests
./gradlew connectedAndroidTest
```

## Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) before submitting a Pull Request.

### For Human Contributors
- See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed contribution guidelines
- Follow the code style and conventions outlined in the guide
- Write tests for new features and bug fixes

### For GitHub Copilot Coding Agent
- See [.github/copilot-instructions.md](.github/copilot-instructions.md) for repository context and guidelines
- Follow the contribution workflow and best practices specified there

## License

This project is licensed under the MIT License.

## Resources

- [ARCore Developer Guide](https://developers.google.com/ar/develop)
- [Room Database Documentation](https://developer.android.com/training/data-storage/room)
- [Firebase Documentation](https://firebase.google.com/docs)
- [ML Kit Documentation](https://developers.google.com/ml-kit)
- [TensorFlow Lite Guide](https://www.tensorflow.org/lite)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

## Support

For issues, questions, or contributions, please open an issue on GitHub.

## Acknowledgments

- Google ARCore team for 3D scanning capabilities
- Firebase team for cloud services
- TensorFlow team for ML frameworks
- Android Jetpack team for modern development tools
