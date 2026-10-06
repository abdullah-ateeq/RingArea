package com.example.ringarea.domain.math

import com.example.ringarea.data.model.MeasurementUnit
import com.example.ringarea.data.model.RingCalculationMode
import com.example.ringarea.data.model.RingResult
import com.example.ringarea.domain.format.ResultFormatter
import com.example.ringarea.domain.unit.UnitConverter
import java.math.BigDecimal

data class CalculationStep(
    val title: String,
    val formula: String,
    val calculation: String,
    val result: String
)

object FormulaGenerator {

    fun generateSteps(
        mode: RingCalculationMode,
        result: RingResult,
        unit: MeasurementUnit,
        decimals: Int
    ): List<CalculationStep> {
        val outerRMm = result.outerRadiusMm
        val innerRMm = result.innerRadiusMm
        val outerR = UnitConverter.fromMillimeters(outerRMm, unit)
        val innerR = UnitConverter.fromMillimeters(innerRMm, unit)

        val rOuterFormatted = ResultFormatter.formatNumber(outerR, decimals)
        val rInnerFormatted = ResultFormatter.formatNumber(innerR, decimals)

        val rOuterSq = BigDecimalMath.square(outerR)
        val rInnerSq = BigDecimalMath.square(innerR)
        val diffSq = rOuterSq.subtract(rInnerSq, BigDecimalMath.MATH_CONTEXT)

        val rOuterSqFormatted = ResultFormatter.formatNumber(rOuterSq, decimals)
        val rInnerSqFormatted = ResultFormatter.formatNumber(rInnerSq, decimals)
        val diffSqFormatted = ResultFormatter.formatNumber(diffSq, decimals)

        val ringArea = UnitConverter.fromSquareMillimeters(result.ringAreaMm2, unit)
        val ringAreaFormatted = ResultFormatter.formatArea(ringArea, unit, decimals)

        val ringWidth = UnitConverter.fromMillimeters(result.ringWidthMm, unit)
        val ringWidthFormatted = ResultFormatter.formatDimension(ringWidth, unit, decimals)

        val outerArea = UnitConverter.fromSquareMillimeters(result.outerCircleAreaMm2, unit)
        val innerArea = UnitConverter.fromSquareMillimeters(result.innerCircleAreaMm2, unit)
        val outerAreaFormatted = ResultFormatter.formatArea(outerArea, unit, decimals)
        val innerAreaFormatted = ResultFormatter.formatArea(innerArea, unit, decimals)

        val outerCirc = UnitConverter.fromMillimeters(result.outerCircumferenceMm, unit)
        val innerCirc = UnitConverter.fromMillimeters(result.innerCircumferenceMm, unit)
        val outerCircFormatted = ResultFormatter.formatDimension(outerCirc, unit, decimals)
        val innerCircFormatted = ResultFormatter.formatDimension(innerCirc, unit, decimals)

        val steps = mutableListOf<CalculationStep>()

        when (mode) {
            RingCalculationMode.CALCULATE -> {
                steps.add(
                    CalculationStep(
                        title = "Ring Area (Primary Annulus Formula)",
                        formula = "A = π × (R² − r²)",
                        calculation = "π × ($rOuterFormatted² − $rInnerFormatted²) = π × ($rOuterSqFormatted − $rInnerSqFormatted) = π × $diffSqFormatted",
                        result = ringAreaFormatted
                    )
                )
                steps.add(
                    CalculationStep(
                        title = "Area Difference (Alternative Verification)",
                        formula = "A = A_outer − A_inner",
                        calculation = "${ResultFormatter.formatNumber(outerArea, decimals)} − ${ResultFormatter.formatNumber(innerArea, decimals)}",
                        result = ringAreaFormatted
                    )
                )
                steps.add(
                    CalculationStep(
                        title = "Ring Width (Radial Thickness)",
                        formula = "w = R − r",
                        calculation = "$rOuterFormatted − $rInnerFormatted",
                        result = ringWidthFormatted
                    )
                )
                steps.add(
                    CalculationStep(
                        title = "Outer Circumference",
                        formula = "C_outer = 2 × π × R",
                        calculation = "2 × π × $rOuterFormatted",
                        result = outerCircFormatted
                    )
                )
                steps.add(
                    CalculationStep(
                        title = "Inner Circumference",
                        formula = "C_inner = 2 × π × r",
                        calculation = "2 × π × $rInnerFormatted",
                        result = innerCircFormatted
                    )
                )
                steps.add(
                    CalculationStep(
                        title = "Outer Circle Area",
                        formula = "A_outer = π × R²",
                        calculation = "π × $rOuterFormatted² = π × $rOuterSqFormatted",
                        result = outerAreaFormatted
                    )
                )
                steps.add(
                    CalculationStep(
                        title = "Inner Circle Area",
                        formula = "A_inner = π × r²",
                        calculation = "π × $rInnerFormatted² = π × $rInnerSqFormatted",
                        result = innerAreaFormatted
                    )
                )
            }
            RingCalculationMode.FIND_INNER_RADIUS -> {
                val areaOverPi = ringArea.divide(BigDecimalMath.PI, BigDecimalMath.MATH_CONTEXT)
                val areaOverPiFormatted = ResultFormatter.formatNumber(areaOverPi, decimals)
                steps.add(
                    CalculationStep(
                        title = "Inner Radius from Ring Area",
                        formula = "r = √(R² − A / π)",
                        calculation = "√($rOuterFormatted² − ${ResultFormatter.formatNumber(ringArea, decimals)} / π) = √($rOuterSqFormatted − $areaOverPiFormatted) = √$rInnerSqFormatted",
                        result = ResultFormatter.formatDimension(innerR, unit, decimals)
                    )
                )
                steps.add(
                    CalculationStep(
                        title = "Resulting Ring Width",
                        formula = "w = R − r",
                        calculation = "$rOuterFormatted − $rInnerFormatted",
                        result = ringWidthFormatted
                    )
                )
                steps.add(
                    CalculationStep(
                        title = "Resulting Ring Area Verification",
                        formula = "A = π × (R² − r²)",
                        calculation = "π × ($rOuterFormatted² − $rInnerFormatted²)",
                        result = ringAreaFormatted
                    )
                )
            }
            RingCalculationMode.FIND_OUTER_RADIUS -> {
                val areaOverPi = ringArea.divide(BigDecimalMath.PI, BigDecimalMath.MATH_CONTEXT)
                val areaOverPiFormatted = ResultFormatter.formatNumber(areaOverPi, decimals)
                steps.add(
                    CalculationStep(
                        title = "Outer Radius from Ring Area",
                        formula = "R = √(r² + A / π)",
                        calculation = "√($rInnerFormatted² + ${ResultFormatter.formatNumber(ringArea, decimals)} / π) = √($rInnerSqFormatted + $areaOverPiFormatted) = √$rOuterSqFormatted",
                        result = ResultFormatter.formatDimension(outerR, unit, decimals)
                    )
                )
                steps.add(
                    CalculationStep(
                        title = "Resulting Ring Width",
                        formula = "w = R − r",
                        calculation = "$rOuterFormatted − $rInnerFormatted",
                        result = ringWidthFormatted
                    )
                )
                steps.add(
                    CalculationStep(
                        title = "Resulting Ring Area Verification",
                        formula = "A = π × (R² − r²)",
                        calculation = "π × ($rOuterFormatted² − $rInnerFormatted²)",
                        result = ringAreaFormatted
                    )
                )
            }
            RingCalculationMode.FIND_RING_WIDTH -> {
                steps.add(
                    CalculationStep(
                        title = "Ring Width",
                        formula = "w = R − r",
                        calculation = "$rOuterFormatted − $rInnerFormatted",
                        result = ringWidthFormatted
                    )
                )
                steps.add(
                    CalculationStep(
                        title = "Enclosed Ring Area",
                        formula = "A = π × (R² − r²)",
                        calculation = "π × ($rOuterFormatted² − $rInnerFormatted²)",
                        result = ringAreaFormatted
                    )
                )
                steps.add(
                    CalculationStep(
                        title = "Circumferences",
                        formula = "C_outer = 2 × π × R,  C_inner = 2 × π × r",
                        calculation = "Outer: 2 × π × $rOuterFormatted,  Inner: 2 × π × $rInnerFormatted",
                        result = "$outerCircFormatted / $innerCircFormatted"
                    )
                )
            }
        }

        return steps
    }
}
