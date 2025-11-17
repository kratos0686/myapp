package com.roomscanner.app.integrations;

import com.roomscanner.app.models.RoomScan;
import com.roomscanner.app.analysis.MaterialAnalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * Water damage assessment for restoration and mitigation projects
 * Compatible with Mitigate by Cotality workflows
 */
public class WaterDamageAssessment {
    
    /**
     * Affected material categories for restoration
     */
    public enum AffectedMaterial {
        FLOORING("Flooring", true),
        DRYWALL("Drywall", true),
        CEILING("Ceiling", true),
        BASEBOARDS("Baseboards", true),
        INSULATION("Insulation", false),
        SUBFLOOR("Subfloor", false),
        STRUCTURAL("Structural Elements", false),
        HVAC("HVAC System", false);
        
        private final String name;
        private final boolean visible;
        
        AffectedMaterial(String name, boolean visible) {
            this.name = name;
            this.visible = visible;
        }
        
        public String getName() {
            return name;
        }
        
        public boolean isVisible() {
            return visible;
        }
    }
    
    /**
     * Assessment result for a room
     */
    public static class AssessmentResult {
        private RoomScan scan;
        private List<AffectedMaterial> affectedMaterials;
        private MitigateIntegration.WaterDamageClass damageClass;
        private MitigateIntegration.DamageSeverity severity;
        private double affectedArea;
        private String recommendations;
        
        public AssessmentResult(RoomScan scan) {
            this.scan = scan;
            this.affectedMaterials = new ArrayList<>();
        }
        
        public void addAffectedMaterial(AffectedMaterial material) {
            affectedMaterials.add(material);
        }
        
        public List<AffectedMaterial> getAffectedMaterials() {
            return affectedMaterials;
        }
        
        public void setDamageClass(MitigateIntegration.WaterDamageClass damageClass) {
            this.damageClass = damageClass;
        }
        
        public MitigateIntegration.WaterDamageClass getDamageClass() {
            return damageClass;
        }
        
        public void setSeverity(MitigateIntegration.DamageSeverity severity) {
            this.severity = severity;
        }
        
        public MitigateIntegration.DamageSeverity getSeverity() {
            return severity;
        }
        
        public void setAffectedArea(double area) {
            this.affectedArea = area;
        }
        
        public double getAffectedArea() {
            return affectedArea;
        }
        
        public void setRecommendations(String recommendations) {
            this.recommendations = recommendations;
        }
        
        public String getRecommendations() {
            return recommendations;
        }
        
        public RoomScan getScan() {
            return scan;
        }
    }
    
    /**
     * Perform water damage assessment on a room scan
     * @param scan The room scan to assess
     * @return Assessment result with damage classification
     */
    public static AssessmentResult assessWaterDamage(RoomScan scan) {
        AssessmentResult result = new AssessmentResult(scan);
        
        // Analyze material type to determine affected areas
        String materialType = scan.getMaterialType();
        if (materialType != null) {
            switch (materialType.toLowerCase()) {
                case "drywall":
                    result.addAffectedMaterial(AffectedMaterial.DRYWALL);
                    result.addAffectedMaterial(AffectedMaterial.BASEBOARDS);
                    result.setDamageClass(MitigateIntegration.WaterDamageClass.CLASS_2);
                    break;
                case "carpet":
                    result.addAffectedMaterial(AffectedMaterial.FLOORING);
                    result.addAffectedMaterial(AffectedMaterial.SUBFLOOR);
                    result.setDamageClass(MitigateIntegration.WaterDamageClass.CLASS_3);
                    break;
                case "wood":
                    result.addAffectedMaterial(AffectedMaterial.FLOORING);
                    result.addAffectedMaterial(AffectedMaterial.STRUCTURAL);
                    result.setDamageClass(MitigateIntegration.WaterDamageClass.CLASS_4);
                    break;
                case "concrete":
                    result.addAffectedMaterial(AffectedMaterial.FLOORING);
                    result.setDamageClass(MitigateIntegration.WaterDamageClass.CLASS_4);
                    break;
                default:
                    result.addAffectedMaterial(AffectedMaterial.FLOORING);
                    result.setDamageClass(MitigateIntegration.WaterDamageClass.CLASS_1);
            }
        } else {
            // Default assessment
            result.setDamageClass(MitigateIntegration.WaterDamageClass.CLASS_2);
        }
        
        // Estimate severity based on area
        MaterialAnalyzer analyzer = new MaterialAnalyzer();
        double area = analyzer.calculateSurfaceArea(scan);
        result.setAffectedArea(area);
        
        if (area < 10) {
            result.setSeverity(MitigateIntegration.DamageSeverity.MINOR);
        } else if (area < 50) {
            result.setSeverity(MitigateIntegration.DamageSeverity.MODERATE);
        } else if (area < 100) {
            result.setSeverity(MitigateIntegration.DamageSeverity.SIGNIFICANT);
        } else {
            result.setSeverity(MitigateIntegration.DamageSeverity.SEVERE);
        }
        
        // Generate recommendations
        result.setRecommendations(generateRecommendations(result));
        
        return result;
    }
    
