package com.roomscanner.app.ml

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.tasks.await

/**
 * ML Kit integration for detecting objects and materials in room scans.
 * Provides on-device machine learning capabilities.
 */
class MLKitDetector(private val context: Context) {
    
    private val objectDetector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        ObjectDetection.getClient(options)
    }
    
    private val imageLabeler by lazy {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
        ImageLabeling.getClient(options)
    }
    
    data class DetectedObject(
        val label: String,
        val confidence: Float,
        val boundingBox: android.graphics.Rect
    )
    
    data class ImageLabel(
        val text: String,
        val confidence: Float
    )
    
    /**
     * Detect objects in the given image.
     * Useful for identifying furniture, fixtures, and other items in the room.
     */
    suspend fun detectObjects(bitmap: Bitmap): List<DetectedObject> {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        return try {
            val results = objectDetector.process(image).await()
            results.mapNotNull { obj ->
                obj.labels.firstOrNull()?.let { label ->
                    DetectedObject(
                        label = label.text,
                        confidence = label.confidence,
                        boundingBox = obj.boundingBox
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Label the image to identify materials and surfaces.
     * Useful for estimating material types in the room.
     */
    suspend fun labelImage(bitmap: Bitmap): List<ImageLabel> {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        return try {
            val results = imageLabeler.process(image).await()
            results.map { label ->
                ImageLabel(
                    text = label.text,
                    confidence = label.confidence
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Analyze an image for both objects and labels.
     * Provides comprehensive scene understanding.
     */
    suspend fun analyzeImage(bitmap: Bitmap): AnalysisResult {
        val objects = detectObjects(bitmap)
        val labels = labelImage(bitmap)
        
        return AnalysisResult(
            detectedObjects = objects,
            imageLabels = labels
        )
    }
    
    data class AnalysisResult(
        val detectedObjects: List<DetectedObject>,
        val imageLabels: List<ImageLabel>
    )
    
    /**
     * Clean up resources when done.
     */
    fun close() {
        objectDetector.close()
        imageLabeler.close()
    }
}
