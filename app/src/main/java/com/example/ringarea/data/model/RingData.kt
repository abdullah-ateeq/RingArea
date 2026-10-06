package com.example.ringarea.data.model

import java.math.BigDecimal

data class RingInput(
    val projectName: String = "",
    val calculationMode: RingCalculationMode = RingCalculationMode.CALCULATE,
    val inputMode: RadiusInputMode = RadiusInputMode.RADIUS,
    val outerRadiusMm: BigDecimal? = null,
    val innerRadiusMm: BigDecimal? = null,
    val outerDiameterMm: BigDecimal? = null,
    val innerDiameterMm: BigDecimal? = null,
    val ringAreaMm2: BigDecimal? = null,
    val unit: MeasurementUnit = MeasurementUnit.CENTIMETER
)

data class RingResult(
    val outerRadiusMm: BigDecimal,
    val innerRadiusMm: BigDecimal,
    val outerDiameterMm: BigDecimal,
    val innerDiameterMm: BigDecimal,
    val ringWidthMm: BigDecimal,
    val ringAreaMm2: BigDecimal,
    val outerCircleAreaMm2: BigDecimal,
    val innerCircleAreaMm2: BigDecimal,
    val outerCircumferenceMm: BigDecimal,
    val innerCircumferenceMm: BigDecimal
)
