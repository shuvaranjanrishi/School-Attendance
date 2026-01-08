package com.therishideveloper.schoolattendance.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
enum class AppLanguage(val code: String, val flag: String) {
    BENGALI("bn", "🇧🇩"),
    ENGLISH("en", "🇺🇸"),
    HINDI("hi", "🇮🇳")
}

private val Context.dataStore by preferencesDataStore("settings")

class SettingsManager @Inject constructor(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val THEME_KEY = stringPreferencesKey("theme_mode")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { pref ->
        pref[IS_LOGGED_IN] ?: false
    }

    // --- Language Logic ---
    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { pref ->
        val code = pref[LANGUAGE_KEY] ?: AppLanguage.ENGLISH.code
        AppLanguage.entries.find { it.code == code } ?: AppLanguage.ENGLISH
    }

    suspend fun saveLanguage(language: AppLanguage) {
        context.dataStore.edit { pref ->
            pref[LANGUAGE_KEY] = language.code
        }
    }

    // --- Theme Logic ---
    val themeFlow: Flow<ThemeMode> = context.dataStore.data.map { pref ->
        val modeName = pref[THEME_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeName)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun saveTheme(themeMode: ThemeMode) {
        context.dataStore.edit { pref ->
            pref[THEME_KEY] = themeMode.name
        }
    }

    suspend fun saveLoginStatus(isLoggedIn: Boolean) {
        context.dataStore.edit { pref ->
            pref[IS_LOGGED_IN] = isLoggedIn
        }
    }
}