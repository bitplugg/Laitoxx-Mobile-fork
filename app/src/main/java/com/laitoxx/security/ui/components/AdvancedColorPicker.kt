package com.laitoxx.security.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.math.*

/**
 * Продвинутый ColorPicker с HSV, RGB, Alpha и HEX
 */
@Composable
fun AdvancedColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    // Конвертация initial color в HSV
    val hsv = remember {
        val rgb = floatArrayOf(
            initialColor.red,
            initialColor.green,
            initialColor.blue
        )
        android.graphics.Color.RGBToHSV(
            (rgb[0] * 255).toInt(),
            (rgb[1] * 255).toInt(),
            (rgb[2] * 255).toInt(),
            FloatArray(3)
        )
        FloatArray(3)
    }

    var hue by remember { mutableStateOf(hsv[0]) }
    var saturation by remember { mutableStateOf(hsv[1]) }
    var value by remember { mutableStateOf(hsv[2]) }
    var alpha by remember { mutableStateOf(initialColor.alpha) }

    // Текущий цвет
    val currentColor by remember {
        derivedStateOf {
            val rgb = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
            Color(
                red = android.graphics.Color.red(rgb) / 255f,
                green = android.graphics.Color.green(rgb) / 255f,
                blue = android.graphics.Color.blue(rgb) / 255f,
                alpha = alpha
            )
        }
    }

    var hexValue by remember {
        mutableStateOf(
            String.format(
                "%02X%02X%02X%02X",
                (initialColor.alpha * 255).toInt(),
                (initialColor.red * 255).toInt(),
                (initialColor.green * 255).toInt(),
                (initialColor.blue * 255).toInt()
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Выбор цвета",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Color Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(currentColor)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
                )

                // HSV Color Wheel
                HsvColorWheel(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onColorChange = { h, s, v ->
                        hue = h
                        saturation = s
                        value = v
                        hexValue = String.format(
                            "%02X%02X%02X%02X",
                            (alpha * 255).toInt(),
                            (currentColor.red * 255).toInt(),
                            (currentColor.green * 255).toInt(),
                            (currentColor.blue * 255).toInt()
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )

                // Alpha Slider
                Text(
                    text = "Прозрачность: ${(alpha * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AlphaSlider(
                    alpha = alpha,
                    color = currentColor.copy(alpha = 1f),
                    onAlphaChange = {
                        alpha = it
                        hexValue = String.format(
                            "%02X%02X%02X%02X",
                            (alpha * 255).toInt(),
                            (currentColor.red * 255).toInt(),
                            (currentColor.green * 255).toInt(),
                            (currentColor.blue * 255).toInt()
                        )
                    }
                )

                // RGB Sliders
                RgbSliders(
                    color = currentColor,
                    onColorChange = { newColor ->
                        // Convert RGB to HSV
                        val newHsv = FloatArray(3)
                        android.graphics.Color.RGBToHSV(
                            (newColor.red * 255).toInt(),
                            (newColor.green * 255).toInt(),
                            (newColor.blue * 255).toInt(),
                            newHsv
                        )
                        hue = newHsv[0]
                        saturation = newHsv[1]
                        value = newHsv[2]
                        alpha = newColor.alpha
                        hexValue = String.format(
                            "%02X%02X%02X%02X",
                            (alpha * 255).toInt(),
                            (newColor.red * 255).toInt(),
                            (newColor.green * 255).toInt(),
                            (newColor.blue * 255).toInt()
                        )
                    }
                )

                // HEX Input
                HexColorInput(
                    hexValue = hexValue,
                    onHexChange = { hex ->
                        hexValue = hex
                        try {
                            val color = android.graphics.Color.parseColor("#$hex")
                            val newHsv = FloatArray(3)
                            android.graphics.Color.RGBToHSV(
                                android.graphics.Color.red(color),
                                android.graphics.Color.green(color),
                                android.graphics.Color.blue(color),
                                newHsv
                            )
                            hue = newHsv[0]
                            saturation = newHsv[1]
                            value = newHsv[2]
                            alpha = android.graphics.Color.alpha(color) / 255f
                        } catch (e: Exception) {
                            // Invalid hex, ignore
                        }
                    }
                )

                // Preset Colors
                PresetColorsGrid(
                    onColorClick = { color ->
                        val newHsv = FloatArray(3)
                        android.graphics.Color.RGBToHSV(
                            (color.red * 255).toInt(),
                            (color.green * 255).toInt(),
                            (color.blue * 255).toInt(),
                            newHsv
                        )
                        hue = newHsv[0]
                        saturation = newHsv[1]
                        value = newHsv[2]
                        alpha = color.alpha
                        hexValue = String.format(
                            "%02X%02X%02X%02X",
                            (alpha * 255).toInt(),
                            (color.red * 255).toInt(),
                            (color.green * 255).toInt(),
                            (color.blue * 255).toInt()
                        )
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отмена")
                    }

                    Button(
                        onClick = {
                            onColorSelected(currentColor)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Выбрать")
                    }
                }
            }
        }
    }
}

/**
 * HSV Color Wheel
 */
@Composable
fun HsvColorWheel(
    hue: Float,
    saturation: Float,
    value: Float,
    onColorChange: (hue: Float, saturation: Float, value: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentHue by remember { mutableStateOf(hue) }
    var currentSaturation by remember { mutableStateOf(saturation) }
    var currentValue by remember { mutableStateOf(value) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val offset = change.position - center
                        val radius = min(size.width, size.height) / 2f

                        val distance = sqrt(offset.x * offset.x + offset.y * offset.y)
                        val angle = (atan2(offset.y, offset.x) * 180 / PI).toFloat()

                        currentHue = (angle + 360) % 360
                        currentSaturation = min(distance / radius, 1f)
                        currentValue = 1f

                        onColorChange(currentHue, currentSaturation, currentValue)
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f

            // Draw HSV color wheel
            for (i in 0..360 step 1) {
                for (j in 0..100 step 2) {
                    val angle = i * PI / 180
                    val r = radius * j / 100
                    val x = center.x + r * cos(angle).toFloat()
                    val y = center.y + r * sin(angle).toFloat()

                    val color = android.graphics.Color.HSVToColor(
                        floatArrayOf(i.toFloat(), j / 100f, 1f)
                    )

                    drawCircle(
                        color = Color(color),
                        radius = 2f,
                        center = Offset(x, y)
                    )
                }
            }

            // Draw selector
            val selectorAngle = currentHue * PI / 180
            val selectorRadius = radius * currentSaturation
            val selectorX = center.x + selectorRadius * cos(selectorAngle).toFloat()
            val selectorY = center.y + selectorRadius * sin(selectorAngle).toFloat()

            drawCircle(
                color = Color.White,
                radius = 12f,
                center = Offset(selectorX, selectorY),
                style = Stroke(width = 3f)
            )
            drawCircle(
                color = Color.Black,
                radius = 12f,
                center = Offset(selectorX, selectorY),
                style = Stroke(width = 1.5f)
            )
        }
    }
}

/**
 * Alpha Slider
 */
@Composable
fun AlphaSlider(
    alpha: Float,
    color: Color,
    onAlphaChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Slider(
            value = alpha,
            onValueChange = onAlphaChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

/**
 * RGB Sliders
 */
@Composable
fun RgbSliders(
    color: Color,
    onColorChange: (Color) -> Unit
) {
    var red by remember { mutableStateOf(color.red) }
    var green by remember { mutableStateOf(color.green) }
    var blue by remember { mutableStateOf(color.blue) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "RGB",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Red
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("R:", modifier = Modifier.width(24.dp), color = Color.Red)
            Slider(
                value = red,
                onValueChange = {
                    red = it
                    onColorChange(Color(red, green, blue, color.alpha))
                },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.Red,
                    activeTrackColor = Color.Red
                )
            )
            Text(
                text = "${(red * 255).toInt()}",
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End,
                fontSize = 12.sp
            )
        }

        // Green
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("G:", modifier = Modifier.width(24.dp), color = Color.Green)
            Slider(
                value = green,
                onValueChange = {
                    green = it
                    onColorChange(Color(red, green, blue, color.alpha))
                },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.Green,
                    activeTrackColor = Color.Green
                )
            )
            Text(
                text = "${(green * 255).toInt()}",
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End,
                fontSize = 12.sp
            )
        }

        // Blue
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("B:", modifier = Modifier.width(24.dp), color = Color.Blue)
            Slider(
                value = blue,
                onValueChange = {
                    blue = it
                    onColorChange(Color(red, green, blue, color.alpha))
                },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.Blue,
                    activeTrackColor = Color.Blue
                )
            )
            Text(
                text = "${(blue * 255).toInt()}",
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * HEX Color Input
 */
@Composable
fun HexColorInput(
    hexValue: String,
    onHexChange: (String) -> Unit
) {
    OutlinedTextField(
        value = hexValue,
        onValueChange = { value ->
            // Allow only hexadecimal characters
            val filtered = value.filter { it.isLetterOrDigit() }.take(8).uppercase()
            onHexChange(filtered)
        },
        label = { Text("HEX (AARRGGBB)") },
        prefix = { Text("#") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    )
}

/**
 * Preset Colors Grid
 */
@Composable
fun PresetColorsGrid(
    onColorClick: (Color) -> Unit
) {
    val presetColors = listOf(
        Color.Red, Color(0xFFFF5722), Color(0xFFFF9800),
        Color(0xFFFFEB3B), Color(0xFF8BC34A), Color(0xFF4CAF50),
        Color(0xFF009688), Color(0xFF00BCD4), Color(0xFF2196F3),
        Color(0xFF3F51B5), Color(0xFF9C27B0), Color(0xFFE91E63),
        Color.White, Color(0xFFE0E0E0), Color(0xFF9E9E9E),
        Color(0xFF607D8B), Color(0xFF424242), Color.Black
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Быстрый выбор",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in presetColors.chunked(6)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { color ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onColorClick(color) }
                        )
                    }
                }
            }
        }
    }
}
