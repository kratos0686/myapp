package com.roomscanner.app.data.dao

import androidx.room.*
import com.roomscanner.app.data.entity.ScanEntity
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Scan entities.
 * Provides methods for CRUD operations with RxJava and Coroutines support.
 */
@Dao
interface ScanDao {
    
    // Coroutines-based operations
    @Query("SELECT * FROM scans ORDER BY scanDate DESC")
    fun getAllScansFlow(): Flow<List<ScanEntity>>
    
    @Query("SELECT * FROM scans WHERE id = :scanId")
    suspend fun getScanById(scanId: Long): ScanEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity): Long
    
    @Update
    suspend fun updateScan(scan: ScanEntity)
    
    @Delete
    suspend fun deleteScan(scan: ScanEntity)
    
    @Query("DELETE FROM scans WHERE id = :scanId")
    suspend fun deleteScanById(scanId: Long)
    
    // RxJava-based operations
    @Query("SELECT * FROM scans ORDER BY scanDate DESC")
    fun getAllScansRx(): Flowable<List<ScanEntity>>
    
    @Query("SELECT * FROM scans WHERE id = :scanId")
    fun getScanByIdRx(scanId: Long): Single<ScanEntity>
    
    @Query("SELECT * FROM scans WHERE isSynced = 0")
    fun getUnsyncedScansRx(): Single<List<ScanEntity>>
    
    // Utility queries
    @Query("SELECT COUNT(*) FROM scans")
    suspend fun getScansCount(): Int
    
    @Query("UPDATE scans SET isSynced = 1, firebaseId = :firebaseId WHERE id = :scanId")
    suspend fun markScanAsSynced(scanId: Long, firebaseId: String)
}
