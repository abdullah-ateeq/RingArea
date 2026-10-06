package com.example.ringarea.ui.screens.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ringarea.data.model.MeasurementUnit
import com.example.ringarea.data.model.RadiusInputMode
import com.example.ringarea.data.model.RingCalculationMode
import com.example.ringarea.data.model.UserSettings
import com.example.ringarea.domain.format.ResultFormatter
import com.example.ringarea.domain.unit.UnitConverter
import com.example.ringarea.ui.components.FormulaCard
import com.example.ringarea.ui.components.RingDiagram
import com.example.ringarea.ui.util.Haptics

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    userSettings: UserSettings,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideLayout = maxWidth >= 640.dp

            if (isWideLayout) {
                // Dual column for tablet/landscape
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HeaderSection()
                        ModeAndUnitSection(
                            uiState = uiState,
                            userSettings = userSettings,
                            onModeChange = {
                                Haptics.performLightFeedback(view, userSettings.hapticFeedback)
                                viewModel.onCalculationModeChange(it)
                            },
                            onInputModeChange = {
                                Haptics.performLightFeedback(view, userSettings.hapticFeedback)
                                viewModel.onInputModeChange(it)
                            },
                            onUnitChange = { viewModel.onUnitChange(it) }
                        )
                        InputsSection(
                            uiState = uiState,
                            onProjectNameChange = { viewModel.onProjectNameChange(it) },
                            onOuterDimensionChange = { viewModel.onOuterDimensionChange(it) },
                            onInnerDimensionChange = { viewModel.onInnerDimensionChange(it) },
                            onRingAreaChange = { viewModel.onRingAreaChange(it) }
                        )
                        ActionsSection(
                            uiState = uiState,
                            userSettings = userSettings,
                            onCopy = {
                                val text = viewModel.getShareSummary(userSettings.decimalPlaces)
                                copyToClipboard(context, text)
                                Haptics.performLightFeedback(view, userSettings.hapticFeedback)
                            },
                            onShare = {
                                val text = viewModel.getShareSummary(userSettings.decimalPlaces)
                                shareText(context, text)
                                Haptics.performLightFeedback(view, userSettings.hapticFeedback)
                            },
                            onSave = {
                                Haptics.performLightFeedback(view, userSettings.hapticFeedback)
                                viewModel.saveCalculation()
                            },
                            onReset = {
                                Haptics.performLightFeedback(view, userSettings.hapticFeedback)
                                viewModel.resetInputs()
                            }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (userSettings.showDiagram && uiState.result != null) {
                            RingDiagram(
                                result = uiState.result,
                                unit = uiState.unit,
                                decimals = userSettings.decimalPlaces
                            )
                        }

                        ResultsSection(
                            uiState = uiState,
                            userSettings = userSettings
                        )

                        if (userSettings.showFormula && uiState.result != null) {
                            FormulaCard(
                                mode = uiState.calculationMode,
                                result = uiState.result,
                                unit = uiState.unit,
                                decimals = userSettings.decimalPlaces
                            )
                        }

                        DisclaimerSection()
                    }
                }
            } else {
                // Single column for portrait phone
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeaderSection()

                    ModeAndUnitSection(
                        uiState = uiState,
                        userSettings = userSettings,
                        onModeChange = {
                            Haptics.performLightFeedback(view, userSettings.hapticFeedback)
                            viewModel.onCalculationModeChange(it)
                        },
                        onInputModeChange = {
                            Haptics.performLightFeedback(view, userSettings.hapticFeedback)
                            viewModel.onInputModeChange(it)
                        },
                        onUnitChange = { viewModel.onUnitChange(it) }
                    )

                    InputsSection(
                        uiState = uiState,
                        onProjectNameChange = { viewModel.onProjectNameChange(it) },
                        onOuterDimensionChange = { viewModel.onOuterDimensionChange(it) },
                        onInnerDimensionChange = { viewModel.onInnerDimensionChange(it) },
                        onRingAreaChange = { viewModel.onRingAreaChange(it) }
                    )

                    if (userSettings.showDiagram && uiState.result != null) {
                        RingDiagram(
                            result = uiState.result,
                            unit = uiState.unit,
                            decimals = userSettings.decimalPlaces
                        )
                    }

                    ResultsSection(
                        uiState = uiState,
                        userSettings = userSettings
                    )

                    ActionsSection(
                        uiState = uiState,
                        userSettings = userSettings,
                        onCopy = {
                            val text = viewModel.getShareSummary(userSettings.decimalPlaces)
                            copyToClipboard(context, text)
                            Haptics.performLightFeedback(view, userSettings.hapticFeedback)
                        },
                        onShare = {
                            val text = viewModel.getShareSummary(userSettings.decimalPlaces)
                            shareText(context, text)
                            Haptics.performLightFeedback(view, userSettings.hapticFeedback)
                        },
                        onSave = {
                            Haptics.performLightFeedback(view, userSettings.hapticFeedback)
                            viewModel.saveCalculation()
                        },
                        onReset = {
                            Haptics.performLightFeedback(view, userSettings.hapticFeedback)
                            viewModel.resetInputs()
                        }
                    )

                    if (userSettings.showFormula && uiState.result != null) {
                        FormulaCard(
                            mode = uiState.calculationMode,
                            result = uiState.result,
                            unit = uiState.unit,
                            decimals = userSettings.decimalPlaces
                        )
                    }

                    DisclaimerSection()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun HeaderSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "RINGAREA",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Calculate area between two concentric circles.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ModeAndUnitSection(
    uiState: CalculatorUiState,
    userSettings: UserSettings,
    onModeChange: (RingCalculationMode) -> Unit,
    onInputModeChange: (RadiusInputMode) -> Unit,
    onUnitChange: (MeasurementUnit) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Calculation Mode Selector Chips
        Text(
            text = "CALCULATION MODE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RingCalculationMode.values().forEach { mode ->
                FilterChip(
                    selected = uiState.calculationMode == mode,
                    onClick = { onModeChange(mode) },
                    label = {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (uiState.calculationMode == mode) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radius vs Diameter selector (for CALCULATE mode)
            if (uiState.calculationMode == RingCalculationMode.CALCULATE) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RadiusInputMode.values().forEach { mode ->
                        val isSelected = uiState.inputMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable { onInputModeChange(mode) }
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Measurement Unit Dropdown
            var unitMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = unitMenuExpanded,
                onExpandedChange = { unitMenuExpanded = !unitMenuExpanded }
            ) {
                OutlinedTextField(
                    value = "${uiState.unit.label} (${uiState.unit.symbol})",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unit", style = MaterialTheme.typography.labelSmall) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .width(160.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                ExposedDropdownMenu(
                    expanded = unitMenuExpanded,
                    onDismissRequest = { unitMenuExpanded = false }
                ) {
                    MeasurementUnit.values().forEach { u ->
                        DropdownMenuItem(
                            text = { Text("${u.label} (${u.symbol})") },
                            onClick = {
                                onUnitChange(u)
                                unitMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InputsSection(
    uiState: CalculatorUiState,
    onProjectNameChange: (String) -> Unit,
    onOuterDimensionChange: (String) -> Unit,
    onInnerDimensionChange: (String) -> Unit,
    onRingAreaChange: (String) -> Unit
) {
    val isDiameter = uiState.inputMode == RadiusInputMode.DIAMETER
    val outerLabel = if (isDiameter) "OUTER DIAMETER" else "OUTER RADIUS"
    val innerLabel = if (isDiameter) "INNER DIAMETER" else "INNER RADIUS"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Optional Project Name
            OutlinedTextField(
                value = uiState.projectName,
                onValueChange = onProjectNameChange,
                label = { Text("Project Name (Optional)") },
                placeholder = { Text("e.g. Pipe Washer, Table Cut, Garden Border") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            when (uiState.calculationMode) {
                RingCalculationMode.CALCULATE, RingCalculationMode.FIND_RING_WIDTH -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.outerDimensionText,
                            onValueChange = onOuterDimensionChange,
                            label = { Text(outerLabel) },
                            suffix = { Text(uiState.unit.symbol, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = uiState.innerDimensionText,
                            onValueChange = onInnerDimensionChange,
                            label = { Text(innerLabel) },
                            suffix = { Text(uiState.unit.symbol, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                RingCalculationMode.FIND_INNER_RADIUS -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.outerDimensionText,
                            onValueChange = onOuterDimensionChange,
                            label = { Text("OUTER RADIUS") },
                            suffix = { Text(uiState.unit.symbol, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = uiState.ringAreaText,
                            onValueChange = onRingAreaChange,
                            label = { Text("RING AREA") },
                            suffix = { Text(uiState.unit.areaSymbol, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                RingCalculationMode.FIND_OUTER_RADIUS -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.innerDimensionText,
                            onValueChange = onInnerDimensionChange,
                            label = { Text("INNER RADIUS") },
                            suffix = { Text(uiState.unit.symbol, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = uiState.ringAreaText,
                            onValueChange = onRingAreaChange,
                            label = { Text("RING AREA") },
                            suffix = { Text(uiState.unit.areaSymbol, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Validation Error Alert if any
            if (uiState.validationError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Validation Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = uiState.validationError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultsSection(
    uiState: CalculatorUiState,
    userSettings: UserSettings
) {
    val result = uiState.result ?: return
    val unit = uiState.unit
    val decimals = userSettings.decimalPlaces

    val outerR = UnitConverter.fromMillimeters(result.outerRadiusMm, unit)
    val innerR = UnitConverter.fromMillimeters(result.innerRadiusMm, unit)
    val ringWidth = UnitConverter.fromMillimeters(result.ringWidthMm, unit)
    val ringArea = UnitConverter.fromSquareMillimeters(result.ringAreaMm2, unit)

    val outerD = UnitConverter.fromMillimeters(result.outerDiameterMm, unit)
    val innerD = UnitConverter.fromMillimeters(result.innerDiameterMm, unit)

    val outerCirc = UnitConverter.fromMillimeters(result.outerCircumferenceMm, unit)
    val innerCirc = UnitConverter.fromMillimeters(result.innerCircumferenceMm, unit)

    val outerArea = UnitConverter.fromSquareMillimeters(result.outerCircleAreaMm2, unit)
    val innerArea = UnitConverter.fromSquareMillimeters(result.innerCircleAreaMm2, unit)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Primary Dominant Result Card: RING AREA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "PRIMARY RESULT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "RING AREA",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = ResultFormatter.formatArea(ringArea, unit, decimals),
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "RING WIDTH",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = ResultFormatter.formatDimension(ringWidth, unit, decimals),
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // 2. Secondary Results Card
        if (userSettings.showSecondaryResults) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "DIMENSIONS & PROPERTIES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ResultMetric(
                            label = "OUTER RADIUS",
                            value = ResultFormatter.formatDimension(outerR, unit, decimals),
                            modifier = Modifier.weight(1f)
                        )
                        ResultMetric(
                            label = "INNER RADIUS",
                            value = ResultFormatter.formatDimension(innerR, unit, decimals),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ResultMetric(
                            label = "OUTER DIAMETER",
                            value = ResultFormatter.formatDimension(outerD, unit, decimals),
                            modifier = Modifier.weight(1f)
                        )
                        ResultMetric(
                            label = "INNER DIAMETER",
                            value = ResultFormatter.formatDimension(innerD, unit, decimals),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (userSettings.showCircumferences) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ResultMetric(
                                label = "OUTER CIRCUMFERENCE",
                                value = ResultFormatter.formatDimension(outerCirc, unit, decimals),
                                modifier = Modifier.weight(1f)
                            )
                            ResultMetric(
                                label = "INNER CIRCUMFERENCE",
                                value = ResultFormatter.formatDimension(innerCirc, unit, decimals),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (userSettings.showCircleAreas) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ResultMetric(
                                label = "OUTER CIRCLE AREA",
                                value = ResultFormatter.formatArea(outerArea, unit, decimals),
                                modifier = Modifier.weight(1f)
                            )
                            ResultMetric(
                                label = "INNER CIRCLE AREA",
                                value = ResultFormatter.formatArea(innerArea, unit, decimals),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActionsSection(
    uiState: CalculatorUiState,
    userSettings: UserSettings,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit
) {
    val hasResult = uiState.result != null

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSave,
            enabled = hasResult,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = if (uiState.isSaved) Icons.Default.Check else Icons.Default.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (uiState.isSaved) "Saved" else "Save")
        }

        OutlinedButton(
            onClick = onCopy,
            enabled = hasResult,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copy")
        }

        OutlinedButton(
            onClick = onShare,
            enabled = hasResult,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share")
        }

        OutlinedButton(
            onClick = onReset,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset inputs",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun DisclaimerSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Geometry calculation only. Verify measurements, tolerances, material specifications, and project requirements before fabrication or construction.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("RingArea Result", text)
    clipboard.setPrimaryClip(clip)
}

private fun shareText(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share RingArea Calculation")
    context.startActivity(shareIntent)
}
