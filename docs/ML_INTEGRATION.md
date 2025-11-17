# ML Kit Integration Guide

## Overview

This guide covers integrating Google ML Kit for on-device machine learning in the Room Scanner app.

## What is ML Kit?

ML Kit is a mobile SDK that brings Google's machine learning expertise to Android and iOS apps. It provides:

- **On-device ML**: No network required, fast inference
- **Pre-trained models**: Ready-to-use APIs for common tasks
- **Custom models**: Support for TensorFlow Lite models
- **Privacy-focused**: Data stays on device

## Available ML Kit APIs

### 1. Object Detection

Detect and track objects in images or camera feeds.

```kotlin
val mlKit = MLKitDetector(context)

lifecycleScope.launch {
    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.room_photo)
    val objects = mlKit.detectObjects(bitmap)
    
    objects.forEach { obj ->
        println("Found: ${obj.label} (${obj.confidence})")
        println("Location: ${obj.boundingBox}")
    }
}
```

**Use Cases in Room Scanner**:
- Identify furniture and fixtures
- Detect appliances and equipment
- Count objects in the room

### 2. Image Labeling

Identify objects, locations, activities, and more in images.

```kotlin
lifecycleScope.launch {
    val labels = mlKit.labelImage(bitmap)
    
    labels.forEach { label ->
        println("${label.text}: ${label.confidence}")
    }
}
```

**Use Cases**:
- Identify room type (kitchen, bathroom, bedroom)
- Detect materials (wood, concrete, tile)
- Identify surfaces and textures

### 3. Complete Analysis

Combine object detection and labeling:

```kotlin
lifecycleScope.launch {
    val result = mlKit.analyzeImage(bitmap)
    
    // Detected objects
    result.detectedObjects.forEach { obj ->
        println("Object: ${obj.label}")
    }
    
    // Image labels
    result.imageLabels.forEach { label ->
        println("Label: ${label.text}")
    }
}
```

## Integration with ARCore

Analyze frames from ARCore in real-time:

```kotlin
class ARScanner(
    private val mlKit: MLKitDetector
) {
    
    fun analyzeFrame(frame: Frame) {
        try {
            // Get camera image
            val image = frame.acquireCameraImage()
            
            // Convert to bitmap (simplified)
            val bitmap = convertToBitmap(image)
            
            // Analyze with ML Kit
            lifecycleScope.launch {
                val objects = mlKit.detectObjects(bitmap)
                onObjectsDetected(objects)
            }
            
            image.close()
        } catch (e: Exception) {
            Log.e("ARScanner", "Error analyzing frame", e)
        }
    }
    
    private fun onObjectsDetected(objects: List<MLKitDetector.DetectedObject>) {
        // Store detected objects with scan
        val objectsJson = Json.encodeToString(objects)
        currentScan.detectedObjects = objectsJson
    }
}
```

## Storing ML Results

Save analysis results with scans:

```kotlin
// In ScanEntity
val detectedObjects: String? = null // JSON string

// When saving scan
val objectsJson = Json.encodeToString(objects)
val scan = scanEntity.copy(detectedObjects = objectsJson)
repository.updateScan(scan)

// When reading scan
val objects = Json.decodeFromString<List<DetectedObject>>(scan.detectedObjects)
```

## Performance Optimization

### 1. Throttle Inference

Don't run inference on every frame:

```kotlin
private var lastInferenceTime = 0L
private val inferenceInterval = 1000L // 1 second

fun shouldRunInference(): Boolean {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastInferenceTime > inferenceInterval) {
        lastInferenceTime = currentTime
        return true
    }
    return false
}

// In AR loop
if (shouldRunInference()) {
    analyzeFrame(frame)
}
```

### 2. Use Appropriate Detector Modes

```kotlin
// For single images
val options = ObjectDetectorOptions.Builder()
    .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
    .build()

// For video/camera stream
val options = ObjectDetectorOptions.Builder()
    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
    .enableClassification()
    .build()
```

### 3. Adjust Confidence Threshold

```kotlin
val options = ImageLabelerOptions.Builder()
    .setConfidenceThreshold(0.7f) // Only high-confidence results
    .build()
```

## Advanced Features

### Multiple Object Detection

```kotlin
val options = ObjectDetectorOptions.Builder()
    .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
    .enableMultipleObjects() // Detect multiple objects
    .enableClassification()  // Classify detected objects
    .build()
```

### Custom Labels

While ML Kit provides generic labels, you can map them to room-specific categories:

```kotlin
fun categorizeLabel(label: String): String {
    return when {
        label in listOf("Furniture", "Chair", "Table", "Couch") -> "Furniture"
        label in listOf("Light", "Lamp", "Chandelier") -> "Lighting"
        label in listOf("Door", "Window") -> "Openings"
        label in listOf("Wall", "Floor", "Ceiling") -> "Structure"
        else -> "Other"
    }
}
```

## Combining with TensorFlow Lite

Use ML Kit for general detection and TFLite for specialized tasks:

