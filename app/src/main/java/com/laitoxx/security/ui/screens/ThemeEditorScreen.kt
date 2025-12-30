package com.laitoxx.security.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.ColorUtils
import androidx.navigation.NavController
import com.laitoxx.security.data.model.AppTheme
import com.laitoxx.security.ui.components.StyledTextField
import com.laitoxx.security.utils.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeEditorFullScreen(navController: NavController) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val scope = rememberCoroutineScope()

    var editingTheme by remember { mutableStateOf(currentTheme.copy(name = "${currentTheme.name} (Copy)", author = "User")) }
    var showColorPicker by remember { mutableStateOf(false) }
    var editingColorProperty by remember { mutableStateOf<ColorProperty?>(null) }
    var themeName by remember { mutableStateOf(editingTheme.name) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактор тем") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val newTheme = editingTheme.copy(name = themeName)
                                themeManager.addCustomTheme(newTheme)
                                themeManager.setTheme(newTheme)
                                Toast.makeText(context, "Тема \"$themeName\" сохранена и применена", Toast.LENGTH_SHORT).show()
                                navController.navigateUp()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, "Save", tint = Color(0xFF4CAF50))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Превью темы
            ThemePreviewCard(editingTheme)

            Spacer(modifier = Modifier.height(16.dp))

            // Название темы
            StyledTextField(
                value = themeName,
                onValueChange = { themeName = it },
                label = "Название темы",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Список настроек цветов
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Background Colors
                item {
                    ColorSectionHeader("Цвета фона")
                }
                item {
                    ColorPropertyItem(
                        label = "Основной фон",
                        color = editingTheme.hexToColor(editingTheme.backgroundPrimary),
                        onClick = {
                            editingColorProperty = ColorProperty.BackgroundPrimary
                            showColorPicker = true
                        }
                    )
                }
                item {
                    ColorPropertyItem(
                        label = "Вторичный фон",
                        color = editingTheme.hexToColor(editingTheme.backgroundSecondary),
                        onClick = {
                            editingColorProperty = ColorProperty.BackgroundSecondary
                            showColorPicker = true
                        }
                    )
                }
                item {
                    ColorPropertyItem(
                        label = "Третичный фон",
                        color = editingTheme.hexToColor(editingTheme.backgroundTertiary),
                        onClick = {
                            editingColorProperty = ColorProperty.BackgroundTertiary
                            showColorPicker = true
                        }
                    )
                }

                // Accent Colors
                item {
                    ColorSectionHeader("Акцентные цвета")
                }
                item {
                    ColorPropertyItem(
                        label = "Основной акцент",
                        color = editingTheme.hexToColor(editingTheme.accentPrimary),
                        onClick = {
                            editingColorProperty = ColorProperty.AccentPrimary
                            showColorPicker = true
                        }
                    )
                }
                item {
                    ColorPropertyItem(
                        label = "Вторичный акцент",
                        color = editingTheme.hexToColor(editingTheme.accentSecondary),
                        onClick = {
                            editingColorProperty = ColorProperty.AccentSecondary
                            showColorPicker = true
                        }
                    )
                }
                item {
                    ColorPropertyItem(
                        label = "Третичный акцент",
                        color = editingTheme.hexToColor(editingTheme.accentTertiary),
                        onClick = {
                            editingColorProperty = ColorProperty.AccentTertiary
                            showColorPicker = true
                        }
                    )
                }

                // Text Colors
                item {
                    ColorSectionHeader("Цвета текста")
                }
                item {
                    ColorPropertyItem(
                        label = "Основной текст",
                        color = editingTheme.hexToColor(editingTheme.textPrimary),
                        onClick = {
                            editingColorProperty = ColorProperty.TextPrimary
                            showColorPicker = true
                        }
                    )
                }
                item {
                    ColorPropertyItem(
                        label = "Вторичный текст",
                        color = editingTheme.hexToColor(editingTheme.textSecondary),
                        onClick = {
                            editingColorProperty = ColorProperty.TextSecondary
                            showColorPicker = true
                        }
                    )
                }

                // Card Colors
                item {
                    ColorSectionHeader("Цвета карточек")
                }
                item {
                    ColorPropertyItem(
                        label = "Фон карточек",
                        color = editingTheme.hexToColor(editingTheme.cardBackground),
                        onClick = {
                            editingColorProperty = ColorProperty.CardBackground
                            showColorPicker = true
                        }
                    )
                }
                item {
                    ColorPropertyItem(
                        label = "Рамка карточек",
                        color = editingTheme.hexToColor(editingTheme.cardBorder),
                        onClick = {
                            editingColorProperty = ColorProperty.CardBorder
                            showColorPicker = true
                        }
                    )
                }

                // Button Colors
                item {
                    ColorSectionHeader("Цвета кнопок")
                }
                item {
                    ColorPropertyItem(
                        label = "Фон кнопок",
                        color = editingTheme.hexToColor(editingTheme.buttonBackground),
                        onClick = {
                            editingColorProperty = ColorProperty.ButtonBackground
                            showColorPicker = true
                        }
                    )
                }
                item {
                    ColorPropertyItem(
                        label = "Текст кнопок",
                        color = editingTheme.hexToColor(editingTheme.buttonText),
                        onClick = {
                            editingColorProperty = ColorProperty.ButtonText
                            showColorPicker = true
                        }
                    )
                }

                // Status Colors
                item {
                    ColorSectionHeader("Статусные цвета")
                }
                item {
                    ColorPropertyItem(
                        label = "Успех",
                        color = editingTheme.hexToColor(editingTheme.successColor),
                        onClick = {
                            editingColorProperty = ColorProperty.SuccessColor
                            showColorPicker = true
                        }
                    )
                }
                item {
                    ColorPropertyItem(
                        label = "Ошибка",
                        color = editingTheme.hexToColor(editingTheme.errorColor),
                        onClick = {
                            editingColorProperty = ColorProperty.ErrorColor
                            showColorPicker = true
                        }
                    )
                }
                item {
                    ColorPropertyItem(
                        label = "Предупреждение",
                        color = editingTheme.hexToColor(editingTheme.warningColor),
                        onClick = {
                            editingColorProperty = ColorProperty.WarningColor
                            showColorPicker = true
                        }
                    )
                }
            }
        }
    }

    // Color Picker Dialog
    if (showColorPicker && editingColorProperty != null) {
        val currentColor = when (editingColorProperty!!) {
            ColorProperty.BackgroundPrimary -> editingTheme.hexToColor(editingTheme.backgroundPrimary)
            ColorProperty.BackgroundSecondary -> editingTheme.hexToColor(editingTheme.backgroundSecondary)
            ColorProperty.BackgroundTertiary -> editingTheme.hexToColor(editingTheme.backgroundTertiary)
            ColorProperty.AccentPrimary -> editingTheme.hexToColor(editingTheme.accentPrimary)
            ColorProperty.AccentSecondary -> editingTheme.hexToColor(editingTheme.accentSecondary)
            ColorProperty.AccentTertiary -> editingTheme.hexToColor(editingTheme.accentTertiary)
            ColorProperty.TextPrimary -> editingTheme.hexToColor(editingTheme.textPrimary)
            ColorProperty.TextSecondary -> editingTheme.hexToColor(editingTheme.textSecondary)
            ColorProperty.CardBackground -> editingTheme.hexToColor(editingTheme.cardBackground)
            ColorProperty.CardBorder -> editingTheme.hexToColor(editingTheme.cardBorder)
            ColorProperty.ButtonBackground -> editingTheme.hexToColor(editingTheme.buttonBackground)
            ColorProperty.ButtonText -> editingTheme.hexToColor(editingTheme.buttonText)
            ColorProperty.SuccessColor -> editingTheme.hexToColor(editingTheme.successColor)
            ColorProperty.ErrorColor -> editingTheme.hexToColor(editingTheme.errorColor)
            ColorProperty.WarningColor -> editingTheme.hexToColor(editingTheme.warningColor)
        }

        SimpleColorPickerDialog(
            initialColor = currentColor,
            onColorSelected = { newColor ->
                val hexColor = "#${Integer.toHexString(newColor.toArgb()).substring(2).uppercase()}"
                editingTheme = when (editingColorProperty!!) {
                    ColorProperty.BackgroundPrimary -> editingTheme.copy(backgroundPrimary = hexColor)
                    ColorProperty.BackgroundSecondary -> editingTheme.copy(backgroundSecondary = hexColor)
                    ColorProperty.BackgroundTertiary -> editingTheme.copy(backgroundTertiary = hexColor)
                    ColorProperty.AccentPrimary -> editingTheme.copy(accentPrimary = hexColor)
                    ColorProperty.AccentSecondary -> editingTheme.copy(accentSecondary = hexColor)
                    ColorProperty.AccentTertiary -> editingTheme.copy(accentTertiary = hexColor)
                    ColorProperty.TextPrimary -> editingTheme.copy(textPrimary = hexColor)
                    ColorProperty.TextSecondary -> editingTheme.copy(textSecondary = hexColor)
                    ColorProperty.CardBackground -> editingTheme.copy(cardBackground = hexColor)
                    ColorProperty.CardBorder -> editingTheme.copy(cardBorder = hexColor)
                    ColorProperty.ButtonBackground -> editingTheme.copy(buttonBackground = hexColor)
                    ColorProperty.ButtonText -> editingTheme.copy(buttonText = hexColor)
                    ColorProperty.SuccessColor -> editingTheme.copy(successColor = hexColor)
                    ColorProperty.ErrorColor -> editingTheme.copy(errorColor = hexColor)
                    ColorProperty.WarningColor -> editingTheme.copy(warningColor = hexColor)
                }
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

enum class ColorProperty {
    BackgroundPrimary, BackgroundSecondary, BackgroundTertiary,
    AccentPrimary, AccentSecondary, AccentTertiary,
    TextPrimary, TextSecondary,
    CardBackground, CardBorder,
    ButtonBackground, ButtonText,
    SuccessColor, ErrorColor, WarningColor
}

@Composable
private fun ThemePreviewCard(theme: AppTheme) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.hexToColor(theme.backgroundSecondary)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Превью темы",
                fontSize = 14.sp,
                color = theme.hexToColor(theme.textSecondary),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Цветовая палитра
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorPreviewBox(theme.hexToColor(theme.accentPrimary))
                ColorPreviewBox(theme.hexToColor(theme.accentSecondary))
                ColorPreviewBox(theme.hexToColor(theme.accentTertiary))
                ColorPreviewBox(theme.hexToColor(theme.backgroundPrimary))
                ColorPreviewBox(theme.hexToColor(theme.textPrimary))
            }
        }
    }
}

