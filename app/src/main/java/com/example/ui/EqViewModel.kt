package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.EqPreset
import com.example.data.EqRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Represents a pre-defined hardware preset
data class PredefinedPreset(
    val id: String,
    val name: String,
    val band60: Float,
    val band230: Float,
    val band910: Float,
    val band4k: Float,
    val band14k: Float,
    val bassBoost: Float
)

// Supported Hammer Earbud models
enum class HammerModel(val displayName: String, val hasAnc: Boolean) {
    SOLO_PRO("Hammer Solo Pro", true),
    AIR_FLOW("Hammer AirFlow", false),
    BASH_BT("Hammer Bash Over-Ear", true),
    TEMPEST("Hammer Tempest Wireless", true)
}

// Connection State
enum class ConnectionState {
    CONNECTED,
    CONNECTING,
    DISCONNECTED
}

class EqViewModel(private val repository: EqRepository) : ViewModel() {

    // Predefined default profiles
    val defaultPresets = listOf(
        PredefinedPreset("signature", "Signature (Balanced)", 1.5f, 0.5f, -0.5f, 2.0f, 1.5f, 30f),
        PredefinedPreset("bass_booster", "Bass Booster", 8.0f, 5.0f, 1.0f, -1.0f, -2.0f, 85f),
        PredefinedPreset("vocal_booster", "Vocal Booster", -4.0f, -1.0f, 4.0f, 6.0f, 2.0f, 10f),
        PredefinedPreset("treble_enhancer", "Treble Enhancer", -2.0f, -1.0f, 1.0f, 5.0f, 8.0f, 15f),
        PredefinedPreset("podcast", "Podcast / Spoken Word", -6.0f, 2.0f, 6.0f, 3.0f, -3.0f, 0f),
        PredefinedPreset("flat", "Flat (Neutral)", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0f)
    )

    // Selection ID (either a predefined ID, or "custom_db_{id}")
    private val _selectedPresetId = MutableStateFlow<String>("signature")
    val selectedPresetId: StateFlow<String> = _selectedPresetId.asStateFlow()

    // Current interactive slider states
    private val _band60 = MutableStateFlow(1.5f)
    val band60: StateFlow<Float> = _band60.asStateFlow()

    private val _band230 = MutableStateFlow(0.5f)
    val band230: StateFlow<Float> = _band230.asStateFlow()

    private val _band910 = MutableStateFlow(-0.5f)
    val band910: StateFlow<Float> = _band910.asStateFlow()

    private val _band4k = MutableStateFlow(2.0f)
    val band4k: StateFlow<Float> = _band4k.asStateFlow()

    private val _band14k = MutableStateFlow(1.5f)
    val band14k: StateFlow<Float> = _band14k.asStateFlow()

    // Sub-Bass Boost slider (0 to 100)
    private val _bassBoost = MutableStateFlow(30f)
    val bassBoost: StateFlow<Float> = _bassBoost.asStateFlow()

    // ANC mode (0 = Off/Normal, 1 = Transparency, 2 = ANC Active)
    private val _ancMode = MutableStateFlow(0)
    val ancMode: StateFlow<Int> = _ancMode.asStateFlow()

    // Gaming Mode (Low Latency)
    private val _gamingMode = MutableStateFlow(false)
    val gamingMode: StateFlow<Boolean> = _gamingMode.asStateFlow()

    // Hammer Model selection
    private val _selectedModel = MutableStateFlow(HammerModel.SOLO_PRO)
    val selectedModel: StateFlow<HammerModel> = _selectedModel.asStateFlow()

