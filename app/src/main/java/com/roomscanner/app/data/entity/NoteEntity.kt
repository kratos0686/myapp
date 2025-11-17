package com.roomscanner.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a note/annotation for a scan.
 * Used for offline storage of job notes and observations.
 */
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = ScanEntity::class,
            parentColumns = ["id"],
            childColumns = ["scanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scanId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val scanId: Long,
    val title: String,
    val content: String,
    val createdDate: Long = Date().time,
    val modifiedDate: Long = Date().time,
    
    // Location in the scan where note applies
    val positionX: Float? = null,
    val positionY: Float? = null,
    val positionZ: Float? = null,
    
    // Categories for filtering
    val category: String = "general", // general, damage, measurement, material
    
    // Sync status
    val isSynced: Boolean = false,
    val firebaseId: String? = null
)
