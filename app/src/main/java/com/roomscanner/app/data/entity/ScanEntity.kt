package com.roomscanner.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a room scan in the database.
 * Stores 3D scan data, measurements, and metadata.
 */
@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val roomName: String,
    val scanDate: Long = Date().time,
    
    // Room dimensions
    val length: Float,
    val width: Float,
    val height: Float,
    
    // ARCore data paths
    val pointCloudPath: String? = null,
    val meshDataPath: String? = null,
    
    // ML analysis results
    val detectedObjects: String? = null, // JSON string of detected objects
    val damageAreas: String? = null, // JSON string of damage locations
    
    // Status
    val isSynced: Boolean = false,
    val firebaseId: String? = null
)
