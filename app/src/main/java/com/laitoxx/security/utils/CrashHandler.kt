package com.laitoxx.security.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.window.Dialog
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

/**
 * Глобальный обработчик необработанных исключений
 */
class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var crashInfo: CrashInfo? by mutableStateOf(null)

    data class CrashInfo(
        val throwable: Throwable,
        val thread: Thread,
        val timestamp: String,
        val deviceInfo: String,
        val fullStackTrace: String
    )

    companion object {
        private const val TAG = "CrashHandler"
        private val instance = CrashHandler()

        /**
         * Инициализация обработчика крашей
         */
        fun init(context: Context) {
            val currentHandler = Thread.getDefaultUncaughtExceptionHandler()
            if (currentHandler != instance) {
                instance.defaultHandler = currentHandler
                Thread.setDefaultUncaughtExceptionHandler(instance)
                Log.d(TAG, "CrashHandler initialized")
            }
        }

        /**
         * Получить информацию о последнем краше
         */
        fun getCrashInfo(): CrashInfo? = instance.crashInfo

        /**
         * Очистить информацию о краше
         */
        fun clearCrashInfo() {
            instance.crashInfo = null
        }
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e(TAG, "CRASH: ${throwable.javaClass.simpleName}: ${throwable.message}")

            // Собираем информацию о краше
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date())

            val deviceInfo = buildDeviceInfo()
            val stackTrace = getStackTrace(throwable)

            crashInfo = CrashInfo(
                throwable = throwable,
                thread = thread,
                timestamp = timestamp,
                deviceInfo = deviceInfo,
                fullStackTrace = stackTrace
            )

            Log.e(TAG, stackTrace)

        } catch (e: Exception) {
            Log.e(TAG, "Error in crash handler", e)
        } finally {
            // Вызываем стандартный обработчик - он покажет системный диалог
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun buildDeviceInfo(): String {
        return buildString {
            appendLine("=== Device Information ===")
            appendLine("Manufacturer: ${Build.MANUFACTURER}")
            appendLine("Model: ${Build.MODEL}")
            appendLine("Device: ${Build.DEVICE}")
            appendLine("Brand: ${Build.BRAND}")
            appendLine("Product: ${Build.PRODUCT}")
            appendLine()
            appendLine("=== System Information ===")
            appendLine("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Build ID: ${Build.ID}")
            appendLine("Build Type: ${Build.TYPE}")
            appendLine("Build Tags: ${Build.TAGS}")
            appendLine()
            appendLine("=== App Information ===")
            appendLine("Package: com.laitoxx.security")
            appendLine("Version: 1.0.0")
            appendLine()
            appendLine("=== Hardware Information ===")
            appendLine("Board: ${Build.BOARD}")
            appendLine("Hardware: ${Build.HARDWARE}")
            appendLine("Supported ABIs: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
        }
    }

    private fun getStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)

        // Добавляем причины ошибок
        var cause = throwable.cause
        while (cause != null) {
            pw.println()
            pw.println("Caused by:")
            cause.printStackTrace(pw)
            cause = cause.cause
        }

        return sw.toString()
    }
}

/**
 * Composable функция для отображения диалога краша
 */
@Composable
fun CrashDialog(
    crashInfo: CrashHandler.CrashInfo?,
    onDismiss: () -> Unit
) {
    if (crashInfo == null) return

    Dialog(onDismissRequest = onDismiss) {
        CrashDialogContent(crashInfo = crashInfo, onDismiss = onDismiss)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrashDialogContent(
    crashInfo: CrashHandler.CrashInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showFullStackTrace by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Заголовок с иконкой ошибки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Error",
                    tint = Color(0xFFDC143C),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Что-то сломалось",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC143C)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Подзаголовок
            Text(
                text = "Свяжитесь с разработчиками",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Ссылка на Telegram
            TextButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/laitoxx"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "t.me/laitoxx",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            // Информация об ошибке
            Text(
                text = "Информация об ошибке:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Время краша
            InfoRow("Время:", crashInfo.timestamp)
            InfoRow("Поток:", crashInfo.thread.name)
            InfoRow("Тип:", crashInfo.throwable.javaClass.simpleName)

            Spacer(modifier = Modifier.height(12.dp))

            // Сообщение об ошибке
            Text(
                text = "Сообщение:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = crashInfo.throwable.message ?: "No message",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка для показа полного стектрейса
            OutlinedButton(
                onClick = { showFullStackTrace = !showFullStackTrace },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (showFullStackTrace) "Скрыть детали" else "Показать полный стектрейс"
                )
            }

            if (showFullStackTrace) {
                Spacer(modifier = Modifier.height(12.dp))

                // Полный стектрейс
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    color = Color(0xFF1A1A1A),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = crashInfo.deviceInfo,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00FF00)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "=== Stack Trace ===",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = crashInfo.fullStackTrace,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE0E0E0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Копировать в буфер обмена
                OutlinedButton(
                    onClick = {
                        val fullReport = buildString {
                            appendLine("Что-то сломалось, свяжитесь с разработчиками t.me/laitoxx")
                            appendLine()
                            appendLine("Время: ${crashInfo.timestamp}")
                            appendLine()
                            append(crashInfo.deviceInfo)
                            appendLine()
                            appendLine("=== Stack Trace ===")
                            append(crashInfo.fullStackTrace)
                        }

                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Crash Report", fullReport)
                        clipboard.setPrimaryClip(clip)

                        // Показать подтверждение
                        android.widget.Toast.makeText(
                            context,
                            "Отчет скопирован в буфер обмена",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Копировать")
                }

                // Закрыть приложение
                Button(
                    onClick = {
                        onDismiss()
                        exitProcess(1)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC143C)
                    )
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
