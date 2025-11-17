package com.roomscanner.app.data.dao

import androidx.room.*
import com.roomscanner.app.data.entity.NoteEntity
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Note entities.
 * Provides methods for CRUD operations with RxJava and Coroutines support.
 */
@Dao
interface NoteDao {
    
    // Coroutines-based operations
    @Query("SELECT * FROM notes WHERE scanId = :scanId ORDER BY createdDate DESC")
    fun getNotesForScanFlow(scanId: Long): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): NoteEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    @Query("DELETE FROM notes WHERE scanId = :scanId")
    suspend fun deleteNotesForScan(scanId: Long)
    
    // RxJava-based operations
    @Query("SELECT * FROM notes WHERE scanId = :scanId ORDER BY createdDate DESC")
    fun getNotesForScanRx(scanId: Long): Flowable<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE category = :category")
    fun getNotesByCategoryRx(category: String): Single<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE isSynced = 0")
    fun getUnsyncedNotesRx(): Single<List<NoteEntity>>
    
    // Utility queries
    @Query("SELECT COUNT(*) FROM notes WHERE scanId = :scanId")
    suspend fun getNotesCountForScan(scanId: Long): Int
    
    @Query("UPDATE notes SET isSynced = 1, firebaseId = :firebaseId WHERE id = :noteId")
    suspend fun markNoteAsSynced(noteId: Long, firebaseId: String)
}
