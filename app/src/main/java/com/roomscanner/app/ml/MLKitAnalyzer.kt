package com.roomscanner.app.ml

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.roomscanner.app.data.BoundingBox
import com.roomscanner.app.data.DamagedArea
import com.roomscanner.app.data.MaterialEstimate
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * ML Kit service for detecting damaged areas and estimating material types.
 * Uses on-device processing for real-time analysis.
 */
class MLKitAnalyzer {

    private val objectDetector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        ObjectDetection.getClient(options)
    }

    private val imageLabeler by lazy {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.6f)
            .build()
        ImageLabeling.getClient(options)
    }

    /**
     * Detect damaged areas in a room image using ML Kit object detection.
     * This is a simplified implementation - in production, you would train
     * a custom TensorFlow Lite model for damage detection.
     */
    fun detectDamagedAreas(bitmap: Bitmap, imageUri: String): Single<List<DamagedArea>> {
        return Single.create { emitter ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                
                objectDetector.process(inputImage)
                    .addOnSuccessListener { detectedObjects ->
                        val damagedAreas = detectedObjects.mapNotNull { obj ->
                            // Filter for potential damage indicators
                            // In production, use a custom model trained on damage types
                            val labels = obj.labels.filter { label ->
                                isDamageRelated(label.text)
                            }
                            
                            if (labels.isNotEmpty()) {
                                val boundingBox = obj.boundingBox
                                DamagedArea(
                                    type = labels.first().text,
                                    location = determineLocation(boundingBox, bitmap.width, bitmap.height),
                                    severity = labels.first().confidence,
                                    boundingBox = BoundingBox(
                                        left = boundingBox.left.toFloat(),
                                        top = boundingBox.top.toFloat(),
                                        right = boundingBox.right.toFloat(),
                                        bottom = boundingBox.bottom.toFloat()
                                    ),
                                    imageUri = imageUri
                                )
                            } else null
                        }
                        emitter.onSuccess(damagedAreas)
                    }
                    .addOnFailureListener { e ->
                        emitter.onError(e)
                    }
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Estimate material types from image using ML Kit image labeling.
     * This provides basic material identification - enhance with custom models.
     */
    fun estimateMaterialTypes(bitmap: Bitmap, surfaceType: String): Single<List<MaterialEstimate>> {
        return Single.create { emitter ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                
                imageLabeler.process(inputImage)
                    .addOnSuccessListener { labels ->
                        val materialEstimates = labels
                            .filter { label -> isMaterialRelated(label.text) }
                            .map { label ->
                                MaterialEstimate(
                                    surfaceType = surfaceType,
                                    materialType = mapToMaterialType(label.text),
                                    confidence = label.confidence,
                                    coverage = 100f // Simplified - would need segmentation for accurate coverage
                                )
                            }
                        emitter.onSuccess(materialEstimates)
                    }
                    .addOnFailureListener { e ->
                        emitter.onError(e)
                    }
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Check if a label is related to potential damage.
     * Extend this list based on your damage detection requirements.
     */
    private fun isDamageRelated(label: String): Boolean {
        val damageKeywords = listOf(
            "crack", "hole", "stain", "mold", "water damage",
            "scratch", "dent", "broken", "damaged", "worn"
        )
        return damageKeywords.any { keyword ->
            label.lowercase().contains(keyword)
        }
    }

    /**
     * Check if a label is related to building materials.
     */
    private fun isMaterialRelated(label: String): Boolean {
        val materialKeywords = listOf(
            "wood", "concrete", "brick", "metal", "glass",
            "drywall", "tile", "carpet", "hardwood", "stone",
            "plastic", "ceramic", "marble", "granite"
        )
        return materialKeywords.any { keyword ->
            label.lowercase().contains(keyword)
        }
    }

    /**
     * Map ML Kit labels to standardized material types.
     */
    private fun mapToMaterialType(label: String): String {
        return when {
            label.lowercase().contains("wood") || label.lowercase().contains("hardwood") -> "wood"
            label.lowercase().contains("concrete") -> "concrete"
            label.lowercase().contains("brick") -> "brick"
            label.lowercase().contains("metal") -> "metal"
            label.lowercase().contains("glass") -> "glass"
            label.lowercase().contains("tile") || label.lowercase().contains("ceramic") -> "tile"
            label.lowercase().contains("carpet") -> "carpet"
            label.lowercase().contains("drywall") -> "drywall"
            label.lowercase().contains("stone") || label.lowercase().contains("marble") || 
            label.lowercase().contains("granite") -> "stone"
            else -> label
        }
    }

    /**
     * Determine the location of an object in the room based on its position in the image.
     */
    private fun determineLocation(boundingBox: Rect, imageWidth: Int, imageHeight: Int): String {
        val centerX = (boundingBox.left + boundingBox.right) / 2f
        val centerY = (boundingBox.top + boundingBox.bottom) / 2f
        
        val horizontalPosition = when {
            centerX < imageWidth / 3 -> "left"
            centerX > imageWidth * 2 / 3 -> "right"
            else -> "center"
        }
        
        val verticalPosition = when {
            centerY < imageHeight / 3 -> "upper"
            centerY > imageHeight * 2 / 3 -> "lower"
            else -> "middle"
        }
        
        return "$verticalPosition $horizontalPosition"
    }

    /**
     * Clean up resources when done.
     */
    fun close() {
        objectDetector.close()
        imageLabeler.close()
    }
}
