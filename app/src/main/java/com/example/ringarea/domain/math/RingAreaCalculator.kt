package com.example.ringarea.domain.math

import com.example.ringarea.data.model.RingResult
import java.math.BigDecimal

object RingAreaCalculator {

    fun validateRingInputs(
        outerRadiusMm: BigDecimal?,
        innerRadiusMm: BigDecimal?,
        isDiameter: Boolean = false
    ): String? {
        val dimensionName = if (isDiameter) "diameter" else "radius"

        if (outerRadiusMm == null || innerRadiusMm == null) {
            return "Enter the required values."
        }

        if (outerRadiusMm.compareTo(BigDecimal.ZERO) <= 0) {
            return if (isDiameter) "Outer diameter must be greater than zero."
            else "Outer radius must be greater than zero."
        }

        if (innerRadiusMm.compareTo(BigDecimal.ZERO) <= 0) {
            return if (isDiameter) "Inner diameter must be greater than zero."
            else "Inner radius must be greater than zero."
        }

        if (innerRadiusMm.compareTo(outerRadiusMm) == 0) {
            return "The inner and outer circles must have different ${if (isDiameter) "diameters" else "radii"}."
        }

        if (innerRadiusMm.compareTo(outerRadiusMm) > 0) {
            return if (isDiameter) "Inner diameter must be smaller than outer diameter."
            else "Inner radius must be smaller than outer radius."
        }

        return null
    }

    fun validateFindInnerRadiusInputs(
        outerRadiusMm: BigDecimal?,
        ringAreaMm2: BigDecimal?
    ): String? {
        if (outerRadiusMm == null || ringAreaMm2 == null) {
            return "Enter the required values."
        }
        if (outerRadiusMm.compareTo(BigDecimal.ZERO) <= 0) {
            return "Outer radius must be greater than zero."
        }
        if (ringAreaMm2.compareTo(BigDecimal.ZERO) <= 0) {
            return "Ring area must be greater than zero."
        }

        val rSquared = BigDecimalMath.square(outerRadiusMm)
        val areaDividedByPi = ringAreaMm2.divide(BigDecimalMath.PI, BigDecimalMath.MATH_CONTEXT)
        val diff = rSquared.subtract(areaDividedByPi, BigDecimalMath.MATH_CONTEXT)

        if (diff.compareTo(BigDecimal.ZERO) <= 0) {
            return "Ring area is too large for the given outer radius."
        }

        return null
    }

    fun validateFindOuterRadiusInputs(
        innerRadiusMm: BigDecimal?,
        ringAreaMm2: BigDecimal?
    ): String? {
        if (innerRadiusMm == null || ringAreaMm2 == null) {
            return "Enter the required values."
        }
        if (innerRadiusMm.compareTo(BigDecimal.ZERO) <= 0) {
            return "Inner radius must be greater than zero."
        }
        if (ringAreaMm2.compareTo(BigDecimal.ZERO) <= 0) {
            return "Ring area must be greater than zero."
        }
        return null
    }

    fun calculateRingArea(outerRadiusMm: BigDecimal, innerRadiusMm: BigDecimal): BigDecimal {
        val rOutSq = BigDecimalMath.square(outerRadiusMm)
        val rInSq = BigDecimalMath.square(innerRadiusMm)
        val diff = rOutSq.subtract(rInSq, BigDecimalMath.MATH_CONTEXT)
        return BigDecimalMath.PI.multiply(diff, BigDecimalMath.MATH_CONTEXT)
    }

    fun calculateRingWidth(outerRadiusMm: BigDecimal, innerRadiusMm: BigDecimal): BigDecimal {
        if (innerRadiusMm >= outerRadiusMm) {
            throw IllegalArgumentException("Inner radius must be smaller than outer radius.")
        }
        return outerRadiusMm.subtract(innerRadiusMm, BigDecimalMath.MATH_CONTEXT)
    }

    fun calculateOuterDiameter(outerRadiusMm: BigDecimal): BigDecimal {
        return outerRadiusMm.multiply(BigDecimalMath.TWO, BigDecimalMath.MATH_CONTEXT)
    }

    fun calculateInnerDiameter(innerRadiusMm: BigDecimal): BigDecimal {
        return innerRadiusMm.multiply(BigDecimalMath.TWO, BigDecimalMath.MATH_CONTEXT)
    }

    fun calculateOuterCircleArea(outerRadiusMm: BigDecimal): BigDecimal {
        val rSq = BigDecimalMath.square(outerRadiusMm)
        return BigDecimalMath.PI.multiply(rSq, BigDecimalMath.MATH_CONTEXT)
    }

    fun calculateInnerCircleArea(innerRadiusMm: BigDecimal): BigDecimal {
        val rSq = BigDecimalMath.square(innerRadiusMm)
        return BigDecimalMath.PI.multiply(rSq, BigDecimalMath.MATH_CONTEXT)
    }

