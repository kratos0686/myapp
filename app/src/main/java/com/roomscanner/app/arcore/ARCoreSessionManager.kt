package com.roomscanner.app.arcore

import android.content.Context
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableException

/**
 * Helper class for ARCore session management and configuration.
 * Handles ARCore availability checking and session setup.
 */
class ARCoreSessionManager(private val context: Context) {
    
    private var session: Session? = null
    
    /**
     * Check if ARCore is supported and installed on this device.
     * @return true if ARCore is available, false otherwise
     */
    fun checkARCoreAvailability(): Boolean {
        return try {
            when (ArCoreApk.getInstance().checkAvailability(context)) {
                ArCoreApk.Availability.SUPPORTED_INSTALLED -> true
                ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD,
                ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                    // ARCore is supported but needs installation/update
                    false
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Request ARCore installation if needed.
     * Should be called from an Activity context.
     */
    fun requestARCoreInstall(): ArCoreApk.InstallStatus? {
        return try {
            ArCoreApk.getInstance().requestInstall(
                context as android.app.Activity,
                true
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Create and configure an ARCore session for room scanning.
     * @return Configured ARCore session
     */
    fun createSession(): Session? {
        return try {
            val newSession = Session(context)
            
            // Configure session for room scanning
            val config = Config(newSession)
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            config.depthMode = Config.DepthMode.AUTOMATIC
            
            newSession.configure(config)
            session = newSession
            newSession
        } catch (e: UnavailableException) {
            null
        }
    }
    
    /**
     * Pause the ARCore session.
     * Should be called in onPause() of the Activity/Fragment.
     */
    fun pause() {
        session?.pause()
    }
    
    /**
     * Resume the ARCore session.
     * Should be called in onResume() of the Activity/Fragment.
     */
    fun resume() {
        session?.resume()
    }
    
    /**
     * Close and cleanup the ARCore session.
     * Should be called when done with scanning.
     */
    fun close() {
        session?.close()
        session = null
    }
    
    fun getSession(): Session? = session
}
