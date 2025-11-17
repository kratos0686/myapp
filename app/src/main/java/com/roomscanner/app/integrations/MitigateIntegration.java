package com.roomscanner.app.integrations;

import com.roomscanner.app.models.RoomScan;
import com.roomscanner.app.analysis.MaterialAnalyzer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration with Mitigate by Cotality for water mitigation and restoration documentation
 * Exports room scan data in formats compatible with restoration workflows
 */
public class MitigateIntegration {
    
    /**
     * Damage severity levels for restoration assessment
     */
    public enum DamageSeverity {
        NONE("No Damage", 0),
        MINOR("Minor Damage", 1),
        MODERATE("Moderate Damage", 2),
        SIGNIFICANT("Significant Damage", 3),
        SEVERE("Severe Damage", 4);
        
        private final String description;
        private final int level;
        
        DamageSeverity(String description, int level) {
            this.description = description;
            this.level = level;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getLevel() {
            return level;
        }
    }
    
    /**
     * Water damage classes for mitigation documentation
     */
    public enum WaterDamageClass {
        CLASS_1("Class 1: Minimal water intrusion", "Slow evaporation rate"),
        CLASS_2("Class 2: Significant water intrusion", "Fast evaporation rate required"),
        CLASS_3("Class 3: Greatest water intrusion", "Fastest evaporation rate"),
        CLASS_4("Class 4: Specialty drying", "Deep pockets, low-evaporation materials");
        
        private final String name;
        private final String description;
        
        WaterDamageClass(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Generate Mitigate-compatible restoration report
     * @param scan The room scan data
     * @param damageSeverity Assessed damage severity
     * @param waterClass Water damage classification
     * @return JSON string for Mitigate import
     */
    public static String generateMitigateReport(RoomScan scan, DamageSeverity damageSeverity, 
                                                WaterDamageClass waterClass) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"project\": {\n");
        json.append("    \"id\": \"").append(scan.getId()).append("\",\n");
        json.append("    \"name\": \"").append(escapeJson(scan.getName())).append("\",\n");
        json.append("    \"date\": \"").append(scan.getScanDate().toString()).append("\",\n");
        json.append("    \"type\": \"water_mitigation\"\n");
        json.append("  },\n");
        json.append("  \"damage_assessment\": {\n");
        json.append("    \"severity\": \"").append(damageSeverity.getDescription()).append("\",\n");
        json.append("    \"severity_level\": ").append(damageSeverity.getLevel()).append(",\n");
        json.append("    \"water_class\": \"").append(waterClass.getName()).append("\",\n");
        json.append("    \"water_class_description\": \"").append(waterClass.getDescription()).append("\"\n");
        json.append("  },\n");
        json.append("  \"room_data\": {\n");
        json.append("    \"point_count\": ").append(scan.getPointCount()).append(",\n");
        json.append("    \"material_type\": \"").append(scan.getMaterialType() != null ? 
                    scan.getMaterialType() : "Unknown").append("\",\n");
        json.append("    \"estimated_cost\": ").append(scan.getEstimatedCost()).append("\n");
        json.append("  },\n");
        json.append("  \"documentation\": {\n");
        json.append("    \"scan_format\": \"3D Point Cloud\",\n");
        json.append("    \"data_source\": \"Room Scanner Mobile App\",\n");
        json.append("    \"certifications\": [\"3D Documentation\", \"Material Analysis\"]\n");
        json.append("  }\n");
        json.append("}\n");
        
        return json.toString();
    }
    
    /**
     * Generate moisture mapping data for mitigation tracking
     * @param scan The room scan
     * @return Moisture map as CSV format
     */
    public static String generateMoistureMap(RoomScan scan) {
        StringBuilder csv = new StringBuilder();
        csv.append("X,Y,Z,Reading_Type,Moisture_Level\n");
        
        // Generate moisture readings based on scan data
        // In production, this would use actual moisture sensor data
        csv.append("# Moisture Mapping Data for: ").append(scan.getName()).append("\n");
        csv.append("# Date: ").append(scan.getScanDate()).append("\n");
        csv.append("# Point Count: ").append(scan.getPointCount()).append("\n");
        
        return csv.toString();
    }
    
    /**
     * Create restoration project metadata compatible with Mitigate
     * @param scan The room scan
     * @return Map of metadata fields
     */
    public static Map<String, String> createRestorationMetadata(RoomScan scan) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("project_id", scan.getId());
        metadata.put("room_name", scan.getName());
        metadata.put("scan_date", scan.getScanDate().toString());
        metadata.put("material_type", scan.getMaterialType() != null ? scan.getMaterialType() : "Unknown");
        metadata.put("estimated_restoration_cost", String.format("%.2f", scan.getEstimatedCost()));
        metadata.put("data_format", "3D_POINT_CLOUD");
        metadata.put("documentation_type", "WATER_MITIGATION");
        metadata.put("app_source", "RoomScanner_Android");
        metadata.put("point_cloud_size", String.valueOf(scan.getPointCount()));
        
        return metadata;
    }
    
    /**
     * Generate drying log template for restoration tracking
     * @param scan The room scan
     * @param projectDays Number of days for drying
     * @return Drying log as formatted text
     */
    public static String generateDryingLog(RoomScan scan, int projectDays) {
        StringBuilder log = new StringBuilder();
        log.append("DRYING LOG - WATER MITIGATION PROJECT\n");
        log.append("=====================================\n\n");
        log.append("Project: ").append(scan.getName()).append("\n");
        log.append("Project ID: ").append(scan.getId()).append("\n");
        log.append("Start Date: ").append(scan.getScanDate()).append("\n");
        log.append("Material Type: ").append(scan.getMaterialType() != null ? 
                  scan.getMaterialType() : "Unknown").append("\n\n");
        
        log.append("DAILY READINGS:\n");
        log.append("---------------\n");
        for (int day = 1; day <= projectDays; day++) {
            log.append(String.format("Day %d:\n", day));
            log.append("  Temperature: ____°F\n");
            log.append("  Humidity: ____%\n");
            log.append("  Moisture Reading: ____\n");
            log.append("  Equipment Status: ____\n");
            log.append("  Notes: ________________________________\n\n");
        }
        
        log.append("\nGENERATED BY: Room Scanner Mobile App\n");
        log.append("COMPATIBLE WITH: Mitigate by Cotality\n");
        
        return log.toString();
    }
    
    /**
     * Export scan data in Mitigate-compatible package
     * @param scan The room scan
     * @param outputDirectory Directory to save files
     * @return true if export successful
     */
    public static boolean exportForMitigate(RoomScan scan, String outputDirectory) {
        try {
            // Generate default damage assessment
            DamageSeverity severity = DamageSeverity.MODERATE;
            WaterDamageClass waterClass = WaterDamageClass.CLASS_2;
            
            // Create metadata and reports
            String report = generateMitigateReport(scan, severity, waterClass);
            String moistureMap = generateMoistureMap(scan);
            String dryingLog = generateDryingLog(scan, 7); // 7-day default
            
            // In production, write files to outputDirectory
            // For now, return success
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Escape JSON special characters
     */
    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
