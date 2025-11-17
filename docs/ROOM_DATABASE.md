# Room Database Integration Guide

## Overview
This guide shows how to use Room Database for offline storage of scan data and job notes in the Room Scanner app.

## Architecture

```
┌─────────────────┐
│   UI Layer      │
│  (Compose)      │
└────────┬────────┘
         │
┌────────▼────────┐
│   Repository    │
│    Layer        │
└────────┬────────┘
         │
┌────────▼────────┐
│   Room DAO      │
└────────┬────────┘
         │
┌────────▼────────┐
│ Room Database   │
│  (SQLite)       │
└─────────────────┘
```

## Key Components

### 1. Entities

#### ScanEntity
Represents a room scan with 3D data and measurements:

```kotlin
@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomName: String,
    val scanDate: Long,
    val length: Float,
    val width: Float,
    val height: Float,
    val pointCloudPath: String?,
    val meshDataPath: String?,
    val detectedObjects: String?, // JSON
    val damageAreas: String?, // JSON
    val isSynced: Boolean = false,
    val firebaseId: String?
)
```

#### NoteEntity
Represents notes/annotations for scans:

```kotlin
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = ScanEntity::class,
            parentColumns = ["id"],
            childColumns = ["scanId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scanId: Long,
    val title: String,
    val content: String,
    val category: String = "general",
    val positionX: Float?,
    val positionY: Float?,
    val positionZ: Float?
)
```

### 2. DAOs (Data Access Objects)

DAOs provide both Coroutines and RxJava APIs:

```kotlin
@Dao
interface ScanDao {
    // Coroutines
    @Query("SELECT * FROM scans ORDER BY scanDate DESC")
    fun getAllScansFlow(): Flow<List<ScanEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity): Long
    
    // RxJava
    @Query("SELECT * FROM scans ORDER BY scanDate DESC")
    fun getAllScansRx(): Flowable<List<ScanEntity>>
}
```

### 3. Database

```kotlin
@Database(
    entities = [ScanEntity::class, NoteEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun noteDao(): NoteDao
}
```

## Usage Examples

### Using Coroutines

```kotlin
class ScanViewModel(private val repository: ScanRepository) : ViewModel() {
    
    val allScans: Flow<List<ScanEntity>> = repository.getAllScans()
    
    fun saveScan(scan: ScanEntity) {
        viewModelScope.launch {
            repository.insertScan(scan)
        }
    }
    
    fun addNote(scanId: Long, title: String, content: String) {
        viewModelScope.launch {
            val note = NoteEntity(
                scanId = scanId,
                title = title,
                content = content
            )
            repository.insertNote(note)
        }
    }
}
```

### Using RxJava

```kotlin
class ScanService(private val repository: ScanRepository) {
    
    fun observeScans(): Flowable<List<ScanEntity>> {
        return repository.getAllScansRx()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
    
    fun syncUnsyncedData(): Single<Boolean> {
        return repository.getUnsyncedScansRx()
            .flatMapCompletable { scans ->
                Completable.merge(
                    scans.map { scan ->
                        uploadToFirebase(scan)
                    }
                )
            }
            .toSingleDefault(true)
            .onErrorReturnItem(false)
    }
}
```

## Best Practices

1. **Use transactions for related operations**:
```kotlin
@Transaction
suspend fun deleteScanWithNotes(scanId: Long) {
    deleteNotesForScan(scanId)
    deleteScanById(scanId)
}
```

2. **Store large files as paths, not BLOBs**:
   - Store point cloud data in files
   - Save file paths in the database
   - This keeps the database size manageable

3. **Use indices for foreign keys**:
```kotlin
@Entity(indices = [Index("scanId")])
```

4. **Implement proper migration strategy**:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE scans ADD COLUMN notes TEXT")
    }
}
```

## Testing

### Sample Unit Test

```kotlin
@RunWith(AndroidJUnit4::class)
class ScanDaoTest {
    
    private lateinit var database: AppDatabase
    private lateinit var scanDao: ScanDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        scanDao = database.scanDao()
    }
    
    @Test
    fun insertAndRetrieveScan() = runBlocking {
        val scan = ScanEntity(
            roomName = "Living Room",
            length = 5.0f,
            width = 4.0f,
            height = 2.5f
        )
        
        val id = scanDao.insertScan(scan)
        val retrieved = scanDao.getScanById(id)
        
        assertEquals(scan.roomName, retrieved?.roomName)
    }
    
    @After
    fun teardown() {
        database.close()
    }
}
```

## Troubleshooting

### Common Issues

1. **"Cannot access database on the main thread"**
   - Use coroutines or RxJava for database operations
   - Or allow main thread queries (not recommended for production):
   ```kotlin
   .allowMainThreadQueries()
   ```

2. **Foreign key constraint failures**
   - Ensure parent entity exists before inserting child
   - Use `@ForeignKey(onDelete = CASCADE)` for automatic cleanup

3. **Migration errors**
   - Either implement proper migrations
   - Or use `.fallbackToDestructiveMigration()` for development

## See Also

- [ARCore Integration Guide](ARCORE_INTEGRATION.md)
- [Firebase Sync Guide](FIREBASE_SETUP.md)
- [ML Kit Integration](ML_INTEGRATION.md)
