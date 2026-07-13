package com.noho501.externalcontrollerkit.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.noho501.externalcontrollerkit.logger.Logger
import com.noho501.externalcontrollerkit.models.Mapping
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.mappingDataStore by preferencesDataStore(name = "external_controller_kit")

@Singleton
class DataStoreMappingStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: Logger,
) : MappingStorage {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun loadMappings(): List<Mapping> {
        val preferences = context.mappingDataStore.data
            .catch { throwable ->
                logger.error("Failed to read mapping storage", throwable)
                emit(emptyPreferences())
            }
            .first()
        val raw = preferences[MAPPINGS_KEY].orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<Mapping>>(raw) }
            .onFailure { logger.error("Failed to decode mappings", it) }
            .getOrDefault(emptyList())
    }

    override suspend fun saveMappings(mappings: List<Mapping>) {
        context.mappingDataStore.edit { preferences ->
            preferences[MAPPINGS_KEY] = json.encodeToString(mappings)
        }
    }

    override suspend fun clearMappings() {
        context.mappingDataStore.edit { preferences ->
            preferences.remove(MAPPINGS_KEY)
        }
    }

    private companion object {
        val MAPPINGS_KEY = stringPreferencesKey("mappings_json")
    }
}
