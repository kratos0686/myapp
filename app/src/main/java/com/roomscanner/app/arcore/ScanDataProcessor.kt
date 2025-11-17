package com.roomscanner.app.arcore

import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.PointCloud
import com.google.ar.core.TrackingState
import java.io.File

/**
 * Utility class for processing ARCore scan data.
 * Extracts room dimensions, point clouds, and mesh data.
 */
class ScanDataProcessor {
    
    data class RoomDimensions(
        val length: Float,
        val width: Float,
        val height: Float
    )
    
    /**
     * Extract room dimensions from detected planes.
     * Analyzes horizontal and vertical planes to estimate room size.
     */
    fun extractRoomDimensions(frame: Frame): RoomDimensions? {
        val planes = frame.getUpdatedTrackables(Plane::class.java)
            .filter { it.trackingState == TrackingState.TRACKING }
        
        if (planes.isEmpty()) return null
        
        // Find the largest horizontal plane (floor)
        val floorPlane = planes
            .filter { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
            .maxByOrNull { it.extentX * it.extentZ }
        
        // Find vertical planes (walls)
        val wallPlanes = planes.filter { it.type == Plane.Type.VERTICAL }
        
        if (floorPlane == null) return null
        
        val length = floorPlane.extentX
        val width = floorPlane.extentZ
        
        // Estimate height from wall planes
        val height = wallPlanes.maxOfOrNull { it.extentY } ?: 2.5f
        
        return RoomDimensions(length, width, height)
    }
    
    /**
     * Save point cloud data to file for offline storage.
     * @param pointCloud ARCore point cloud
     * @param outputFile File to save the point cloud data
     */
    fun savePointCloud(pointCloud: PointCloud, outputFile: File): Boolean {
        return try {
            val points = pointCloud.points
            points.rewind()
            
            outputFile.outputStream().use { output ->
                val buffer = ByteArray(points.remaining())
                points.get(buffer)
                output.write(buffer)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Extract mesh data from detected planes.
     * Combines plane information for 3D reconstruction.
     */
    fun extractMeshData(frame: Frame): List<PlaneData> {
        return frame.getUpdatedTrackables(Plane::class.java)
            .filter { it.trackingState == TrackingState.TRACKING }
            .map { plane ->
                PlaneData(
                    centerX = plane.centerPose.tx(),
                    centerY = plane.centerPose.ty(),
                    centerZ = plane.centerPose.tz(),
                    extentX = plane.extentX,
                    extentZ = plane.extentZ,
                    type = plane.type.toString()
                )
            }
    }
    
    data class PlaneData(
        val centerX: Float,
        val centerY: Float,
        val centerZ: Float,
        val extentX: Float,
        val extentZ: Float,
        val type: String
    )
    
    /**
     * Calculate the total area covered by the scan.
     */
    fun calculateTotalArea(dimensions: RoomDimensions): Float {
        // Floor + ceiling + 4 walls
        val floorArea = dimensions.length * dimensions.width
        val ceilingArea = floorArea
        val wallArea = 2 * (dimensions.length * dimensions.height + dimensions.width * dimensions.height)
        
        return floorArea + ceilingArea + wallArea
    }
}
