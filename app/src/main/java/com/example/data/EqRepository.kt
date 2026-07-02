package com.example.data

import kotlinx.coroutines.flow.Flow

class EqRepository(private val eqPresetDao: EqPresetDao) {
    val allPresets: Flow<List<EqPreset>> = eqPresetDao.getAllPresets()

    suspend fun insertPreset(preset: EqPreset) {
        eqPresetDao.insertPreset(preset)
    }

    suspend fun deletePreset(preset: EqPreset) {
        eqPresetDao.deletePreset(preset)
    }

    suspend fun getPresetById(id: Int): EqPreset? {
        return eqPresetDao.getPresetById(id)
    }
}
