# Room Scanner Android App

An advanced Android application for 3D room scanning with ARCore, ML-based damage detection, and cloud synchronization.

## Architecture Overview

This application implements a modern Android architecture with the following components:

### Tech Stack

- **ARCore**: 3D room scanning, plane detection, and spatial measurements
- **ML Kit & TensorFlow Lite**: On-device AI for damage detection and material type estimation
- **Room Database**: Offline-first data storage with reactive queries
- **Firebase**: Cloud storage and synchronization (Firestore, Storage, Auth)
- **Jetpack Compose**: Modern declarative UI framework
- **RxJava 3**: Reactive data flow and async operations
- **Kotlin Coroutines**: Async/await patterns and Flow
- **Material Design 3**: Latest Material Design components

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer (Compose)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ ScanListScreen│  │ScanDetailScreen│ │  AR Scanner  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                           ↓ ↑ (Flows & RxJava)
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer (Use Cases)                  │
│  ┌─────────────────────┐  ┌──────────────────────────┐     │
│  │CreateRoomScanUseCase│  │GenerateDryingPlanUseCase │     │
│  └─────────────────────┘  └──────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                           ↓ ↑ (RxJava Streams)
┌─────────────────────────────────────────────────────────────┐
│                     Data Layer (Repository)                  │
│              ┌─────────────────────────┐                     │
│              │  RoomScanRepository     │                     │
│              └─────────────────────────┘                     │
│                      ↓              ↓                        │
│         ┌────────────┴──┐    ┌─────┴──────────┐            │
│         │ Room Database │    │ Firebase Sync  │            │
│         └───────────────┘    └────────────────┘            │
└─────────────────────────────────────────────────────────────┘
                           ↓ ↑
┌─────────────────────────────────────────────────────────────┐
│              External Services & Libraries                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  ARCore  │  │  ML Kit  │  │ Firebase │  │ TFLite   │  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Key Features

### 1. 3D Room Scanning with ARCore

**File**: `app/src/main/java/com/roomscanner/app/arcore/ARCoreManager.kt`

Features:
- Horizontal and vertical plane detection
- Point cloud data collection
- Real-time room dimension calculation
- Anchor-based measurement system
- Depth sensing for accurate measurements

### 2. ML-Based Analysis

**File**: `app/src/main/java/com/roomscanner/app/ml/MLKitAnalyzer.kt`

Features:
- Damage detection (cracks, water damage, holes)
- Material type estimation (wood, concrete, drywall, etc.)
- Confidence scoring
- Location mapping for detected issues

### 3. Offline-First Data Storage

**Files**: 
- `app/src/main/java/com/roomscanner/app/data/RoomScanEntities.kt`
- `app/src/main/java/com/roomscanner/app/data/RoomScanDao.kt`
- `app/src/main/java/com/roomscanner/app/data/RoomScannerDatabase.kt`

Features:
- Room Database with full CRUD operations
- Reactive queries with Flow and RxJava
- Type converters for complex data structures
- Offline storage of scan data and job notes

### 4. Firebase Cloud Sync

**File**: `app/src/main/java/com/roomscanner/app/data/FirebaseSyncService.kt`

Features:
- Automatic sync of unsynced data
- Firestore for structured data
- Storage for images and point clouds
- Conflict resolution and retry logic

### 5. Modern UI with Jetpack Compose

**Files**:
- `app/src/main/java/com/roomscanner/app/ui/ScanListScreen.kt`
- `app/src/main/java/com/roomscanner/app/ui/ScanDetailScreen.kt`

Features:
- Material Design 3 components
- Responsive layouts
- Real-time data updates
- Intuitive navigation

## Data Flow Example

### Creating a Room Scan

