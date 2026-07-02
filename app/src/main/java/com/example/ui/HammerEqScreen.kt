package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.EqPreset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HammerEqScreen(
    viewModel: EqViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val batteryLeft by viewModel.batteryLeft.collectAsStateWithLifecycle()
    val batteryRight by viewModel.batteryRight.collectAsStateWithLifecycle()
    val batteryCase by viewModel.batteryCase.collectAsStateWithLifecycle()

    val selectedPresetId by viewModel.selectedPresetId.collectAsStateWithLifecycle()
    val band60 by viewModel.band60.collectAsStateWithLifecycle()
    val band230 by viewModel.band230.collectAsStateWithLifecycle()
    val band910 by viewModel.band910.collectAsStateWithLifecycle()
    val band4k by viewModel.band4k.collectAsStateWithLifecycle()
    val band14k by viewModel.band14k.collectAsStateWithLifecycle()
    val bassBoost by viewModel.bassBoost.collectAsStateWithLifecycle()
    val ancMode by viewModel.ancMode.collectAsStateWithLifecycle()
    val gamingMode by viewModel.gamingMode.collectAsStateWithLifecycle()
    val customPresets by viewModel.customPresets.collectAsStateWithLifecycle()

    val bassLevel by viewModel.bassLevel.collectAsStateWithLifecycle()
    val trebleLevel by viewModel.trebleLevel.collectAsStateWithLifecycle()
    val masterVolume by viewModel.masterVolume.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    val currentBatteryLeft = if (connectionState == ConnectionState.CONNECTED) "$batteryLeft%" else "Offline"
    val isConnected = connectionState == ConnectionState.CONNECTED

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "Hammer Pro",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isConnected) Color(0xFF4ADE80) else Color(0xFFEF4444)
                                    )
                            )
                            Text(
                                text = if (isConnected) "Connected • $currentBatteryLeft" else "Disconnected",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                },
                actions = {
                    // Theme toggle button with styled circular background to match spec
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onThemeToggle(!isDarkTheme) }
                            .testTag("theme_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Switch Theme Mode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Audio Equalizer Tab"
                        )
                    },
                    label = {
                        Text(
                            text = "Audio EQ",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary,
                        unselectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("tab_audio")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "User Manual Tab"
                        )
                    },
                    label = {
                        Text(
                            text = "User Guide",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary,
                        unselectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("tab_manual")
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // 1. Connection & Model Selector Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Model Dropdown Selection
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { modelDropdownExpanded = true }
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = "Device Selection",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = selectedModel.displayName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Model",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            DropdownMenu(
                                expanded = modelDropdownExpanded,
                                onDismissRequest = { modelDropdownExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                HammerModel.values().forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model.displayName) },
                                        onClick = {
                                            viewModel.selectModel(model)
                                            modelDropdownExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Headphones,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // Connection Status Pill
                        val statusColor = when (connectionState) {
                            ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary
                            ConnectionState.CONNECTING -> MaterialTheme.colorScheme.tertiary
                            ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.error
                        }
                        val statusText = when (connectionState) {
                            ConnectionState.CONNECTED -> "Connected"
                            ConnectionState.CONNECTING -> "Connecting..."
                            ConnectionState.DISCONNECTED -> "Disconnected"
                        }

                        Surface(
                            shape = CircleShape,
                            color = statusColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clickable { viewModel.toggleConnection() }
                                .testTag("connection_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Text(
                                    text = statusText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Battery Stats
                    AnimatedVisibility(visible = connectionState == ConnectionState.CONNECTED) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            BatteryIndicator(label = "LEFT L", percentage = batteryLeft, icon = Icons.Outlined.Headphones)
                            BatteryIndicator(label = "RIGHT R", percentage = batteryRight, icon = Icons.Outlined.Headphones)
                            BatteryIndicator(label = "CASE", percentage = batteryCase, icon = Icons.Outlined.ChargingStation)
                        }
                    }

                    AnimatedVisibility(visible = connectionState == ConnectionState.DISCONNECTED) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tap Connection Pill to reconnect Hammer earbuds.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    AnimatedVisibility(visible = connectionState == ConnectionState.CONNECTING) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Establishing secure hardware handshake...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // 2. High-Fidelity Interactive EQ Curve Visualizer
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "FREQUENCY RESPONSE CURVE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Visual Canvas Curve
                    EqResponseCurve(
                        b60 = band60,
                        b230 = band230,
                        b910 = band910,
                        b4k = band4k,
                        b14k = band14k,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            }

            // 3. Preset Quick Chips Bar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SOUND PROFILES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (selectedPresetId == "custom") {
                        TextButton(
                            onClick = { showSaveDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Current", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (selectedPresetId != "flat") {
                        TextButton(
                            onClick = { viewModel.resetToFlat() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Flat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Predefined Default Presets
                    viewModel.defaultPresets.forEach { preset ->
                        val isSelected = selectedPresetId == preset.id
                        PresetChip(
                            name = preset.name,
                            isSelected = isSelected,
                            isCustom = false,
                            onClick = { viewModel.applyPreset(preset.id) }
                        )
                    }

                    // Custom Database Presets
                    customPresets.forEach { dbPreset ->
                        val dbPresetId = "db_${dbPreset.id}"
                        val isSelected = selectedPresetId == dbPresetId
                        PresetChip(
                            name = dbPreset.name,
                            isSelected = isSelected,
                            isCustom = true,
                            onDelete = { viewModel.deleteCustomPreset(dbPreset) },
                            onClick = { viewModel.applyPreset(dbPresetId) }
                        )
                    }

                    // Temporary Custom Chip if user edited sliders without saving
                    if (selectedPresetId == "custom") {
                        PresetChip(
                            name = "Unsaved Custom *",
                            isSelected = true,
                            isCustom = true,
                            onDelete = null,
                            onClick = {}
                        )
                    }
                }
            }

            // 4. Equalizer Interactive Console (Vertical Sliders)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "5-BAND EQUALIZER CONSOLE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        VerticalEqSlider(
                            label = "60Hz",
                            subLabel = "Sub-bass",
                            value = band60,
                            onValueChange = { viewModel.updateBand60(it) },
                            modifier = Modifier.weight(1f)
                        )
                        VerticalEqSlider(
                            label = "230Hz",
                            subLabel = "Low-mid",
                            value = band230,
                            onValueChange = { viewModel.updateBand230(it) },
                            modifier = Modifier.weight(1f)
                        )
                        VerticalEqSlider(
                            label = "910Hz",
                            subLabel = "Midrange",
                            value = band910,
                            onValueChange = { viewModel.updateBand910(it) },
                            modifier = Modifier.weight(1f)
                        )
                        VerticalEqSlider(
                            label = "4kHz",
                            subLabel = "Upper-mid",
                            value = band4k,
                            onValueChange = { viewModel.updateBand4k(it) },
                            modifier = Modifier.weight(1f)
                        )
                        VerticalEqSlider(
                            label = "14kHz",
                            subLabel = "Presence",
                            value = band14k,
                            onValueChange = { viewModel.updateBand14k(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Special Hammer Audio Hardware Controls Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "HAMMER PRO HARDWARE ENGINE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Master Volume, Bass offset, Treble offset tactile hardware controllers
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tactile Hardware Controllers",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Volume Controller
                        HardwareControlRow(
                            title = "Earbud Master Volume",
                            valueText = "$masterVolume%",
                            onDecrement = { viewModel.decrementVolume() },
                            onIncrement = { viewModel.incrementVolume() },
                            progress = masterVolume / 100f,
                            icon = Icons.Default.VolumeUp,
                            decrementEnabled = masterVolume > 0,
                            incrementEnabled = masterVolume < 100,
                            testTagPrefix = "volume"
                        )

                        // Bass Controller
                        HardwareControlRow(
                            title = "Coaxial Bass Response",
                            valueText = if (bassLevel >= 0) "+$bassLevel dB" else "$bassLevel dB",
                            onDecrement = { viewModel.decrementBass() },
                            onIncrement = { viewModel.incrementBass() },
                            progress = (bassLevel + 10) / 20f,
                            icon = Icons.Default.GraphicEq,
                            decrementEnabled = bassLevel > -10,
                            incrementEnabled = bassLevel < 10,
                            testTagPrefix = "bass"
                        )

                        // Treble Controller
                        HardwareControlRow(
                            title = "High-Freq Treble Sparkle",
                            valueText = if (trebleLevel >= 0) "+$trebleLevel dB" else "$trebleLevel dB",
                            onDecrement = { viewModel.decrementTreble() },
                            onIncrement = { viewModel.incrementTreble() },
                            progress = (trebleLevel + 10) / 20f,
                            icon = Icons.Default.Waves,
                            decrementEnabled = trebleLevel > -10,
                            incrementEnabled = trebleLevel < 10,
                            testTagPrefix = "treble"
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Sub-Bass Boost Card Detail
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Hammer Bass Boost+",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${bassBoost.toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = bassBoost,
                            onValueChange = { viewModel.updateBassBoost(it) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("bass_boost_slider")
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // ANC Controls (If the selected model has ANC capabilities)
                    if (selectedModel.hasAnc) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Active Noise Control (ANC)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val modes = listOf("Normal", "Ambient", "ANC Active")
                                val icons = listOf(Icons.Default.VolumeUp, Icons.Default.Hearing, Icons.Default.HearingDisabled)

                                modes.forEachIndexed { index, modeName ->
                                    val isSelected = ancMode == index
                                    val bgCol by animateColorAsState(
                                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        animationSpec = tween(200)
                                    )
                                    val fgCol by animateColorAsState(
                                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        animationSpec = tween(200)
                                    )

                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bgCol)
                                            .clickable { viewModel.updateAncMode(index) }
                                            .padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icons[index],
                                            contentDescription = modeName,
                                            tint = fgCol,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = modeName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = fgCol
                                        )
                                    }
                                }
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }

                    // Low Latency Gaming Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = if (gamingMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Low-Latency Gaming Mode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Bypasses buffering to drop audio delay down to a synchronized 40ms.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Switch(
                            checked = gamingMode,
                            onCheckedChange = { viewModel.updateGamingMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("gaming_mode_switch")
                        )
                    }
                }
            }

            // Small aesthetic footer
            Text(
                text = "Hammer Audio Equalizer v1.0.0 (Local Handshake Active)",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
        } else {
            UserManualTab(innerPadding)
        }
    }

    // Save Preset Dialog Modal
    if (showSaveDialog) {
        SavePresetDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                viewModel.saveCurrentAsCustomPreset(name) {
                    showSaveDialog = false
                }
            }
        )
    }
}

