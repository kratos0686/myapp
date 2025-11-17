package com.roomscanner.app.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Firebase Storage integration for uploading scan files.
 * Handles point cloud data, mesh data, and images.
 */
class StorageManager {
    
    private val storage: FirebaseStorage = Firebase.storage
    private val storageRef = storage.reference
    
    /**
     * Upload a point cloud file to Firebase Storage.
     * 
     * @param scanId The ID of the scan
     * @param file The point cloud file to upload
     * @return Download URL if successful, null otherwise
     */
    suspend fun uploadPointCloud(scanId: String, file: File): String? {
        return uploadFile(file, "scans/$scanId/pointcloud.bin")
    }
    
    /**
     * Upload mesh data to Firebase Storage.
     * 
     * @param scanId The ID of the scan
     * @param file The mesh data file
     * @return Download URL if successful, null otherwise
     */
    suspend fun uploadMeshData(scanId: String, file: File): String? {
        return uploadFile(file, "scans/$scanId/mesh.obj")
    }
    
    /**
     * Upload an image from the scan to Firebase Storage.
     * 
     * @param scanId The ID of the scan
     * @param file The image file
     * @param imageIndex Index of the image (for multiple images per scan)
     * @return Download URL if successful, null otherwise
     */
    suspend fun uploadScanImage(scanId: String, file: File, imageIndex: Int): String? {
        return uploadFile(file, "scans/$scanId/images/image_$imageIndex.jpg")
    }
    
    /**
     * Upload a TFLite model to Firebase Storage.
     * Useful for deploying custom models.
     * 
     * @param modelName Name of the model
     * @param file The .tflite model file
     * @return Download URL if successful, null otherwise
     */
    suspend fun uploadModel(modelName: String, file: File): String? {
        return uploadFile(file, "models/$modelName.tflite")
    }
    
    /**
     * Generic file upload method.
     */
    private suspend fun uploadFile(file: File, remotePath: String): String? {
        return try {
            val fileRef = storageRef.child(remotePath)
            val uri = Uri.fromFile(file)
            
            fileRef.putFile(uri).await()
            
            // Get download URL
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Download a file from Firebase Storage.
     * 
     * @param remotePath Path to the file in storage
     * @param localFile Local file to save to
     * @return true if successful, false otherwise
     */
    suspend fun downloadFile(remotePath: String, localFile: File): Boolean {
        return try {
            val fileRef = storageRef.child(remotePath)
            fileRef.getFile(localFile).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Delete a scan's files from storage.
     * 
     * @param scanId The ID of the scan
     */
    suspend fun deleteScanFiles(scanId: String): Boolean {
        return try {
            val scanRef = storageRef.child("scans/$scanId")
            scanRef.delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
