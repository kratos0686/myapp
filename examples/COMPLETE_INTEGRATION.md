# Complete Integration Example

This example shows how to wire all components together in a real scanning workflow.

## Complete Scan Workflow

```kotlin
package com.roomscanner.app.example

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomscanner.app.arcore.ARCoreSessionManager
import com.roomscanner.app.arcore.ScanDataProcessor
import com.roomscanner.app.data.entity.NoteEntity
import com.roomscanner.app.data.entity.ScanEntity
import com.roomscanner.app.data.repository.ScanRepository
import com.roomscanner.app.firebase.CloudFunctionsClient
import com.roomscanner.app.firebase.FirestoreSync
import com.roomscanner.app.firebase.StorageManager
import com.roomscanner.app.ml.MLKitDetector
import com.roomscanner.app.ml.TFLiteModelRunner
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

/**
 * Complete example showing how to integrate all components
 * for a full room scanning workflow.
 */
class CompleteScanViewModel(
    private val context: Context,
    private val repository: ScanRepository,
    private val sessionManager: ARCoreSessionManager,
    private val dataProcessor: ScanDataProcessor,
    private val mlKit: MLKitDetector,
    private val tflite: TFLiteModelRunner,
    private val firestoreSync: FirestoreSync,
    private val storageManager: StorageManager,
    private val cloudFunctions: CloudFunctionsClient
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState

    sealed class ScanState {
        object Idle : ScanState()
        object Scanning : ScanState()
        data class Processing(val progress: Int) : ScanState()
        data class Analyzing(val stage: String) : ScanState()
        data class Completed(val scanId: Long) : ScanState()
        data class Error(val message: String) : ScanState()
    }

    /**
     * Step 1: Initialize AR session and start scanning
     */
    fun startScanning() {
        viewModelScope.launch {
            try {
                _scanState.value = ScanState.Scanning
                
                // Create AR session
                val session = sessionManager.createSession()
                if (session == null) {
                    _scanState.value = ScanState.Error("Failed to create AR session")
                    return@launch
                }
                
                // Session ready - user can now scan the room
                // UI should display AR view and guidance
                
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Step 2: Capture and process scan data
     */
    fun captureScan(roomName: String) {
        viewModelScope.launch {
            try {
                _scanState.value = ScanState.Processing(10)
                
                val session = sessionManager.getSession() 
                    ?: throw Exception("AR session not available")
                
                // Get current frame
                val frame = session.update()
                
                // Extract room dimensions
                _scanState.value = ScanState.Processing(20)
                val dimensions = dataProcessor.extractRoomDimensions(frame)
                    ?: throw Exception("Failed to extract room dimensions")
                
                // Save point cloud data
                _scanState.value = ScanState.Processing(30)
                val pointCloud = frame.acquirePointCloud()
                val pointCloudFile = File(
                    context.getExternalFilesDir(null),
                    "scan_${System.currentTimeMillis()}.bin"
                )
                dataProcessor.savePointCloud(pointCloud, pointCloudFile)
                pointCloud.release()
                
                // Extract mesh data
                _scanState.value = ScanState.Processing(40)
                val meshData = dataProcessor.extractMeshData(frame)
                
                // Create scan entity
                val scan = ScanEntity(
                    roomName = roomName,
                    length = dimensions.length,
                    width = dimensions.width,
                    height = dimensions.height,
                    pointCloudPath = pointCloudFile.absolutePath
                )
                
                // Save to database
                _scanState.value = ScanState.Processing(50)
                val scanId = repository.insertScan(scan)
                
                // Analyze with ML (async)
                _scanState.value = ScanState.Analyzing("Running ML analysis...")
                analyzeAndSave(scanId, frame)
                
                // Upload to cloud (async)
                _scanState.value = ScanState.Analyzing("Uploading to cloud...")
                uploadScanData(scanId, scan, pointCloudFile)
                
                _scanState.value = ScanState.Completed(scanId)
                
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Failed to capture scan")
            }
        }
    }

    /**
     * Step 3: Run ML analysis on captured frames
     */
    private suspend fun analyzeAndSave(scanId: Long, frame: Frame) {
        try {
            // Get camera image
            val cameraImage = frame.acquireCameraImage()
            // Convert to bitmap (simplified - actual implementation would be more complex)
            val bitmap = convertImageToBitmap(cameraImage)
            cameraImage.close()
            
            // Run ML Kit detection
            val objects = mlKit.detectObjects(bitmap)
            val labels = mlKit.labelImage(bitmap)
            
            // Run custom TFLite models
            val damage = tflite.detectDamage(bitmap)
            val material = tflite.estimateMaterial(bitmap)
            
            // Store results as JSON
            val objectsJson = serializeToJson(objects)
            val damageJson = serializeToJson(damage)
            
            // Update scan with ML results
            val scan = repository.getScanById(scanId)
            if (scan != null) {
                repository.updateScan(
                    scan.copy(
                        detectedObjects = objectsJson,
                        damageAreas = damageJson
                    )
                )
            }
            
            // Create notes for significant findings
            damage.forEach { detection ->
                if (detection.confidence > 0.7f) {
                    val note = NoteEntity(
                        scanId = scanId,
                        title = "Damage Detected: ${detection.type}",
                        content = "Confidence: ${(detection.confidence * 100).toInt()}%",
                        category = "damage"
                    )
                    repository.insertNote(note)
                }
            }
            
        } catch (e: Exception) {
            // Log error but don't fail the scan
            println("ML analysis error: ${e.message}")
        }
    }

    /**
     * Step 4: Upload data to Firebase
     */
    private suspend fun uploadScanData(scanId: Long, scan: ScanEntity, pointCloudFile: File) {
        try {
            // Upload point cloud to Storage
            val pointCloudUrl = storageManager.uploadPointCloud(
                scanId.toString(),
                pointCloudFile
            )
            
            // Upload scan metadata to Firestore
            val scanWithUrl = scan.copy(pointCloudPath = pointCloudUrl)
            val firebaseId = firestoreSync.uploadScan(scanWithUrl)
            
            // Mark as synced in local database
            if (firebaseId != null) {
                repository.markScanAsSynced(scanId, firebaseId)
            }
            
            // Trigger cloud processing
            firebaseId?.let {
                cloudFunctions.processScanData(it)
            }
            
        } catch (e: Exception) {
            // Sync failed - will retry later
            println("Upload error: ${e.message}")
        }
    }

    /**
     * Step 5: Add notes to a scan
     */
    fun addNote(scanId: Long, title: String, content: String, category: String) {
        viewModelScope.launch {
            try {
                val note = NoteEntity(
                    scanId = scanId,
                    title = title,
                    content = content,
                    category = category
                )
                
                val noteId = repository.insertNote(note)
                
                // Upload to Firebase
                val scan = repository.getScanById(scanId)
                if (scan?.firebaseId != null) {
                    firestoreSync.uploadNote(note, scan.firebaseId)
                }
                
            } catch (e: Exception) {
                println("Failed to add note: ${e.message}")
            }
        }
    }

    /**
     * Step 6: Generate report
     */
    fun generateReport(scanId: Long) {
        viewModelScope.launch {
            try {
                val scan = repository.getScanById(scanId)
                if (scan?.firebaseId == null) {
                    throw Exception("Scan not synced to cloud")
                }
                
                // Generate PDF report via Cloud Function
                val reportUrl = cloudFunctions.generatePDFReport(scan.firebaseId)
                
                if (reportUrl != null) {
                    // Open or download report
                    println("Report available at: $reportUrl")
                }
                
            } catch (e: Exception) {
                println("Failed to generate report: ${e.message}")
            }
        }
    }

    /**
     * Step 7: Get cost estimate
     */
    fun estimateCosts(scanId: Long) {
        viewModelScope.launch {
            try {
                val scan = repository.getScanById(scanId)
                if (scan?.firebaseId == null) {
                    throw Exception("Scan not synced to cloud")
                }
                
                val costData = cloudFunctions.estimateCosts(scan.firebaseId)
                
                costData?.let { costs ->
                    // Display cost estimate to user
                    val totalCost = costs["totalCost"] as? Double
                    println("Estimated cost: $${totalCost}")
                }
                
            } catch (e: Exception) {
                println("Failed to estimate costs: ${e.message}")
            }
        }
    }

    /**
     * Observe all scans (reactive with Flow)
     */
    val allScans: Flow<List<ScanEntity>> = repository.getAllScans()

    /**
     * Observe notes for a scan
     */
    fun getNotesForScan(scanId: Long): Flow<List<NoteEntity>> {
        return repository.getNotesForScan(scanId)
    }

    /**
     * Clean up resources
     */
    override fun onCleared() {
        super.onCleared()
        sessionManager.close()
        mlKit.close()
        tflite.close()
    }

    // Helper functions
    private fun convertImageToBitmap(image: android.media.Image): android.graphics.Bitmap {
        // Simplified - actual implementation would handle image format conversion
        TODO("Implement image to bitmap conversion")
    }

    private fun serializeToJson(data: Any): String {
        // Use kotlinx.serialization or Gson
        TODO("Implement JSON serialization")
    }
}

/**
 * Usage in Activity/Fragment with Jetpack Compose
 */
@Composable
fun ScanScreen(viewModel: CompleteScanViewModel) {
    val scanState by viewModel.scanState.collectAsState()
    val scans by viewModel.allScans.collectAsState(initial = emptyList())
    
    Column(modifier = Modifier.fillMaxSize()) {
        when (scanState) {
            is CompleteScanViewModel.ScanState.Idle -> {
                Button(onClick = { viewModel.startScanning() }) {
                    Text("Start Scanning")
                }
            }
            is CompleteScanViewModel.ScanState.Scanning -> {
                // Show AR view
                Text("Scanning room...")
                Button(onClick = { viewModel.captureScan("My Room") }) {
                    Text("Capture Scan")
                }
            }
            is CompleteScanViewModel.ScanState.Processing -> {
                val progress = (scanState as CompleteScanViewModel.ScanState.Processing).progress
                CircularProgressIndicator(progress = progress / 100f)
                Text("Processing: $progress%")
            }
            is CompleteScanViewModel.ScanState.Analyzing -> {
                val stage = (scanState as CompleteScanViewModel.ScanState.Analyzing).stage
                CircularProgressIndicator()
                Text(stage)
            }
            is CompleteScanViewModel.ScanState.Completed -> {
                val scanId = (scanState as CompleteScanViewModel.ScanState.Completed).scanId
                Text("Scan completed! ID: $scanId")
                
                Button(onClick = { viewModel.generateReport(scanId) }) {
                    Text("Generate Report")
                }
                Button(onClick = { viewModel.estimateCosts(scanId) }) {
                    Text("Estimate Costs")
                }
            }
            is CompleteScanViewModel.ScanState.Error -> {
                val message = (scanState as CompleteScanViewModel.ScanState.Error).message
                Text("Error: $message", color = Color.Red)
            }
        }
        
        // Display all scans
        LazyColumn {
            items(scans) { scan ->
                ScanCard(
                    scan = scan,
                    onAddNote = { viewModel.addNote(scan.id, "Note", "Content", "general") },
                    onGenerateReport = { viewModel.generateReport(scan.id) }
                )
            }
        }
    }
}
```

