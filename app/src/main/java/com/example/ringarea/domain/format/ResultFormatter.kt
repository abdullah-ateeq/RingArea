package com.example.ringarea.domain.format

import com.example.ringarea.data.model.MeasurementUnit
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object ResultFormatter {

    fun formatNumber(value: BigDecimal, decimals: Int): String {
        if (value.compareTo(BigDecimal.ZERO) == 0) return "0"

        val rounded = value.setScale(decimals, RoundingMode.HALF_UP)
        // If whole number, strip trailing zero: e.g. 10.00 -> 10
        val stripped = rounded.stripTrailingZeros()
        return if (stripped.scale() <= 0) {
            stripped.toPlainString()
        } else {
            // Respect decimals max but clean up redundant zeros
            stripped.toPlainString()
        }
    }

    fun formatDimension(value: BigDecimal, unit: MeasurementUnit, decimals: Int): String {
        val formattedNum = formatNumber(value, decimals)
        return "$formattedNum ${unit.symbol}"
    }

    fun formatArea(value: BigDecimal, unit: MeasurementUnit, decimals: Int): String {
        val formattedNum = formatNumber(value, decimals)
        return "$formattedNum ${unit.areaSymbol}"
    }

    fun formatPlain(value: BigDecimal, decimals: Int): String {
        return value.setScale(decimals, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    }
}
