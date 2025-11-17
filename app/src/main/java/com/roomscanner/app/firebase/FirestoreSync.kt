package com.roomscanner.app.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.roomscanner.app.data.entity.NoteEntity
import com.roomscanner.app.data.entity.ScanEntity
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore integration for cloud sync of scan data.
 * Provides methods to sync local Room database with cloud storage.
 */
class FirestoreSync {
    
    private val db: FirebaseFirestore = Firebase.firestore
    
    companion object {
        private const val SCANS_COLLECTION = "scans"
        private const val NOTES_COLLECTION = "notes"
    }
    
    /**
     * Upload a scan to Firestore.
     * @return The Firestore document ID if successful, null otherwise
     */
    suspend fun uploadScan(scan: ScanEntity): String? {
        return try {
            val scanData = hashMapOf(
                "roomName" to scan.roomName,
                "scanDate" to scan.scanDate,
                "length" to scan.length,
                "width" to scan.width,
                "height" to scan.height,
                "pointCloudPath" to scan.pointCloudPath,
                "meshDataPath" to scan.meshDataPath,
                "detectedObjects" to scan.detectedObjects,
                "damageAreas" to scan.damageAreas
            )
            
            val docRef = if (scan.firebaseId != null) {
                // Update existing document
                db.collection(SCANS_COLLECTION)
                    .document(scan.firebaseId)
                    .set(scanData)
                    .await()
                scan.firebaseId
            } else {
                // Create new document
                val newDocRef = db.collection(SCANS_COLLECTION)
                    .add(scanData)
                    .await()
                newDocRef.id
            }
            
            docRef
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Upload a note to Firestore.
     * @return The Firestore document ID if successful, null otherwise
     */
    suspend fun uploadNote(note: NoteEntity, scanFirebaseId: String): String? {
        return try {
            val noteData = hashMapOf(
                "scanId" to scanFirebaseId,
                "title" to note.title,
                "content" to note.content,
                "createdDate" to note.createdDate,
                "modifiedDate" to note.modifiedDate,
                "positionX" to note.positionX,
                "positionY" to note.positionY,
                "positionZ" to note.positionZ,
                "category" to note.category
            )
            
            val docRef = if (note.firebaseId != null) {
                db.collection(NOTES_COLLECTION)
                    .document(note.firebaseId)
                    .set(noteData)
                    .await()
                note.firebaseId
            } else {
                val newDocRef = db.collection(NOTES_COLLECTION)
                    .add(noteData)
                    .await()
                newDocRef.id
            }
            
            docRef
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Download all scans from Firestore.
     */
    suspend fun downloadScans(): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection(SCANS_COLLECTION)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Download notes for a specific scan.
     */
    suspend fun downloadNotesForScan(scanFirebaseId: String): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection(NOTES_COLLECTION)
                .whereEqualTo("scanId", scanFirebaseId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Delete a scan from Firestore.
     */
    suspend fun deleteScan(firebaseId: String): Boolean {
        return try {
            db.collection(SCANS_COLLECTION)
                .document(firebaseId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
