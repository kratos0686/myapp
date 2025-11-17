package com.roomscanner.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Entity representing a 3D room scan with measurements and metadata.
 */
@Entity(tableName = "room_scans")
@TypeConverters(Converters::class)
data class RoomScan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomName: String,
    val scanDate: Date,
    val width: Float,  // in meters
    val length: Float, // in meters
    val height: Float, // in meters
    val area: Float,   // in square meters
    val volume: Float, // in cubic meters
    val pointCloudData: String?, // JSON or file path to point cloud data
    val damagedAreas: List<DamagedArea>,
    val materialEstimates: List<MaterialEstimate>,
    val syncedToFirebase: Boolean = false,
    val firebaseDocId: String? = null
)

/**
 * Represents a damaged area detected by ML Kit.
 */
data class DamagedArea(
    val type: String, // e.g., "crack", "water damage", "hole"
    val location: String, // e.g., "north wall", "ceiling"
    val severity: Float, // 0.0 to 1.0
    val boundingBox: BoundingBox,
    val imageUri: String?
)

/**
 * Represents a bounding box for detected objects.
 */
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

/**
 * Represents estimated material type for surfaces.
 */
data class MaterialEstimate(
    val surfaceType: String, // e.g., "wall", "floor", "ceiling"
    val materialType: String, // e.g., "drywall", "concrete", "wood"
    val confidence: Float, // 0.0 to 1.0
    val coverage: Float // percentage of surface
)

/**
 * Entity for storing job notes related to room scans.
 */
@Entity(tableName = "job_notes")
@TypeConverters(Converters::class)
data class JobNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomScanId: Long,
    val title: String,
    val content: String,
    val createdDate: Date,
    val updatedDate: Date,
    val tags: List<String>,
    val attachmentUris: List<String>,
    val syncedToFirebase: Boolean = false
)

/**
 * Type converters for Room Database.
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromDamagedAreaList(value: List<DamagedArea>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDamagedAreaList(value: String?): List<DamagedArea>? {
        val listType = object : TypeToken<List<DamagedArea>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromMaterialEstimateList(value: List<MaterialEstimate>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMaterialEstimateList(value: String?): List<MaterialEstimate>? {
        val listType = object : TypeToken<List<MaterialEstimate>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
}