    // Connection State
    private val _connectionState = MutableStateFlow(ConnectionState.CONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // Simulated batteries
    private val _batteryLeft = MutableStateFlow(85)
    val batteryLeft: StateFlow<Int> = _batteryLeft.asStateFlow()

    private val _batteryRight = MutableStateFlow(90)
    val batteryRight: StateFlow<Int> = _batteryRight.asStateFlow()

    private val _batteryCase = MutableStateFlow(60)
    val batteryCase: StateFlow<Int> = _batteryCase.asStateFlow()

    // Dedicated physical/hardware level controls
    private val _bassLevel = MutableStateFlow(0) // -10 to 10
    val bassLevel: StateFlow<Int> = _bassLevel.asStateFlow()

    private val _trebleLevel = MutableStateFlow(0) // -10 to 10
    val trebleLevel: StateFlow<Int> = _trebleLevel.asStateFlow()

    private val _masterVolume = MutableStateFlow(70) // 0 to 100
    val masterVolume: StateFlow<Int> = _masterVolume.asStateFlow()

    fun incrementBass() {
        if (_bassLevel.value < 10) {
            _bassLevel.value += 1
            // Sync with hardware EQ sliders: slightly boost low frequencies
            _band60.value = (_band60.value + 0.5f).coerceIn(-12f, 12f)
            _band230.value = (_band230.value + 0.3f).coerceIn(-12f, 12f)
            checkCustomPresetActive()
        }
    }

    fun decrementBass() {
        if (_bassLevel.value > -10) {
            _bassLevel.value -= 1
            // Sync with hardware EQ sliders: slightly attenuate low frequencies
            _band60.value = (_band60.value - 0.5f).coerceIn(-12f, 12f)
            _band230.value = (_band230.value - 0.3f).coerceIn(-12f, 12f)
            checkCustomPresetActive()
        }
    }

    fun incrementTreble() {
        if (_trebleLevel.value < 10) {
            _trebleLevel.value += 1
            // Sync with hardware EQ sliders: slightly boost high frequencies
            _band4k.value = (_band4k.value + 0.5f).coerceIn(-12f, 12f)
            _band14k.value = (_band14k.value + 0.5f).coerceIn(-12f, 12f)
            checkCustomPresetActive()
        }
    }

    fun decrementTreble() {
        if (_trebleLevel.value > -10) {
            _trebleLevel.value -= 1
            // Sync with hardware EQ sliders: slightly attenuate high frequencies
            _band4k.value = (_band4k.value - 0.5f).coerceIn(-12f, 12f)
            _band14k.value = (_band14k.value - 0.5f).coerceIn(-12f, 12f)
            checkCustomPresetActive()
        }
    }

    fun incrementVolume() {
        _masterVolume.value = (_masterVolume.value + 5).coerceIn(0, 100)
    }

    fun decrementVolume() {
        _masterVolume.value = (_masterVolume.value - 5).coerceIn(0, 100)
    }

    fun setMasterVolume(value: Int) {
        _masterVolume.value = value.coerceIn(0, 100)
    }

    // Observe saved database custom presets
    val customPresets: StateFlow<List<EqPreset>> = repository.allPresets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Live combined presets list (predefined + custom db presets) for state observation if needed
    fun updateBand60(value: Float) {
        _band60.value = value
        checkCustomPresetActive()
    }

    fun updateBand230(value: Float) {
        _band230.value = value
        checkCustomPresetActive()
    }

    fun updateBand910(value: Float) {
        _band910.value = value
        checkCustomPresetActive()
    }

    fun updateBand4k(value: Float) {
        _band4k.value = value
        checkCustomPresetActive()
    }

    fun updateBand14k(value: Float) {
        _band14k.value = value
        checkCustomPresetActive()
    }

    fun updateBassBoost(value: Float) {
        _bassBoost.value = value
        checkCustomPresetActive()
    }

    fun updateAncMode(mode: Int) {
        _ancMode.value = mode
    }

    fun updateGamingMode(enabled: Boolean) {
        _gamingMode.value = enabled
    }

    fun selectModel(model: HammerModel) {
        _selectedModel.value = model
        // Re-simulate connection
        reconnectDevice()
    }

    fun toggleConnection() {
        viewModelScope.launch {
            if (_connectionState.value == ConnectionState.CONNECTED) {
                _connectionState.value = ConnectionState.DISCONNECTED
            } else {
                reconnectDevice()
            }
        }
    }

    private fun reconnectDevice() {
        viewModelScope.launch {
            _connectionState.value = ConnectionState.CONNECTING
            kotlinx.coroutines.delay(1000) // Small simulation delay
            _connectionState.value = ConnectionState.CONNECTED
            // Randomize batteries slightly for fidelity
            _batteryLeft.value = (75..95).random()
            _batteryRight.value = (80..100).random()
            _batteryCase.value = (40..80).random()
        }
    }

    // Selects a preset (can be predefined ID like "bass_booster" or DB custom preset "db_ID")
    fun applyPreset(presetId: String) {
        _selectedPresetId.value = presetId
        if (presetId.startsWith("db_")) {
            val dbIdStr = presetId.substringAfter("db_").toIntOrNull()
            if (dbIdStr != null) {
                viewModelScope.launch {
                    val dbPreset = repository.getPresetById(dbIdStr)
                    if (dbPreset != null) {
                        _band60.value = dbPreset.band60
                        _band230.value = dbPreset.band230
                        _band910.value = dbPreset.band910
                        _band4k.value = dbPreset.band4k
                        _band14k.value = dbPreset.band14k
                        _bassBoost.value = dbPreset.bassBoost
                        _ancMode.value = dbPreset.ancMode
                        _gamingMode.value = dbPreset.gamingMode
                    }
                }
            }
        } else {
            val predefined = defaultPresets.find { it.id == presetId }
            if (predefined != null) {
                _band60.value = predefined.band60
                _band230.value = predefined.band230
                _band910.value = predefined.band910
                _band4k.value = predefined.band4k
                _band14k.value = predefined.band14k
                _bassBoost.value = predefined.bassBoost
            }
        }
    }

    // Checks if the current slider config matches any predefined preset, otherwise marks as "custom"
    private fun checkCustomPresetActive() {
        val current60 = _band60.value
        val current230 = _band230.value
        val current910 = _band910.value
        val current4k = _band4k.value
        val current14k = _band14k.value
        val currentBass = _bassBoost.value

        val matchingPredefined = defaultPresets.find {
            it.band60 == current60 &&
            it.band230 == current230 &&
            it.band910 == current910 &&
            it.band4k == current4k &&
            it.band14k == current14k &&
            it.bassBoost == currentBass
        }

        if (matchingPredefined != null) {
            _selectedPresetId.value = matchingPredefined.id
        } else {
            // Check if matches any db custom presets
            val customList = customPresets.value
            val matchingCustom = customList.find {
                it.band60 == current60 &&
                it.band230 == current230 &&
                it.band910 == current910 &&
                it.band4k == current4k &&
                it.band14k == current14k &&
                it.bassBoost == currentBass
            }
            if (matchingCustom != null) {
                _selectedPresetId.value = "db_${matchingCustom.id}"
            } else {
                _selectedPresetId.value = "custom"
            }
        }
    }

    // Save the current config as a custom preset in Database
    fun saveCurrentAsCustomPreset(name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val newPreset = EqPreset(
                name = name,
                isCustom = true,
                band60 = _band60.value,
                band230 = _band230.value,
                band910 = _band910.value,
                band4k = _band4k.value,
                band14k = _band14k.value,
                bassBoost = _bassBoost.value,
                ancMode = _ancMode.value,
                gamingMode = _gamingMode.value
            )
            repository.insertPreset(newPreset)
            onSuccess()
            // Wait a split second to receive database updates, then select it
            // We can match by name or get the flow updates
        }
    }

    // Delete a custom preset
    fun deleteCustomPreset(preset: EqPreset) {
        viewModelScope.launch {
            repository.deletePreset(preset)
            if (_selectedPresetId.value == "db_${preset.id}") {
                applyPreset("signature")
            }
        }
    }

    // Reset current sliders to Flat neutral state
    fun resetToFlat() {
        applyPreset("flat")
    }
}

// ViewModel factory for custom instantiation
class EqViewModelFactory(private val repository: EqRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EqViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EqViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
