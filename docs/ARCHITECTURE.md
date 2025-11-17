# Architecture Overview

## System Architecture

The Room Scanner app follows a clean, layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                             │
│                   (Jetpack Compose)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Scan UI    │  │   Notes UI   │  │ Settings UI  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                     ViewModel Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ ScanViewModel│  │ NoteViewModel│  │ARCoreViewModel│     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    Repository Layer                          │
│  ┌────────────────────────────────────────────────────┐     │
│  │              ScanRepository                        │     │
│  │  - Coordinates data sources                        │     │
│  │  - Handles offline/online sync                     │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
         │                    │                    │
┌────────▼────────┐  ┌────────▼────────┐  ┌───────▼──────┐
│  Local Storage  │  │  Cloud Storage  │  │  ML Services │
│  (Room DB)      │  │   (Firebase)    │  │  (ML Kit)    │
│                 │  │                 │  │              │
│ ┌─────────────┐ │  │ ┌─────────────┐ │  │ ┌──────────┐ │
│ │  Scan DAO   │ │  │ │  Firestore  │ │  │ │  Object  │ │
│ │  Note DAO   │ │  │ │  Functions  │ │  │ │Detection │ │
│ └─────────────┘ │  │ │  Storage    │ │  │ └──────────┘ │
│                 │  │ └─────────────┘ │  │ ┌──────────┐ │
│                 │  │                 │  │ │ TFLite   │ │
│                 │  │                 │  │ │ Models   │ │
│                 │  │                 │  │ └──────────┘ │
└─────────────────┘  └─────────────────┘  └──────────────┘
```

## Component Responsibilities

### UI Layer (Jetpack Compose)
- **Responsibility**: User interface and user interactions
- **Technologies**: Jetpack Compose, Material Design 3
- **Key Files**:
  - `MainActivity.kt` - Main entry point
  - `ui/theme/Theme.kt` - App theming

### ViewModel Layer
- **Responsibility**: UI state management and business logic coordination
- **Technologies**: Android ViewModel, Kotlin Coroutines, RxJava
- **Pattern**: MVVM (Model-View-ViewModel)

### Repository Layer
- **Responsibility**: Data source abstraction and coordination
- **Technologies**: Kotlin Coroutines, RxJava
- **Key Files**:
  - `data/repository/ScanRepository.kt`

### Data Layer

#### Room Database (Local Storage)
- **Responsibility**: Offline data persistence
- **Technologies**: Room, SQLite
- **Key Components**:
  - Entities: `ScanEntity`, `NoteEntity`
  - DAOs: `ScanDao`, `NoteDao`
  - Database: `AppDatabase`

#### Firebase (Cloud Storage)
- **Responsibility**: Cloud sync and serverless functions
- **Technologies**: Firestore, Cloud Functions, Storage
- **Key Components**:
  - `FirestoreSync` - Data synchronization
  - `CloudFunctionsClient` - Serverless function calls
  - `StorageManager` - File uploads

#### ML Services
- **Responsibility**: On-device machine learning
- **Technologies**: ML Kit, TensorFlow Lite
- **Key Components**:
  - `MLKitDetector` - Object detection and image labeling
  - `TFLiteModelRunner` - Custom model inference

#### ARCore
- **Responsibility**: 3D scanning and spatial understanding
- **Technologies**: ARCore SDK
- **Key Components**:
  - `ARCoreSessionManager` - Session lifecycle
  - `ScanDataProcessor` - Data extraction

## Data Flow

### Scan Creation Flow

```
User → UI → ViewModel → ARCore → Process Data → Repository → Room DB
                                                           ↓
                                              (Background Sync)
                                                           ↓
                                                      Firebase
```

1. User initiates scan in UI
2. ViewModel coordinates ARCore session
3. ARCore captures 3D data and planes
4. Data is processed (dimensions, point cloud)
5. Repository saves to Room DB (offline-first)
6. Background sync uploads to Firebase

### Note Addition Flow

```
User → UI → ViewModel → Repository → Room DB
                                   ↓
                       (Background Sync)
                                   ↓
                              Firebase
