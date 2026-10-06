package com.example.ringarea.data.model

import java.util.UUID

data class SavedCalculation(
    val id: String = UUID.randomUUID().toString(),
    val projectName: String = "Untitled Ring",
    val timestamp: Long = System.currentTimeMillis(),
    val calculationMode: RingCalculationMode,
    val inputMode: RadiusInputMode,
    val outerRadiusMm: String,
    val innerRadiusMm: String,
    val outerDiameterMm: String,
    val innerDiameterMm: String,
    val ringWidthMm: String,
    val ringAreaMm2: String,
    val outerCircleAreaMm2: String,
    val innerCircleAreaMm2: String,
    val outerCircumferenceMm: String,
    val innerCircumferenceMm: String,
    val unit: MeasurementUnit,
    val isFavorite: Boolean = false
)

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultMode: RingCalculationMode = RingCalculationMode.CALCULATE,
    val defaultUnit: MeasurementUnit = MeasurementUnit.CENTIMETER,
    val decimalPlaces: Int = 2,
    val showDiagram: Boolean = true,
    val showFormula: Boolean = true,
    val showSecondaryResults: Boolean = true,
    val showCircleAreas: Boolean = true,
    val showCircumferences: Boolean = true,
    val hapticFeedback: Boolean = true,
    val confirmDelete: Boolean = true
)
