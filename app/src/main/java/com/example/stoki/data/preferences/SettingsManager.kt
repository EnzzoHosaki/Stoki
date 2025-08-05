package com.example.stoki.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Cria uma instância do DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    // Chave para guardar a preferência do tema
    private val themeKey = booleanPreferencesKey("dark_theme_enabled")

    // Fluxo que emite 'true' se o modo escuro estiver ativado, 'false' caso contrário
    val themeFlow: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[themeKey]
    }

    // Função para salvar a preferência do usuário
    suspend fun setTheme(isDark: Boolean) {
        context.dataStore.edit { settings ->
            settings[themeKey] = isDark
        }
    }
}