@Composable
private fun ColorPreviewBox(color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
    )
}

@Composable
private fun ColorSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ColorPropertyItem(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "#${Integer.toHexString(color.toArgb()).substring(2).uppercase()}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Normal
                )
            }

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SimpleColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var red by remember { mutableStateOf(initialColor.red) }
    var green by remember { mutableStateOf(initialColor.green) }
    var blue by remember { mutableStateOf(initialColor.blue) }
    val currentColor = Color(red, green, blue)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Выбор цвета",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Превью цвета
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(currentColor)
                        .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Hex код
                Text(
                    text = "#${Integer.toHexString(currentColor.toArgb()).substring(2).uppercase()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Red slider
                ColorSlider(
                    label = "Red",
                    value = red,
                    onValueChange = { red = it },
                    color = Color.Red
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Green slider
                ColorSlider(
                    label = "Green",
                    value = green,
                    onValueChange = { green = it },
                    color = Color.Green
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Blue slider
                ColorSlider(
                    label = "Blue",
                    value = blue,
                    onValueChange = { blue = it },
                    color = Color.Blue
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Предустановленные цвета
                Text(
                    text = "Быстрый выбор:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetColorButton(Color(0xFFDC143C)) { red = it.red; green = it.green; blue = it.blue }
                    PresetColorButton(Color(0xFF00D9FF)) { red = it.red; green = it.green; blue = it.blue }
                    PresetColorButton(Color(0xFF00FF00)) { red = it.red; green = it.green; blue = it.blue }
                    PresetColorButton(Color(0xFF9C27B0)) { red = it.red; green = it.green; blue = it.blue }
                    PresetColorButton(Color(0xFFFF5722)) { red = it.red; green = it.green; blue = it.blue }
                    PresetColorButton(Color(0xFF2196F3)) { red = it.red; green = it.green; blue = it.blue }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отмена")
                    }

                    Button(
                        onClick = { onColorSelected(currentColor) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Выбрать")
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = (value * 255).toInt().toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun PresetColorButton(color: Color, onClick: (Color) -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .clickable { onClick(color) }
    )
}