```kotlin
// 1. Start AR scanning
val arCoreManager = ARCoreManager(context)
arCoreManager.initializeSession()

// 2. Process AR frames
val dimensions = arCoreManager.calculateRoomDimensions(planes)
val pointCloudData = arCoreManager.exportPointCloudData()

// 3. Analyze with ML Kit
val mlKitAnalyzer = MLKitAnalyzer()
val damagedAreas = mlKitAnalyzer.detectDamagedAreas(bitmap, imageUri)
val materialEstimates = mlKitAnalyzer.estimateMaterialTypes(bitmap, "wall")

// 4. Save to database and sync to Firebase (RxJava chain)
val useCase = CreateRoomScanUseCase(repository, mlKitAnalyzer)
useCase.execute(roomName, dimensions, pointCloudData, capturedImages)
    .subscribe(
        { scanId -> println("Scan created: $scanId") },
        { error -> println("Error: ${error.message}") }
    )
```

### RxJava Data Flow

The application uses RxJava for reactive data flow:

```kotlin
// Data flows from Room Database -> Repository -> UI
repository.getAllScans()  // Returns Flow<List<RoomScan>>
    .collect { scans ->
        // UI automatically updates when data changes
        updateUI(scans)
    }

// Async operations with RxJava
repository.syncScansToFirebase()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        { /* Success */ },
        { error -> /* Handle error */ }
    )
```

## Setup Instructions

### Prerequisites

1. Android Studio Hedgehog or newer
2. Android SDK 26 or higher
3. ARCore supported device for testing
4. Firebase project (for cloud features)

### Firebase Setup

1. Create a Firebase project at [firebase.google.com](https://firebase.google.com)
2. Download `google-services.json`
3. Place it in `app/` directory
4. Enable Firestore, Storage, and Authentication in Firebase Console

### Build and Run

```bash
# Clone the repository
git clone https://github.com/kratos0686/Android2.0roomscanner.git

# Open in Android Studio
# Sync Gradle
# Connect ARCore-compatible device
# Run the app
```

## Code Structure

```
app/src/main/java/com/roomscanner/app/
├── data/                      # Data layer
│   ├── RoomScanEntities.kt    # Room entities and converters
│   ├── RoomScanDao.kt         # Database access objects
│   ├── RoomScannerDatabase.kt # Database configuration
│   ├── FirebaseSyncService.kt # Firebase integration
│   └── RoomScanRepository.kt  # Repository pattern
├── domain/                    # Business logic
│   ├── CreateRoomScanUseCase.kt      # Scan creation workflow
│   └── GenerateDryingPlanUseCase.kt  # Plan generation
├── ui/                        # Compose UI
│   ├── ScanListScreen.kt      # List of scans
│   ├── ScanDetailScreen.kt    # Scan details
│   └── theme/                 # Material Design theme
├── arcore/                    # ARCore integration
│   └── ARCoreManager.kt       # AR session management
├── ml/                        # Machine learning
│   └── MLKitAnalyzer.kt       # ML Kit integration
├── MainActivity.kt            # Main activity
└── RoomScannerApplication.kt  # Application class
```

## Dependencies

See `app/build.gradle.kts` for complete dependency list:

- ARCore 1.41.0
- ML Kit (Object Detection, Image Labeling)
- TensorFlow Lite 2.14.0
- Room 2.6.1
- RxJava 3.1.8
- Firebase BOM 32.7.0
- Jetpack Compose BOM 2023.10.01
- Kotlin 1.9.20

## Testing

The app includes test infrastructure:

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device)
./gradlew connectedAndroidTest
```

## Performance Considerations

- **Offline-First**: All data stored locally first, synced in background
- **On-Device ML**: No server calls for damage detection
- **Reactive Updates**: UI updates automatically with Flow
- **Background Sync**: Firebase sync happens on background threads
- **Memory Efficient**: Point cloud data compressed and paginated

## Future Enhancements

- [ ] Custom TensorFlow Lite model for better damage detection
- [ ] Real-time collaborative editing
- [ ] Export to PDF/CAD formats
- [ ] Integration with insurance APIs
- [ ] Augmented reality overlay for repair instructions
- [ ] Multi-room project management
- [ ] Cost estimation based on damage analysis

## License

See LICENSE file for details.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss proposed changes.