```

1. User adds note to a scan
2. ViewModel creates NoteEntity
3. Repository saves to Room DB
4. Background sync uploads to Firebase

### ML Analysis Flow

```
ARCore Frame → ML Kit/TFLite → Results → Repository → Room DB
```

1. Capture frame from ARCore
2. Run ML inference (object detection, damage detection)
3. Store results with scan data

## Offline-First Architecture

The app is designed to work offline-first:

1. **All data is stored locally first** in Room Database
2. **Background sync** uploads to Firebase when connection available
3. **Sync status** tracked with `isSynced` flag
4. **Conflict resolution** handled by timestamp comparison

```kotlin
// Example sync logic
suspend fun syncScan(scan: ScanEntity) {
    if (!scan.isSynced) {
        val firebaseId = firestoreSync.uploadScan(scan)
        if (firebaseId != null) {
            repository.markScanAsSynced(scan.id, firebaseId)
        }
    }
}
```

## Reactive Programming with RxJava

The app supports both Coroutines and RxJava for reactive programming:

### Coroutines (Flow)
```kotlin
val scans: Flow<List<ScanEntity>> = repository.getAllScans()

scans.collect { scanList ->
    // Update UI
}
```

### RxJava (Flowable)
```kotlin
repository.getAllScansRx()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe { scanList ->
        // Update UI
    }
```

## Dependency Injection (Recommended)

While not implemented in the sample code, it's recommended to use Hilt/Dagger for DI:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideScanDao(database: AppDatabase): ScanDao {
        return database.scanDao()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    
    @Provides
    fun provideScanRepository(
        scanDao: ScanDao,
        noteDao: NoteDao
    ): ScanRepository {
        return ScanRepository(scanDao, noteDao)
    }
}
```

## Testing Strategy

### Unit Tests
- Repository logic
- ViewModel business logic
- Data transformations

### Integration Tests
- Room database operations
- Firebase sync operations

### UI Tests
- Compose UI testing
- User flow testing

### Example Test Structure

```
test/
├── data/
│   ├── repository/
│   │   └── ScanRepositoryTest.kt
│   └── dao/
│       ├── ScanDaoTest.kt
│       └── NoteDaoTest.kt
├── ml/
│   ├── MLKitDetectorTest.kt
│   └── TFLiteModelRunnerTest.kt
└── arcore/
    └── ScanDataProcessorTest.kt

androidTest/
└── ui/
    ├── ScanScreenTest.kt
    └── NoteScreenTest.kt
```

## Security Considerations

1. **Data Encryption**
   - Use SQLCipher for Room Database encryption
   - Enable encryption at rest for Firebase

2. **Authentication**
   - Implement Firebase Authentication
   - Secure API calls with auth tokens

3. **Input Validation**
   - Validate all user inputs
   - Sanitize data before storage

4. **Network Security**
   - Use HTTPS for all network calls
   - Implement certificate pinning

## Performance Optimization

1. **Database**
   - Use indices on frequently queried columns
   - Implement pagination for large datasets
   - Use transactions for bulk operations

2. **ML Models**
   - Use quantized models for smaller size
   - Enable GPU/NNAPI acceleration
   - Cache inference results

3. **ARCore**
   - Disable unused features (depth, light estimation)
   - Limit plane detection updates
   - Optimize rendering pipeline

4. **Memory Management**
   - Release ARCore resources properly
   - Close ML model interpreters
   - Use proper lifecycle awareness

## Scalability

The architecture supports scaling in multiple dimensions:

1. **Feature Scaling**: Add new ML models, ARCore features easily
2. **Data Scaling**: Room handles large datasets efficiently
3. **User Scaling**: Firebase scales automatically
4. **Team Scaling**: Clear separation of concerns

## Future Enhancements

Potential improvements to the architecture:

1. **Modularization**: Split into feature modules
2. **Work Manager**: Better background sync
3. **DataStore**: Replace SharedPreferences
4. **Paging 3**: For large scan lists
5. **GraphQL**: Alternative to REST for Firebase

## Resources

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Firebase](https://firebase.google.com/docs/android/setup)
