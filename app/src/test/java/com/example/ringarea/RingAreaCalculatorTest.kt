package com.example.ringarea

import com.example.ringarea.data.model.MeasurementUnit
import com.example.ringarea.data.model.RadiusInputMode
import com.example.ringarea.data.model.RingCalculationMode
import com.example.ringarea.data.model.SavedCalculation
import com.example.ringarea.data.model.UserSettings
import com.example.ringarea.domain.format.ResultFormatter
import com.example.ringarea.domain.math.BigDecimalMath
import com.example.ringarea.domain.math.RingAreaCalculator
import com.example.ringarea.domain.unit.UnitConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class RingAreaCalculatorTest {

    // 1. Ring area: R = 100mm (10cm), r = 60mm (6cm) -> Area = π * (100² - 60²) = π * 6400 ≈ 20106.19298 mm²
    @Test
    fun test_01_ring_area() {
        val outerRadiusMm = BigDecimal("100")
        val innerRadiusMm = BigDecimal("60")
        val area = RingAreaCalculator.calculateRingArea(outerRadiusMm, innerRadiusMm)
        val areaCm2 = UnitConverter.fromSquareMillimeters(area, MeasurementUnit.CENTIMETER)
        val formatted = ResultFormatter.formatNumber(areaCm2, 2)
        assertEquals("201.06", formatted)
    }

    // 2. Ring width: 100mm - 60mm = 40mm (4cm)
    @Test
    fun test_02_ring_width() {
        val outerRadiusMm = BigDecimal("100")
        val innerRadiusMm = BigDecimal("60")
        val width = RingAreaCalculator.calculateRingWidth(outerRadiusMm, innerRadiusMm)
        val widthCm = UnitConverter.fromMillimeters(width, MeasurementUnit.CENTIMETER)
        val formatted = ResultFormatter.formatNumber(widthCm, 2)
        assertEquals("4", formatted)
    }

    // 3. Outer diameter: 2 * 100mm = 200mm (20cm)
    @Test
    fun test_03_outer_diameter() {
        val outerRadiusMm = BigDecimal("100")
        val diameter = RingAreaCalculator.calculateOuterDiameter(outerRadiusMm)
        val diameterCm = UnitConverter.fromMillimeters(diameter, MeasurementUnit.CENTIMETER)
        val formatted = ResultFormatter.formatNumber(diameterCm, 2)
        assertEquals("20", formatted)
    }

    // 4. Inner diameter: 2 * 60mm = 120mm (12cm)
    @Test
    fun test_04_inner_diameter() {
        val innerRadiusMm = BigDecimal("60")
        val diameter = RingAreaCalculator.calculateInnerDiameter(innerRadiusMm)
        val diameterCm = UnitConverter.fromMillimeters(diameter, MeasurementUnit.CENTIMETER)
        val formatted = ResultFormatter.formatNumber(diameterCm, 2)
        assertEquals("12", formatted)
    }

    // 5. Outer circumference: 2 * π * 100mm ≈ 628.3185 mm (62.83 cm)
    @Test
    fun test_05_outer_circumference() {
        val outerRadiusMm = BigDecimal("100")
        val circ = RingAreaCalculator.calculateOuterCircumference(outerRadiusMm)
        val circCm = UnitConverter.fromMillimeters(circ, MeasurementUnit.CENTIMETER)
        val formatted = ResultFormatter.formatNumber(circCm, 2)
        assertEquals("62.83", formatted)
    }

    // 6. Inner circumference: 2 * π * 60mm ≈ 376.9911 mm (37.70 cm)
    @Test
    fun test_06_inner_circumference() {
        val innerRadiusMm = BigDecimal("60")
        val circ = RingAreaCalculator.calculateInnerCircumference(innerRadiusMm)
        val circCm = UnitConverter.fromMillimeters(circ, MeasurementUnit.CENTIMETER)
        val formatted = ResultFormatter.formatNumber(circCm, 2)
        assertEquals("37.7", formatted)
    }

    // 7. Outer circle area: π * 100² ≈ 31415.93 mm² (314.16 cm²)
    @Test
    fun test_07_outer_circle_area() {
        val outerRadiusMm = BigDecimal("100")
        val area = RingAreaCalculator.calculateOuterCircleArea(outerRadiusMm)
        val areaCm2 = UnitConverter.fromSquareMillimeters(area, MeasurementUnit.CENTIMETER)
        val formatted = ResultFormatter.formatNumber(areaCm2, 2)
        assertEquals("314.16", formatted)
    }

    // 8. Inner circle area: π * 60² ≈ 11309.73 mm² (113.10 cm²)
    @Test
    fun test_08_inner_circle_area() {
        val innerRadiusMm = BigDecimal("60")
        val area = RingAreaCalculator.calculateInnerCircleArea(innerRadiusMm)
        val areaCm2 = UnitConverter.fromSquareMillimeters(area, MeasurementUnit.CENTIMETER)
        val formatted = ResultFormatter.formatNumber(areaCm2, 2)
        assertEquals("113.1", formatted)
    }

    // 9. Radius input mode: calculate full ring from radius
    @Test
    fun test_09_radius_input() {
        val res = RingAreaCalculator.calculateRing(BigDecimal("100"), BigDecimal("60"))
        assertEquals(BigDecimal("100"), res.outerRadiusMm)
        assertEquals(BigDecimal("60"), res.innerRadiusMm)
        assertEquals(BigDecimal("40"), res.ringWidthMm)
    }

    // 10. Diameter input mode: Outer dia 20cm (200mm), Inner dia 12cm (120mm)
    @Test
    fun test_10_diameter_input() {
        val outerDiameterMm = BigDecimal("200")
        val innerDiameterMm = BigDecimal("120")
        val outerRadiusMm = outerDiameterMm.divide(BigDecimalMath.TWO, BigDecimalMath.MATH_CONTEXT)
        val innerRadiusMm = innerDiameterMm.divide(BigDecimalMath.TWO, BigDecimalMath.MATH_CONTEXT)

        val res = RingAreaCalculator.calculateRing(outerRadiusMm, innerRadiusMm)
        val areaCm2 = UnitConverter.fromSquareMillimeters(res.ringAreaMm2, MeasurementUnit.CENTIMETER)
        assertEquals("201.06", ResultFormatter.formatNumber(areaCm2, 2))
    }

    // 11. Thin ring: Outer 10cm (100mm), Inner 9cm (90mm) -> Width 1cm, Area ≈ 59.69 cm²
    @Test
    fun test_11_thin_ring() {
        val outerMm = BigDecimal("100")
        val innerMm = BigDecimal("90")
        val res = RingAreaCalculator.calculateRing(outerMm, innerMm)
        val widthCm = UnitConverter.fromMillimeters(res.ringWidthMm, MeasurementUnit.CENTIMETER)
        val areaCm2 = UnitConverter.fromSquareMillimeters(res.ringAreaMm2, MeasurementUnit.CENTIMETER)

        assertEquals("1", ResultFormatter.formatNumber(widthCm, 2))
        assertEquals("59.69", ResultFormatter.formatNumber(areaCm2, 2))
    }

    // 12. Thick ring: Outer 10cm (100mm), Inner 1cm (10mm) -> Width 9cm, Area ≈ 311.02 cm²
    @Test
    fun test_12_thick_ring() {
        val outerMm = BigDecimal("100")
        val innerMm = BigDecimal("10")
        val res = RingAreaCalculator.calculateRing(outerMm, innerMm)
        val widthCm = UnitConverter.fromMillimeters(res.ringWidthMm, MeasurementUnit.CENTIMETER)
        val areaCm2 = UnitConverter.fromSquareMillimeters(res.ringAreaMm2, MeasurementUnit.CENTIMETER)

        assertEquals("9", ResultFormatter.formatNumber(widthCm, 2))
        assertEquals("311.02", ResultFormatter.formatNumber(areaCm2, 2))
    }

    // 13. Find inner radius: Outer 10cm (100mm), Ring Area 201.0619298 cm² (20106.19298 mm²) -> Inner Radius ≈ 6cm (60mm)
    @Test
    fun test_13_find_inner_radius() {
        val outerRadiusMm = BigDecimal("100")
        val ringAreaMm2 = BigDecimal("20106.19298")
        val innerRadiusMm = RingAreaCalculator.calculateInnerRadiusFromArea(outerRadiusMm, ringAreaMm2)
        val innerRadiusCm = UnitConverter.fromMillimeters(innerRadiusMm, MeasurementUnit.CENTIMETER)
        assertEquals("6", ResultFormatter.formatNumber(innerRadiusCm, 2))
    }

    // 14. Find outer radius: Inner 6cm (60mm), Ring Area 201.0619298 cm² (20106.19298 mm²) -> Outer Radius ≈ 10cm (100mm)
    @Test
    fun test_14_find_outer_radius() {
        val innerRadiusMm = BigDecimal("60")
        val ringAreaMm2 = BigDecimal("20106.19298")
        val outerRadiusMm = RingAreaCalculator.calculateOuterRadiusFromArea(innerRadiusMm, ringAreaMm2)
        val outerRadiusCm = UnitConverter.fromMillimeters(outerRadiusMm, MeasurementUnit.CENTIMETER)
        assertEquals("10", ResultFormatter.formatNumber(outerRadiusCm, 2))
    }

    // 15. Find ring width: R - r
    @Test
    fun test_15_find_ring_width() {
        val width = RingAreaCalculator.calculateRingWidth(BigDecimal("100"), BigDecimal("60"))
        assertEquals(BigDecimal("40"), width)
    }

    // 16. Invalid equal radii: Inner == Outer -> Error message
    @Test
    fun test_16_invalid_equal_radii() {
        val error = RingAreaCalculator.validateRingInputs(BigDecimal("100"), BigDecimal("100"))
        assertEquals("The inner and outer circles must have different radii.", error)
    }

    // 17. Invalid reversed radii: Inner > Outer -> Error message
    @Test
    fun test_17_invalid_reversed_radii() {
        val error = RingAreaCalculator.validateRingInputs(BigDecimal("60"), BigDecimal("100"))
        assertEquals("Inner radius must be smaller than outer radius.", error)
    }

    // 18. Zero radius
    @Test
    fun test_18_zero_radius() {
        val errorOuter = RingAreaCalculator.validateRingInputs(BigDecimal("0"), BigDecimal("60"))
        assertEquals("Outer radius must be greater than zero.", errorOuter)

        val errorInner = RingAreaCalculator.validateRingInputs(BigDecimal("100"), BigDecimal("0"))
        assertEquals("Inner radius must be greater than zero.", errorInner)
    }

    // 19. Negative radius
    @Test
    fun test_19_negative_radius() {
        val error = RingAreaCalculator.validateRingInputs(BigDecimal("-10"), BigDecimal("5"))
        assertEquals("Outer radius must be greater than zero.", error)
    }

    // 20. Zero area in inverse solver
    @Test
    fun test_20_zero_area() {
        val error = RingAreaCalculator.validateFindInnerRadiusInputs(BigDecimal("100"), BigDecimal("0"))
        assertEquals("Ring area must be greater than zero.", error)
    }

    // 21. Negative area in inverse solver
    @Test
    fun test_21_negative_area() {
        val error = RingAreaCalculator.validateFindInnerRadiusInputs(BigDecimal("100"), BigDecimal("-500"))
        assertEquals("Ring area must be greater than zero.", error)
    }

    // 22. Invalid inverse calculation: Ring area too large for outer radius (R² - A/π <= 0)
    @Test
    fun test_22_invalid_inverse_calculation() {
        val outerRadiusMm = BigDecimal("100") // outer area = π * 10000 ≈ 31415.9 mm²
        val oversizedArea = BigDecimal("40000") // larger than entire outer circle
        val error = RingAreaCalculator.validateFindInnerRadiusInputs(outerRadiusMm, oversizedArea)
        assertEquals("Ring area is too large for the given outer radius.", error)
    }

    // 23. Linear unit conversions: 1 mm, cm, m, in, ft
    @Test
    fun test_23_unit_conversion() {
        val val10Cm = BigDecimal("10")
        val mm = UnitConverter.toMillimeters(val10Cm, MeasurementUnit.CENTIMETER)
        assertEquals("100", mm.stripTrailingZeros().toPlainString())

        val m = UnitConverter.fromMillimeters(mm, MeasurementUnit.METER)
        assertEquals("0.1", m.stripTrailingZeros().toPlainString())

        val oneInch = BigDecimal("1")
        val inchMm = UnitConverter.toMillimeters(oneInch, MeasurementUnit.INCH)
        assertEquals("25.4", inchMm.stripTrailingZeros().toPlainString())

        val oneFoot = BigDecimal("1")
        val footMm = UnitConverter.toMillimeters(oneFoot, MeasurementUnit.FOOT)
        assertEquals("304.8", footMm.stripTrailingZeros().toPlainString())
    }

    // 24. Area unit conversions: 1 cm² = 100 mm², 1 m² = 1000000 mm², 1 in² = 645.16 mm², 1 ft² = 92903.04 mm²
    @Test
    fun test_24_area_unit_conversion() {
        val oneCm2 = BigDecimal("1")
        val mm2FromCm2 = UnitConverter.toSquareMillimeters(oneCm2, MeasurementUnit.CENTIMETER)
        assertEquals("100", mm2FromCm2.stripTrailingZeros().toPlainString())

        val oneM2 = BigDecimal("1")
        val mm2FromM2 = UnitConverter.toSquareMillimeters(oneM2, MeasurementUnit.METER)
        assertEquals("1000000", mm2FromM2.stripTrailingZeros().toPlainString())

        val oneIn2 = BigDecimal("1")
        val mm2FromIn2 = UnitConverter.toSquareMillimeters(oneIn2, MeasurementUnit.INCH)
        assertEquals("645.16", mm2FromIn2.stripTrailingZeros().toPlainString())

        val oneFt2 = BigDecimal("1")
        val mm2FromFt2 = UnitConverter.toSquareMillimeters(oneFt2, MeasurementUnit.FOOT)
        assertEquals("92903.04", mm2FromFt2.stripTrailingZeros().toPlainString())
    }

    // 25. Decimal precision: 0, 1, 2, 3, 4 decimal places
    @Test
    fun test_25_decimal_precision() {
        val value = BigDecimal("201.0619298")
        assertEquals("201", ResultFormatter.formatNumber(value, 0))
        assertEquals("201.1", ResultFormatter.formatNumber(value, 1))
        assertEquals("201.06", ResultFormatter.formatNumber(value, 2))
        assertEquals("201.062", ResultFormatter.formatNumber(value, 3))
        assertEquals("201.0619", ResultFormatter.formatNumber(value, 4))
    }

    // 26. BigDecimal precision: check high precision π and Newton-Raphson sqrt
    @Test
    fun test_26_bigdecimal_precision() {
        val sixtyFour = BigDecimal("64")
        val sqrt64 = BigDecimalMath.sqrt(sixtyFour)
        assertEquals("8", sqrt64.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString())

        val two = BigDecimal("2")
        val sqrt2 = BigDecimalMath.sqrt(two)
        val sqrt2Squared = sqrt2.multiply(sqrt2, BigDecimalMath.MATH_CONTEXT)
        assertEquals("2", ResultFormatter.formatNumber(sqrt2Squared, 6))
    }

    // 27. History serialization
    @Test
    fun test_27_history_serialization() {
        val gson = Gson()
        val item = SavedCalculation(
            id = "test-123",
            projectName = "Test Pipe Washer",
            timestamp = 1000L,
            calculationMode = RingCalculationMode.CALCULATE,
            inputMode = RadiusInputMode.RADIUS,
            outerRadiusMm = "100",
            innerRadiusMm = "60",
            outerDiameterMm = "200",
            innerDiameterMm = "120",
            ringWidthMm = "40",
            ringAreaMm2 = "20106.19298",
            outerCircleAreaMm2 = "31415.9265",
            innerCircleAreaMm2 = "11309.7335",
            outerCircumferenceMm = "628.3185",
            innerCircumferenceMm = "376.9911",
            unit = MeasurementUnit.CENTIMETER,
            isFavorite = true
        )

        val json = gson.toJson(listOf(item))
        val type = object : TypeToken<List<SavedCalculation>>() {}.type
        val restored: List<SavedCalculation> = gson.fromJson(json, type)

        assertEquals(1, restored.size)
        assertEquals("Test Pipe Washer", restored[0].projectName)
        assertEquals("100", restored[0].outerRadiusMm)
        assertEquals(true, restored[0].isFavorite)
    }

    // 28. Settings persistence data structure
    @Test
    fun test_28_settings_persistence() {
        val settings = UserSettings(
            decimalPlaces = 3,
            showDiagram = false,
            defaultUnit = MeasurementUnit.INCH
        )
        assertEquals(3, settings.decimalPlaces)
        assertEquals(false, settings.showDiagram)
        assertEquals(MeasurementUnit.INCH, settings.defaultUnit)
    }

    // 29. Favorites sorting: Favorites appear first
    @Test
    fun test_29_favorites() {
        val item1 = SavedCalculation(
            id = "1",
            projectName = "Non-Favorite New",
            timestamp = 2000L,
            calculationMode = RingCalculationMode.CALCULATE,
            inputMode = RadiusInputMode.RADIUS,
            outerRadiusMm = "100",
            innerRadiusMm = "60",
            outerDiameterMm = "200",
            innerDiameterMm = "120",
            ringWidthMm = "40",
            ringAreaMm2 = "20106",
            outerCircleAreaMm2 = "31415",
            innerCircleAreaMm2 = "11309",
            outerCircumferenceMm = "628",
            innerCircumferenceMm = "376",
            unit = MeasurementUnit.CENTIMETER,
            isFavorite = false
        )

        val item2 = SavedCalculation(
            id = "2",
            projectName = "Favorite Old",
            timestamp = 1000L,
            calculationMode = RingCalculationMode.CALCULATE,
            inputMode = RadiusInputMode.RADIUS,
            outerRadiusMm = "100",
            innerRadiusMm = "60",
            outerDiameterMm = "200",
            innerDiameterMm = "120",
            ringWidthMm = "40",
            ringAreaMm2 = "20106",
            outerCircleAreaMm2 = "31415",
            innerCircleAreaMm2 = "11309",
            outerCircumferenceMm = "628",
            innerCircumferenceMm = "376",
            unit = MeasurementUnit.CENTIMETER,
            isFavorite = true
        )

        val list = listOf(item1, item2)
        val sorted = list.sortedWith(
            compareByDescending<SavedCalculation> { it.isFavorite }
                .thenByDescending { it.timestamp }
        )

        assertEquals("2", sorted[0].id)
        assertEquals("Favorite Old", sorted[0].projectName)
        assertEquals("1", sorted[1].id)
    }
}
