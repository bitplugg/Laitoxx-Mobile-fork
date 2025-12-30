package com.laitoxx.security.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.laitoxx.security.ui.components.*
import com.laitoxx.security.ui.theme.*
import com.laitoxx.security.viewmodel.OsintViewModel
import com.laitoxx.security.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IPInfoScreen(
    navController: NavController,
    viewModel: OsintViewModel = viewModel()
) {
    var target by remember { mutableStateOf("") }
    val ipInfoState by viewModel.ipInfoState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IP Information", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StyledTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = "IP Address or Domain",
                    leadingIcon = Icons.Default.Public
                )
            }

            item {
                ActionButton(
                    text = "Lookup",
                    onClick = { viewModel.getIPInfo(target) },
                    enabled = target.isNotBlank(),
                    icon = Icons.Default.Search
                )
            }

            item {
                when (val state = ipInfoState) {
                    is UiState.Loading -> LoadingIndicator()
                    is UiState.Success -> {
                        val info = state.data
                        ResultCard(
                            title = "IP Information",
                            content = buildString {
                                appendLine("🌐 Информация об IP-адресе:")
                                appendLine()
                                appendLine("📍 IP: ${info.ip}")
                                appendLine("🔖 Тип: ${info.type}")
                                appendLine()
                                appendLine("─".repeat(40))
                                appendLine()
                                appendLine("🗺️ Геолокация:")
                                appendLine("🏳️ Страна: ${info.country}")
                                appendLine("🏙️ Регион: ${info.region}")
                                appendLine("🌆 Город: ${info.city}")
                                appendLine("📮 Индекс: ${info.postal}")
                                appendLine()
                                appendLine("📐 Координаты:")
                                appendLine("• Широта: ${info.latitude}")
                                appendLine("• Долгота: ${info.longitude}")

                                info.connection?.let { conn ->
                                    appendLine()
                                    appendLine("─".repeat(40))
                                    appendLine()
                                    appendLine("🔌 Информация о подключении:")
                                    appendLine("🔢 ASN: ${conn.asn}")
                                    appendLine("🏢 Провайдер: ${conn.isp}")
                                    appendLine("🏛️ Организация: ${conn.org}")
                                    appendLine("🌐 Домен: ${conn.domain}")
                                }

                                appendLine()
                                appendLine("💡 Используйте эти данные для анализа сети")
                            }
                        )
                    }
                    is UiState.Error -> {
                        ErrorMessage(
                            message = state.message,
                            onRetry = { viewModel.getIPInfo(target) }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
