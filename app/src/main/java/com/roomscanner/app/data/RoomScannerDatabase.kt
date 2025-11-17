package com.roomscanner.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room Database for storing scan data and job notes offline.
 */
@Database(
    entities = [RoomScan::class, JobNote::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RoomScannerDatabase : RoomDatabase() {
    abstract fun roomScanDao(): RoomScanDao
    abstract fun jobNoteDao(): JobNoteDao

    companion object {
        @Volatile
        private var INSTANCE: RoomScannerDatabase? = null

        fun getDatabase(context: Context): RoomScannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoomScannerDatabase::class.java,
                    "room_scanner_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
