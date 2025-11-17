package com.roomscanner.app.domain

import android.graphics.Bitmap
import com.roomscanner.app.arcore.ARCoreManager
import com.roomscanner.app.arcore.RoomDimensions
import com.roomscanner.app.data.DamagedArea
import com.roomscanner.app.data.MaterialEstimate
import com.roomscanner.app.data.RoomScanRepository
import com.roomscanner.app.ml.MLKitAnalyzer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Use case demonstrating the integration of ARCore, ML Kit, Room Database,
 * and Firebase with RxJava data flow.
 * 
 * This is a sample implementation showing how to wire up all components
 * for the complete scan-to-storage-to-sync workflow.
 */
class CreateRoomScanUseCase(
    private val repository: RoomScanRepository,
    private val mlKitAnalyzer: MLKitAnalyzer
) {

    /**
     * Complete workflow for creating a room scan:
     * 1. Process ARCore data to get room dimensions
     * 2. Analyze images with ML Kit to detect damage and materials
     * 3. Save to Room Database
     * 4. Sync to Firebase
     * 
     * This demonstrates RxJava chaining for the entire data flow.
     */
    fun execute(
        roomName: String,
        dimensions: RoomDimensions,
        pointCloudData: String?,
        capturedImages: List<CapturedImage>
    ): Single<Long> {
        
        // Step 1: Analyze all captured images with ML Kit in parallel
        val analysisObservables = capturedImages.map { image ->
            Single.zip(
                mlKitAnalyzer.detectDamagedAreas(image.bitmap, image.uri),
                mlKitAnalyzer.estimateMaterialTypes(image.bitmap, image.surfaceType),
                { damages, materials -> AnalysisResult(damages, materials) }
            )
        }
        
        // Step 2: Combine all analysis results
        return Single.zip(analysisObservables) { results ->
            val allDamagedAreas = mutableListOf<DamagedArea>()
            val allMaterialEstimates = mutableListOf<MaterialEstimate>()
            
            results.forEach { result ->
                val analysisResult = result as AnalysisResult
                allDamagedAreas.addAll(analysisResult.damagedAreas)
                allMaterialEstimates.addAll(analysisResult.materialEstimates)
            }
            
            CombinedAnalysis(allDamagedAreas, allMaterialEstimates)
        }
        // Step 3: Create and save the room scan to Room Database
        .flatMap { analysis ->
            repository.createRoomScan(
                roomName = roomName,
                dimensions = dimensions,
                pointCloudData = pointCloudData,
                damagedAreas = analysis.damagedAreas,
                materialEstimates = analysis.materialEstimates
            )
        }
        // Step 4: Trigger Firebase sync (async, doesn't block the result)
        .doOnSuccess {
            repository.syncScansToFirebase()
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { /* Sync completed */ },
                    { error -> /* Handle sync error */ }
                )
        }
        .subscribeOn(Schedulers.io())
    }
}

/**
 * Represents a captured image with metadata.
 */
data class CapturedImage(
    val bitmap: Bitmap,
    val uri: String,
    val surfaceType: String // "wall", "floor", "ceiling"
)

/**
 * Analysis result from ML Kit.
 */
private data class AnalysisResult(
    val damagedAreas: List<DamagedArea>,
    val materialEstimates: List<MaterialEstimate>
)

/**
 * Combined analysis from multiple images.
 */
private data class CombinedAnalysis(
    val damagedAreas: List<DamagedArea>,
    val materialEstimates: List<MaterialEstimate>
)
