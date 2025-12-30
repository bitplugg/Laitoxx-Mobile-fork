package com.laitoxx.security.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laitoxx.security.ui.effects.applyVisualEffect
import com.laitoxx.security.utils.ThemeManager
import kotlinx.serialization.json.Json

/**
 * Расширенная карточка результатов с поддержкой:
 * - Понятного вывода
 * - Кнопки "Поделиться"
 * - Просмотра raw JSON
 * - Копирования в буфер обмена
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedResultCard(
    title: String,
    readableContent: String,
    rawJsonData: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier,
    success: Boolean = true
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val theme by ThemeManager.getInstance(context).currentTheme.collectAsState()
    val effect = theme.getVisualEffect()

    var showRawJson by remember { mutableStateOf(false) }
    var selectedSource by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .applyVisualEffect(effect, theme),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок с иконками действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (success) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Кнопка "Поделиться"
                    IconButton(
                        onClick = {
                            shareText(context, title, readableContent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Поделиться",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Кнопка "Копировать"
                    IconButton(
                        onClick = {
                            val clip = ClipData.newPlainText("Result", readableContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Скопировано в буфер обмена", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Копировать",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Кнопка "Показать JSON" (если есть данные)
                    if (rawJsonData.isNotEmpty()) {
                        IconButton(
                            onClick = { showRawJson = !showRawJson }
                        ) {
                            Icon(
                                imageVector = if (showRawJson) Icons.Default.ExpandLess else Icons.Default.Code,
                                contentDescription = if (showRawJson) "Скрыть JSON" else "Показать JSON",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Читаемое содержимое
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.background,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                val scrollState = rememberScrollState()
                Text(
                    text = readableContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(scrollState)
                )
            }

            // Raw JSON секция (раскрывающаяся)
            AnimatedVisibility(
                visible = showRawJson && rawJsonData.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "📊 Raw JSON Data",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Выбор источника
                    if (rawJsonData.size > 1) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rawJsonData.keys.forEachIndexed { index, source ->
                                SegmentedButton(
                                    selected = selectedSource == source,
                                    onClick = { selectedSource = source },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = rawJsonData.size
                                    )
                                ) {
                                    Text(
                                        text = source,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        selectedSource = rawJsonData.keys.firstOrNull()
                    }

                    // Отображение JSON
                    selectedSource?.let { source ->
                        val jsonData = rawJsonData[source] ?: ""
                        val prettyJson = try {
                            val json = Json { prettyPrint = true }
                            json.parseToJsonElement(jsonData).toString()
                        } catch (e: Exception) {
                            jsonData
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Источник: $source",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            IconButton(
                                onClick = {
                                    val clip = ClipData.newPlainText("Raw JSON", prettyJson)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "JSON скопирован", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Копировать JSON",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF1E1E1E),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            val horizontalScrollState = rememberScrollState()
                            val verticalScrollState = rememberScrollState()

                            Text(
                                text = prettyJson,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE0E0E0),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                                    .horizontalScroll(horizontalScrollState)
                                    .verticalScroll(verticalScrollState)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Поделиться текстом через системный диалог
 */
private fun shareText(context: Context, title: String, content: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, content)
    }

    val chooser = Intent.createChooser(intent, "Поделиться через...")
    context.startActivity(chooser)
}

/**
 * Упрощенная версия результата без JSON
 */
@Composable
fun SimpleResultCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    success: Boolean = true
) {
    EnhancedResultCard(
        title = title,
        readableContent = content,
        rawJsonData = emptyMap(),
        modifier = modifier,
        success = success
    )
}
