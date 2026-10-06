package com.example.ringarea.ui.screens.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringarea.data.model.MeasurementUnit
import com.example.ringarea.data.model.RadiusInputMode
import com.example.ringarea.data.model.RingCalculationMode
import com.example.ringarea.data.model.RingResult
import com.example.ringarea.data.model.SavedCalculation
import com.example.ringarea.data.repository.HistoryRepository
import com.example.ringarea.data.repository.SettingsRepository
import com.example.ringarea.domain.format.ResultFormatter
import com.example.ringarea.domain.math.BigDecimalMath
import com.example.ringarea.domain.math.RingAreaCalculator
import com.example.ringarea.domain.unit.UnitConverter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

data class CalculatorUiState(
    val projectName: String = "",
    val calculationMode: RingCalculationMode = RingCalculationMode.CALCULATE,
    val inputMode: RadiusInputMode = RadiusInputMode.RADIUS,
    val outerDimensionText: String = "10",
    val innerDimensionText: String = "6",
    val ringAreaText: String = "",
    val unit: MeasurementUnit = MeasurementUnit.CENTIMETER,
    val result: RingResult? = null,
    val validationError: String? = null,
    val isSaved: Boolean = false
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val historyRepository = HistoryRepository(application)

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            _uiState.update {
                it.copy(
                    calculationMode = settings.defaultMode,
                    unit = settings.defaultUnit
                )
            }
            recalculate()
        }
    }

    fun onProjectNameChange(name: String) {
        _uiState.update { it.copy(projectName = name, isSaved = false) }
    }

    fun onCalculationModeChange(mode: RingCalculationMode) {
        _uiState.update { it.copy(calculationMode = mode, isSaved = false) }
        recalculate()
    }

    fun onInputModeChange(mode: RadiusInputMode) {
        val currentInputMode = _uiState.value.inputMode
        if (currentInputMode != mode) {
            val outerBd = BigDecimalMath.parseSafe(_uiState.value.outerDimensionText)
            val innerBd = BigDecimalMath.parseSafe(_uiState.value.innerDimensionText)

            val newOuterText = if (outerBd != null) {
                if (mode == RadiusInputMode.DIAMETER) {
                    // Radius -> Diameter: multiply by 2
                    ResultFormatter.formatPlain(outerBd.multiply(BigDecimalMath.TWO), 4)
                } else {
                    // Diameter -> Radius: divide by 2
                    ResultFormatter.formatPlain(outerBd.divide(BigDecimalMath.TWO, BigDecimalMath.MATH_CONTEXT), 4)
                }
            } else _uiState.value.outerDimensionText

            val newInnerText = if (innerBd != null) {
                if (mode == RadiusInputMode.DIAMETER) {
                    ResultFormatter.formatPlain(innerBd.multiply(BigDecimalMath.TWO), 4)
                } else {
                    ResultFormatter.formatPlain(innerBd.divide(BigDecimalMath.TWO, BigDecimalMath.MATH_CONTEXT), 4)
                }
            } else _uiState.value.innerDimensionText

            _uiState.update {
                it.copy(
                    inputMode = mode,
                    outerDimensionText = newOuterText,
                    innerDimensionText = newInnerText,
                    isSaved = false
                )
            }
            recalculate()
        }
    }

    fun onOuterDimensionChange(text: String) {
        _uiState.update { it.copy(outerDimensionText = text, isSaved = false) }
        recalculate()
    }

    fun onInnerDimensionChange(text: String) {
        _uiState.update { it.copy(innerDimensionText = text, isSaved = false) }
        recalculate()
    }

    fun onRingAreaChange(text: String) {
        _uiState.update { it.copy(ringAreaText = text, isSaved = false) }
        recalculate()
    }

    fun onUnitChange(newUnit: MeasurementUnit) {
        val currentUnit = _uiState.value.unit
        if (currentUnit == newUnit) return

        val outerBd = BigDecimalMath.parseSafe(_uiState.value.outerDimensionText)
        val innerBd = BigDecimalMath.parseSafe(_uiState.value.innerDimensionText)
        val areaBd = BigDecimalMath.parseSafe(_uiState.value.ringAreaText)

        val newOuterText = if (outerBd != null) {
            val converted = UnitConverter.convertLinear(outerBd, currentUnit, newUnit)
            ResultFormatter.formatPlain(converted, 4)
        } else _uiState.value.outerDimensionText

        val newInnerText = if (innerBd != null) {
            val converted = UnitConverter.convertLinear(innerBd, currentUnit, newUnit)
            ResultFormatter.formatPlain(converted, 4)
        } else _uiState.value.innerDimensionText

        val newAreaText = if (areaBd != null) {
            val converted = UnitConverter.convertArea(areaBd, currentUnit, newUnit)
            ResultFormatter.formatPlain(converted, 4)
        } else _uiState.value.ringAreaText

        _uiState.update {
            it.copy(
                unit = newUnit,
                outerDimensionText = newOuterText,
                innerDimensionText = newInnerText,
                ringAreaText = newAreaText,
                isSaved = false
            )
        }
        recalculate()
    }

    fun resetInputs() {
        _uiState.update {
            it.copy(
                projectName = "",
                calculationMode = RingCalculationMode.CALCULATE,
                inputMode = RadiusInputMode.RADIUS,
                outerDimensionText = "10",
                innerDimensionText = "6",
                ringAreaText = "",
                validationError = null,
                isSaved = false
            )
        }
        recalculate()
    }

    fun restoreCalculation(calc: SavedCalculation) {
        val targetUnit = calc.unit
        val outerRadiusMm = BigDecimal(calc.outerRadiusMm)
        val innerRadiusMm = BigDecimal(calc.innerRadiusMm)
        val ringAreaMm2 = BigDecimal(calc.ringAreaMm2)

        val outerVal = if (calc.inputMode == RadiusInputMode.DIAMETER) {
            UnitConverter.fromMillimeters(outerRadiusMm.multiply(BigDecimalMath.TWO), targetUnit)
        } else {
            UnitConverter.fromMillimeters(outerRadiusMm, targetUnit)
        }

        val innerVal = if (calc.inputMode == RadiusInputMode.DIAMETER) {
            UnitConverter.fromMillimeters(innerRadiusMm.multiply(BigDecimalMath.TWO), targetUnit)
        } else {
            UnitConverter.fromMillimeters(innerRadiusMm, targetUnit)
        }

        val areaVal = UnitConverter.fromSquareMillimeters(ringAreaMm2, targetUnit)

        _uiState.update {
            it.copy(
                projectName = calc.projectName,
                calculationMode = calc.calculationMode,
                inputMode = calc.inputMode,
                unit = calc.unit,
                outerDimensionText = ResultFormatter.formatPlain(outerVal, 4),
                innerDimensionText = ResultFormatter.formatPlain(innerVal, 4),
                ringAreaText = ResultFormatter.formatPlain(areaVal, 4),
                isSaved = true
            )
        }
        recalculate()
    }

    fun saveCalculation() {
        val state = _uiState.value
        val result = state.result ?: return
        val name = state.projectName.ifBlank { "Untitled Ring" }

        val item = SavedCalculation(
            id = UUID.randomUUID().toString(),
            projectName = name,
            timestamp = System.currentTimeMillis(),
            calculationMode = state.calculationMode,
            inputMode = state.inputMode,
            outerRadiusMm = result.outerRadiusMm.toPlainString(),
            innerRadiusMm = result.innerRadiusMm.toPlainString(),
            outerDiameterMm = result.outerDiameterMm.toPlainString(),
            innerDiameterMm = result.innerDiameterMm.toPlainString(),
            ringWidthMm = result.ringWidthMm.toPlainString(),
            ringAreaMm2 = result.ringAreaMm2.toPlainString(),
            outerCircleAreaMm2 = result.outerCircleAreaMm2.toPlainString(),
            innerCircleAreaMm2 = result.innerCircleAreaMm2.toPlainString(),
            outerCircumferenceMm = result.outerCircumferenceMm.toPlainString(),
            innerCircumferenceMm = result.innerCircumferenceMm.toPlainString(),
            unit = state.unit,
            isFavorite = false
        )

        viewModelScope.launch {
            historyRepository.saveCalculation(item)
            _uiState.update { it.copy(isSaved = true) }
            _snackbarMessage.emit("Calculation saved to history")
        }
    }

    fun getShareSummary(decimals: Int): String {
        val state = _uiState.value
        val result = state.result ?: return ""
        val unit = state.unit
        val projectName = state.projectName.ifBlank { "Concentric Ring" }

        val outerRadius = UnitConverter.fromMillimeters(result.outerRadiusMm, unit)
        val innerRadius = UnitConverter.fromMillimeters(result.innerRadiusMm, unit)
        val ringWidth = UnitConverter.fromMillimeters(result.ringWidthMm, unit)
        val ringArea = UnitConverter.fromSquareMillimeters(result.ringAreaMm2, unit)
        val outerDiameter = UnitConverter.fromMillimeters(result.outerDiameterMm, unit)
        val innerDiameter = UnitConverter.fromMillimeters(result.innerDiameterMm, unit)
        val outerCirc = UnitConverter.fromMillimeters(result.outerCircumferenceMm, unit)
        val innerCirc = UnitConverter.fromMillimeters(result.innerCircumferenceMm, unit)

        return buildString {
            appendLine("RingArea — $projectName")
            appendLine("Mode: ${state.calculationMode.label}")
            appendLine("Outer radius: ${ResultFormatter.formatDimension(outerRadius, unit, decimals)}")
            appendLine("Inner radius: ${ResultFormatter.formatDimension(innerRadius, unit, decimals)}")
            appendLine("Ring width: ${ResultFormatter.formatDimension(ringWidth, unit, decimals)}")
            appendLine("Ring area: ${ResultFormatter.formatArea(ringArea, unit, decimals)}")
            appendLine("Outer diameter: ${ResultFormatter.formatDimension(outerDiameter, unit, decimals)}")
            appendLine("Inner diameter: ${ResultFormatter.formatDimension(innerDiameter, unit, decimals)}")
            appendLine("Outer circumference: ${ResultFormatter.formatDimension(outerCirc, unit, decimals)}")
            appendLine("Inner circumference: ${ResultFormatter.formatDimension(innerCirc, unit, decimals)}")
            appendLine()
            append("Geometry calculation only. Verify measurements before fabrication.")
        }
    }

    private fun recalculate() {
        val state = _uiState.value
        val unit = state.unit
        val isDiameter = state.inputMode == RadiusInputMode.DIAMETER

        when (state.calculationMode) {
            RingCalculationMode.CALCULATE, RingCalculationMode.FIND_RING_WIDTH -> {
                val outerBd = BigDecimalMath.parseSafe(state.outerDimensionText)
                val innerBd = BigDecimalMath.parseSafe(state.innerDimensionText)

                val validation = RingAreaCalculator.validateRingInputs(outerBd, innerBd, isDiameter)
                if (validation != null) {
                    _uiState.update { it.copy(result = null, validationError = validation) }
                    return
                }

                val outerMmRaw = UnitConverter.toMillimeters(outerBd!!, unit)
                val innerMmRaw = UnitConverter.toMillimeters(innerBd!!, unit)

                val outerRadiusMm = if (isDiameter) {
                    outerMmRaw.divide(BigDecimalMath.TWO, BigDecimalMath.MATH_CONTEXT)
                } else outerMmRaw

                val innerRadiusMm = if (isDiameter) {
                    innerMmRaw.divide(BigDecimalMath.TWO, BigDecimalMath.MATH_CONTEXT)
                } else innerMmRaw

                try {
                    val res = RingAreaCalculator.calculateRing(outerRadiusMm, innerRadiusMm)
                    _uiState.update { it.copy(result = res, validationError = null) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(result = null, validationError = e.message ?: "Invalid geometry inputs.") }
                }
            }

            RingCalculationMode.FIND_INNER_RADIUS -> {
                val outerBd = BigDecimalMath.parseSafe(state.outerDimensionText)
                val areaBd = BigDecimalMath.parseSafe(state.ringAreaText)

                val validation = RingAreaCalculator.validateFindInnerRadiusInputs(outerBd, areaBd)
                if (validation != null) {
                    _uiState.update { it.copy(result = null, validationError = validation) }
                    return
                }

                val outerRadiusMm = UnitConverter.toMillimeters(outerBd!!, unit)
                val ringAreaMm2 = UnitConverter.toSquareMillimeters(areaBd!!, unit)

                try {
                    val innerRadiusMm = RingAreaCalculator.calculateInnerRadiusFromArea(outerRadiusMm, ringAreaMm2)
                    val res = RingAreaCalculator.calculateRing(outerRadiusMm, innerRadiusMm)
                    _uiState.update { it.copy(result = res, validationError = null) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(result = null, validationError = e.message ?: "Invalid geometry inputs.") }
                }
            }

            RingCalculationMode.FIND_OUTER_RADIUS -> {
                val innerBd = BigDecimalMath.parseSafe(state.innerDimensionText)
                val areaBd = BigDecimalMath.parseSafe(state.ringAreaText)

                val validation = RingAreaCalculator.validateFindOuterRadiusInputs(innerBd, areaBd)
                if (validation != null) {
                    _uiState.update { it.copy(result = null, validationError = validation) }
                    return
                }

                val innerRadiusMm = UnitConverter.toMillimeters(innerBd!!, unit)
                val ringAreaMm2 = UnitConverter.toSquareMillimeters(areaBd!!, unit)

                try {
                    val outerRadiusMm = RingAreaCalculator.calculateOuterRadiusFromArea(innerRadiusMm, ringAreaMm2)
                    val res = RingAreaCalculator.calculateRing(outerRadiusMm, innerRadiusMm)
                    _uiState.update { it.copy(result = res, validationError = null) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(result = null, validationError = e.message ?: "Invalid geometry inputs.") }
                }
            }
        }
    }
}
