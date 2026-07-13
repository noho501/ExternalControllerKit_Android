package com.noho501.externalcontrollerkit.storage

import com.noho501.externalcontrollerkit.models.Mapping

interface MappingStorage {
    suspend fun loadMappings(): List<Mapping>
    suspend fun saveMappings(mappings: List<Mapping>)
    suspend fun clearMappings()
}
