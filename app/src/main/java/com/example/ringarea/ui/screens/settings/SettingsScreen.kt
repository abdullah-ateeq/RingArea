package com.example.ringarea.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ringarea.data.model.MeasurementUnit
import com.example.ringarea.data.model.RingCalculationMode
import com.example.ringarea.data.model.ThemeMode
import com.example.ringarea.ui.util.Haptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val view = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "SETTINGS & PREFERENCES",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // Appearance Section
            SettingsSectionCard(title = "APPEARANCE") {
                // Theme Mode Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.values().forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = {
                                    Haptics.performLightFeedback(view, settings.hapticFeedback)
                                    viewModel.updateThemeMode(mode)
                                },
                                label = { Text(mode.label) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Dynamic Color Toggle
                SettingsSwitchRow(
                    title = "Dynamic Color",
                    subtitle = "Use Material You system palette (Android 12+)",
                    checked = settings.dynamicColor,
                    onCheckedChange = {
                        Haptics.performLightFeedback(view, settings.hapticFeedback)
                        viewModel.updateDynamicColor(it)
                    }
                )
            }

            // Calculation Defaults Section
            SettingsSectionCard(title = "CALCULATION DEFAULTS") {
                // Default Mode
                var modeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = modeExpanded,
                    onExpandedChange = { modeExpanded = !modeExpanded }
                ) {
                    OutlinedTextField(
                        value = settings.defaultMode.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Default Calculation Mode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = modeExpanded,
                        onDismissRequest = { modeExpanded = false }
                    ) {
                        RingCalculationMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.label) },
                                onClick = {
                                    Haptics.performLightFeedback(view, settings.hapticFeedback)
                                    viewModel.updateDefaultMode(mode)
                                    modeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Default Unit
                var unitExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded }
                ) {
                    OutlinedTextField(
                        value = "${settings.defaultUnit.label} (${settings.defaultUnit.symbol})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Default Measurement Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        MeasurementUnit.values().forEach { unit ->
                            DropdownMenuItem(
                                text = { Text("${unit.label} (${unit.symbol})") },
                                onClick = {
                                    Haptics.performLightFeedback(view, settings.hapticFeedback)
                                    viewModel.updateDefaultUnit(unit)
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Decimal Places Selector: 0, 1, 2, 3, 4
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Decimal Places: ${settings.decimalPlaces}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (0..4).forEach { p ->
                            FilterChip(
                                selected = settings.decimalPlaces == p,
                                onClick = {
                                    Haptics.performLightFeedback(view, settings.hapticFeedback)
                                    viewModel.updateDecimalPlaces(p)
                                },
                                label = { Text(p.toString()) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Display Options Section
            SettingsSectionCard(title = "DISPLAY & VISIBILITY") {
                SettingsSwitchRow(
                    title = "Show Technical Diagram",
                    subtitle = "Display concentric circles diagram on calculator",
                    checked = settings.showDiagram,
                    onCheckedChange = { viewModel.updateShowDiagram(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsSwitchRow(
                    title = "Show Formula Breakdown",
                    subtitle = "Expandable 'How it was calculated' card",
                    checked = settings.showFormula,
                    onCheckedChange = { viewModel.updateShowFormula(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsSwitchRow(
                    title = "Show Secondary Results",
                    subtitle = "Diameters, circumferences, and individual circle areas",
                    checked = settings.showSecondaryResults,
                    onCheckedChange = { viewModel.updateShowSecondaryResults(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsSwitchRow(
                    title = "Show Individual Circle Areas",
                    subtitle = "Outer Circle Area and Inner Circle Area",
                    checked = settings.showCircleAreas,
                    onCheckedChange = { viewModel.updateShowCircleAreas(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsSwitchRow(
                    title = "Show Circumferences",
                    subtitle = "Outer Circumference and Inner Circumference",
                    checked = settings.showCircumferences,
                    onCheckedChange = { viewModel.updateShowCircumferences(it) }
                )
            }

            // Interaction & Safeguards
            SettingsSectionCard(title = "INTERACTION & SAFEGUARDS") {
                SettingsSwitchRow(
                    title = "Haptic Feedback",
                    subtitle = "Subtle vibration response for key button actions",
                    checked = settings.hapticFeedback,
                    onCheckedChange = { viewModel.updateHapticFeedback(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsSwitchRow(
                    title = "Confirm Delete",
                    subtitle = "Prompt before deleting calculations or clearing history",
                    checked = settings.confirmDelete,
                    onCheckedChange = { viewModel.updateConfirmDelete(it) }
                )
            }

            // Data Management / Reset Actions
            SettingsSectionCard(title = "DATA & RESET") {
                OutlinedButton(
                    onClick = { showResetSettingsDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset All Settings to Default")
                }

                OutlinedButton(
                    onClick = { showClearHistoryDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Saved Calculations")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Dialog for Reset Settings
        if (showResetSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showResetSettingsDialog = false },
                title = { Text("Reset Settings?") },
                text = { Text("All preferences will be restored to their factory defaults. Your saved calculations will not be deleted.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.resetSettings()
                        showResetSettingsDialog = false
                    }) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetSettingsDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Dialog for Clear History
        if (showClearHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showClearHistoryDialog = false },
                title = { Text("Clear History?") },
                text = { Text("Are you sure you want to permanently clear all saved calculations?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.clearHistory()
                        showClearHistoryDialog = false
                    }) {
                        Text("Clear All", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
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
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
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
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
