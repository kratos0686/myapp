package com.roomscanner.app.cad;

import com.roomscanner.app.models.Point3D;
import com.roomscanner.app.models.RoomScan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Integrates with CAD/BIM systems by exporting to industry-standard formats
 * Supports DXF (AutoCAD) and IFC (BIM) format exports
 */
public class CADBIMIntegration {
    
    /**
     * Export room scan to DXF format (AutoCAD)
     * @param scan The room scan to export
     * @param outputFile The output file path
     * @return true if export successful
     */
    public static boolean exportToDXF(RoomScan scan, File outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            // Write DXF header
            writer.write("0\nSECTION\n2\nHEADER\n");
            writer.write("9\n$ACADVER\n1\nAC1015\n"); // AutoCAD 2000 format
            writer.write("0\nENDSEC\n");
            
            // Write entities section
            writer.write("0\nSECTION\n2\nENTITIES\n");
            
            List<Point3D> points = scan.getPointCloud();
            for (Point3D point : points) {
                // Write point entity
                writer.write("0\nPOINT\n");
                writer.write("8\n0\n"); // Layer
                writer.write(String.format("10\n%.6f\n", point.getX())); // X coordinate
                writer.write(String.format("20\n%.6f\n", point.getY())); // Y coordinate
                writer.write(String.format("30\n%.6f\n", point.getZ())); // Z coordinate
            }
            
            writer.write("0\nENDSEC\n");
            writer.write("0\nEOF\n");
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Export room scan to IFC format (BIM)
     * @param scan The room scan to export
     * @param outputFile The output file path
     * @return true if export successful
     */
    public static boolean exportToIFC(RoomScan scan, File outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            // Write IFC header
            writer.write("ISO-10303-21;\n");
            writer.write("HEADER;\n");
            writer.write("FILE_DESCRIPTION(('Room Scanner Export'),'2;1');\n");
            writer.write("FILE_NAME('" + scan.getName() + "','");
            writer.write(scan.getScanDate().toString());
            writer.write("',('Room Scanner'),('Room Scanner App'),'','','');\n");
            writer.write("FILE_SCHEMA(('IFC4'));\n");
            writer.write("ENDSEC;\n");
            
            // Write data section
            writer.write("DATA;\n");
            writer.write("#1=IFCPROJECT('Project','Room Scan','");
            writer.write(scan.getName());
            writer.write("',$,$,$,$,$,$);\n");
            
            // Add space representation
            writer.write("#2=IFCSPACE('Space','" + scan.getName() + "',$,$,$,$,$,$);\n");
            
            // Close IFC file
            writer.write("ENDSEC;\n");
            writer.write("END-ISO-10303-21;\n");
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generate BIM metadata for the scan
     * @param scan The room scan
     * @return Metadata as JSON string
     */
    public static String generateBIMMetadata(RoomScan scan) {
        StringBuilder metadata = new StringBuilder();
        metadata.append("{\n");
        metadata.append("  \"id\": \"").append(scan.getId()).append("\",\n");
        metadata.append("  \"name\": \"").append(scan.getName()).append("\",\n");
        metadata.append("  \"scanDate\": \"").append(scan.getScanDate()).append("\",\n");
        metadata.append("  \"pointCount\": ").append(scan.getPointCount()).append(",\n");
        metadata.append("  \"materialType\": \"").append(scan.getMaterialType()).append("\",\n");
        metadata.append("  \"estimatedCost\": ").append(scan.getEstimatedCost()).append("\n");
        metadata.append("}\n");
        return metadata.toString();
    }
}
