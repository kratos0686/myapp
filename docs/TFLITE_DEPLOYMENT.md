# TensorFlow Lite Model Deployment Guide

## Overview
This guide shows how to convert, deploy, and use TensorFlow Lite models in the Room Scanner app for on-device machine learning.

## Model Conversion Pipeline

### From TensorFlow/Keras to TFLite

```python
import tensorflow as tf

# Option 1: Convert from saved model
converter = tf.lite.TFLiteConverter.from_saved_model('path/to/saved_model')
tflite_model = converter.convert()

# Option 2: Convert from Keras model
model = tf.keras.models.load_model('model.h5')
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Save the model
with open('damage_detector.tflite', 'wb') as f:
    f.write(tflite_model)
```

### From PyTorch to TFLite

```python
import torch
import torch.onnx
import onnx
from onnx_tf.backend import prepare
import tensorflow as tf

# 1. Export PyTorch model to ONNX
model = torch.load('pytorch_model.pth')
model.eval()

dummy_input = torch.randn(1, 3, 224, 224)
torch.onnx.export(
    model,
    dummy_input,
    "model.onnx",
    export_params=True,
    opset_version=11,
    do_constant_folding=True,
    input_names=['input'],
    output_names=['output']
)

# 2. Convert ONNX to TensorFlow
onnx_model = onnx.load("model.onnx")
tf_model = prepare(onnx_model)
tf_model.export_graph("tf_model")

# 3. Convert TensorFlow to TFLite
converter = tf.lite.TFLiteConverter.from_saved_model("tf_model")
tflite_model = converter.convert()

with open('model.tflite', 'wb') as f:
    f.write(tflite_model)
```

## Optimization Techniques

### Quantization

```python
import tensorflow as tf

# Dynamic range quantization (reduces model size by ~4x)
converter = tf.lite.TFLiteConverter.from_saved_model('saved_model')
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_quantized = converter.convert()

# Full integer quantization (best performance on CPU)
def representative_dataset():
    for _ in range(100):
        # Use real data samples
        yield [np.random.random((1, 224, 224, 3)).astype(np.float32)]

converter = tf.lite.TFLiteConverter.from_saved_model('saved_model')
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.representative_dataset = representative_dataset
converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
converter.inference_input_type = tf.uint8
converter.inference_output_type = tf.uint8
tflite_int8 = converter.convert()

# Float16 quantization (good balance)
converter = tf.lite.TFLiteConverter.from_saved_model('saved_model')
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.target_spec.supported_types = [tf.float16]
tflite_fp16 = converter.convert()
```

## Model Integration in Android

### 1. Add Model to Project

Place the `.tflite` file in `app/src/main/assets/`:

```
app/
  src/
    main/
      assets/
        damage_detector.tflite
        material_classifier.tflite
        labels.txt
```

### 2. Using TFLite Model

The app includes a `TFLiteModelRunner` class. Here's how to use it:

```kotlin
class MLProcessor(context: Context) {
    private val modelRunner = TFLiteModelRunner(context)
    
    init {
        // Load model on initialization
        modelRunner.loadModel("damage_detector.tflite")
    }
    
    fun analyzeSurface(bitmap: Bitmap): List<TFLiteModelRunner.DamageDetection> {
        return modelRunner.detectDamage(bitmap)
    }
    
    fun identifyMaterial(bitmap: Bitmap): TFLiteModelRunner.MaterialEstimation? {
        return modelRunner.estimateMaterial(bitmap)
    }
    
    fun cleanup() {
        modelRunner.close()
    }
}
```

### 3. GPU Acceleration

Add GPU delegate for better performance:

```kotlin
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.Interpreter

val options = Interpreter.Options().apply {
    setNumThreads(4)
    
    // Use GPU delegate
    val gpuDelegate = GpuDelegate()
    addDelegate(gpuDelegate)
}

val interpreter = Interpreter(modelFile, options)
```

## Creating Custom Models

### Example: Damage Detection Model

```python
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers

# Define model architecture
def create_damage_detector():
    model = keras.Sequential([
        # Input layer
        layers.Input(shape=(224, 224, 3)),
        
        # Convolutional base
        layers.Conv2D(32, 3, activation='relu'),
        layers.MaxPooling2D(),
        layers.Conv2D(64, 3, activation='relu'),
        layers.MaxPooling2D(),
        layers.Conv2D(128, 3, activation='relu'),
        layers.MaxPooling2D(),
        
        # Classification head
        layers.GlobalAveragePooling2D(),
        layers.Dense(128, activation='relu'),
        layers.Dropout(0.5),
        layers.Dense(5, activation='softmax')  # 5 damage types
    ])
    
    return model

# Train model
model = create_damage_detector()
model.compile(
    optimizer='adam',
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

# Train with your data
# model.fit(train_dataset, epochs=10, validation_data=val_dataset)

# Convert to TFLite
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()

with open('damage_detector.tflite', 'wb') as f:
    f.write(tflite_model)
```

### Example: Material Classification

