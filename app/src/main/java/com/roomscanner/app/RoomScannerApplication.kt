package com.roomscanner.app

import android.app.Application
import com.roomscanner.app.data.RoomScannerDatabase

/**
 * Application class for initializing Room Database and other services.
 */
class RoomScannerApplication : Application() {
    
    // Database instance
    val database: RoomScannerDatabase by lazy {
        RoomScannerDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize any other services here
    }
}
