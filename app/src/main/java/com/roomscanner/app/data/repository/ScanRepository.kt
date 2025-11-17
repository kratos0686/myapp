package com.roomscanner.app.data.repository

import com.roomscanner.app.data.dao.ScanDao
import com.roomscanner.app.data.dao.NoteDao
import com.roomscanner.app.data.entity.ScanEntity
import com.roomscanner.app.data.entity.NoteEntity
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing scan and note data.
 * Provides a clean API for the UI layer and handles data source coordination.
 */
class ScanRepository(
    private val scanDao: ScanDao,
    private val noteDao: NoteDao
) {
    
    // Scan operations
    fun getAllScans(): Flow<List<ScanEntity>> = scanDao.getAllScansFlow()
    
    fun getAllScansRx(): Flowable<List<ScanEntity>> = scanDao.getAllScansRx()
    
    suspend fun getScanById(scanId: Long): ScanEntity? = scanDao.getScanById(scanId)
    
    suspend fun insertScan(scan: ScanEntity): Long = scanDao.insertScan(scan)
    
    suspend fun updateScan(scan: ScanEntity) = scanDao.updateScan(scan)
    
    suspend fun deleteScan(scan: ScanEntity) = scanDao.deleteScan(scan)
    
    suspend fun getScansCount(): Int = scanDao.getScansCount()
    
    // Note operations
    fun getNotesForScan(scanId: Long): Flow<List<NoteEntity>> = 
        noteDao.getNotesForScanFlow(scanId)
    
    fun getNotesForScanRx(scanId: Long): Flowable<List<NoteEntity>> = 
        noteDao.getNotesForScanRx(scanId)
    
    suspend fun getNoteById(noteId: Long): NoteEntity? = noteDao.getNoteById(noteId)
    
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    
    // Sync operations
    suspend fun markScanAsSynced(scanId: Long, firebaseId: String) =
        scanDao.markScanAsSynced(scanId, firebaseId)
    
    suspend fun markNoteAsSynced(noteId: Long, firebaseId: String) =
        noteDao.markNoteAsSynced(noteId, firebaseId)
    
    fun getUnsyncedScansRx(): Single<List<ScanEntity>> = scanDao.getUnsyncedScansRx()
    
    fun getUnsyncedNotesRx(): Single<List<NoteEntity>> = noteDao.getUnsyncedNotesRx()
}