```python
def create_material_classifier():
    # Use transfer learning with MobileNetV2
    base_model = keras.applications.MobileNetV2(
        input_shape=(224, 224, 3),
        include_top=False,
        weights='imagenet'
    )
    
    base_model.trainable = False
    
    model = keras.Sequential([
        base_model,
        layers.GlobalAveragePooling2D(),
        layers.Dense(256, activation='relu'),
        layers.Dropout(0.5),
        layers.Dense(10, activation='softmax')  # 10 material types
    ])
    
    return model

# Train and convert similar to damage detector
```

## Model Download from Firebase

### Upload Model to Firebase Storage

```bash
# Using Firebase CLI
firebase storage:upload damage_detector.tflite models/damage_detector.tflite

# Or use Firebase Console
```

### Download in Android App

```kotlin
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader

class ModelManager(private val context: Context) {
    
    suspend fun downloadModel(modelName: String): File? {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()
            .build()
        
        return suspendCoroutine { continuation ->
            FirebaseModelDownloader.getInstance()
                .getModel(
                    modelName,
                    DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND,
                    conditions
                )
                .addOnSuccessListener { model ->
                    continuation.resume(model.file)
                }
                .addOnFailureListener { e ->
                    continuation.resume(null)
                }
        }
    }
    
    suspend fun checkModelVersion(modelName: String): Long? {
        return suspendCoroutine { continuation ->
            FirebaseModelDownloader.getInstance()
                .getModel(modelName, DownloadType.LOCAL_MODEL)
                .addOnSuccessListener { model ->
                    continuation.resume(model.downloadId)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }
}
```

## Best Practices

### 1. Model Size Optimization

- Use quantization to reduce model size
- Remove unused operations
- Use pruning during training
- Consider using MobileNet or EfficientNet architectures

### 2. Performance Optimization

```kotlin
// Warm up the model
fun warmUpModel(interpreter: Interpreter) {
    val dummyInput = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)
    val dummyOutput = ByteBuffer.allocateDirect(1 * 10 * 4)
    interpreter.run(dummyInput, dummyOutput)
}

// Use appropriate delegates
val options = Interpreter.Options().apply {
    setNumThreads(Runtime.getRuntime().availableProcessors())
    if (isGPUAvailable()) {
        addDelegate(GpuDelegate())
    } else {
        // Use NNAPI delegate for better CPU performance
        setUseNNAPI(true)
    }
}
```

### 3. Input Preprocessing

```kotlin
fun preprocessImage(bitmap: Bitmap): ByteBuffer {
    val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(127.5f, 127.5f))  // Normalize to [-1, 1]
        .build()
    
    var tensorImage = TensorImage.fromBitmap(bitmap)
    tensorImage = imageProcessor.process(tensorImage)
    
    return tensorImage.buffer
}
```

### 4. Error Handling

```kotlin
fun runInference(bitmap: Bitmap): Result<List<Detection>> {
    return try {
        val results = modelRunner.detectDamage(bitmap)
        Result.success(results)
    } catch (e: IllegalStateException) {
        // Model not loaded
        Result.failure(e)
    } catch (e: Exception) {
        // Inference error
        Result.failure(e)
    }
}
```

## Testing Models

### Unit Tests

```kotlin
@Test
fun testModelInference() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val modelRunner = TFLiteModelRunner(context)
    
    assertTrue(modelRunner.loadModel("damage_detector.tflite"))
    
    val testBitmap = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.test_damage
    )
    
    val results = modelRunner.detectDamage(testBitmap)
    assertNotNull(results)
    assertTrue(results.isNotEmpty())
}
```

### Benchmark Performance

```kotlin
fun benchmarkModel(iterations: Int = 100) {
    val times = mutableListOf<Long>()
    
    repeat(iterations) {
        val startTime = System.nanoTime()
        modelRunner.detectDamage(testBitmap)
        val endTime = System.nanoTime()
        times.add((endTime - startTime) / 1_000_000) // Convert to ms
    }
    
    val avgTime = times.average()
    val p95Time = times.sorted()[iterations * 95 / 100]
    
    Log.d("Benchmark", "Average: ${avgTime}ms, P95: ${p95Time}ms")
}
```

## Troubleshooting

### Common Issues

1. **"Model file not found"**
   - Check that the .tflite file is in `app/src/main/assets/`
   - Use correct file name when loading

2. **"Inference failed"**
   - Verify input tensor shape matches model requirements
   - Check data type (float32 vs uint8)

3. **Poor accuracy**
   - Ensure preprocessing matches training preprocessing
   - Check if quantization affected accuracy
   - Validate with test dataset

4. **Out of memory**
   - Use smaller batch size
   - Reduce input image size
   - Use quantized models

## Resources

- [TensorFlow Lite Guide](https://www.tensorflow.org/lite/guide)
- [Model Optimization](https://www.tensorflow.org/lite/performance/model_optimization)
- [GPU Acceleration](https://www.tensorflow.org/lite/performance/gpu)
- [NNAPI Delegate](https://www.tensorflow.org/lite/performance/nnapi)

## See Also

- [ML Kit Integration](ML_INTEGRATION.md)
- [Firebase Model Download](FIREBASE_SETUP.md)
- [ARCore Integration](ARCORE_INTEGRATION.md)
