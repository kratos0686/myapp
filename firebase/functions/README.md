# Firebase Cloud Functions

This directory contains the Cloud Functions for the Room Scanner app.

## Setup

1. Install Firebase CLI:
```bash
npm install -g firebase-tools
```

2. Login to Firebase:
```bash
firebase login
```

3. Initialize project:
```bash
firebase init functions
```

4. Install dependencies:
```bash
cd functions
npm install
```

## Available Functions

### HTTP Callable Functions

- `processScanData` - Calculate room metrics from scan data
- `generateReport` - Generate PDF reports for scans
- `runMLInference` - Run ML inference on images
- `batchProcessScans` - Process multiple scans at once
- `estimateCosts` - Estimate renovation/repair costs

### Firestore Triggers

- `onScanCreated` - Auto-process scans when created

### Scheduled Functions

- `cleanupOldScans` - Remove scans older than 90 days (runs daily)

## Development

### Run locally with emulators:
```bash
npm run serve
```

### Test functions:
```bash
npm run shell
```

### View logs:
```bash
npm run logs
```

## Deployment

Deploy all functions:
```bash
npm run deploy
```

Deploy specific function:
```bash
firebase deploy --only functions:processScanData
```

## Usage from Android

```kotlin
val functions = Firebase.functions

// Call function
functions
    .getHttpsCallable("processScanData")
    .call(hashMapOf("scanId" to scanId))
    .await()
```

See the [Firebase Setup Guide](../docs/FIREBASE_SETUP.md) for more details.
