package com.roomscanner.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.roomscanner.app.data.RoomScan
import com.roomscanner.app.data.JobNote
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Firebase service for syncing scan data and job notes to the cloud.
 * Integrates with Room Database for offline-first architecture.
 */
class FirebaseSyncService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    companion object {
        private const val ROOM_SCANS_COLLECTION = "room_scans"
        private const val JOB_NOTES_COLLECTION = "job_notes"
        private const val SCAN_IMAGES_PATH = "scan_images"
    }

    /**
     * Upload room scan to Firebase Firestore.
     */
    fun uploadRoomScan(scan: RoomScan): Single<String> {
        return Single.create<String> { emitter ->
            val scanData = hashMapOf(
                "roomName" to scan.roomName,
                "scanDate" to scan.scanDate,
                "width" to scan.width,
                "length" to scan.length,
                "height" to scan.height,
                "area" to scan.area,
                "volume" to scan.volume,
                "pointCloudData" to scan.pointCloudData,
                "damagedAreas" to scan.damagedAreas.map { area ->
                    mapOf(
                        "type" to area.type,
                        "location" to area.location,
                        "severity" to area.severity,
                        "boundingBox" to mapOf(
                            "left" to area.boundingBox.left,
                            "top" to area.boundingBox.top,
                            "right" to area.boundingBox.right,
                            "bottom" to area.boundingBox.bottom
                        ),
                        "imageUri" to area.imageUri
                    )
                },
                "materialEstimates" to scan.materialEstimates.map { estimate ->
                    mapOf(
                        "surfaceType" to estimate.surfaceType,
                        "materialType" to estimate.materialType,
                        "confidence" to estimate.confidence,
                        "coverage" to estimate.coverage
                    )
                }
            )

            firestore.collection(ROOM_SCANS_COLLECTION)
                .add(scanData)
                .addOnSuccessListener { documentReference ->
                    emitter.onSuccess(documentReference.id)
                }
                .addOnFailureListener { e ->
                    emitter.onError(e)
                }
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Upload job note to Firebase Firestore.
     */
    fun uploadJobNote(note: JobNote): Single<String> {
        return Single.create<String> { emitter ->
            val noteData = hashMapOf(
                "roomScanId" to note.roomScanId,
                "title" to note.title,
                "content" to note.content,
                "createdDate" to note.createdDate,
                "updatedDate" to note.updatedDate,
                "tags" to note.tags,
                "attachmentUris" to note.attachmentUris
            )

            firestore.collection(JOB_NOTES_COLLECTION)
                .add(noteData)
                .addOnSuccessListener { documentReference ->
                    emitter.onSuccess(documentReference.id)
                }
                .addOnFailureListener { e ->
                    emitter.onError(e)
                }
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Upload image file to Firebase Storage.
     */
    fun uploadImage(localFilePath: String, remotePath: String): Single<String> {
        return Single.create<String> { emitter ->
            val file = File(localFilePath)
            if (!file.exists()) {
                emitter.onError(Exception("File not found: $localFilePath"))
                return@create
            }

            val storageRef = storage.reference.child("$SCAN_IMAGES_PATH/$remotePath")
            
            storageRef.putFile(android.net.Uri.fromFile(file))
                .addOnSuccessListener {
                    storageRef.downloadUrl
                        .addOnSuccessListener { uri ->
                            emitter.onSuccess(uri.toString())
                        }
                        .addOnFailureListener { e ->
                            emitter.onError(e)
                        }
                }
                .addOnFailureListener { e ->
                    emitter.onError(e)
                }
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Download room scan from Firebase Firestore.
     */
    suspend fun downloadRoomScan(documentId: String): RoomScan? {
        return try {
            val document = firestore.collection(ROOM_SCANS_COLLECTION)
                .document(documentId)
                .get()
                .await()
            
            // Convert Firestore document to RoomScan
            // Note: This is simplified - add proper conversion logic
            document.toObject(RoomScan::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Sync all unsynced scans to Firebase.
     */
    fun syncUnsyncedScans(scans: List<RoomScan>): Completable {
        return Completable.create { emitter ->
            val uploadTasks = scans.map { scan ->
                uploadRoomScan(scan)
            }
            
            Single.merge(uploadTasks)
                .toList()
                .subscribe(
                    { 
                        emitter.onComplete()
                    },
                    { error ->
                        emitter.onError(error)
                    }
                )
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Sync all unsynced notes to Firebase.
     */
    fun syncUnsyncedNotes(notes: List<JobNote>): Completable {
        return Completable.create { emitter ->
            val uploadTasks = notes.map { note ->
                uploadJobNote(note)
            }
            
            Single.merge(uploadTasks)
                .toList()
                .subscribe(
                    { 
                        emitter.onComplete()
                    },
                    { error ->
                        emitter.onError(error)
                    }
                )
        }.subscribeOn(Schedulers.io())
    }
}
