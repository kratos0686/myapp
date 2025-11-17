package com.roomscanner.app.arcore

import android.content.Context
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.math.sqrt

/**
 * ARCore manager for 3D room scanning and measurement.
 * Handles AR session management and spatial data collection.
 */
class ARCoreManager(private val context: Context) {

    private var session: Session? = null
    private val anchorList = mutableListOf<Anchor>()
    private val pointCloudPoints = mutableListOf<FloatArray>()
    
    private val scanStateSubject = BehaviorSubject.create<ARScanState>()
    val scanState: Observable<ARScanState> = scanStateSubject

    /**
     * Initialize ARCore session.
     */
    fun initializeSession(): Result<Session> {
        return try {
            // Check if ARCore is installed and up to date
            when (ArCoreApk.getInstance().requestInstall(null, true)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    return Result.failure(Exception("ARCore installation requested"))
                }
                ArCoreApk.InstallStatus.INSTALLED -> {
                    // Continue with session creation
                }
                else -> {
                    return Result.failure(Exception("Unknown ARCore install status"))
                }
            }

            val session = Session(context, setOf(Session.Feature.SHARED_CAMERA)).apply {
                configure(
                    Config(this).apply {
                        planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                        depthMode = Config.DepthMode.AUTOMATIC
                        instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                        lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    }
                )
            }
            
            this.session = session
            scanStateSubject.onNext(ARScanState.Ready)
            Result.success(session)
        } catch (e: Exception) {
            when (e) {
                is UnavailableUserDeclinedInstallationException -> {
                    Result.failure(Exception("ARCore installation declined by user"))
                }
                is UnavailableDeviceNotCompatibleException -> {
                    Result.failure(Exception("Device not compatible with ARCore"))
                }
                is UnavailableApkTooOldException -> {
                    Result.failure(Exception("ARCore APK too old, please update"))
                }
                else -> Result.failure(e)
            }
        }
    }

    /**
     * Process AR frame and collect spatial data.
     */
    fun processFrame(frame: Frame): ARFrameData {
        val camera = frame.camera
        val trackingState = camera.trackingState
        
        // Update point cloud data
        val pointCloud = frame.acquirePointCloud()
        try {
            val points = pointCloud.points
            if (points.hasRemaining()) {
                val pointData = FloatArray(points.remaining())
                points.get(pointData)
                pointCloudPoints.add(pointData)
            }
        } finally {
            pointCloud.close()
        }

        // Detect planes
        val planes = frame.getUpdatedTrackables(Plane::class.java)
            .filter { it.trackingState == TrackingState.TRACKING }
            .toList()

        return ARFrameData(
            trackingState = trackingState,
            planes = planes,
            pointCloudSize = pointCloudPoints.size,
            anchors = anchorList.toList()
        )
    }

    /**
     * Add an anchor at a specific pose for measurement.
     */
    fun addAnchor(hitResult: HitResult): Anchor? {
        return try {
            val anchor = hitResult.createAnchor()
            anchorList.add(anchor)
            anchor
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate distance between two anchors.
     */
    fun calculateDistance(anchor1: Anchor, anchor2: Anchor): Float {
        val pose1 = anchor1.pose
        val pose2 = anchor2.pose
        
        val dx = pose2.tx() - pose1.tx()
        val dy = pose2.ty() - pose1.ty()
        val dz = pose2.tz() - pose1.tz()
        
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /**
     * Calculate room dimensions from detected planes and anchors.
     */
    fun calculateRoomDimensions(planes: List<Plane>): RoomDimensions {
        val horizontalPlanes = planes.filter { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
        val verticalPlanes = planes.filter { it.type == Plane.Type.VERTICAL }
        
        // Find floor plane (largest horizontal plane)
        val floorPlane = horizontalPlanes.maxByOrNull { it.extentX * it.extentZ }
        
        // Calculate room dimensions
        val width = floorPlane?.extentX ?: 0f
        val length = floorPlane?.extentZ ?: 0f
        
        // Estimate height from vertical planes or default
        val height = verticalPlanes.maxOfOrNull { it.extentY } ?: 2.5f
        
        return RoomDimensions(
            width = width,
            length = length,
            height = height,
            area = width * length,
            volume = width * length * height
        )
    }

    /**
     * Export point cloud data as JSON string.
     */
    fun exportPointCloudData(): String {
        val pointsData = pointCloudPoints.joinToString(",") { points ->
            points.joinToString(",") { "%.3f".format(it) }
        }
        return """{"points":[$pointsData],"count":${pointCloudPoints.size}}"""
    }

    /**
     * Clear all anchors and reset scan data.
     */
    fun clearScanData() {
        anchorList.forEach { it.detach() }
        anchorList.clear()
        pointCloudPoints.clear()
        scanStateSubject.onNext(ARScanState.Ready)
    }

    /**
     * Pause the AR session.
     */
    fun pause() {
        session?.pause()
        scanStateSubject.onNext(ARScanState.Paused)
    }

    /**
     * Resume the AR session.
     */
    fun resume() {
        session?.resume()
        scanStateSubject.onNext(ARScanState.Scanning)
    }

    /**
     * Close the AR session and clean up resources.
     */
    fun close() {
        clearScanData()
        session?.close()
        session = null
        scanStateSubject.onComplete()
    }
}

/**
 * AR scan state.
 */
sealed class ARScanState {
    object Ready : ARScanState()
    object Scanning : ARScanState()
    object Paused : ARScanState()
    data class Error(val message: String) : ARScanState()
}

/**
 * Data from a single AR frame.
 */
data class ARFrameData(
    val trackingState: TrackingState,
    val planes: List<Plane>,
    val pointCloudSize: Int,
    val anchors: List<Anchor>
)

/**
 * Room dimensions calculated from AR scan.
 */
data class RoomDimensions(
    val width: Float,
    val length: Float,
    val height: Float,
    val area: Float,
    val volume: Float
)
