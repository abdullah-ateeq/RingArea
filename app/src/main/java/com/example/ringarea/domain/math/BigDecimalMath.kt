package com.example.ringarea.domain.math

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object BigDecimalMath {
    val MATH_CONTEXT = MathContext(34, RoundingMode.HALF_UP)
    val PI: BigDecimal = BigDecimal("3.1415926535897932384626433832795028841971", MATH_CONTEXT)
    val TWO: BigDecimal = BigDecimal("2", MATH_CONTEXT)
    val FOUR: BigDecimal = BigDecimal("4", MATH_CONTEXT)
    val ZERO: BigDecimal = BigDecimal.ZERO
    val ONE: BigDecimal = BigDecimal.ONE

    fun parseSafe(text: String?): BigDecimal? {
        if (text.isNullOrBlank()) return null
        val cleaned = text.trim().replace(',', '.')
        return try {
            val bd = BigDecimal(cleaned)
            if (bd.compareTo(BigDecimal.ZERO) < 0) null else bd
        } catch (e: Exception) {
            null
        }
    }

    fun parseAnySafe(text: String?): BigDecimal? {
        if (text.isNullOrBlank()) return null
        val cleaned = text.trim().replace(',', '.')
        return try {
            BigDecimal(cleaned)
        } catch (e: Exception) {
            null
        }
    }

    fun square(value: BigDecimal): BigDecimal {
        return value.multiply(value, MATH_CONTEXT)
    }

    /**
     * Newton-Raphson BigDecimal square root for maximum precision across all Android SDK levels.
     */
    fun sqrt(value: BigDecimal, mc: MathContext = MATH_CONTEXT): BigDecimal {
        if (value.signum() < 0) {
            throw IllegalArgumentException("Cannot calculate square root of negative value: $value")
        }
        if (value.signum() == 0) {
            return BigDecimal.ZERO
        }

        // Initial estimate using standard double sqrt
        val initialDouble = Math.sqrt(value.toDouble())
        var x0 = if (initialDouble.isFinite() && initialDouble > 0.0) {
            BigDecimal(initialDouble, mc)
        } else {
            BigDecimal.ONE
        }

        val two = BigDecimal("2", mc)
        val tolerance = BigDecimal("1E-32")

        for (i in 0..100) {
            val x1 = x0.add(value.divide(x0, mc), mc).divide(two, mc)
            if (x1.subtract(x0, mc).abs() < tolerance) {
                return x1
            }
            x0 = x1
        }
        return x0
    }
}
