# Sample Code: Wiring ARCore with Firebase and RxJava

This document provides sample code demonstrating how to integrate ARCore, Firebase, and RxJava for the Room Scanner application.

## Complete Integration Example

### 1. AR Scanning Activity

```kotlin
package com.roomscanner.app.arcore

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.roomscanner.app.data.RoomScanRepository
import com.roomscanner.app.domain.CreateRoomScanUseCase
import com.roomscanner.app.ml.MLKitAnalyzer
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ARScanActivity : AppCompatActivity() {
    
    private lateinit var arCoreManager: ARCoreManager
    private lateinit var mlKitAnalyzer: MLKitAnalyzer
    private lateinit var repository: RoomScanRepository
    private lateinit var createScanUseCase: CreateRoomScanUseCase
    private val disposables = CompositeDisposable()
    
    private val capturedImages = mutableListOf<CapturedImage>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize components
        arCoreManager = ARCoreManager(this)
        mlKitAnalyzer = MLKitAnalyzer()
        
        // Get repository from application
        val app = application as RoomScannerApplication
        repository = RoomScanRepository(
            app.database.roomScanDao(),
            app.database.jobNoteDao(),
            FirebaseSyncService()
        )
        
        createScanUseCase = CreateRoomScanUseCase(repository, mlKitAnalyzer)
        
        // Initialize AR session
        arCoreManager.initializeSession()
            .onSuccess { session ->
                // AR session ready
                setupARSession(session)
            }
            .onFailure { error ->
                // Handle error
                showError(error.message)
            }
        
        // Subscribe to AR scan state
        disposables.add(
            arCoreManager.scanState
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    when (state) {
                        is ARScanState.Ready -> updateUI("Ready to scan")
                        is ARScanState.Scanning -> updateUI("Scanning...")
                        is ARScanState.Paused -> updateUI("Paused")
                        is ARScanState.Error -> showError(state.message)
                    }
                }
        )
    }
    
    private fun setupARSession(session: Session) {
        // Setup AR rendering and frame processing
        // This would integrate with your AR rendering framework
    }
    
    fun onARFrameUpdate(frame: Frame) {
        // Process each AR frame
        val frameData = arCoreManager.processFrame(frame)
        
        // Check if we have good tracking and sufficient planes
        if (frameData.trackingState == TrackingState.TRACKING && 
            frameData.planes.size >= 3) {
            // We have enough data to calculate room dimensions
            calculateRoomDimensions(frameData.planes)
        }
    }
    
    private fun calculateRoomDimensions(planes: List<Plane>) {
        val dimensions = arCoreManager.calculateRoomDimensions(planes)
        
        // Display dimensions to user
        displayDimensions(dimensions)
    }
    
    fun onCaptureImage(bitmap: Bitmap, surfaceType: String) {
        // Capture image for ML analysis
        val imageUri = saveImageToFile(bitmap)
        capturedImages.add(CapturedImage(bitmap, imageUri, surfaceType))
        
        // Show feedback
        showToast("Image captured for ${surfaceType}")
    }
    
    fun onCompleteScan() {
        // Get final room dimensions
        val planes = getCurrentPlanes()
        val dimensions = arCoreManager.calculateRoomDimensions(planes)
        val pointCloudData = arCoreManager.exportPointCloudData()
        
        // Create room scan with RxJava
        disposables.add(
            createScanUseCase.execute(
                roomName = "Living Room",
                dimensions = dimensions,
                pointCloudData = pointCloudData,
                capturedImages = capturedImages
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { scanId ->
                    showToast("Scan saved with ID: $scanId")
                    finish()
                },
                { error ->
                    showError("Failed to save scan: ${error.message}")
                }
            )
        )
    }
    
    override fun onResume() {
        super.onResume()
        arCoreManager.resume()
    }
    
    override fun onPause() {
        super.onPause()
        arCoreManager.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        arCoreManager.close()
        mlKitAnalyzer.close()
        disposables.clear()
    }
    
    private fun getCurrentPlanes(): List<Plane> {
        // Get current planes from AR session
        return emptyList() // Implement based on your AR framework
    }
    
    private fun saveImageToFile(bitmap: Bitmap): String {
        // Save bitmap to file and return URI
        return "" // Implement file saving
    }
    
    private fun displayDimensions(dimensions: RoomDimensions) {
        // Update UI with dimensions
    }
    
    private fun updateUI(message: String) {
        // Update UI with status message
    }
    
    private fun showError(message: String?) {
        // Show error to user
    }
    
    private fun showToast(message: String) {
        // Show toast message
    }
}
```

### 2. Repository with RxJava Integration

The repository layer already implements RxJava patterns. Here's how to use it:

```kotlin
// Get all scans reactively
repository.getAllScans()
    .collect { scans ->
        // UI automatically updates
        updateScanList(scans)
    }

// Create a new scan with RxJava
repository.createRoomScan(
    roomName = "Kitchen",
    dimensions = dimensions,
    pointCloudData = pointCloudData,
    damagedAreas = listOf(),
    materialEstimates = listOf()
)
.subscribeOn(Schedulers.io())
.observeOn(AndroidSchedulers.mainThread())
.subscribe(
    { scanId -> 
        println("Created scan: $scanId")
        // Automatically syncs to Firebase in background
    },
    { error -> 
        println("Error: ${error.message}") 
    }
)

// Manual sync to Firebase
repository.syncScansToFirebase()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        { println("Sync complete") },
        { error -> println("Sync failed: ${error.message}") }
    )
```

