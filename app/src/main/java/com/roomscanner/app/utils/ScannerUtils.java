package com.roomscanner.app.utils;

import com.roomscanner.app.analysis.CostEstimator;
import com.roomscanner.app.analysis.MaterialAnalyzer;
import com.roomscanner.app.ar.ARVisualizationManager;
import com.roomscanner.app.cad.CADBIMIntegration;
import com.roomscanner.app.cloud.CloudSyncManager;
import com.roomscanner.app.export.OBJExporter;
import com.roomscanner.app.export.PLYExporter;
import com.roomscanner.app.models.RoomScan;
import com.roomscanner.app.scanning.MultiRoomScanner;

import java.io.File;

/**
 * Utility class providing convenient access to all scanner features
 */
public class ScannerUtils {
    
    /**
     * Export a scan to multiple formats
     * @param scan The scan to export
     * @param outputDirectory Directory to save exported files
     * @param exportOBJ Export to OBJ format
     * @param exportPLY Export to PLY format
     * @param exportDXF Export to DXF format (CAD)
     * @param exportIFC Export to IFC format (BIM)
     * @return true if all requested exports succeeded
     */
    public static boolean exportScanMultiFormat(RoomScan scan, File outputDirectory,
                                               boolean exportOBJ, boolean exportPLY,
                                               boolean exportDXF, boolean exportIFC) {
        boolean success = true;
        String baseName = scan.getName().replaceAll("[^a-zA-Z0-9]", "_");
        
        if (exportOBJ) {
            File objFile = new File(outputDirectory, baseName + ".obj");
            success = success && OBJExporter.exportToOBJ(scan, objFile);
        }
        
        if (exportPLY) {
            File plyFile = new File(outputDirectory, baseName + ".ply");
            success = success && PLYExporter.exportToPLY(scan, plyFile);
        }
        
        if (exportDXF) {
            File dxfFile = new File(outputDirectory, baseName + ".dxf");
            success = success && CADBIMIntegration.exportToDXF(scan, dxfFile);
        }
        
        if (exportIFC) {
            File ifcFile = new File(outputDirectory, baseName + ".ifc");
            success = success && CADBIMIntegration.exportToIFC(scan, ifcFile);
        }
        
        return success;
    }
    
    /**
     * Process a scan with full analysis pipeline
     * @param scan The scan to process
     * @return Comprehensive analysis report
     */
    public static String processFullAnalysis(RoomScan scan) {
        StringBuilder report = new StringBuilder();
        
        // Material analysis
        MaterialAnalyzer materialAnalyzer = new MaterialAnalyzer();
        MaterialAnalyzer.MaterialType material = materialAnalyzer.analyzeMaterial(scan);
        double surfaceArea = materialAnalyzer.calculateSurfaceArea(scan);
        String[] damages = materialAnalyzer.detectSurfaceDamage(scan);
        
        // Cost estimation
        CostEstimator costEstimator = new CostEstimator();
        double totalCost = costEstimator.estimateRenovationCost(scan);
        
        // Build report
        report.append("=== ROOM SCAN ANALYSIS REPORT ===\n\n");
        report.append("Scan ID: ").append(scan.getId()).append("\n");
        report.append("Room Name: ").append(scan.getName()).append("\n");
        report.append("Scan Date: ").append(scan.getScanDate()).append("\n");
        report.append("Point Count: ").append(scan.getPointCount()).append("\n\n");
        
        report.append("--- Material Analysis ---\n");
        report.append("Material Type: ").append(material.getDisplayName()).append("\n");
        report.append("Surface Area: ").append(String.format("%.2f sq m", surfaceArea)).append("\n");
        report.append("Detected Damage: ").append(damages.length).append(" issues\n\n");
        
        report.append("--- Cost Estimation ---\n");
        report.append(costEstimator.getCostBreakdown(scan)).append("\n");
        
        return report.toString();
    }
    
    /**
     * Sync scan to cloud and export locally
     * @param scan The scan to process
     * @param exportDir Directory for local exports
     * @return true if all operations succeeded
     */
    public static boolean syncAndExport(RoomScan scan, File exportDir) {
        // Upload to cloud
        CloudSyncManager cloudSync = CloudSyncManager.getInstance();
        boolean cloudSuccess = cloudSync.uploadScan(scan);
        
        // Export locally
        boolean exportSuccess = exportScanMultiFormat(scan, exportDir, 
                                                      true, true, false, false);
        
        return cloudSuccess && exportSuccess;
    }
}
