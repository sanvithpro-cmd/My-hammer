package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EqPresetDao {
    @Query("SELECT * FROM eq_presets ORDER BY id DESC")
    fun getAllPresets(): Flow<List<EqPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqPreset)

    @Delete
    suspend fun deletePreset(preset: EqPreset)

    @Query("SELECT * FROM eq_presets WHERE id = :id LIMIT 1")
    suspend fun getPresetById(id: Int): EqPreset?
}
