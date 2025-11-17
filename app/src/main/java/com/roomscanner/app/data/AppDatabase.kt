package com.roomscanner.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.roomscanner.app.data.dao.NoteDao
import com.roomscanner.app.data.dao.ScanDao
import com.roomscanner.app.data.entity.NoteEntity
import com.roomscanner.app.data.entity.ScanEntity

/**
 * Room Database for offline storage of scan data and job notes.
 * 
 * Features:
 * - Offline-first architecture
 * - Automatic backup configuration
 * - Support for both Coroutines and RxJava
 */
@Database(
    entities = [ScanEntity::class, NoteEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun scanDao(): ScanDao
    abstract fun noteDao(): NoteDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "room_scanner.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