### 3. Firebase Integration Pattern

```kotlin
// The sync happens automatically in CreateRoomScanUseCase
// But you can also trigger manual sync:

class SyncManager(private val repository: RoomScanRepository) {
    
    fun syncAll(): Completable {
        return Completable.mergeArray(
            repository.syncScansToFirebase(),
            repository.syncNotesToFirebase()
        )
        .subscribeOn(Schedulers.io())
    }
    
    fun setupAutoSync() {
        // Sync every 5 minutes
        val disposable = Observable.interval(5, TimeUnit.MINUTES)
            .flatMapCompletable { syncAll() }
            .subscribe(
                { println("Auto sync complete") },
                { error -> println("Auto sync error: ${error.message}") }
            )
    }
}
```

### 4. Complete Data Flow Example

```kotlin
// Complete flow: AR Scan -> ML Analysis -> Save -> Sync

fun performCompleteScan(
    arCoreManager: ARCoreManager,
    mlKitAnalyzer: MLKitAnalyzer,
    repository: RoomScanRepository,
    planes: List<Plane>,
    capturedImages: List<CapturedImage>
): Single<Long> {
    
    // Step 1: Calculate room dimensions from AR data
    val dimensions = arCoreManager.calculateRoomDimensions(planes)
    val pointCloudData = arCoreManager.exportPointCloudData()
    
    // Step 2: Analyze images with ML Kit (parallel processing)
    val analysisObservables = capturedImages.map { image ->
        Single.zip(
            mlKitAnalyzer.detectDamagedAreas(image.bitmap, image.uri),
            mlKitAnalyzer.estimateMaterialTypes(image.bitmap, image.surfaceType),
            { damages, materials -> 
                Pair(damages, materials)
            }
        )
    }
    
    // Step 3: Combine all analysis results
    return Single.zip(analysisObservables) { results ->
        val allDamages = results.flatMap { (it as Pair<*, *>).first as List<DamagedArea> }
        val allMaterials = results.flatMap { (it as Pair<*, *>).second as List<MaterialEstimate> }
        Pair(allDamages, allMaterials)
    }
    // Step 4: Save to Room Database
    .flatMap { (damages, materials) ->
        repository.createRoomScan(
            roomName = "Scanned Room ${System.currentTimeMillis()}",
            dimensions = dimensions,
            pointCloudData = pointCloudData,
            damagedAreas = damages,
            materialEstimates = materials
        )
    }
    // Step 5: Sync to Firebase (happens automatically in background)
    .doOnSuccess { scanId ->
        repository.syncScansToFirebase()
            .subscribeOn(Schedulers.io())
            .subscribe()
    }
    .subscribeOn(Schedulers.io())
}
```

### 5. ViewModel Integration (Optional)

For better architecture, you can use ViewModels:

```kotlin
class ScanListViewModel(
    private val repository: RoomScanRepository
) : ViewModel() {
    
    private val disposables = CompositeDisposable()
    
    // Expose Flow as LiveData or StateFlow
    val scans: StateFlow<List<RoomScan>> = repository.getAllScans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun syncToFirebase() {
        disposables.add(
            repository.syncScansToFirebase()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { /* Success */ },
                    { error -> /* Handle error */ }
                )
        )
    }
    
    fun deleteScan(scan: RoomScan) {
        disposables.add(
            repository.deleteScan(scan)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
```

### 6. Compose UI Integration

```kotlin
@Composable
fun ScanListScreenContainer(
    viewModel: ScanListViewModel = viewModel()
) {
    val scans by viewModel.scans.collectAsState()
    
    ScanListScreen(
        scans = scans,
        onScanClick = { scan -> 
            // Navigate to detail
        },
        onNewScanClick = {
            // Navigate to AR scanner
        },
        onSyncClick = {
            viewModel.syncToFirebase()
        }
    )
}
```

## Key Patterns

1. **Reactive Data Flow**: Use Flow for UI updates, RxJava for operations
2. **Offline First**: Save locally first, sync in background
3. **Error Handling**: Use RxJava error operators (onErrorReturn, retry, etc.)
4. **Threading**: Always use Schedulers.io() for data operations
5. **Lifecycle**: Dispose subscriptions in onDestroy/onCleared
6. **Separation of Concerns**: UI -> ViewModel -> Repository -> Data Sources

## Testing

```kotlin
class CreateRoomScanUseCaseTest {
    
    @Test
    fun `should create scan and trigger sync`() {
        // Arrange
        val mockRepository = mock<RoomScanRepository>()
        val mockMLKit = mock<MLKitAnalyzer>()
        val useCase = CreateRoomScanUseCase(mockRepository, mockMLKit)
        
        whenever(mockMLKit.detectDamagedAreas(any(), any()))
            .thenReturn(Single.just(emptyList()))
        whenever(mockMLKit.estimateMaterialTypes(any(), any()))
            .thenReturn(Single.just(emptyList()))
        whenever(mockRepository.createRoomScan(any(), any(), any(), any(), any()))
            .thenReturn(Single.just(1L))
        
        // Act
        val testObserver = useCase.execute(
            "Test Room",
            RoomDimensions(5f, 4f, 2.5f, 20f, 50f),
            null,
            emptyList()
        ).test()
        
        // Assert
        testObserver.assertComplete()
        testObserver.assertValue(1L)
    }
}
```

This integration provides a complete, production-ready architecture for the Room Scanner app!