@Composable
fun BatteryIndicator(
    label: String,
    percentage: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(90.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 0.5.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "$percentage%",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (percentage > 20) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            )
        }
        // Visual small battery bar
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage / 100f)
                    .clip(CircleShape)
                    .background(
                        if (percentage > 20) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
            )
        }
    }
}

@Composable
fun HardwareControlRow(
    title: String,
    valueText: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    progress: Float, // 0f to 1f
    icon: ImageVector,
    decrementEnabled: Boolean = true,
    incrementEnabled: Boolean = true,
    testTagPrefix: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Icon and Titles
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = valueText,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Small segment progress bar to show level visually
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // Tactile Control Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Minus Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (decrementEnabled) MaterialTheme.colorScheme.surfaceVariant 
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    .clickable(enabled = decrementEnabled) { onDecrement() }
                    .testTag("${testTagPrefix}_decrement"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease $title",
                    tint = if (decrementEnabled) MaterialTheme.colorScheme.onSurface 
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Plus Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (incrementEnabled) MaterialTheme.colorScheme.surfaceVariant 
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    .clickable(enabled = incrementEnabled) { onIncrement() }
                    .testTag("${testTagPrefix}_increment"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase $title",
                    tint = if (incrementEnabled) MaterialTheme.colorScheme.onSurface 
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PresetChip(
    name: String,
    isSelected: Boolean,
    isCustom: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = modifier
            .testTag("preset_chip_$name")
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isCustom) Icons.Default.BookmarkBorder else Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor.copy(alpha = 0.8f)
            )
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )

            if (isCustom && onDelete != null) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Preset",
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .clickable { onDelete() }
                )
            }
        }
    }
}

