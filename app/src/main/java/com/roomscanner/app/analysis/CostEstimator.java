package com.roomscanner.app.analysis;

import com.roomscanner.app.models.RoomScan;

/**
 * Estimates costs based on detected materials and room dimensions
 */
public class CostEstimator {
    
    private MaterialAnalyzer materialAnalyzer;
    
    public CostEstimator() {
        this.materialAnalyzer = new MaterialAnalyzer();
    }
    
    /**
     * Calculate estimated renovation cost for a room
     * @param scan The room scan to estimate
     * @return Estimated cost in dollars
     */
    public double estimateRenovationCost(RoomScan scan) {
        MaterialAnalyzer.MaterialType material = materialAnalyzer.analyzeMaterial(scan);
        double surfaceArea = materialAnalyzer.calculateSurfaceArea(scan);
        
        // Base cost calculation
        double materialCost = surfaceArea * material.getCostPerSqMeter();
        
        // Add labor cost (typically 40% of material cost)
        double laborCost = materialCost * 0.4;
        
        // Total cost
        double totalCost = materialCost + laborCost;
        
        scan.setEstimatedCost(totalCost);
        scan.setMaterialType(material.getDisplayName());
        
        return totalCost;
    }
    
    /**
     * Estimate painting cost for the room
     * @param scan The room scan
     * @return Estimated painting cost
     */
    public double estimatePaintingCost(RoomScan scan) {
        double surfaceArea = materialAnalyzer.calculateSurfaceArea(scan);
        // Average painting cost: $2-4 per sq meter
        double paintCostPerSqMeter = 3.0;
        return surfaceArea * paintCostPerSqMeter;
    }
    
    /**
     * Estimate flooring replacement cost
     * @param scan The room scan
     * @param floorMaterial The desired flooring material
     * @return Estimated flooring cost
     */
    public double estimateFlooringCost(RoomScan scan, MaterialAnalyzer.MaterialType floorMaterial) {
        double floorArea = materialAnalyzer.calculateSurfaceArea(scan) * 0.3; // Approximate floor area
        return floorArea * floorMaterial.getCostPerSqMeter();
    }
    
    /**
     * Get detailed cost breakdown
     * @param scan The room scan
     * @return Cost breakdown as a formatted string
     */
    public String getCostBreakdown(RoomScan scan) {
        double totalCost = estimateRenovationCost(scan);
        double paintCost = estimatePaintingCost(scan);
        double surfaceArea = materialAnalyzer.calculateSurfaceArea(scan);
        
        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Cost Estimation Report\n");
        breakdown.append("======================\n\n");
        breakdown.append(String.format("Room: %s\n", scan.getName()));
        breakdown.append(String.format("Surface Area: %.2f sq m\n", surfaceArea));
        breakdown.append(String.format("Material Type: %s\n\n", scan.getMaterialType()));
        breakdown.append(String.format("Renovation Cost: $%.2f\n", totalCost));
        breakdown.append(String.format("Painting Cost: $%.2f\n", paintCost));
        breakdown.append(String.format("Total Estimate: $%.2f\n", totalCost + paintCost));
        
        return breakdown.toString();
    }
}
