package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eq_presets")
data class EqPreset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isCustom: Boolean = true,
    // 5-band Equalizer values from -12dB to +12dB
    val band60: Float = 0f,
    val band230: Float = 0f,
    val band910: Float = 0f,
    val band4k: Float = 0f,
    val band14k: Float = 0f,
    // Sub-Bass Boost slider value (0 to 100)
    val bassBoost: Float = 0f,
    // ANC Mode: 0 = Normal, 1 = Transparency, 2 = ANC
    val ancMode: Int = 0,
    // Gaming Mode (Low Latency)
    val gamingMode: Boolean = false
)
