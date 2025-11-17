# Firebase Setup Guide

## Overview
This guide walks you through setting up Firebase for the Room Scanner app, including Firestore, Cloud Functions, and Storage.

## Prerequisites
- Android Studio Arctic Fox or later
- Google account
- Firebase project

## Setup Steps

### 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Enter project name: "Room Scanner"
4. Enable Google Analytics (optional)
5. Click "Create project"

### 2. Add Android App to Firebase

1. In Firebase Console, click "Add app" → Android
2. Register app:
   - Package name: `com.roomscanner.app`
   - App nickname: "Room Scanner"
   - Debug signing certificate (optional for testing)
3. Download `google-services.json`
4. Place it in `app/` directory

### 3. Configure gradle files

The project already includes Firebase dependencies. Verify they're in place:

**Project-level `build.gradle.kts`:**
```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

**App-level `build.gradle.kts`:**
```kotlin
plugins {
    id("com.google.gms.google-services")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-functions")
}
```

### 4. Enable Firebase Services

In Firebase Console, enable:

1. **Firestore Database**:
   - Go to Firestore Database
   - Click "Create database"
   - Choose production mode
   - Select location (choose closest to users)

2. **Storage**:
   - Go to Storage
   - Click "Get started"
   - Use default security rules (update for production)

3. **Cloud Functions**:
   - Go to Functions
   - Click "Get started"
   - Follow setup instructions

## Firestore Data Structure

```
scans/
  {scanId}/
    - roomName: string
    - scanDate: timestamp
    - length: number
    - width: number
    - height: number
    - pointCloudPath: string
    - meshDataPath: string
    - detectedObjects: string (JSON)
    - damageAreas: string (JSON)

notes/
  {noteId}/
    - scanId: string (reference to scan)
    - title: string
    - content: string
    - createdDate: timestamp
    - category: string
    - position: GeoPoint (optional)
```

## Security Rules

### Firestore Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Scans collection
    match /scans/{scanId} {
      // Allow authenticated users to read/write their own scans
      allow read, write: if request.auth != null;
    }
    
    // Notes collection
    match /notes/{noteId} {
      // Allow authenticated users to read/write notes
      allow read, write: if request.auth != null;
    }
  }
}
```

### Storage Rules

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    // Scan files
    match /scans/{scanId}/{allPaths=**} {
      allow read, write: if request.auth != null;
    }
    
    // ML Models
    match /models/{modelName} {
      allow read: if request.auth != null;
      allow write: if false; // Only admins can upload models
    }
  }
}
```

## Cloud Functions Setup

### Initialize Cloud Functions

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login to Firebase
firebase login

# Initialize functions
firebase init functions
```

### Sample Cloud Functions

Create `functions/index.js`:

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// Process scan data
exports.processScanData = functions.https.onCall(async (data, context) => {
  // Verify authentication
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'User must be authenticated'
    );
  }
  
  const scanId = data.scanId;
  
  // Fetch scan from Firestore
  const scanDoc = await admin.firestore()
    .collection('scans')
    .doc(scanId)
    .get();
  
  if (!scanDoc.exists) {
    throw new functions.https.HttpsError('not-found', 'Scan not found');
  }
  
  const scanData = scanDoc.data();
  
  // Process the scan (example: calculate area)
  const area = scanData.length * scanData.width;
  const volume = area * scanData.height;
  
  // Update scan with processed data
  await scanDoc.ref.update({
    area: area,
    volume: volume,
    processedAt: admin.firestore.FieldValue.serverTimestamp()
  });
  
  return {
    success: true,
    area: area,
    volume: volume
  };
});

// Generate PDF report
exports.generateReport = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Not authenticated');
  }
  
  const scanId = data.scanId;
  
  // TODO: Implement PDF generation
  // This would typically use a library like PDFKit or puppeteer
  
  return {
    url: `https://example.com/reports/${scanId}.pdf`
  };
});