    /**
     * Generate restoration recommendations based on assessment
     */
    private static String generateRecommendations(AssessmentResult result) {
        StringBuilder recommendations = new StringBuilder();
        
        recommendations.append("RESTORATION RECOMMENDATIONS\n");
        recommendations.append("===========================\n\n");
        
        recommendations.append("Damage Classification: ")
                       .append(result.getDamageClass().getName()).append("\n");
        recommendations.append("Severity Level: ")
                       .append(result.getSeverity().getDescription()).append("\n\n");
        
        recommendations.append("Affected Materials:\n");
        for (AffectedMaterial material : result.getAffectedMaterials()) {
            recommendations.append("  - ").append(material.getName()).append("\n");
        }
        recommendations.append("\n");
        
        recommendations.append("Affected Area: ")
                       .append(String.format("%.2f sq m", result.getAffectedArea())).append("\n\n");
        
        recommendations.append("Recommended Actions:\n");
        switch (result.getDamageClass()) {
            case CLASS_1:
                recommendations.append("  - Minimal extraction required\n");
                recommendations.append("  - Standard air movers and dehumidifiers\n");
                recommendations.append("  - Monitor for 2-3 days\n");
                break;
            case CLASS_2:
                recommendations.append("  - Extract standing water\n");
                recommendations.append("  - Deploy commercial dehumidifiers\n");
                recommendations.append("  - Remove damaged baseboards if necessary\n");
                recommendations.append("  - Monitor for 4-7 days\n");
                break;
            case CLASS_3:
                recommendations.append("  - Complete water extraction\n");
                recommendations.append("  - High-capacity dehumidification\n");
                recommendations.append("  - Remove carpet and pad\n");
                recommendations.append("  - Inspect subfloor for damage\n");
                recommendations.append("  - Monitor for 7-14 days\n");
                break;
            case CLASS_4:
                recommendations.append("  - Specialty drying equipment required\n");
                recommendations.append("  - Inject dry air into wall cavities\n");
                recommendations.append("  - Consider structural drying mats\n");
                recommendations.append("  - Extended monitoring (14+ days)\n");
                recommendations.append("  - Consult structural engineer if needed\n");
                break;
        }
        
        recommendations.append("\nEstimated Restoration Cost: $")
                       .append(String.format("%.2f", result.getScan().getEstimatedCost()))
                       .append("\n");
        
        return recommendations.toString();
    }
    
    /**
     * Generate equipment list for mitigation project
     * @param assessment The damage assessment
     * @return List of recommended equipment
     */
    public static String generateEquipmentList(AssessmentResult assessment) {
        StringBuilder equipment = new StringBuilder();
        
        equipment.append("RECOMMENDED EQUIPMENT LIST\n");
        equipment.append("==========================\n\n");
        
        switch (assessment.getDamageClass()) {
            case CLASS_1:
                equipment.append("- Air Movers (2-3 units)\n");
                equipment.append("- Dehumidifier (Small, 1 unit)\n");
                equipment.append("- Moisture Meter\n");
                break;
            case CLASS_2:
                equipment.append("- Air Movers (4-6 units)\n");
                equipment.append("- Dehumidifier (Large, 1-2 units)\n");
                equipment.append("- Water Extractor\n");
                equipment.append("- Moisture Meter\n");
                equipment.append("- Thermal Camera (Optional)\n");
                break;
            case CLASS_3:
                equipment.append("- Air Movers (8-12 units)\n");
                equipment.append("- Dehumidifiers (Commercial, 2-3 units)\n");
                equipment.append("- Water Extractor (High-capacity)\n");
                equipment.append("- Moisture Meter\n");
                equipment.append("- Thermal Camera\n");
                equipment.append("- Air Scrubber\n");
                break;
            case CLASS_4:
                equipment.append("- Air Movers (12+ units)\n");
                equipment.append("- Dehumidifiers (Industrial, 3-4 units)\n");
                equipment.append("- Specialty Drying Equipment\n");
                equipment.append("- Injection Drying System\n");
                equipment.append("- Moisture Meter\n");
                equipment.append("- Thermal Camera\n");
                equipment.append("- Air Scrubber\n");
                equipment.append("- Structural Drying Mats\n");
                break;
        }
        
        equipment.append("\nNote: Equipment quantities based on affected area of ")
                 .append(String.format("%.2f sq m", assessment.getAffectedArea()))
                 .append("\n");
        
        return equipment.toString();
    }
}
