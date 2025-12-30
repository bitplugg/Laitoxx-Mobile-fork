package com.laitoxx.security.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.laitoxx.security.data.model.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Менеджер для управления темами приложения
 * Сохраняет пользовательские темы и текущую выбранную тему
 */
class ThemeManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentTheme = MutableStateFlow(loadCurrentTheme())
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    private val _customThemes = MutableStateFlow(loadCustomThemes())
    val customThemes: StateFlow<List<AppTheme>> = _customThemes.asStateFlow()

    private val _allThemes = MutableStateFlow(getAllThemesList())
    val allThemes: StateFlow<List<AppTheme>> = _allThemes.asStateFlow()

    companion object {
        private const val PREFS_NAME = "laitoxx_themes"
        private const val KEY_CURRENT_THEME = "current_theme"
        private const val KEY_CUSTOM_THEMES = "custom_themes"
        private const val KEY_THEME_INDEX = "theme_index"

        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Загрузить текущую тему
     */
    private fun loadCurrentTheme(): AppTheme {
        val themeJson = prefs.getString(KEY_CURRENT_THEME, null)
        return if (themeJson != null) {
            AppTheme.fromJson(themeJson) ?: AppTheme.getPresetThemes()[0]
        } else {
            AppTheme.getPresetThemes()[0]
        }
    }

    /**
     * Загрузить пользовательские темы
     */
    private fun loadCustomThemes(): List<AppTheme> {
        val themesJson = prefs.getString(KEY_CUSTOM_THEMES, null) ?: return emptyList()
        return try {
            val themes = mutableListOf<AppTheme>()
            val jsonArray = org.json.JSONArray(themesJson)
            for (i in 0 until jsonArray.length()) {
                val themeJson = jsonArray.getString(i)
                AppTheme.fromJson(themeJson)?.let { themes.add(it) }
            }
            themes
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Получить список всех доступных тем (preset + custom)
     */
    private fun getAllThemesList(): List<AppTheme> {
        return AppTheme.getPresetThemes() + _customThemes.value
    }

    /**
     * Установить текущую тему
     */
    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        prefs.edit().putString(KEY_CURRENT_THEME, theme.toJson()).apply()
    }

    /**
     * Установить тему по индексу
     */
    fun setThemeByIndex(index: Int) {
        val theme = _allThemes.value.getOrNull(index)
        if (theme != null) {
            setTheme(theme)
            prefs.edit().putInt(KEY_THEME_INDEX, index).apply()
        }
    }

    /**
     * Получить индекс текущей темы
     */
    fun getCurrentThemeIndex(): Int {
        return prefs.getInt(KEY_THEME_INDEX, 0)
    }

    /**
     * Добавить пользовательскую тему
     */
    fun addCustomTheme(theme: AppTheme) {
        val updatedThemes = _customThemes.value.toMutableList()
        updatedThemes.add(theme)
        _customThemes.value = updatedThemes
        _allThemes.value = getAllThemesList()
        saveCustomThemes()
    }

    /**
     * Обновить пользовательскую тему
     */
    fun updateCustomTheme(index: Int, theme: AppTheme) {
        val customIndex = index - AppTheme.getPresetThemes().size
        if (customIndex in _customThemes.value.indices) {
            val updatedThemes = _customThemes.value.toMutableList()
            updatedThemes[customIndex] = theme
            _customThemes.value = updatedThemes
            _allThemes.value = getAllThemesList()
            saveCustomThemes()

            // Если это текущая тема, обновить её
            if (getCurrentThemeIndex() == index) {
                setTheme(theme)
            }
        }
    }

    /**
     * Удалить пользовательскую тему
     */
    fun deleteCustomTheme(index: Int) {
        val customIndex = index - AppTheme.getPresetThemes().size
        if (customIndex in _customThemes.value.indices) {
            val updatedThemes = _customThemes.value.toMutableList()
            updatedThemes.removeAt(customIndex)
            _customThemes.value = updatedThemes
            _allThemes.value = getAllThemesList()
            saveCustomThemes()

            // Если удалена текущая тема, переключиться на дефолтную
            if (getCurrentThemeIndex() == index) {
                setThemeByIndex(0)
            }
        }
    }

    /**
     * Сохранить пользовательские темы
     */
    private fun saveCustomThemes() {
        val jsonArray = org.json.JSONArray()
        _customThemes.value.forEach { theme ->
            jsonArray.put(theme.toJson())
        }
        prefs.edit().putString(KEY_CUSTOM_THEMES, jsonArray.toString()).apply()
    }

    /**
     * Экспортировать тему в JSON строку
     */
    fun exportTheme(theme: AppTheme): String {
        return theme.toJson()
    }

    /**
     * Импортировать тему из JSON строки
     */
    fun importTheme(jsonString: String): AppTheme? {
        return AppTheme.fromJson(jsonString)
    }

    /**
     * Сбросить на дефолтную тему
     */
    fun resetToDefault() {
        setThemeByIndex(0)
    }

    /**
     * Получить тему по имени
     */
    fun getThemeByName(name: String): AppTheme? {
        return _allThemes.value.find { it.name == name }
    }

    /**
     * Проверить является ли тема пользовательской
     */
    fun isCustomTheme(index: Int): Boolean {
        return index >= AppTheme.getPresetThemes().size
    }

    /**
     * Дублировать тему
     */
    fun duplicateTheme(theme: AppTheme, newName: String? = null): AppTheme {
        val name = newName ?: "${theme.name} (Copy)"
        return theme.copy(
            name = name,
            author = "User",
            version = "1.0"
        )
    }

    /**
     * Создать новую тему на основе текущей
     */
    fun createThemeFromCurrent(name: String): AppTheme {
        return _currentTheme.value.copy(
            name = name,
            author = "User",
            version = "1.0"
        )
    }
}