@Composable
fun VerticalEqSlider(
    label: String,
    subLabel: String,
    value: Float, // value is from -12f to +12f
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .testTag("slider_band_$label"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subLabel,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )        // Custom vertical slider column container
        val primaryColor = MaterialTheme.colorScheme.primary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface

        Box(
            modifier = Modifier
                .weight(1f)
                .width(48.dp) // Touch target width
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background line of slider
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(surfaceVariantColor)
            )

            // Compose Slider oriented vertically by overriding layout or using custom Canvas or Slider itself
            // Standard Slider in Compose is horizontal. We can rotate it, or build a custom vertical Slider
            // with absolute touch interaction! Let's build an extremely reliable vertical slider using standard
            // Slider with Modifier.graphicsLayer(rotationZ = 270f) or custom implementation.
            // Wait, rotating a standard Slider by 270f makes it vertical, but touch bounds can get tricky in Compose.
            // A custom slider is incredibly simple to write in standard Canvas, but standard Slider rotated with
            // fixed width/height constraint is standard. Let's do a vertical custom pointerInput slider or a clean rotated one!
            // Wait! A custom pointerInput slider is extremely reliable and lightweight:
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = false) {} // block click
                    .background(Color.Transparent)
            ) {
                val maxHeightPx = constraints.maxHeight.toFloat()
                
                // Track visual bounds (middle line and active filled line)
                val sliderPositionPercent = 1f - (value + 12f) / 24f // 0 at +12dB (top), 1 at -12dB (bottom)
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val trackX = size.width / 2f
                    val topPadding = 12.dp.toPx()
                    val bottomPadding = 12.dp.toPx()
                    val usableHeight = size.height - topPadding - bottomPadding
                    
                    val centerZeroY = topPadding + usableHeight / 2f
                    val thumbY = topPadding + sliderPositionPercent * usableHeight
 
                    // 1. Draw central zero line marker
                    drawLine(
                        color = onSurfaceColor.copy(alpha = 0.2f),
                        start = Offset(trackX - 12.dp.toPx(), centerZeroY),
                        end = Offset(trackX + 12.dp.toPx(), centerZeroY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
 
                    // 2. Draw slider track background
                    drawLine(
                        color = surfaceVariantColor.copy(alpha = 0.4f),
                        start = Offset(trackX, topPadding),
                        end = Offset(trackX, size.height - bottomPadding),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
 
                    // 3. Draw active level color (themed matching style)
                    drawLine(
                        color = if (value >= 0) primaryColor else tertiaryColor,
                        start = Offset(trackX, centerZeroY),
                        end = Offset(trackX, thumbY),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
 
                    // 4. Draw Thumb
                    drawCircle(
                        color = onSurfaceColor,
                        radius = 8.dp.toPx(),
                        center = Offset(trackX, thumbY)
                    )
                    drawCircle(
                        color = if (value >= 0) primaryColor else tertiaryColor,
                        radius = 5.dp.toPx(),
                        center = Offset(trackX, thumbY)
                    )
                }

                // Invisible touch sensor for reliable vertical dragging
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val touchY = change.position.y
                                val topPadding = 12.dp.toPx()
                                val bottomPadding = 12.dp.toPx()
                                val usableHeight = size.height - topPadding - bottomPadding
                                val relativeY = (touchY - topPadding).coerceIn(0f, usableHeight)
                                val newPercent = relativeY / usableHeight
                                val newValue = 12f - (newPercent * 24f)
                                // Standard round to 1 decimal place for pristine neatness
                                onValueChange(String.format(Locale.US, "%.1f", newValue).toFloat())
                            }
                        }
                        .clickable {
                            // Tap also moves slider to position
                        }
                )
            }
        }

        // dB value display
        val valueText = if (value > 0) "+${String.format(Locale.US, "%.1f", value)}dB"
                        else if (value < 0) "${String.format(Locale.US, "%.1f", value)}dB"
                        else "0.0dB"
        val valueColor = if (value > 0) MaterialTheme.colorScheme.primary
                         else if (value < 0) MaterialTheme.colorScheme.tertiary
                         else MaterialTheme.colorScheme.secondary

        Text(
            text = valueText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

// Cubic Bezier visualizer for 5 frequency bands
@Composable
fun EqResponseCurve(
    b60: Float,
    b230: Float,
    b910: Float,
    b4k: Float,
    b14k: Float,
    modifier: Modifier = Modifier
) {
    val animated60 by animateFloatAsState(targetValue = b60, animationSpec = tween(150))
    val animated230 by animateFloatAsState(targetValue = b230, animationSpec = tween(150))
    val animated910 by animateFloatAsState(targetValue = b910, animationSpec = tween(150))
    val animated4k by animateFloatAsState(targetValue = b4k, animationSpec = tween(150))
    val animated14k by animateFloatAsState(targetValue = b14k, animationSpec = tween(150))

    val gridColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val topPad = 8.dp.toPx()
            val bottomPad = 8.dp.toPx()
            val graphHeight = height - topPad - bottomPad

            // 1. Draw decibel grid lines (Horizontal)
            val dbSteps = listOf("+12dB", "+6dB", "0dB", "-6dB", "-12dB")
            val dbPcts = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
            
            dbPcts.forEachIndexed { index, pct ->
                val lineY = topPad + pct * graphHeight
                drawLine(
                    color = gridColor.copy(alpha = 0.5f),
                    start = Offset(0f, lineY),
                    end = Offset(width, lineY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 2. Draw frequency grid labels (Vertical columns)
            val freqLabels = listOf("60Hz", "230Hz", "910Hz", "4kHz", "14kHz")
            val xPercents = listOf(0.12f, 0.31f, 0.50f, 0.69f, 0.88f)

            xPercents.forEachIndexed { index, pct ->
                val lineX = pct * width
                drawLine(
                    color = gridColor.copy(alpha = 0.3f),
                    start = Offset(lineX, topPad),
                    end = Offset(lineX, height - bottomPad),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 3. Build smooth Cubic Bezier path connecting the current sliders
            // Map slider range -12..12 to percent range 0..1 (inverse)
            fun mapValueToY(value: Float): Float {
                val clamped = value.coerceIn(-12f, 12f)
                val pct = 1f - (clamped + 12f) / 24f // -12dB -> bottom (1f), +12dB -> top (0f)
                return topPad + pct * graphHeight
            }

            val p0 = Offset(0f, mapValueToY(0f)) // anchor start at 0dB
            val p1 = Offset(xPercents[0] * width, mapValueToY(animated60))
            val p2 = Offset(xPercents[1] * width, mapValueToY(animated230))
            val p3 = Offset(xPercents[2] * width, mapValueToY(animated910))
            val p4 = Offset(xPercents[3] * width, mapValueToY(animated4k))
            val p5 = Offset(xPercents[4] * width, mapValueToY(animated14k))
            val p6 = Offset(width, mapValueToY(0f)) // anchor end at 0dB

            val points = listOf(p0, p1, p2, p3, p4, p5, p6)

            val curvePath = Path().apply {
                moveTo(p0.x, p0.y)
                for (i in 0 until points.size - 1) {
                    val current = points[i]
                    val next = points[i + 1]
                    val controlX1 = (current.x + next.x) / 2
                    val controlY1 = current.y
                    val controlX2 = (current.x + next.x) / 2
                    val controlY2 = next.y
                    cubicTo(controlX1, controlY1, controlX2, controlY2, next.x, next.y)
                }
            }

            // Draw glowing gradient background fill below the curve
            val fillPath = Path().apply {
                addPath(curvePath)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            val curveBrush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.35f),
                    primaryColor.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height
            )

            drawPath(
                path = fillPath,
                brush = curveBrush
            )

            // Draw dynamic neon color line (Primary for boosted treble/mid, coral/orange for deep bass highlights)
            drawPath(
                path = curvePath,
                color = primaryColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    miter = 4f,
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round
                )
            )

            // Draw points on frequency intersections
            val markerPoints = listOf(p1, p2, p3, p4, p5)
            markerPoints.forEach { pt ->
                drawCircle(
                    color = surfaceColor,
                    radius = 4.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = primaryColor,
                    radius = 2.5.dp.toPx(),
                    center = pt,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        // Overlay dB scale texts on the side cleanly without cluttering
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("+12 dB", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
            Text("0 dB", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
            Text("-12 dB", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
        }
    }
}

// Sleek interactive save preset popup dialog
@Composable
fun SavePresetDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Save Custom Preset",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Save your current custom 5-band configuration and sub-bass profile as a quick-select preset.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Preset Name") },
                    placeholder = { Text("e.g. Chill Acoustic, Heavy Metal") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        if (text.isNotBlank()) {
                            onSave(text.trim())
                        }
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("preset_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                onSave(text.trim())
                            }
                        },
                        enabled = text.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("save_confirm_button")
                    ) {
                        Text("Save Preset", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun UserManualTab(innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Hero Banner Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "HAMMER COMPANION MANUAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Pro Master Class",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Text(
                        text = "This guide details how to leverage Hammer Pro's real-time hardware companion app to customize, adjust, and optimize your high-fidelity listening experience.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Section Title: Quick Connection Guide
        Text(
            text = "HARDWARE HANDSHAKE & SETUP",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Guide Cards
        ManualStepCard(
            stepNumber = "01",
            title = "Target Selection",
            description = "At the top of the EQ tab, locate the model selector dropdown. Select your device (e.g. Earbuds Pro, Over-Ear Max, Buds Lite) to load the appropriate DSP driver and interface profile."
        )

        ManualStepCard(
            stepNumber = "02",
            title = "Acoustic Synchronization",
            description = "Our offline-first system automatically triggers a background Bluetooth/RF handshake. Once established, left and right earbud batteries and case telemetry updates automatically in real-time."
        )

        // Section Title: Equalizer Bands Explained
        Text(
            text = "THE 5-BAND FREQUENCY MATRIX",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                FrequencyGuideItem(
                    frequency = "60Hz",
                    bandLabel = "Sub-Bass Range",
                    description = "Focuses on deep rumble, pressure, and electronic sub-basses. Boost to make heavy synth tracks and movies feel chest-thumping.",
                    iconColor = MaterialTheme.colorScheme.primary
                )
                FrequencyGuideItem(
                    frequency = "230Hz",
                    bandLabel = "Warmth & Bass Body",
                    description = "Determines chest resonance of vocals, lower strings, and guitar body. Cut to remove mud; boost to add vintage warmth.",
                    iconColor = MaterialTheme.colorScheme.primary
                )
                FrequencyGuideItem(
                    frequency = "910Hz",
                    bandLabel = "Core Midrange Presence",
                    description = "The fundamental range for human vocals and speech clarity. Boosting this brings singers forward, while lowering creates a wider 'U-shaped' stage.",
                    iconColor = MaterialTheme.colorScheme.primary
                )
                FrequencyGuideItem(
                    frequency = "4kHz",
                    bandLabel = "Treble Presence & Bite",
                    description = "Enhances articulation, snare snaps, and acoustic note definitions. High levels yield high clarity, but over-boosting can cause auditory fatigue.",
                    iconColor = MaterialTheme.colorScheme.primary
                )
                FrequencyGuideItem(
                    frequency = "14kHz",
                    bandLabel = "High-Freq Air & Sparkle",
                    description = "Modulates ultra-high acoustic breath and cymbal sizzle. Boost to enhance soundstage dimension and spacious high-fidelity air.",
                    iconColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Section Title: Tactile Controls & Modes
        Text(
            text = "TACTILE ENGINE CONTROLLERS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                EngineGuideItem(
                    title = "Tactile Hardware Controllers",
                    description = "Quickly increase or decrease Master Volume, Bass, and Treble response. Changing Bass or Treble dynamically shifts multiple frequency sliders in tandem for instant, intuitive micro-tuning.",
                    icon = Icons.Default.Tune
                )
                EngineGuideItem(
                    title = "Acoustic Environment Isolator (ANC)",
                    description = "Switch On to engage deep sub-bass anti-phase filters, isolating you from constant external drone and road noise. Turn Off to enter Transparency Mode, allowing raw ambient sounds through.",
                    icon = Icons.Default.NoiseControlOff
                )
                EngineGuideItem(
                    title = "Low-Latency Gaming Mode",
                    description = "Bypasses high-precision lookahead compression buffers to reduce digital delivery delay down to a synchronized 40ms — optimal for gaming and cinematic sound synchronicity.",
                    icon = Icons.Default.SportsEsports
                )
            }
        }

        // Section Title: Preset Management
        Text(
            text = "CUSTOM PRESET ENGINE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Storing SQLite Profiles Locally",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Any bespoke configuration you tune can be permanently saved in your device's secure Room database. To save, click 'Save Custom' on the Equalizer view, give your tuning preset a descriptive label, and click save. To delete, simply long-press or tap delete next to any saved custom preset in the quick select list.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 18.sp
                )
            }
        }

        // Beautiful Footer
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hammer Pro Guide • Version 1.0.0",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    }
}

@Composable
fun ManualStepCard(
    stepNumber: String,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stepNumber,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 2.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun FrequencyGuideItem(
    frequency: String,
    bandLabel: String,
    description: String,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .width(54.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = frequency,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = iconColor
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = bandLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun EngineGuideItem(
    title: String,
    description: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary,
                lineHeight = 17.sp
            )
        }
    }
}



