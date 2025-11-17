package com.roomscanner.app.domain

import com.roomscanner.app.arcore.RoomDimensions
import com.roomscanner.app.data.DamagedArea
import com.roomscanner.app.data.MaterialEstimate
import com.roomscanner.app.data.RoomScan
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Unit tests for GenerateDryingPlanUseCase.
 */
class GenerateDryingPlanUseCaseTest {

    private lateinit var useCase: GenerateDryingPlanUseCase

    @Before
    fun setup() {
        useCase = GenerateDryingPlanUseCase()
    }

    @Test
    fun `should generate basic drying plan for empty room`() {
        // Arrange
        val scan = RoomScan(
            id = 1,
            roomName = "Test Room",
            scanDate = Date(),
            width = 5f,
            length = 4f,
            height = 2.5f,
            area = 20f,
            volume = 50f,
            pointCloudData = null,
            damagedAreas = emptyList(),
            materialEstimates = emptyList()
        )

        // Act
        val plan = useCase.execute(scan)

        // Assert
        assertNotNull(plan)
        assertEquals(1, plan.scanId)
        assertEquals("Test Room", plan.roomName)
        assertTrue(plan.estimatedDryingHours > 0)
        assertTrue(plan.equipment.dehumidifiers > 0)
        assertTrue(plan.equipment.airMovers > 0)
        assertTrue(plan.recommendations.isNotEmpty())
    }

    @Test
    fun `should increase drying time for absorbent materials`() {
        // Arrange
        val scanWithConcrete = createScanWithMaterial("concrete", 0.8f)
        val scanWithCarpet = createScanWithMaterial("carpet", 0.8f)

        // Act
        val planConcrete = useCase.execute(scanWithConcrete)
        val planCarpet = useCase.execute(scanWithCarpet)

        // Assert
        // Carpet should require more drying time than concrete
        assertTrue(planCarpet.estimatedDryingHours >= planConcrete.estimatedDryingHours)
    }

    @Test
    fun `should create high priority recommendations for severe damage`() {
        // Arrange
        val scan = RoomScan(
            id = 1,
            roomName = "Damaged Room",
            scanDate = Date(),
            width = 5f,
            length = 4f,
            height = 2.5f,
            area = 20f,
            volume = 50f,
            pointCloudData = null,
            damagedAreas = listOf(
                DamagedArea(
                    type = "water damage",
                    location = "north wall",
                    severity = 0.9f,
                    boundingBox = com.roomscanner.app.data.BoundingBox(0f, 0f, 100f, 100f),
                    imageUri = "test.jpg"
                )
            ),
            materialEstimates = emptyList()
        )

        // Act
        val plan = useCase.execute(scan)

        // Assert
        val damageRecommendations = plan.recommendations.filter { 
            it.category == RecommendationCategory.REPAIR 
        }
        assertTrue(damageRecommendations.isNotEmpty())
        assertEquals(Priority.HIGH, damageRecommendations.first().priority)
    }

    @Test
    fun `should recommend more equipment for larger areas`() {
        // Arrange
        val smallRoom = createScanWithArea(20f)
        val largeRoom = createScanWithArea(100f)

        // Act
        val smallPlan = useCase.execute(smallRoom)
        val largePlan = useCase.execute(largeRoom)

        // Assert
        assertTrue(largePlan.equipment.dehumidifiers > smallPlan.equipment.dehumidifiers)
        assertTrue(largePlan.equipment.airMovers > smallPlan.equipment.airMovers)
    }

    @Test
    fun `should include monitoring recommendations`() {
        // Arrange
        val scan = createScanWithMaterial("drywall", 0.7f)

        // Act
        val plan = useCase.execute(scan)

        // Assert
        val monitoringRecs = plan.recommendations.filter { 
            it.category == RecommendationCategory.MONITORING 
        }
        assertTrue(monitoringRecs.isNotEmpty())
    }

    // Helper methods

    private fun createScanWithMaterial(material: String, confidence: Float): RoomScan {
        return RoomScan(
            id = 1,
            roomName = "Test Room",
            scanDate = Date(),
            width = 5f,
            length = 4f,
            height = 2.5f,
            area = 20f,
            volume = 50f,
            pointCloudData = null,
            damagedAreas = emptyList(),
            materialEstimates = listOf(
                MaterialEstimate(
                    surfaceType = "floor",
                    materialType = material,
                    confidence = confidence,
                    coverage = 100f
                )
            )
        )
    }

    private fun createScanWithArea(area: Float): RoomScan {
        val width = 5f
        val length = area / width
        val height = 2.5f
        
        return RoomScan(
            id = 1,
            roomName = "Test Room",
            scanDate = Date(),
            width = width,
            length = length,
            height = height,
            area = area,
            volume = area * height,
            pointCloudData = null,
            damagedAreas = emptyList(),
            materialEstimates = emptyList()
        )
    }
}
