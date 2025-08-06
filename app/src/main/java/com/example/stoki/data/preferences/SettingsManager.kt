package com.example.stoki.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    private val themeKey = booleanPreferencesKey("dark_theme_enabled")

    val themeFlow: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[themeKey]
    }

    suspend fun setTheme(isDark: Boolean) {
        context.dataStore.edit { settings ->
            settings[themeKey] = isDark
        }
    }
}