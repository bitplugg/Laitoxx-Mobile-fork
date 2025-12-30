package com.laitoxx.security.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun SubdomainFinderScreen(
    navController: NavController,
    viewModel: OsintViewModel = viewModel()
) {
    var domain by remember { mutableStateOf("") }
    val subdomainState by viewModel.subdomainState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subdomain Finder", fontWeight = FontWeight.Bold) },
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
                    value = domain,
                    onValueChange = { domain = it },
                    label = "Domain (e.g., example.com)",
                    leadingIcon = Icons.Default.Language
                )
            }

            item {
                ActionButton(
                    text = "Find Subdomains",
                    onClick = { viewModel.findSubdomains(domain) },
                    enabled = domain.isNotBlank(),
                    icon = Icons.Default.Search
                )
            }

            when (val state = subdomainState) {
                is UiState.Loading -> item { LoadingIndicator() }
                is UiState.Success -> {
                    val data = state.data
                    item {
                        ResultCard(
                            title = "Subdomain Finder",
                            content = buildString {
                                appendLine("🌐 Результаты поиска поддоменов:")
                                appendLine()
                                appendLine("🎯 Домен: ${domain}")
                                appendLine("📊 Найдено: ${data.count} поддоменов")
                                appendLine()
                                appendLine("─".repeat(40))
                                appendLine()
                                appendLine("🔗 Список поддоменов:")
                                appendLine()

                                data.subdomains.forEachIndexed { index, subdomain ->
                                    appendLine("${index + 1}. $subdomain")
                                }

                                appendLine()
                                appendLine("💡 Скопируйте список для дальнейшего анализа")
                                appendLine()
                                appendLine("🔍 Используйте эти поддомены для:")
                                appendLine("• Сканирования портов")
                                appendLine("• Поиска уязвимостей")
                                appendLine("• Картирования инфраструктуры")
                            }
                        )
                    }
                }
                is UiState.Error -> item {
                    ErrorMessage(
                        message = state.message,
                        onRetry = { viewModel.findSubdomains(domain) }
                    )
                }
                else -> {}
            }
        }
    }
}
