# ARCore Integration Guide

## Overview
This guide covers integrating Google ARCore for 3D room scanning in the Room Scanner app.

## Prerequisites

- Android device with ARCore support (check [supported devices](https://developers.google.com/ar/devices))
- Android 7.0 (API level 24) or higher
- Camera permissions
- ARCore SDK (already included in dependencies)

## Architecture

```
┌─────────────────────┐
│   MainActivity      │
│   (UI Layer)        │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ ARCoreSessionManager│
│  (Session Mgmt)     │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ ScanDataProcessor   │
│ (Data Extraction)   │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│   Room Database     │
│  (Offline Storage)  │
└─────────────────────┘
```

## Setup

### 1. Verify Manifest Configuration

The `AndroidManifest.xml` already includes necessary permissions and features:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera.ar" android:required="true" />
<uses-feature android:glEsVersion="0x00030000" android:required="true" />

<meta-data
    android:name="com.google.ar.core"
    android:value="required" />
```

### 2. Check ARCore Availability

```kotlin
import com.google.ar.core.ArCoreApk

class ScanActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: ARCoreSessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = ARCoreSessionManager(this)
        
        if (!sessionManager.checkARCoreAvailability()) {
            // ARCore not available
            showARCoreNotAvailableDialog()
            return
        }
        
        requestCameraPermission()
    }
    
    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            setupARSession()
        }
    }
}
```

## Creating AR Session

### Basic Setup

```kotlin
class ARScanFragment : Fragment() {
    
