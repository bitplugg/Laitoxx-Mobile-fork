package com.laitoxx.security.ui.effects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.laitoxx.security.data.model.AppTheme
import com.laitoxx.security.data.model.EffectParameters
import com.laitoxx.security.data.model.VisualEffect

/**
 * Extension function для применения визуальных эффектов к Modifier
 */
fun Modifier.applyVisualEffect(
    effect: VisualEffect,
    theme: AppTheme,
    isPressed: Boolean = false
): Modifier = when (effect) {
    VisualEffect.NEUMORPHISM -> this.neumorphism(theme.effectParameters, theme, isPressed)
    VisualEffect.CYBERPUNK_NEON -> this.cyberpunkGlow(theme.effectParameters, theme)
    VisualEffect.HOLOGRAPHIC -> this.holographicShimmer(theme.effectParameters, theme)
    VisualEffect.METALLIC_CHROME -> this.metallicChrome(theme.effectParameters, theme)
    VisualEffect.AURORA -> this.auroraGradient(theme.effectParameters, theme)
    VisualEffect.OCEAN_WAVE -> this.oceanWave(theme.effectParameters, theme)
    VisualEffect.SPACE_NEBULA -> this.spaceNebula(theme.effectParameters, theme)
    VisualEffect.CANDY_PASTEL -> this.candyPastel(theme.effectParameters, theme)
    else -> this
}

/**
 * Glassmorphism Effect - полупрозрачное стекло с blur
 */
fun Modifier.glassmorphism(
    params: EffectParameters,
    theme: AppTheme
): Modifier = this
    .background(
        color = theme.hexToColor(theme.backgroundSecondary)
            .copy(alpha = params.glassOpacity),
        shape = RoundedCornerShape(12.dp)
    )
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.2f),
                Color.White.copy(alpha = 0.05f)
            )
        ),
        shape = RoundedCornerShape(12.dp)
    )
    .blur(
        radius = params.blurRadius.dp,
        edgeTreatment = BlurredEdgeTreatment.Unbounded
    )

/**
 * Neumorphism Effect - мягкие выдавленные тени
 */
fun Modifier.neumorphism(
    params: EffectParameters,
    theme: AppTheme,
    pressed: Boolean = false
): Modifier = this
    .background(
        color = theme.hexToColor(theme.backgroundPrimary),
        shape = RoundedCornerShape(12.dp)
    )
    .drawBehind {
        val lightColor = Color.White.copy(alpha = 0.6f)
        val darkColor = Color.Black.copy(alpha = 0.3f)
        val elevation = params.shadowElevation

        if (!pressed) {
            // Светлая тень (сверху-слева)
            drawCircle(
                color = lightColor,
                radius = size.minDimension / 2,
                center = Offset(-elevation, -elevation)
            )

            // Тёмная тень (снизу-справа)
            drawCircle(
                color = darkColor,
                radius = size.minDimension / 2,
                center = Offset(elevation, elevation)
            )
        } else {
            // Pressed state - инвертированные тени
            drawCircle(
                color = darkColor,
                radius = size.minDimension / 2,
                center = Offset(-elevation / 2, -elevation / 2)
            )

            drawCircle(
                color = lightColor,
                radius = size.minDimension / 2,
                center = Offset(elevation / 2, elevation / 2)
            )
        }
    }

/**
 * Cyberpunk Neon Glow Effect
 */
fun Modifier.cyberpunkGlow(
    params: EffectParameters,
    theme: AppTheme
): Modifier = this
    .shadow(
        elevation = (params.glowIntensity * 12).dp,
        shape = RoundedCornerShape(12.dp),
        ambientColor = theme.hexToColor(theme.accentPrimary).copy(alpha = 0.6f),
        spotColor = theme.hexToColor(theme.accentPrimary).copy(alpha = 0.8f)
    )
    .border(
        width = 2.dp,
        brush = Brush.linearGradient(
            colors = params.gradientColors.map { Color(android.graphics.Color.parseColor(it)) }
        ),
        shape = RoundedCornerShape(12.dp)
    )
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                theme.hexToColor(theme.backgroundSecondary),
                theme.hexToColor(theme.backgroundTertiary)
            )
        ),
        shape = RoundedCornerShape(12.dp)
    )

