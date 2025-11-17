const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Cloud Function to process scan data
 * Calculates area, volume, and other metrics
 */
exports.processScanData = functions.https.onCall(async (data, context) => {
  // Verify authentication
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'User must be authenticated to process scans'
    );
  }

  const scanId = data.scanId;
  
  try {
    // Fetch scan from Firestore
    const scanDoc = await admin.firestore()
      .collection('scans')
      .doc(scanId)
      .get();
    
    if (!scanDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Scan not found');
    }
    
    const scanData = scanDoc.data();
    
    // Calculate metrics
    const floorArea = scanData.length * scanData.width;
    const volume = floorArea * scanData.height;
    const wallArea = 2 * (scanData.length * scanData.height + scanData.width * scanData.height);
    const totalSurfaceArea = (2 * floorArea) + wallArea; // floor + ceiling + walls
    
    // Update scan with processed data
    await scanDoc.ref.update({
      floorArea: floorArea,
      volume: volume,
      wallArea: wallArea,
      totalSurfaceArea: totalSurfaceArea,
      processedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    return {
      success: true,
      metrics: {
        floorArea,
        volume,
        wallArea,
        totalSurfaceArea
      }
    };
  } catch (error) {
    console.error('Error processing scan:', error);
    throw new functions.https.HttpsError('internal', error.message);
  }
});

/**
 * Generate PDF report for a scan
 */
exports.generateReport = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Not authenticated');
  }
  
  const scanId = data.scanId;
  const format = data.format || 'pdf';
  
  try {
    const scanDoc = await admin.firestore()
      .collection('scans')
      .doc(scanId)
      .get();
    
    if (!scanDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Scan not found');
    }
    
    const scanData = scanDoc.data();
    
    // TODO: Implement actual PDF generation
    // This would typically use libraries like PDFKit, puppeteer, or similar
    // For now, return a placeholder URL
    
    const reportUrl = `https://storage.googleapis.com/${process.env.GCLOUD_PROJECT}/reports/${scanId}.pdf`;
    
    return {
      success: true,
      url: reportUrl,
      generatedAt: new Date().toISOString()
    };
  } catch (error) {
    console.error('Error generating report:', error);
    throw new functions.https.HttpsError('internal', error.message);
  }
});

/**
 * Run ML inference on images
 */
exports.runMLInference = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Not authenticated');
  }
  
  const imageUrl = data.imageUrl;
  const modelName = data.modelName;
  
  try {
    // TODO: Implement ML inference
    // This could use Google Cloud Vision API, AutoML, or custom models
    
    // Placeholder response
    return {
      success: true,
      predictions: [
        { label: 'wall', confidence: 0.95 },
        { label: 'floor', confidence: 0.88 }
      ],
      modelUsed: modelName
    };
  } catch (error) {
    console.error('Error running inference:', error);
    throw new functions.https.HttpsError('internal', error.message);
  }
});

/**
 * Batch process multiple scans
 */
exports.batchProcessScans = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Not authenticated');
  }
  
  const scanIds = data.scanIds;
  
  if (!Array.isArray(scanIds) || scanIds.length === 0) {
    throw new functions.https.HttpsError('invalid-argument', 'scanIds must be a non-empty array');
  }
  
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

/**
 * Internal function to process a single scan
 */
async function processScanInternal(scanId) {
  const scanDoc = await admin.firestore()
    .collection('scans')
    .doc(scanId)
    .get();
  
  if (!scanDoc.exists) {
    throw new Error('Scan not found');
  }
  
  const scanData = scanDoc.data();
  
  const floorArea = scanData.length * scanData.width;
  const volume = floorArea * scanData.height;
  
  await scanDoc.ref.update({
    floorArea,
    volume,
    processedAt: admin.firestore.FieldValue.serverTimestamp()
  });
  
  return { floorArea, volume };
}

/**
 * Estimate costs based on scan data
 */
exports.estimateCosts = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Not authenticated');
  }
  
  const scanId = data.scanId;
  
  try {
    const scanDoc = await admin.firestore()
      .collection('scans')
      .doc(scanId)
      .get();
    
    if (!scanDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Scan not found');
    }
    
    const scanData = scanDoc.data();
    
    // Calculate areas
    const floorArea = scanData.length * scanData.width;
    const wallArea = 2 * (scanData.length * scanData.height + scanData.width * scanData.height);
    
    // Cost estimation (customize these rates)
    const paintCostPerSqFt = 2.5;
    const flooringCostPerSqFt = 5.0;
    const laborCostPerSqFt = 3.0;
    
    const paintCost = wallArea * paintCostPerSqFt;
    const flooringCost = floorArea * flooringCostPerSqFt;
    const laborCost = (wallArea + floorArea) * laborCostPerSqFt;
    
    const totalMaterialCost = paintCost + flooringCost;
    const totalCost = totalMaterialCost + laborCost;
    
    return {
      success: true,
      estimate: {
        materials: {
          paint: paintCost,
          flooring: flooringCost,
          total: totalMaterialCost
        },
        labor: laborCost,
        total: totalCost,
        areas: {
          floor: floorArea,
          walls: wallArea
        }
      }
    };
  } catch (error) {
    console.error('Error estimating costs:', error);
    throw new functions.https.HttpsError('internal', error.message);
  }
});

/**
 * Triggered when a new scan is created
 * Automatically processes the scan
 */
exports.onScanCreated = functions.firestore
  .document('scans/{scanId}')
  .onCreate(async (snap, context) => {
    const scanData = snap.data();
    const scanId = context.params.scanId;
    
    try {
      // Auto-calculate metrics
      const floorArea = scanData.length * scanData.width;
      const volume = floorArea * scanData.height;
      
      await snap.ref.update({
        floorArea,
        volume,
        autoProcessedAt: admin.firestore.FieldValue.serverTimestamp()
      });
      
      console.log(`Auto-processed scan ${scanId}`);
    } catch (error) {
      console.error(`Error auto-processing scan ${scanId}:`, error);
    }
  });

/**
 * Cleanup old scans (triggered daily)
 */
exports.cleanupOldScans = functions.pubsub
  .schedule('every 24 hours')
  .onRun(async (context) => {
    const cutoffDate = new Date();
    cutoffDate.setDate(cutoffDate.getDate() - 90); // 90 days old
    
    const oldScans = await admin.firestore()
      .collection('scans')
      .where('scanDate', '<', cutoffDate.getTime())
      .get();
    
    const batch = admin.firestore().batch();
    oldScans.docs.forEach(doc => {
      batch.delete(doc.ref);
    });
    
    await batch.commit();
    
    console.log(`Cleaned up ${oldScans.size} old scans`);
  });