    private lateinit var sessionManager: ARCoreSessionManager
    private lateinit var arSurfaceView: GLSurfaceView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_ar_scan, container, false)
        
        arSurfaceView = view.findViewById(R.id.ar_surface_view)
        arSurfaceView.preserveEGLContextOnPause = true
        arSurfaceView.setEGLContextClientVersion(3)
        arSurfaceView.setRenderer(ARRenderer())
        
        sessionManager = ARCoreSessionManager(requireContext())
        
        return view
    }
    
    override fun onResume() {
        super.onResume()
        
        val session = sessionManager.createSession()
        if (session == null) {
            // Failed to create session
            return
        }
        
        sessionManager.resume()
        arSurfaceView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        sessionManager.pause()
        arSurfaceView.onPause()
    }
}
```

## Scanning and Data Extraction

### Detect Planes and Extract Dimensions

```kotlin
class ARScanner(
    private val sessionManager: ARCoreSessionManager,
    private val dataProcessor: ScanDataProcessor
) {
    
    fun updateFrame() {
        val session = sessionManager.getSession() ?: return
        
        try {
            val frame = session.update()
            
            // Check tracking state
            if (frame.camera.trackingState != TrackingState.TRACKING) {
                return
            }
            
            // Extract room dimensions
            val dimensions = dataProcessor.extractRoomDimensions(frame)
            dimensions?.let {
                // Update UI with dimensions
                onDimensionsDetected(it)
            }
            
            // Get detected planes
            val planes = frame.getUpdatedTrackables(Plane::class.java)
                .filter { it.trackingState == TrackingState.TRACKING }
            
            // Visualize planes
            renderPlanes(planes)
            
        } catch (e: Exception) {
            Log.e("ARScanner", "Error updating frame", e)
        }
    }
    
    private fun onDimensionsDetected(dimensions: ScanDataProcessor.RoomDimensions) {
        Log.d("ARScanner", "Room: ${dimensions.length}m x ${dimensions.width}m x ${dimensions.height}m")
        
        // Calculate area
        val area = dataProcessor.calculateTotalArea(dimensions)
        Log.d("ARScanner", "Total area: ${area}m²")
    }
}
```

### Save Point Cloud Data

```kotlin
fun capturePointCloud(frame: Frame): File? {
    val pointCloud = frame.acquirePointCloud()
    
    try {
        val outputDir = context.getExternalFilesDir(null)
        val outputFile = File(outputDir, "scan_${System.currentTimeMillis()}.bin")
        
        val success = dataProcessor.savePointCloud(pointCloud, outputFile)
        
        return if (success) outputFile else null
    } finally {
        pointCloud.release()
    }
}
```

### Complete Scan Workflow

```kotlin
class ScanViewModel(
    private val repository: ScanRepository,
    private val sessionManager: ARCoreSessionManager,
    private val dataProcessor: ScanDataProcessor
) : ViewModel() {
    
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState
    
    fun startScan() {
        _scanState.value = ScanState.Scanning
    }
    
    fun captureScan(roomName: String) {
        viewModelScope.launch {
            _scanState.value = ScanState.Processing
            
            try {
                val session = sessionManager.getSession() ?: return@launch
                val frame = session.update()
                
                // Extract dimensions
                val dimensions = dataProcessor.extractRoomDimensions(frame)
                    ?: throw Exception("Failed to extract dimensions")
                
                // Save point cloud
                val pointCloud = frame.acquirePointCloud()
                val outputFile = File(
                    context.getExternalFilesDir(null),
                    "scan_${System.currentTimeMillis()}.bin"
                )
                dataProcessor.savePointCloud(pointCloud, outputFile)
                pointCloud.release()
                
                // Save to database
                val scan = ScanEntity(
                    roomName = roomName,
                    length = dimensions.length,
                    width = dimensions.width,
                    height = dimensions.height,
                    pointCloudPath = outputFile.absolutePath
                )
                
                val scanId = repository.insertScan(scan)
                
                _scanState.value = ScanState.Completed(scanId)
                
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    sealed class ScanState {
        object Idle : ScanState()
        object Scanning : ScanState()
        object Processing : ScanState()
        data class Completed(val scanId: Long) : ScanState()
        data class Error(val message: String) : ScanState()
    }
}
```

## Advanced Features

### Anchors for Annotations

```kotlin
fun placeAnnotation(hitResult: HitResult, note: String): Anchor? {
    val session = sessionManager.getSession() ?: return null
    
    return try {
        val anchor = hitResult.createAnchor()
        
        // Save anchor information
        val pose = anchor.pose
        val noteEntity = NoteEntity(
            scanId = currentScanId,
            title = "Annotation",
            content = note,
            positionX = pose.tx(),
            positionY = pose.ty(),
            positionZ = pose.tz()
        )
        
        // Save to database
        viewModelScope.launch {
            repository.insertNote(noteEntity)
        }
        
        anchor
    } catch (e: Exception) {
        null
    }
}
```

### Depth API

```kotlin
fun processDepthData(frame: Frame) {
    try {
        val depthImage = frame.acquireDepthImage16Bits()
        
        val width = depthImage.width
        val height = depthImage.height
        
        // Process depth data
        val buffer = depthImage.planes[0].buffer
        
        // Example: Find closest point
        var minDepth = Float.MAX_VALUE
        buffer.rewind()
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val depth = buffer.short.toFloat() / 1000f // Convert to meters
                if (depth > 0 && depth < minDepth) {
                    minDepth = depth
                }
            }
        }
        
        Log.d("Depth", "Closest point: ${minDepth}m")
        
        depthImage.close()
    } catch (e: Exception) {
        // Depth not available
    }
}
```

### Light Estimation

```kotlin
fun getLightEstimate(frame: Frame): Float? {
    return try {
        val lightEstimate = frame.lightEstimate
        if (lightEstimate.state == LightEstimate.State.VALID) {
            lightEstimate.pixelIntensity
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
```

## Rendering

### Simple Plane Renderer

```kotlin
class ARRenderer(
    private val sessionManager: ARCoreSessionManager
) : GLSurfaceView.Renderer {
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        
        // Initialize shaders, textures, etc.
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }
    
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        
        val session = sessionManager.getSession() ?: return
        
        try {
            val frame = session.update()
            
            // Draw camera background
            drawCameraBackground(frame)
            
            // Draw detected planes
            val planes = frame.getUpdatedTrackables(Plane::class.java)
            for (plane in planes) {
                if (plane.trackingState == TrackingState.TRACKING) {
                    drawPlane(plane)
                }
            }
            
        } catch (e: Exception) {
            Log.e("ARRenderer", "Error rendering frame", e)
        }
    }
    
    private fun drawCameraBackground(frame: Frame) {
        // Implementation depends on your shader setup
    }
    
    private fun drawPlane(plane: Plane) {
        // Implementation depends on your shader setup
    }
}
```

## Best Practices

### 1. Session Lifecycle Management

```kotlin
class ARActivity : AppCompatActivity() {
    
    override fun onResume() {
        super.onResume()
        sessionManager.resume()
    }
    
    override fun onPause() {
        super.onPause()
        sessionManager.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        sessionManager.close()
    }
}
```

### 2. Handle ARCore Updates

```kotlin
private fun checkARCoreVersion() {
    when (ArCoreApk.getInstance().requestInstall(this, true)) {
        ArCoreApk.InstallStatus.INSTALLED -> {
            // ARCore is installed and up to date
            setupARSession()
        }
        ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
            // ARCore install/update requested
            // Will be handled in onResume
        }
    }
}
```

### 3. Performance Optimization

```kotlin
// Configure session for better performance
val config = Config(session)
config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
config.lightEstimationMode = Config.LightEstimationMode.AMBIENT_INTENSITY
// Only enable depth if needed
config.depthMode = if (needsDepth) {
    Config.DepthMode.AUTOMATIC
} else {
    Config.DepthMode.DISABLED
}
session.configure(config)
```

### 4. User Guidance

```kotlin
fun provideScanningGuidance(frame: Frame): String {
    val planes = frame.getUpdatedTrackables(Plane::class.java)
        .filter { it.trackingState == TrackingState.TRACKING }
    
    return when {
        planes.isEmpty() -> "Move your device to detect surfaces"
        planes.size < 3 -> "Continue scanning to detect more surfaces"
        else -> "Tap to place points or capture scan"
    }
}
```

## Testing

### Unit Tests

```kotlin
@Test
fun testRoomDimensionExtraction() {
    val dataProcessor = ScanDataProcessor()
    
    // Create mock frame with test planes
    val mockFrame = createMockFrame()
    
    val dimensions = dataProcessor.extractRoomDimensions(mockFrame)
    
    assertNotNull(dimensions)
    assertTrue(dimensions!!.length > 0)
    assertTrue(dimensions.width > 0)
    assertTrue(dimensions.height > 0)
}
```

## Troubleshooting

### Common Issues

1. **"ARCore not available"**
   - Check device compatibility
   - Ensure ARCore is installed and up to date
   - Verify manifest configuration

2. **"Camera permission denied"**
   - Request CAMERA permission at runtime
   - Check permission in manifest

3. **"Tracking lost"**
   - Ensure good lighting
   - Move device slowly
   - Avoid featureless surfaces

4. **"No planes detected"**
   - Move device to help tracking
   - Ensure sufficient lighting
   - Point at textured surfaces

## Resources

- [ARCore Developer Guide](https://developers.google.com/ar/develop)
- [Supported Devices](https://developers.google.com/ar/devices)
- [ARCore Samples](https://github.com/google-ar/arcore-android-sdk)

## See Also

- [ML Kit Integration](ML_INTEGRATION.md)
- [Room Database](ROOM_DATABASE.md)
- [Firebase Sync](FIREBASE_SETUP.md)
