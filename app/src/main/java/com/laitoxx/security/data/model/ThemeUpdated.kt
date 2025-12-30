package com.laitoxx.security.data.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Кастомная тема приложения с поддержкой визуальных эффектов
 */
@Serializable
data class AppTheme(
    val name: String = "Default",
    val author: String = "LAITOXX",
    val version: String = "1.0",

    // Background colors
    val backgroundPrimary: String = "#0D0D0D",
    val backgroundSecondary: String = "#1A1A1A",
    val backgroundTertiary: String = "#252525",

    // Text colors
    val textPrimary: String = "#E0E0E0",
    val textSecondary: String = "#B0B0B0",
    val textTertiary: String = "#808080",

    // Accent colors
    val accentPrimary: String = "#DC143C",
    val accentSecondary: String = "#B22222",
    val accentTertiary: String = "#8B0000",

    // Card colors
    val cardBackground: String = "#1A1A1A",
    val cardBorder: String = "#DC143C",
    val cardShadow: String = "#000000",

    // Button colors
    val buttonBackground: String = "#DC143C",
    val buttonText: String = "#FFFFFF",
    val buttonBorder: String = "#FF1744",
    val buttonHover: String = "#B22222",

    // Input field colors
    val inputBackground: String = "#1A1A1A",
    val inputBorder: String = "#DC143C",
    val inputText: String = "#E0E0E0",
    val inputPlaceholder: String = "#808080",
    val inputFocusBorder: String = "#FF1744",

    // Status colors
    val successColor: String = "#4CAF50",
    val errorColor: String = "#F44336",
    val warningColor: String = "#FF9800",
    val infoColor: String = "#2196F3",

    // Special effects
    val glowColor: String = "#DC143C",
    val shadowColor: String = "#000000",
    val highlightColor: String = "#FF1744",

    // Scrollbar colors
    val scrollbarThumb: String = "#DC143C",
    val scrollbarTrack: String = "#1A1A1A",

    // Additional UI elements
    val dividerColor: String = "#333333",
    val tooltipBackground: String = "#252525",
    val tooltipText: String = "#E0E0E0",

    // Custom properties
    val borderRadius: Int = 8,
    val cardElevation: Int = 4,
    val buttonElevation: Int = 2,

    // Typography
    val fontSizeSmall: Int = 12,
    val fontSizeMedium: Int = 14,
    val fontSizeLarge: Int = 16,
    val fontSizeXLarge: Int = 20,

    // Spacing
    val spacingSmall: Int = 8,
    val spacingMedium: Int = 16,
    val spacingLarge: Int = 24,

    // Optional background image
    val backgroundImageUrl: String? = null,
    val backgroundBlur: Int = 0,
    val backgroundOpacity: Float = 1.0f,

    // Visual Effect (NEW!)
    val visualEffect: String = "NONE",
    val effectParameters: EffectParameters = EffectParameters()
) {
    fun getVisualEffect(): VisualEffect = VisualEffect.fromString(visualEffect)

    fun hexToColor(hex: String): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            when (cleanHex.length) {
                6 -> Color(android.graphics.Color.parseColor("#$cleanHex"))
                8 -> Color(android.graphics.Color.parseColor("#$cleanHex"))
                else -> Color.White
            }
        } catch (e: Exception) {
            Color.White
        }
    }

    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        fun fromJson(jsonString: String): AppTheme? {
            return try {
                json.decodeFromString<AppTheme>(jsonString)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 14 предустановленных тем с визуальными эффектами
         */
        fun getPresetThemes(): List<AppTheme> {
            return listOf(
                // 1. Default Dark (NONE)
                AppTheme(
                    name = "Default Dark",
                    author = "LAITOXX",
                    visualEffect = "NONE"
                ),

                // 2. Soft Neumorphism (NEUMORPHISM)
                AppTheme(
                    name = "Soft Neumorphism",
                    author = "LAITOXX",
                    backgroundPrimary = "#E0E5EC",
                    backgroundSecondary = "#E0E5EC",
                    backgroundTertiary = "#D1D9E6",
                    textPrimary = "#444444",
                    textSecondary = "#666666",
                    textTertiary = "#888888",
                    accentPrimary = "#9C27B0",
                    accentSecondary = "#7B1FA2",
                    accentTertiary = "#6A1B9A",
                    cardBackground = "#E0E5EC",
                    cardBorder = "#9C27B0",
                    buttonBackground = "#9C27B0",
                    inputBackground = "#E0E5EC",
                    inputText = "#444444",
                    inputPlaceholder = "#888888",
                    dividerColor = "#C8D0E7",
                    visualEffect = "NEUMORPHISM",
                    effectParameters = EffectParameters.getDefaultForEffect(VisualEffect.NEUMORPHISM)
                ),

                // 4. Aurora Borealis (AURORA)
                AppTheme(
                    name = "Aurora Borealis",
                    author = "LAITOXX",
                    backgroundPrimary = "#0A0E27",
                    backgroundSecondary = "#101836",
                    backgroundTertiary = "#162247",
                    accentPrimary = "#00FF00",
                    accentSecondary = "#9C27B0",
                    accentTertiary = "#2196F3",
                    cardBackground = "#101836",
                    cardBorder = "#00FF00",
                    buttonBackground = "#00FF00",
                    buttonText = "#000000",
                    glowColor = "#00FF00",
                    visualEffect = "AURORA",
                    effectParameters = EffectParameters.getDefaultForEffect(VisualEffect.AURORA)
                ),

                // 5. Cyberpunk Neon (CYBERPUNK_NEON)
                AppTheme(
                    name = "Cyberpunk Neon",
                    author = "LAITOXX",
                    backgroundPrimary = "#0D0D0D",
                    backgroundSecondary = "#1A1A1A",
                    backgroundTertiary = "#252525",
                    accentPrimary = "#FF1493",
                    accentSecondary = "#00D9FF",
                    accentTertiary = "#00FFFF",
                    cardBorder = "#FF1493",
                    buttonBackground = "#FF1493",
                    inputBorder = "#FF1493",
                    glowColor = "#FF1493",
                    visualEffect = "CYBERPUNK_NEON",
                    effectParameters = EffectParameters.getDefaultForEffect(VisualEffect.CYBERPUNK_NEON)
                ),

                // 6. Metallic Chrome (METALLIC_CHROME)
                AppTheme(
                    name = "Metallic Chrome",
                    author = "LAITOXX",
                    backgroundPrimary = "#1A1A1A",
                    backgroundSecondary = "#2A2A2A",
                    backgroundTertiary = "#3A3A3A",
                    accentPrimary = "#C0C0C0",
                    accentSecondary = "#E8E8E8",
                    accentTertiary = "#A8A8A8",
                    cardBackground = "#2A2A2A",
                    visualEffect = "METALLIC_CHROME",
                    effectParameters = EffectParameters.getDefaultForEffect(VisualEffect.METALLIC_CHROME)
                ),

                // 7. Sky & Clouds (SKY_CLOUDS)
                AppTheme(
                    name = "Sky & Clouds",
                    author = "LAITOXX",
                    backgroundPrimary = "#87CEEB",
                    backgroundSecondary = "#B0E0E6",
                    backgroundTertiary = "#ADD8E6",
                    textPrimary = "#2C3E50",
                    textSecondary = "#34495E",
                    textTertiary = "#5D6D7E",
                    accentPrimary = "#3498DB",
                    accentSecondary = "#2980B9",
                    accentTertiary = "#1F618D",
                    cardBackground = "#FFFFFF",
                    cardBorder = "#3498DB",
                    buttonBackground = "#3498DB",
                    inputBackground = "#FFFFFF",
                    inputText = "#2C3E50",
                    visualEffect = "SKY_CLOUDS",
                    effectParameters = EffectParameters.getDefaultForEffect(VisualEffect.SKY_CLOUDS)
                ),

                // 8. Retro Vaporwave (VAPORWAVE)
                AppTheme(
                    name = "Retro Vaporwave",
                    author = "LAITOXX",
                    backgroundPrimary = "#1A0033",
                    backgroundSecondary = "#2D004D",
                    backgroundTertiary = "#3F0066",
                    accentPrimary = "#FF1493",
                    accentSecondary = "#00D9FF",
                    accentTertiary = "#9C27B0",
                    cardBorder = "#FF1493",
                    buttonBackground = "#FF1493",
                    glowColor = "#FF1493",
                    visualEffect = "VAPORWAVE",
                    effectParameters = EffectParameters.getDefaultForEffect(VisualEffect.VAPORWAVE)
                ),

                // 9. Classic Skeuomorphism (SKEUOMORPHISM)
                AppTheme(
                    name = "Classic Skeuomorphism",
                    author = "LAITOXX",
                    backgroundPrimary = "#3E2723",
                    backgroundSecondary = "#5D4037",
                    backgroundTertiary = "#6D4C41",
                    accentPrimary = "#8D6E63",
                    accentSecondary = "#A1887F",
                    accentTertiary = "#BCAAA4",
                    textPrimary = "#EFEBE9",
                    visualEffect = "SKEUOMORPHISM",
                    effectParameters = EffectParameters.getDefaultForEffect(VisualEffect.SKEUOMORPHISM)
                ),

                // 10. Minimal Dark Glass (MINIMAL_DARK_GLASS)
                AppTheme(
                    name = "Minimal Dark Glass",
                    author = "LAITOXX",
                    backgroundPrimary = "#000000",
                    backgroundSecondary = "#0D0D0D",
                    backgroundTertiary = "#1A1A1A",
                    accentPrimary = "#FFFFFF",
                    accentSecondary = "#E0E0E0",
                    accentTertiary = "#B0B0B0",
                    textPrimary = "#FFFFFF",
                    textSecondary = "#E0E0E0",
                    cardBackground = "#0D0D0D",
                    cardBorder = "#FFFFFF",
                    buttonBackground = "#FFFFFF",
                    buttonText = "#000000",
                    visualEffect = "MINIMAL_DARK_GLASS",
                    effectParameters = EffectParameters.getDefaultForEffect(VisualEffect.MINIMAL_DARK_GLASS)
                ),

                // 11. Ocean Wave (OCEAN_WAVE)
                AppTheme(
                    name = "Ocean Wave",
                    author = "LAITOXX",
                    backgroundPrimary = "#001F3F",
                    backgroundSecondary = "#003366",
                    backgroundTertiary = "#004080",
                    accentPrimary = "#0099CC",
                    accentSecondary = "#00D9FF",
                    accentTertiary = "#006994",
                    cardBackground = "#003366",
                    cardBorder = "#0099CC",
                    buttonBackground = "#0099CC",
                    glowColor = "#00D9FF",
                    visualEffect = "OCEAN_WAVE",
                    effectParameters = EffectParameters.getDefaultForEffect(VisualEffect.OCEAN_WAVE)
                ),

                // 12. Holographic Liquid (HOLOGRAPHIC)
                AppTheme(
                    name = "Holographic Liquid",
                    author = "LAITOXX",
                    backgroundPrimary = "#0D0D0D",
                    backgroundSecondary = "#1A1A1A",
                    backgroundTertiary = "#252525",
                    accentPrimary = "#FF1493",
                    accentSecondary = "#00D9FF",
                    accentTertiary = "#9C27B0",
                    cardBorder = "#FF1493",
                    buttonBackground = "#FF1493",
                    glowColor = "#FF1493",
                    visualEffect = "HOLOGRAPHIC",
                    effectParameters = EffectParameters.getDefaultForEffect(VisualEffect.HOLOGRAPHIC)
                ),

                // 13. Candy Pastel (CANDY_PASTEL)
                AppTheme(
                    name = "Candy Pastel",
                    author = "LAITOXX",
                    backgroundPrimary = "#FFF0F5",
                    backgroundSecondary = "#FFE4E1",
                    backgroundTertiary = "#FFD1DC",
                    textPrimary = "#8B4789",
                    textSecondary = "#A569BD",
                    textTertiary = "#C39BD3",
                    accentPrimary = "#FFB6C1",
                    accentSecondary = "#98D8C8",
                    accentTertiary = "#E6E6FA",
                    cardBackground = "#FFFFFF",
                    cardBorder = "#FFB6C1",
                    buttonBackground = "#FFB6C1",
                    buttonText = "#8B4789",
                    inputBackground = "#FFFFFF",
                    inputText = "#8B4789",
                    visualEffect = "CANDY_PASTEL",
                    effectParameters = EffectParameters.getDefaultForEffect(VisualEffect.CANDY_PASTEL)
                ),

                // 14. Space Nebula (SPACE_NEBULA)
                AppTheme(
                    name = "Space Nebula",
                    author = "LAITOXX",
                    backgroundPrimary = "#0F0F1E",
                    backgroundSecondary = "#16213E",
                    backgroundTertiary = "#1A1A2E",
                    accentPrimary = "#E94560",
                    accentSecondary = "#0F3460",
                    accentTertiary = "#533483",
                    cardBackground = "#16213E",
                    cardBorder = "#E94560",
                    buttonBackground = "#E94560",
                    glowColor = "#E94560",
                    visualEffect = "SPACE_NEBULA",
                    effectParameters = EffectParameters.getDefaultForEffect(VisualEffect.SPACE_NEBULA)
                )
            )
        }
    }
}

/**
 * Менеджер тем для управления пользовательскими темами
 */
@Serializable
data class ThemeCollection(
    val themes: MutableList<AppTheme> = mutableListOf(),
    val currentThemeIndex: Int = 0
) {
    fun getCurrentTheme(): AppTheme {
        return themes.getOrNull(currentThemeIndex) ?: AppTheme()
    }

    fun addTheme(theme: AppTheme) {
        themes.add(theme)
    }

    fun removeTheme(index: Int) {
        if (index in themes.indices && themes.size > 1) {
            themes.removeAt(index)
        }
    }

    fun updateTheme(index: Int, theme: AppTheme) {
        if (index in themes.indices) {
            themes[index] = theme
        }
    }

    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        fun fromJson(jsonString: String): ThemeCollection? {
            return try {
                json.decodeFromString<ThemeCollection>(jsonString)
            } catch (e: Exception) {
                null
            }
        }
    }
}