## Key Integration Points

### 1. Dependency Injection Setup

```kotlin
// In your Application class or dependency module
val database = AppDatabase.getDatabase(context)
val repository = ScanRepository(database.scanDao(), database.noteDao())
val sessionManager = ARCoreSessionManager(context)
val dataProcessor = ScanDataProcessor()
val mlKit = MLKitDetector(context)
val tflite = TFLiteModelRunner(context)
val firestoreSync = FirestoreSync()
val storageManager = StorageManager()
val cloudFunctions = CloudFunctionsClient()

val viewModel = CompleteScanViewModel(
    context,
    repository,
    sessionManager,
    dataProcessor,
    mlKit,
    tflite,
    firestoreSync,
    storageManager,
    cloudFunctions
)
```

### 2. RxJava Alternative

```kotlin
// Using RxJava instead of Coroutines
fun observeScansRx(): Flowable<List<ScanEntity>> {
    return repository.getAllScansRx()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

fun captureScanRx(roomName: String): Single<Long> {
    return Single.fromCallable {
        // Capture logic
        scanId
    }
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
}
```

### 3. Background Sync Worker

```kotlin
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val repository = // Get repository
        val firestoreSync = FirestoreSync()
        
        return try {
            // Get unsynced scans
            val unsynced = repository.getUnsyncedScansRx().blockingGet()
            
            // Upload each
            unsynced.forEach { scan ->
                val firebaseId = firestoreSync.uploadScan(scan)
                if (firebaseId != null) {
                    repository.markScanAsSynced(scan.id, firebaseId)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

This example demonstrates a complete workflow integrating all components!
