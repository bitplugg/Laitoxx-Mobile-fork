package com.laitoxx.security.data.model

import kotlinx.serialization.Serializable

/**
 * Визуальные эффекты для тем
 */
@Serializable
enum class VisualEffect(
    val displayName: String,
    val description: String
) {
    NONE(
        "Без эффектов",
        "Стандартный Material Design без спецэффектов"
    ),
    NEUMORPHISM(
        "Neumorphism",
        "Мягкие выдавленные элементы с тенями"
    ),
    AURORA(
        "Aurora Borealis",
        "Градиенты северного сияния"
    ),
    CYBERPUNK_NEON(
        "Cyberpunk Neon",
        "Неоновые акценты с glow-эффектами"
    ),
    METALLIC_CHROME(
        "Metallic Chrome",
        "Хромированные металлические поверхности"
    ),
    SKY_CLOUDS(
        "Sky & Clouds",
        "Небо с плавающими облаками"
    ),
    VAPORWAVE(
        "Retro Vaporwave",
        "Неоновые 80-90х с grid-линиями"
    ),
    SKEUOMORPHISM(
        "Classic Skeuomorphism",
        "Реалистичные текстуры (кожа, дерево, металл)"
    ),
    MINIMAL_DARK_GLASS(
        "Minimal Dark Glass",
        "Минималистичные полупрозрачные элементы"
    ),
    OCEAN_WAVE(
        "Ocean Wave",
        "Синие градиенты волн с ripple-анимацией"
    ),
    HOLOGRAPHIC(
        "Holographic Liquid",
        "Переливающиеся голографические градиенты"
    ),
    CANDY_PASTEL(
        "Candy Pastel",
        "Мягкие пастельные цвета"
    ),
    SPACE_NEBULA(
        "Space Nebula",
        "Космический фон с туманностями и звёздами"
    );

    companion object {
        fun fromString(name: String?): VisualEffect {
            return values().find { it.name == name } ?: NONE
        }
    }
}

/**
 * Параметры визуальных эффектов
 */
@Serializable
data class EffectParameters(
    val blurRadius: Float = 10f,           // Радиус blur для glassmorphism
    val glassOpacity: Float = 0.15f,       // Прозрачность стекла
    val shadowElevation: Float = 8f,       // Высота тени для neumorphism
    val glowIntensity: Float = 0.8f,       // Интенсивность свечения
    val animationSpeed: Float = 1.0f,      // Скорость анимаций
    val particleDensity: Float = 0.5f,     // Плотность частиц (для space nebula)

    // Градиенты для эффектов
    val gradientColors: List<String> = listOf(
        "#DC143C",
        "#FF1744",
        "#B22222"
    ),

    // Настройки анимации
    val enableFluidAnimation: Boolean = true,
    val enableParticles: Boolean = false,
    val enableGlow: Boolean = false,

    // Настройки текстур
    val textureUrl: String? = null,
    val textureOpacity: Float = 0.3f
) {
    companion object {
        fun getDefaultForEffect(effect: VisualEffect): EffectParameters {
            return when (effect) {
                VisualEffect.NEUMORPHISM -> EffectParameters(
                    shadowElevation = 12f,
                    glassOpacity = 0.05f
                )
                VisualEffect.AURORA -> EffectParameters(
                    gradientColors = listOf("#00FF00", "#9C27B0", "#2196F3"),
                    enableFluidAnimation = true,
                    glowIntensity = 0.9f
                )
                VisualEffect.CYBERPUNK_NEON -> EffectParameters(
                    gradientColors = listOf("#FF1493", "#00D9FF", "#00FFFF"),
                    enableGlow = true,
                    glowIntensity = 1.0f
                )
                VisualEffect.METALLIC_CHROME -> EffectParameters(
                    gradientColors = listOf("#C0C0C0", "#E8E8E8", "#A8A8A8"),
                    glassOpacity = 0.3f
                )
                VisualEffect.SKY_CLOUDS -> EffectParameters(
                    gradientColors = listOf("#87CEEB", "#FFFFFF", "#B0E0E6"),
                    enableFluidAnimation = true,
                    blurRadius = 15f
                )
                VisualEffect.VAPORWAVE -> EffectParameters(
                    gradientColors = listOf("#FF1493", "#00D9FF", "#9C27B0"),
                    enableGlow = true
                )
                VisualEffect.OCEAN_WAVE -> EffectParameters(
                    gradientColors = listOf("#006994", "#0099CC", "#00D9FF"),
                    enableFluidAnimation = true
                )
                VisualEffect.HOLOGRAPHIC -> EffectParameters(
                    gradientColors = listOf("#FF1493", "#00D9FF", "#9C27B0", "#FFD700"),
                    glowIntensity = 0.9f,
                    enableGlow = true
                )
                VisualEffect.CANDY_PASTEL -> EffectParameters(
                    gradientColors = listOf("#FFB6C1", "#98D8C8", "#E6E6FA"),
                    blurRadius = 3f,
                    glassOpacity = 0.2f
                )
                VisualEffect.SPACE_NEBULA -> EffectParameters(
                    gradientColors = listOf("#1A1A2E", "#16213E", "#0F3460", "#E94560"),
                    enableParticles = true,
                    particleDensity = 0.7f,
                    enableGlow = true
                )
                else -> EffectParameters()
            }
        }
    }
}