```kotlin
class MLAnalyzer(
    context: Context,
    private val mlKit: MLKitDetector,
    private val tflite: TFLiteModelRunner
) {
    
    suspend fun analyzeRoom(bitmap: Bitmap): RoomAnalysis {
        // General object detection with ML Kit
        val objects = mlKit.detectObjects(bitmap)
        val labels = mlKit.labelImage(bitmap)
        
        // Specialized damage detection with custom TFLite model
        val damage = tflite.detectDamage(bitmap)
        
        // Material classification with custom TFLite model
        val material = tflite.estimateMaterial(bitmap)
        
        return RoomAnalysis(
            objects = objects,
            labels = labels,
            damage = damage,
            material = material
        )
    }
}
```

## Error Handling

```kotlin
sealed class MLResult<out T> {
    data class Success<T>(val data: T) : MLResult<T>()
    data class Error(val exception: Exception) : MLResult<Nothing>()
    object Loading : MLResult<Nothing>()
}

suspend fun detectObjectsSafely(bitmap: Bitmap): MLResult<List<DetectedObject>> {
    return try {
        MLResult.Loading
        val objects = mlKit.detectObjects(bitmap)
        MLResult.Success(objects)
    } catch (e: Exception) {
        MLResult.Error(e)
    }
}
```

## UI Integration with Compose

Display ML results in Compose UI:

```kotlin
@Composable
fun MLResultsScreen(
    viewModel: ScanViewModel
) {
    val mlResults by viewModel.mlResults.collectAsState()
    
    Column {
        Text("Detected Objects", style = MaterialTheme.typography.headlineMedium)
        
        when (mlResults) {
            is MLResult.Loading -> CircularProgressIndicator()
            is MLResult.Success -> {
                val objects = (mlResults as MLResult.Success).data
                LazyColumn {
                    items(objects) { obj ->
                        ObjectCard(obj)
                    }
                }
            }
            is MLResult.Error -> {
                Text("Error: ${(mlResults as MLResult.Error).exception.message}")
            }
        }
    }
}

@Composable
fun ObjectCard(obj: DetectedObject) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(obj.label, style = MaterialTheme.typography.titleMedium)
            Text("Confidence: ${(obj.confidence * 100).toInt()}%")
            Text("Location: (${obj.boundingBox.centerX()}, ${obj.boundingBox.centerY()})")
        }
    }
}
```

## Testing

### Unit Tests

```kotlin
@Test
fun testObjectDetection() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val mlKit = MLKitDetector(context)
    
    val testBitmap = createTestBitmap()
    val objects = mlKit.detectObjects(testBitmap)
    
    assertNotNull(objects)
}

@Test
fun testImageLabeling() = runTest {
    val mlKit = MLKitDetector(context)
    val bitmap = loadTestImage()
    
    val labels = mlKit.labelImage(bitmap)
    
    assertTrue(labels.isNotEmpty())
    assertTrue(labels.all { it.confidence > 0.0f })
}
```

### Integration Tests

```kotlin
@Test
fun testMLKitWithARCore() = runTest {
    val sessionManager = ARCoreSessionManager(context)
    val mlKit = MLKitDetector(context)
    
    val session = sessionManager.createSession()
    assertNotNull(session)
    
    // Simulate frame capture and analysis
    // ...
}
```

## Best Practices

1. **Resource Management**
   ```kotlin
   override fun onDestroy() {
       super.onDestroy()
       mlKit.close() // Release ML Kit resources
   }
   ```

2. **Background Processing**
   ```kotlin
   lifecycleScope.launch(Dispatchers.Default) {
       val result = mlKit.detectObjects(bitmap)
       withContext(Dispatchers.Main) {
           updateUI(result)
       }
   }
   ```

3. **Caching Results**
   ```kotlin
   private val resultsCache = mutableMapOf<String, List<DetectedObject>>()
   
   fun getOrDetect(imageId: String, bitmap: Bitmap): List<DetectedObject> {
       return resultsCache.getOrPut(imageId) {
           mlKit.detectObjects(bitmap)
       }
   }
   ```

4. **Batch Processing**
   ```kotlin
   suspend fun processMultipleImages(bitmaps: List<Bitmap>): List<List<DetectedObject>> {
       return bitmaps.map { bitmap ->
           async { mlKit.detectObjects(bitmap) }
       }.awaitAll()
   }
   ```

## Troubleshooting

### "ML Kit not initialized"
- Ensure ML Kit dependencies are in build.gradle
- Check that Google Play Services is installed

### "Detection returns empty results"
- Check image quality and size
- Adjust confidence threshold
- Ensure sufficient lighting in images

### "Performance issues"
- Use STREAM_MODE for video
- Throttle inference frequency
- Reduce image resolution before processing

## Resources

- [ML Kit Documentation](https://developers.google.com/ml-kit)
- [Object Detection Guide](https://developers.google.com/ml-kit/vision/object-detection)
- [Image Labeling Guide](https://developers.google.com/ml-kit/vision/image-labeling)
- [Custom Models with ML Kit](https://developers.google.com/ml-kit/custom-models)

## See Also

- [TFLite Deployment Guide](TFLITE_DEPLOYMENT.md)
- [ARCore Integration](ARCORE_INTEGRATION.md)
- [Architecture Overview](ARCHITECTURE.md)