// Run ML inference on uploaded images
exports.runMLInference = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Not authenticated');
  }
  
  const imageUrl = data.imageUrl;
  const modelName = data.modelName;
  
  // TODO: Implement ML inference
  // This could use Google Cloud AI APIs or custom models
  
  return {
    predictions: [],
    confidence: 0.95
  };
});

// Batch process multiple scans
exports.batchProcessScans = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Not authenticated');
  }
  
  const scanIds = data.scanIds;
  const results = [];
  
  for (const scanId of scanIds) {
    try {
      // Process each scan
      const result = await processScanInternal(scanId);
      results.push({ scanId, success: true, result });
    } catch (error) {
      results.push({ scanId, success: false, error: error.message });
    }
  }
  
  return { results };
});

// Estimate costs based on scan data
exports.estimateCosts = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Not authenticated');
  }
  
  const scanId = data.scanId;
  
  const scanDoc = await admin.firestore()
    .collection('scans')
    .doc(scanId)
    .get();
  
  if (!scanDoc.exists) {
    throw new functions.https.HttpsError('not-found', 'Scan not found');
  }
  
  const scanData = scanDoc.data();
  const area = scanData.length * scanData.width;
  
  // Example cost estimation (customize based on your needs)
  const paintCostPerSqFt = 2.5;
  const laborCostPerSqFt = 3.0;
  
  const materialCost = area * paintCostPerSqFt;
  const laborCost = area * laborCostPerSqFt;
  
  return {
    materialCost: materialCost,
    laborCost: laborCost,
    totalCost: materialCost + laborCost,
    area: area
  };
});
```

### Deploy Cloud Functions

```bash
firebase deploy --only functions
```

## Usage in Android App

### Initialize Firebase

```kotlin
// Firebase is auto-initialized via google-services.json
// Access services directly:

val firestore = Firebase.firestore
val storage = Firebase.storage
val functions = Firebase.functions
```

### Sync Data to Firestore

```kotlin
class FirebaseRepository(
    private val firestoreSync: FirestoreSync,
    private val localRepository: ScanRepository
) {
    
    suspend fun syncScan(scan: ScanEntity) {
        val firebaseId = firestoreSync.uploadScan(scan)
        if (firebaseId != null) {
            localRepository.markScanAsSynced(scan.id, firebaseId)
        }
    }
}
```

### Call Cloud Functions

```kotlin
val cloudFunctions = CloudFunctionsClient()

// Process scan
lifecycleScope.launch {
    val result = cloudFunctions.processScanData(scanId)
    result?.let {
        // Handle processed data
        val area = it["area"] as? Double
        val volume = it["volume"] as? Double
    }
}
```

### Upload to Storage

```kotlin
val storageManager = StorageManager()

lifecycleScope.launch {
    val pointCloudFile = File("/path/to/pointcloud.bin")
    val url = storageManager.uploadPointCloud(scanId, pointCloudFile)
    
    if (url != null) {
        // Update scan with storage URL
        scan.copy(pointCloudPath = url)
    }
}
```

## Testing

### Use Firebase Emulator Suite

```bash
# Install emulators
firebase init emulators

# Start emulators
firebase emulators:start
```

In your app, connect to emulators:

```kotlin
if (BuildConfig.DEBUG) {
    Firebase.firestore.useEmulator("10.0.2.2", 8080)
    Firebase.functions.useEmulator("10.0.2.2", 5001)
    Firebase.storage.useEmulator("10.0.2.2", 9199)
}
```

## Troubleshooting

### Common Issues

1. **"Default FirebaseApp is not initialized"**
   - Ensure `google-services.json` is in the `app/` directory
   - Check that `com.google.gms.google-services` plugin is applied

2. **"Permission denied" errors**
   - Update Firestore/Storage security rules
   - Ensure user is authenticated

3. **Cloud Functions timeout**
   - Increase function timeout in Firebase Console
   - Optimize function code for performance

## See Also

- [Cloud Functions Documentation](CLOUD_FUNCTIONS.md)
- [Storage Integration](STORAGE_INTEGRATION.md)
- [Room Database Sync](ROOM_DATABASE.md)
