package com.roomscanner.app.data

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

/**
 * DAO for RoomScan operations with RxJava and Flow support.
 */
@Dao
interface RoomScanDao {
    // Flow-based queries for reactive UI
    @Query("SELECT * FROM room_scans ORDER BY scanDate DESC")
    fun getAllScansFlow(): Flow<List<RoomScan>>

    @Query("SELECT * FROM room_scans WHERE id = :scanId")
    fun getScanByIdFlow(scanId: Long): Flow<RoomScan?>

    @Query("SELECT * FROM room_scans WHERE syncedToFirebase = 0")
    fun getUnsyncedScansFlow(): Flow<List<RoomScan>>

    // RxJava queries for data operations
    @Query("SELECT * FROM room_scans ORDER BY scanDate DESC")
    fun getAllScansRx(): Flowable<List<RoomScan>>

    @Query("SELECT * FROM room_scans WHERE id = :scanId")
    fun getScanByIdRx(scanId: Long): Single<RoomScan>

    @Query("SELECT * FROM room_scans WHERE syncedToFirebase = 0")
    fun getUnsyncedScansRx(): Single<List<RoomScan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertScan(scan: RoomScan): Single<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanSuspend(scan: RoomScan): Long

    @Update
    fun updateScan(scan: RoomScan): Completable

    @Update
    suspend fun updateScanSuspend(scan: RoomScan)

    @Delete
    fun deleteScan(scan: RoomScan): Completable

    @Query("DELETE FROM room_scans WHERE id = :scanId")
    suspend fun deleteScanById(scanId: Long)

    @Query("UPDATE room_scans SET syncedToFirebase = 1, firebaseDocId = :docId WHERE id = :scanId")
    fun markAsSynced(scanId: Long, docId: String): Completable

    @Query("SELECT COUNT(*) FROM room_scans")
    fun getScanCount(): Flow<Int>
}

/**
 * DAO for JobNote operations with RxJava and Flow support.
 */
@Dao
interface JobNoteDao {
    // Flow-based queries for reactive UI
    @Query("SELECT * FROM job_notes WHERE roomScanId = :scanId ORDER BY updatedDate DESC")
    fun getNotesForScanFlow(scanId: Long): Flow<List<JobNote>>

    @Query("SELECT * FROM job_notes WHERE id = :noteId")
    fun getNoteByIdFlow(noteId: Long): Flow<JobNote?>

    @Query("SELECT * FROM job_notes WHERE syncedToFirebase = 0")
    fun getUnsyncedNotesFlow(): Flow<List<JobNote>>

    // RxJava queries for data operations
    @Query("SELECT * FROM job_notes WHERE roomScanId = :scanId ORDER BY updatedDate DESC")
    fun getNotesForScanRx(scanId: Long): Flowable<List<JobNote>>

    @Query("SELECT * FROM job_notes WHERE syncedToFirebase = 0")
    fun getUnsyncedNotesRx(): Single<List<JobNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: JobNote): Single<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteSuspend(note: JobNote): Long

    @Update
    fun updateNote(note: JobNote): Completable

    @Update
    suspend fun updateNoteSuspend(note: JobNote)

    @Delete
    fun deleteNote(note: JobNote): Completable

    @Query("DELETE FROM job_notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Long)

    @Query("UPDATE job_notes SET syncedToFirebase = 1 WHERE id = :noteId")
    fun markAsSynced(noteId: Long): Completable

    @Query("SELECT COUNT(*) FROM job_notes WHERE roomScanId = :scanId")
    fun getNoteCountForScan(scanId: Long): Flow<Int>
}