    fun calculateOuterCircumference(outerRadiusMm: BigDecimal): BigDecimal {
        return BigDecimalMath.TWO.multiply(BigDecimalMath.PI, BigDecimalMath.MATH_CONTEXT)
            .multiply(outerRadiusMm, BigDecimalMath.MATH_CONTEXT)
    }

    fun calculateInnerCircumference(innerRadiusMm: BigDecimal): BigDecimal {
        return BigDecimalMath.TWO.multiply(BigDecimalMath.PI, BigDecimalMath.MATH_CONTEXT)
            .multiply(innerRadiusMm, BigDecimalMath.MATH_CONTEXT)
    }

    fun calculateInnerRadiusFromArea(outerRadiusMm: BigDecimal, ringAreaMm2: BigDecimal): BigDecimal {
        require(outerRadiusMm.compareTo(BigDecimal.ZERO) > 0) { "Outer radius must be greater than zero." }
        require(ringAreaMm2.compareTo(BigDecimal.ZERO) > 0) { "Ring area must be greater than zero." }

        val rSquared = BigDecimalMath.square(outerRadiusMm)
        val areaDividedByPi = ringAreaMm2.divide(BigDecimalMath.PI, BigDecimalMath.MATH_CONTEXT)
        val diff = rSquared.subtract(areaDividedByPi, BigDecimalMath.MATH_CONTEXT)

        if (diff.compareTo(BigDecimal.ZERO) <= 0) {
            throw IllegalArgumentException("Ring area is too large for the given outer radius.")
        }

        val result = BigDecimalMath.sqrt(diff, BigDecimalMath.MATH_CONTEXT)
        if (result.compareTo(outerRadiusMm) >= 0) {
            throw IllegalArgumentException("Calculated inner radius must be smaller than outer radius.")
        }
        return result
    }

    fun calculateOuterRadiusFromArea(innerRadiusMm: BigDecimal, ringAreaMm2: BigDecimal): BigDecimal {
        require(innerRadiusMm.compareTo(BigDecimal.ZERO) > 0) { "Inner radius must be greater than zero." }
        require(ringAreaMm2.compareTo(BigDecimal.ZERO) > 0) { "Ring area must be greater than zero." }

        val rSquared = BigDecimalMath.square(innerRadiusMm)
        val areaDividedByPi = ringAreaMm2.divide(BigDecimalMath.PI, BigDecimalMath.MATH_CONTEXT)
        val sum = rSquared.add(areaDividedByPi, BigDecimalMath.MATH_CONTEXT)

        val result = BigDecimalMath.sqrt(sum, BigDecimalMath.MATH_CONTEXT)
        if (result.compareTo(innerRadiusMm) <= 0) {
            throw IllegalArgumentException("Calculated outer radius must be greater than inner radius.")
        }
        return result
    }

    fun calculateRing(outerRadiusMm: BigDecimal, innerRadiusMm: BigDecimal): RingResult {
        require(outerRadiusMm.compareTo(BigDecimal.ZERO) > 0) { "Outer radius must be greater than zero." }
        require(innerRadiusMm.compareTo(BigDecimal.ZERO) > 0) { "Inner radius must be greater than zero." }
        require(innerRadiusMm.compareTo(outerRadiusMm) < 0) {
            if (innerRadiusMm.compareTo(outerRadiusMm) == 0) {
                "The inner and outer circles must have different radii."
            } else {
                "Inner radius must be smaller than outer radius."
            }
        }

        val ringArea = calculateRingArea(outerRadiusMm, innerRadiusMm)
        val ringWidth = calculateRingWidth(outerRadiusMm, innerRadiusMm)
        val outerDiameter = calculateOuterDiameter(outerRadiusMm)
        val innerDiameter = calculateInnerDiameter(innerRadiusMm)
        val outerCircleArea = calculateOuterCircleArea(outerRadiusMm)
        val innerCircleArea = calculateInnerCircleArea(innerRadiusMm)
        val outerCircumference = calculateOuterCircumference(outerRadiusMm)
        val innerCircumference = calculateInnerCircumference(innerRadiusMm)

        return RingResult(
            outerRadiusMm = outerRadiusMm,
            innerRadiusMm = innerRadiusMm,
            outerDiameterMm = outerDiameter,
            innerDiameterMm = innerDiameter,
            ringWidthMm = ringWidth,
            ringAreaMm2 = ringArea,
            outerCircleAreaMm2 = outerCircleArea,
            innerCircleAreaMm2 = innerCircleArea,
            outerCircumferenceMm = outerCircumference,
            innerCircumferenceMm = innerCircumference
        )
    }
}
