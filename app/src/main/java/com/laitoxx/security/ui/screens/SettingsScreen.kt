package com.laitoxx.security.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.laitoxx.security.data.model.AppTheme
import com.laitoxx.security.ui.components.StyledTextField
import com.laitoxx.security.ui.navigation.Screen
import com.laitoxx.security.utils.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val allThemes by themeManager.allThemes.collectAsState()
    val currentThemeIndex = remember { mutableStateOf(themeManager.getCurrentThemeIndex()) }

    var showThemeEditor by remember { mutableStateOf(false) }
    var showThemeSelector by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Секция: Внешний вид
            item {
                SectionHeader("Внешний вид")
            }

            item {
                SettingsCard(
                    title = "Текущая тема",
                    subtitle = currentTheme.name,
                    icon = Icons.Default.Palette,
                    onClick = { showThemeSelector = true }
                )
            }

            item {
                SettingsCard(
                    title = "Редактор тем",
                    subtitle = "Создать или изменить тему",
                    icon = Icons.Default.Edit,
                    onClick = { navController.navigate(Screen.ThemeEditor.route) }
                )
            }

            item {
                SettingsCard(
                    title = "Экспорт темы",
                    subtitle = "Поделиться текущей темой",
                    icon = Icons.Default.Share,
                    onClick = {
                        val themeJson = currentTheme.toJson()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Theme", themeJson))
                        Toast.makeText(context, "Тема скопирована в буфер обмена", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                SettingsCard(
                    title = "Импорт темы",
                    subtitle = "Импортировать тему из JSON",
                    icon = Icons.Default.FileDownload,
                    onClick = { showImportDialog = true }
                )
            }

            // Секция: О приложении
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("О приложении")
            }

            item {
                SettingsCard(
                    title = "LAITOXX Android",
                    subtitle = "Версия 1.0.0",
                    icon = Icons.Default.Info,
                    onClick = {}
                )
            }

            item {
                SettingsCard(
                    title = "Разработчики",
                    subtitle = "t.me/laitoxx",
                    icon = Icons.Default.People,
                    onClick = {
                        // Открыть Telegram (можно добавить Intent)
                    }
                )
            }

            item {
                SettingsCard(
                    title = "Python Engine",
                    subtitle = "Python 3.8 via Chaquopy",
                    icon = Icons.Default.Code,
                    onClick = {}
                )
            }

            // Секция: Сброс
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Сброс")
            }

            item {
                SettingsCard(
                    title = "Сбросить тему",
                    subtitle = "Вернуться к теме по умолчанию",
                    icon = Icons.Default.Refresh,
                    tint = Color(0xFFFF9800), // Orange color for reset - keep as warning color
                    onClick = {
                        themeManager.resetToDefault()
                        currentThemeIndex.value = 0
                        Toast.makeText(context, "Тема сброшена", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Диалог выбора темы
    if (showThemeSelector) {
        ThemeSelectorDialog(
            themes = allThemes,
            currentIndex = currentThemeIndex.value,
            onThemeSelected = { index ->
                themeManager.setThemeByIndex(index)
                currentThemeIndex.value = index
                showThemeSelector = false
            },
            onDismiss = { showThemeSelector = false }
        )
    }

    // Диалог редактора тем
    if (showThemeEditor) {
        ThemeEditorDialog(
            currentTheme = currentTheme,
            themeManager = themeManager,
            onDismiss = { showThemeEditor = false }
        )
    }

    // Диалог импорта темы
    if (showImportDialog) {
        ImportThemeDialog(
            themeManager = themeManager,
            onDismiss = { showImportDialog = false }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
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
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Перейти",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ThemeSelectorDialog(
    themes: List<AppTheme>,
    currentIndex: Int,
    onThemeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Выбор темы",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(themes) { index, theme ->
                        ThemeItem(
                            theme = theme,
                            isSelected = index == currentIndex,
                            onClick = { onThemeSelected(index) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}

@Composable
private fun ThemeItem(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Превью цветов темы
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ColorPreview(theme.hexToColor(theme.accentPrimary))
                ColorPreview(theme.hexToColor(theme.accentSecondary))
                ColorPreview(theme.hexToColor(theme.accentTertiary))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "by ${theme.author}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Выбрано",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ColorPreview(color: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
    )
}

@Composable
private fun ThemeEditorDialog(
    currentTheme: AppTheme,
    themeManager: ThemeManager,
    onDismiss: () -> Unit
) {
    var themeName by remember { mutableStateOf(currentTheme.name) }
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedColorProperty by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Редактор тем",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Название темы
                StyledTextField(
                    value = themeName,
                    onValueChange = { themeName = it },
                    label = "Название темы",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Основные цвета",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Список цветовых настроек (упрощенная версия)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorSetting(
                        label = "Основной акцент",
                        color = currentTheme.hexToColor(currentTheme.accentPrimary),
                        onClick = { /* TODO: Color picker */ }
                    )
                    ColorSetting(
                        label = "Вторичный акцент",
                        color = currentTheme.hexToColor(currentTheme.accentSecondary),
                        onClick = { /* TODO: Color picker */ }
                    )
                    ColorSetting(
                        label = "Фон приложения",
                        color = currentTheme.hexToColor(currentTheme.backgroundPrimary),
                        onClick = { /* TODO: Color picker */ }
                    )
                    ColorSetting(
                        label = "Фон карточек",
                        color = currentTheme.hexToColor(currentTheme.cardBackground),
                        onClick = { /* TODO: Color picker */ }
                    )
                    ColorSetting(
                        label = "Основной текст",
                        color = currentTheme.hexToColor(currentTheme.textPrimary),
                        onClick = { /* TODO: Color picker */ }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопки действий
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Отмена")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val newTheme = currentTheme.copy(name = themeName)
                                themeManager.addCustomTheme(newTheme)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSetting(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Изменить",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ImportThemeDialog(
    themeManager: ThemeManager,
    onDismiss: () -> Unit
) {
    var themeJson by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Импорт темы",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = themeJson,
                    onValueChange = {
                        themeJson = it
                        errorMessage = null
                    },
                    label = { Text("JSON темы") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                        onClick = {
                            val theme = themeManager.importTheme(themeJson)
                            if (theme != null) {
                                themeManager.addCustomTheme(theme)
                                Toast.makeText(context, "Тема импортирована", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                errorMessage = "Неверный формат JSON"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Импортировать")
                    }
                }
            }
        }
    }
}
