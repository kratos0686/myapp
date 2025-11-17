package com.roomscanner.app.data

import com.roomscanner.app.arcore.RoomDimensions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository layer for RoomScanner app with RxJava integration.
 * Handles data flow between Room Database, Firebase, and UI layers.
 */
class RoomScanRepository(
    private val roomScanDao: RoomScanDao,
    private val jobNoteDao: JobNoteDao,
    private val firebaseSyncService: FirebaseSyncService
) {

    // ========== Room Scan Operations ==========

    /**
     * Get all room scans as Flow for reactive UI updates.
     */
    fun getAllScans(): Flow<List<RoomScan>> {
        return roomScanDao.getAllScansFlow()
    }

    /**
     * Get room scan by ID as Flow.
     */
    fun getScanById(scanId: Long): Flow<RoomScan?> {
        return roomScanDao.getScanByIdFlow(scanId)
    }

    /**
     * Create a new room scan from AR data.
     */
    fun createRoomScan(
        roomName: String,
        dimensions: RoomDimensions,
        pointCloudData: String?,
        damagedAreas: List<DamagedArea>,
        materialEstimates: List<MaterialEstimate>
    ): Single<Long> {
        val scan = RoomScan(
            roomName = roomName,
            scanDate = Date(),
            width = dimensions.width,
            length = dimensions.length,
            height = dimensions.height,
            area = dimensions.area,
            volume = dimensions.volume,
            pointCloudData = pointCloudData,
            damagedAreas = damagedAreas,
            materialEstimates = materialEstimates,
            syncedToFirebase = false
        )
        
        return roomScanDao.insertScan(scan)
            .subscribeOn(Schedulers.io())
    }

    /**
     * Update existing room scan.
     */
    fun updateScan(scan: RoomScan): Completable {
        return roomScanDao.updateScan(scan)
            .subscribeOn(Schedulers.io())
    }

    /**
     * Delete room scan.
     */
    fun deleteScan(scan: RoomScan): Completable {
        return roomScanDao.deleteScan(scan)
            .subscribeOn(Schedulers.io())
    }

    /**
     * Sync unsynced scans to Firebase.
     */
    fun syncScansToFirebase(): Completable {
        return roomScanDao.getUnsyncedScansRx()
            .flatMapCompletable { scans ->
                if (scans.isEmpty()) {
                    Completable.complete()
                } else {
                    firebaseSyncService.syncUnsyncedScans(scans)
                        .andThen(
                            Completable.merge(
                                scans.map { scan ->
                                    firebaseSyncService.uploadRoomScan(scan)
                                        .flatMapCompletable { docId ->
                                            roomScanDao.markAsSynced(scan.id, docId)
                                        }
                                }
                            )
                        )
                }
            }
            .subscribeOn(Schedulers.io())
    }

    // ========== Job Note Operations ==========

    /**
     * Get all notes for a specific scan as Flow.
     */
    fun getNotesForScan(scanId: Long): Flow<List<JobNote>> {
        return jobNoteDao.getNotesForScanFlow(scanId)
    }

    /**
     * Create a new job note.
     */
    fun createJobNote(
        roomScanId: Long,
        title: String,
        content: String,
        tags: List<String>,
        attachmentUris: List<String>
    ): Single<Long> {
        val now = Date()
        val note = JobNote(
            roomScanId = roomScanId,
            title = title,
            content = content,
            createdDate = now,
            updatedDate = now,
            tags = tags,
            attachmentUris = attachmentUris,
            syncedToFirebase = false
        )
        
        return jobNoteDao.insertNote(note)
            .subscribeOn(Schedulers.io())
    }

    /**
     * Update existing job note.
     */
    fun updateJobNote(note: JobNote): Completable {
        val updatedNote = note.copy(updatedDate = Date())
        return jobNoteDao.updateNote(updatedNote)
            .subscribeOn(Schedulers.io())
    }

    /**
     * Delete job note.
     */
    fun deleteJobNote(note: JobNote): Completable {
        return jobNoteDao.deleteNote(note)
            .subscribeOn(Schedulers.io())
    }

    /**
     * Sync unsynced notes to Firebase.
     */
    fun syncNotesToFirebase(): Completable {
        return jobNoteDao.getUnsyncedNotesRx()
            .flatMapCompletable { notes ->
                if (notes.isEmpty()) {
                    Completable.complete()
                } else {
                    firebaseSyncService.syncUnsyncedNotes(notes)
                        .andThen(
                            Completable.merge(
                                notes.map { note ->
                                    firebaseSyncService.uploadJobNote(note)
                                        .flatMapCompletable { _ ->
                                            jobNoteDao.markAsSynced(note.id)
                                        }
                                }
                            )
                        )
                }
            }
            .subscribeOn(Schedulers.io())
    }

    /**
     * Get count of scans.
     */
    fun getScanCount(): Flow<Int> {
        return roomScanDao.getScanCount()
    }

    /**
     * Get count of notes for a scan.
     */
    fun getNoteCountForScan(scanId: Long): Flow<Int> {
        return jobNoteDao.getNoteCountForScan(scanId)
    }
}
