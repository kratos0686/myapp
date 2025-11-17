package com.roomscanner.app.domain

import com.roomscanner.app.data.MaterialEstimate
import com.roomscanner.app.data.RoomScan

/**
 * Use case for generating a drying plan based on room scan data.
 * This demonstrates how to process scan data and material estimates
 * to create actionable recommendations.
 */
class GenerateDryingPlanUseCase {

    /**
     * Generate a drying plan based on room dimensions, damage, and materials.
     */
    fun execute(scan: RoomScan): DryingPlan {
        val recommendations = mutableListOf<Recommendation>()
        
        // Calculate drying time based on room volume and material types
        val baseDryingHours = calculateBaseDryingTime(scan.volume)
        
        // Analyze materials to adjust drying time
        val materialAdjustment = analyzeMaterials(scan.materialEstimates)
        val totalDryingHours = baseDryingHours * materialAdjustment
        
        // Generate equipment recommendations
        val equipment = recommendEquipment(scan.area, scan.materialEstimates)
        
        // Generate action items based on damaged areas
        scan.damagedAreas.forEach { area ->
            val priority = when {
                area.severity > 0.7f -> Priority.HIGH
                area.severity > 0.4f -> Priority.MEDIUM
                else -> Priority.LOW
            }
            
            recommendations.add(
                Recommendation(
                    title = "Address ${area.type} in ${area.location}",
                    description = "Severity: ${(area.severity * 100).toInt()}%",
                    priority = priority,
                    category = RecommendationCategory.REPAIR
                )
            )
        }
        
        // Add drying recommendations
        recommendations.add(
            Recommendation(
                title = "Set up dehumidifiers",
                description = "Position ${equipment.dehumidifiers} dehumidifier(s) evenly throughout the room",
                priority = Priority.HIGH,
                category = RecommendationCategory.DRYING
            )
        )
        
        recommendations.add(
            Recommendation(
                title = "Install air movers",
                description = "Place ${equipment.airMovers} air mover(s) targeting wet areas",
                priority = Priority.HIGH,
                category = RecommendationCategory.DRYING
            )
        )
        
        // Add monitoring recommendations
        recommendations.add(
            Recommendation(
                title = "Monitor moisture levels",
                description = "Check moisture readings daily, especially in ${getMostAbsorbentMaterials(scan.materialEstimates).joinToString(", ")}",
                priority = Priority.MEDIUM,
                category = RecommendationCategory.MONITORING
            )
        )
        
        return DryingPlan(
            scanId = scan.id,
            roomName = scan.roomName,
            estimatedDryingHours = totalDryingHours.toInt(),
            equipment = equipment,
            recommendations = recommendations.sortedByDescending { it.priority.ordinal }
        )
    }
    
    private fun calculateBaseDryingTime(volume: Float): Float {
        // Base calculation: ~24 hours per 100 cubic meters
        return (volume / 100f) * 24f + 24f
    }
    
    private fun analyzeMaterials(materials: List<MaterialEstimate>): Float {
        var multiplier = 1.0f
        
        materials.forEach { material ->
            when (material.materialType.lowercase()) {
                "concrete" -> multiplier += 0.3f * material.confidence
                "drywall" -> multiplier += 0.2f * material.confidence
                "carpet" -> multiplier += 0.4f * material.confidence
                "wood" -> multiplier += 0.25f * material.confidence
            }
        }
        
        return multiplier
    }
    
    private fun recommendEquipment(area: Float, materials: List<MaterialEstimate>): Equipment {
        // Basic calculation: 1 dehumidifier per 50 m², 1 air mover per 25 m²
        val baseDehumidifiers = (area / 50f).toInt() + 1
        val baseAirMovers = (area / 25f).toInt() + 1
        
        // Adjust based on materials
        val hasAbsorbentMaterials = materials.any { 
            it.materialType.lowercase() in listOf("carpet", "drywall", "wood")
        }
        
        return Equipment(
            dehumidifiers = if (hasAbsorbentMaterials) baseDehumidifiers + 1 else baseDehumidifiers,
            airMovers = if (hasAbsorbentMaterials) baseAirMovers + 2 else baseAirMovers,
            moistureMeters = 2
        )
    }
    
    private fun getMostAbsorbentMaterials(materials: List<MaterialEstimate>): List<String> {
        val absorbentMaterials = listOf("carpet", "drywall", "wood", "concrete")
        return materials
            .filter { it.materialType.lowercase() in absorbentMaterials }
            .map { it.materialType }
            .distinct()
    }
}

/**
 * Drying plan with equipment and recommendations.
 */
data class DryingPlan(
    val scanId: Long,
    val roomName: String,
    val estimatedDryingHours: Int,
    val equipment: Equipment,
    val recommendations: List<Recommendation>
)

/**
 * Equipment recommendations.
 */
data class Equipment(
    val dehumidifiers: Int,
    val airMovers: Int,
    val moistureMeters: Int
)

/**
 * Single recommendation item.
 */
data class Recommendation(
    val title: String,
    val description: String,
    val priority: Priority,
    val category: RecommendationCategory
)

enum class Priority {
    HIGH, MEDIUM, LOW
}

enum class RecommendationCategory {
    REPAIR, DRYING, MONITORING, SAFETY
}
