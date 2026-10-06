package com.example.ringarea.domain.unit

import com.example.ringarea.data.model.MeasurementUnit
import com.example.ringarea.domain.math.BigDecimalMath
import java.math.BigDecimal

object UnitConverter {
    val MM_FACTOR = BigDecimal("1")
    val CM_FACTOR = BigDecimal("10")
    val M_FACTOR = BigDecimal("1000")
    val IN_FACTOR = BigDecimal("25.4")
    val FT_FACTOR = BigDecimal("304.8")

    val MM2_FACTOR = BigDecimal("1")
    val CM2_FACTOR = BigDecimal("100")
    val M2_FACTOR = BigDecimal("1000000")
    val IN2_FACTOR = BigDecimal("645.16")
    val FT2_FACTOR = BigDecimal("92903.04")

    fun toMillimeters(value: BigDecimal, unit: MeasurementUnit): BigDecimal {
        val factor = when (unit) {
            MeasurementUnit.MILLIMETER -> MM_FACTOR
            MeasurementUnit.CENTIMETER -> CM_FACTOR
            MeasurementUnit.METER -> M_FACTOR
            MeasurementUnit.INCH -> IN_FACTOR
            MeasurementUnit.FOOT -> FT_FACTOR
        }
        return value.multiply(factor, BigDecimalMath.MATH_CONTEXT)
    }

    fun fromMillimeters(valueMm: BigDecimal, unit: MeasurementUnit): BigDecimal {
        val factor = when (unit) {
            MeasurementUnit.MILLIMETER -> MM_FACTOR
            MeasurementUnit.CENTIMETER -> CM_FACTOR
            MeasurementUnit.METER -> M_FACTOR
            MeasurementUnit.INCH -> IN_FACTOR
            MeasurementUnit.FOOT -> FT_FACTOR
        }
        return valueMm.divide(factor, BigDecimalMath.MATH_CONTEXT)
    }

    fun toSquareMillimeters(value: BigDecimal, unit: MeasurementUnit): BigDecimal {
        val factor = when (unit) {
            MeasurementUnit.MILLIMETER -> MM2_FACTOR
            MeasurementUnit.CENTIMETER -> CM2_FACTOR
            MeasurementUnit.METER -> M2_FACTOR
            MeasurementUnit.INCH -> IN2_FACTOR
            MeasurementUnit.FOOT -> FT2_FACTOR
        }
        return value.multiply(factor, BigDecimalMath.MATH_CONTEXT)
    }

    fun fromSquareMillimeters(valueMm2: BigDecimal, unit: MeasurementUnit): BigDecimal {
        val factor = when (unit) {
            MeasurementUnit.MILLIMETER -> MM2_FACTOR
            MeasurementUnit.CENTIMETER -> CM2_FACTOR
            MeasurementUnit.METER -> M2_FACTOR
            MeasurementUnit.INCH -> IN2_FACTOR
            MeasurementUnit.FOOT -> FT2_FACTOR
        }
        return valueMm2.divide(factor, BigDecimalMath.MATH_CONTEXT)
    }

    /**
     * Converts a linear measurement from an old unit to a new unit, preserving physical measurement.
     */
    fun convertLinear(value: BigDecimal, fromUnit: MeasurementUnit, toUnit: MeasurementUnit): BigDecimal {
        if (fromUnit == toUnit) return value
        val mm = toMillimeters(value, fromUnit)
        return fromMillimeters(mm, toUnit)
    }

    /**
     * Converts an area measurement from an old unit to a new unit, preserving physical measurement.
     */
    fun convertArea(value: BigDecimal, fromUnit: MeasurementUnit, toUnit: MeasurementUnit): BigDecimal {
        if (fromUnit == toUnit) return value
        val mm2 = toSquareMillimeters(value, fromUnit)
        return fromSquareMillimeters(mm2, toUnit)
    }
}
