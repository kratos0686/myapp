package com.roomscanner.app.firebase

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Firebase Cloud Functions integration.
 * Provides methods to trigger serverless functions for processing scan data.
 */
class CloudFunctionsClient {
    
    private val functions: FirebaseFunctions = Firebase.functions
    
    /**
     * Trigger a cloud function to process scan data.
     * Example: Generate a detailed report from scan measurements.
     * 
     * @param scanId The ID of the scan to process
     * @return The result data from the cloud function
     */
    suspend fun processScanData(scanId: String): Map<String, Any>? {
        return try {
            val data = hashMapOf(
                "scanId" to scanId,
                "action" to "process"
            )
            
            val result = functions
                .getHttpsCallable("processScanData")
                .call(data)
                .await()
            
            result.data as? Map<String, Any>
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Trigger a cloud function to generate a PDF report.
     * 
     * @param scanId The ID of the scan
     * @return URL to the generated PDF
     */
    suspend fun generatePDFReport(scanId: String): String? {
        return try {
            val data = hashMapOf(
                "scanId" to scanId,
                "format" to "pdf"
            )
            
            val result = functions
                .getHttpsCallable("generateReport")
                .call(data)
                .await()
            
            (result.data as? Map<*, *>)?.get("url") as? String
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Trigger ML model inference via cloud function.
     * Useful for heavy ML tasks that shouldn't run on device.
     * 
     * @param imageUrl URL to the image stored in Firebase Storage
     * @param modelName Name of the model to use
     * @return Inference results
     */
    suspend fun runCloudMLInference(imageUrl: String, modelName: String): Map<String, Any>? {
        return try {
            val data = hashMapOf(
                "imageUrl" to imageUrl,
                "modelName" to modelName
            )
            
            val result = functions
                .getHttpsCallable("runMLInference")
                .call(data)
                .await()
            
            result.data as? Map<String, Any>
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Trigger batch processing of multiple scans.
     * 
     * @param scanIds List of scan IDs to process
     * @return Processing status
     */
    suspend fun batchProcessScans(scanIds: List<String>): Boolean {
        return try {
            val data = hashMapOf(
                "scanIds" to scanIds
            )
            
            functions
                .getHttpsCallable("batchProcessScans")
                .call(data)
                .await()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Request cost estimation for a scan.
     * 
     * @param scanId The ID of the scan
     * @return Estimated costs for materials and labor
     */
    suspend fun estimateCosts(scanId: String): Map<String, Any>? {
        return try {
            val data = hashMapOf(
                "scanId" to scanId
            )
            
            val result = functions
                .getHttpsCallable("estimateCosts")
                .call(data)
                .await()
            
            result.data as? Map<String, Any>
        } catch (e: Exception) {
            null
        }
    }
}