/**
 * Holographic Shimmer Effect
 */
fun Modifier.holographicShimmer(
    params: EffectParameters,
    theme: AppTheme
): Modifier {
    val cornerShape = RoundedCornerShape(12.dp)
    return this
        .clip(cornerShape)
        .drawWithContent {
            drawContent()

            // Голографический градиент поверх контента
            drawRect(
                brush = Brush.linearGradient(
                    colors = params.gradientColors.map {
                        Color(android.graphics.Color.parseColor(it)).copy(alpha = 0.2f)
                    },
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
        }
        .border(
            width = 1.dp,
            brush = Brush.sweepGradient(
                colors = params.gradientColors.map { Color(android.graphics.Color.parseColor(it)) }
            ),
            shape = cornerShape
        )
}

/**
 * Metallic Chrome Effect
 */
fun Modifier.metallicChrome(
    params: EffectParameters,
    theme: AppTheme
): Modifier {
    val cornerShape = RoundedCornerShape(12.dp)
    return this
        .clip(cornerShape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE8E8E8),
                    Color(0xFFC0C0C0),
                    Color(0xFFA8A8A8),
                    Color(0xFFC0C0C0),
                    Color(0xFFE8E8E8)
                )
            )
        )
        .drawWithContent {
            drawContent()

            // Shine effect
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.1f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f)
                )
            )
        }
        .border(
            width = 1.dp,
            color = Color(0xFF999999),
            shape = cornerShape
        )
}

/**
 * Aurora Borealis Gradient Effect
 */
fun Modifier.auroraGradient(
    params: EffectParameters,
    theme: AppTheme
): Modifier = this
    .background(
        brush = Brush.verticalGradient(
            colors = params.gradientColors.map {
                Color(android.graphics.Color.parseColor(it)).copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = params.gradientColors.map { Color(android.graphics.Color.parseColor(it)) }
        ),
        shape = RoundedCornerShape(12.dp)
    )

/**
 * Ocean Wave Effect
 */
fun Modifier.oceanWave(
    params: EffectParameters,
    theme: AppTheme
): Modifier {
    val cornerShape = RoundedCornerShape(12.dp)
    return this
        .clip(cornerShape)
        .background(
            brush = Brush.verticalGradient(
                colors = params.gradientColors.map {
                    Color(android.graphics.Color.parseColor(it))
                }
            )
        )
        .drawWithContent {
            drawContent()

            // Wave overlay
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.1f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
        }
        .border(
            width = 1.dp,
            color = theme.hexToColor(theme.accentPrimary).copy(alpha = 0.3f),
            shape = cornerShape
        )
}

/**
 * Space Nebula Effect
 */
fun Modifier.spaceNebula(
    params: EffectParameters,
    theme: AppTheme
): Modifier = this
    .background(
        brush = Brush.radialGradient(
            colors = params.gradientColors.map {
                Color(android.graphics.Color.parseColor(it))
            }
        ),
        shape = RoundedCornerShape(12.dp)
    )
    .border(
        width = 1.dp,
        color = theme.hexToColor(theme.accentPrimary).copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    )

/**
 * Candy Pastel Effect
 */
fun Modifier.candyPastel(
    params: EffectParameters,
    theme: AppTheme
): Modifier = this
    .background(
        brush = Brush.linearGradient(
            colors = params.gradientColors.map {
                Color(android.graphics.Color.parseColor(it)).copy(alpha = 0.6f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    )
    .blur(
        radius = params.blurRadius.dp,
        edgeTreatment = BlurredEdgeTreatment.Unbounded
    )
    .border(
        width = 2.dp,
        color = Color.White.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp)
    )
