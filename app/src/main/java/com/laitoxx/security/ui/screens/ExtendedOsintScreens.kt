package com.laitoxx.security.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.laitoxx.security.python.PythonBridge
import com.laitoxx.security.ui.components.*
import kotlinx.coroutines.launch
import org.json.JSONObject

// ==================== Gmail OSINT Screen ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GmailOsintScreen(navController: NavController) {
    var emailPrefix by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gmail OSINT") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyledTextField(
                value = emailPrefix,
                onValueChange = { emailPrefix = it },
                label = "Email Prefix (без @gmail.com)",
                leadingIcon = Icons.Default.Email
            )

            ActionButton(
                text = "Исследовать",
                onClick = {
                    scope.launch {
                        isLoading = true
                        error = null
                        result = null

                        PythonBridge.gmailOsint(emailPrefix).fold(
                            onSuccess = { json ->
                                if (json.has("error")) {
                                    error = json.getString("error")
                                } else {
                                    // Format user-friendly output
                                    val formatted = buildString {
                                        appendLine("📧 Email: ${json.getString("full_email")}")
                                        appendLine()

                                        if (json.has("gaia_id") && !json.isNull("gaia_id")) {
                                            appendLine("🆔 Google ID (GAIA): ${json.getString("gaia_id")}")
                                        }

                                        if (json.has("last_edit") && !json.isNull("last_edit")) {
                                            appendLine("📅 Последнее изменение: ${json.getString("last_edit")}")
                                        }

                                        if (json.getBoolean("has_custom_avatar")) {
                                            appendLine("🖼️ Кастомный аватар: Да")
                                        }

                                        appendLine()
                                        appendLine("🔗 Возможные профили:")

                                        val social = json.getJSONObject("social_media_links")
                                        appendLine("• YouTube: ${social.getString("youtube")}")
                                        appendLine("• Google Photos: ${social.getString("google_photos")}")
                                        appendLine("• Google Maps: ${social.getString("google_maps")}")
                                        appendLine("• Google+: ${social.getString("google_plus")}")

                                        appendLine()
                                        appendLine("💡 Проверьте эти ссылки вручную")
                                    }
                                    result = formatted
                                }
                            },
                            onFailure = { e ->
                                error = e.message ?: "Unknown error"
                            }
                        )

                        isLoading = false
                    }
                },
                enabled = !isLoading && emailPrefix.isNotBlank(),
                isLoading = isLoading
            )

            error?.let {
                ErrorMessage(it)
            }

            result?.let {
                ResultCard(
                    title = "Результаты Gmail OSINT",
                    content = it
                )
            }
        }
    }
}

// ==================== MAC Lookup Screen ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacLookupScreen(navController: NavController) {
    var macAddress by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MAC Address Lookup") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyledTextField(
                value = macAddress,
                onValueChange = { macAddress = it },
                label = "MAC Address",
                leadingIcon = Icons.Default.DeviceHub
            )

            ActionButton(
                text = "Найти производителя",
                onClick = {
                    scope.launch {
                        isLoading = true
                        error = null
                        result = null

                        PythonBridge.macLookup(macAddress).fold(
                            onSuccess = { json ->
                                if (json.has("error")) {
                                    error = json.getString("error")
                                } else {
                                    // Format user-friendly output
                                    val formatted = buildString {
                                        appendLine("📡 MAC Address информация:")
                                        appendLine()
                                        appendLine("🔢 Адрес: ${json.getString("mac_address")}")
                                        appendLine("🏭 Производитель: ${json.getString("vendor")}")
                                        appendLine("🔑 Префикс OUI: ${json.getString("prefix")}")
                                        appendLine()
                                        appendLine("📋 Что такое MAC-адрес?")
                                        appendLine("MAC (Media Access Control) — уникальный")
                                        appendLine("идентификатор сетевого устройства.")
                                        appendLine()
                                        appendLine("Первые 6 символов (OUI) указывают на")
                                        appendLine("производителя сетевого оборудования.")
                                    }
                                    result = formatted
                                }
                            },
                            onFailure = { e ->
                                error = e.message ?: "Unknown error"
                            }
                        )

                        isLoading = false
                    }
                },
                enabled = !isLoading && macAddress.isNotBlank(),
                isLoading = isLoading
            )

            error?.let {
                ErrorMessage(it)
            }

            result?.let {
                ResultCard(
                    title = "Информация о производителе",
                    content = it
                )
            }
        }
    }
}

// ==================== Username Checker Screen ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsernameCheckerScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var foundPlatforms by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var totalChecked by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Username Checker") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StyledTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Username",
                        leadingIcon = Icons.Default.Person
                )

                ActionButton(
                    text = "Проверить (29 платформ)",
                    onClick = {
                        scope.launch {
                            isLoading = true
                            error = null
                            foundPlatforms = emptyList()

                            PythonBridge.usernameCheck(username).fold(
                                onSuccess = { json ->
                                    if (json.has("error")) {
                                        error = json.getString("error")
                                    } else {
                                        totalChecked = json.getInt("checked")
                                        val found = json.getJSONArray("found")
                                        val platforms = mutableListOf<Pair<String, String>>()

                                        for (i in 0 until found.length()) {
                                            val item = found.getJSONObject(i)
                                            platforms.add(
                                                Pair(
                                                    item.getString("platform"),
                                                    item.getString("url")
                                                )
                                            )
                                        }

                                        foundPlatforms = platforms
                                    }
                                },
                                onFailure = { e ->
                                    error = e.message ?: "Unknown error"
                                }
                            )

                            isLoading = false
                        }
                    },
                    enabled = !isLoading && username.isNotBlank(),
                    isLoading = isLoading
                )

                if (foundPlatforms.isNotEmpty()) {
                    Text(
                        text = "Найдено: ${foundPlatforms.size}/$totalChecked",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }

                error?.let {
                    ErrorMessage(it)
                }
            }

            if (foundPlatforms.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(foundPlatforms) { (platform, url) ->
                        PlatformCard(platform = platform, url = url)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformCard(platform: String, url: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFF1A1A1A),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Found",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = platform,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = url,
                    fontSize = 12.sp,
                    color = Color(0xFF808080),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ==================== Google Dork Generator Screen ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDorkScreen(navController: NavController) {
    var keyword by remember { mutableStateOf("") }
    var site by remember { mutableStateOf("") }
    var filetype by remember { mutableStateOf("") }
    var inurl by remember { mutableStateOf("") }
    var intitle by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google Dork Generator") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyledTextField(
                value = keyword,
                onValueChange = { keyword = it },
                label = "Keyword",
                leadingIcon = Icons.Default.Search
            )

            StyledTextField(
                value = site,
                onValueChange = { site = it },
                label = "Site",
            )

            StyledTextField(
                value = filetype,
                onValueChange = { filetype = it },
                label = "File Type",
            )

            StyledTextField(
                value = inurl,
                onValueChange = { inurl = it },
                label = "In URL",
            )

            StyledTextField(
                value = intitle,
                onValueChange = { intitle = it },
                label = "In Title",
            )

            ActionButton(
                text = "Генерировать Dork",
                onClick = {
                    scope.launch {
                        isLoading = true
                        error = null
                        result = null

                        val operators = buildMap {
                            if (site.isNotBlank()) put("site", site)
                            if (filetype.isNotBlank()) put("filetype", filetype)
                            if (inurl.isNotBlank()) put("inurl", inurl)
                            if (intitle.isNotBlank()) put("intitle", intitle)
                        }

                        val operatorsJson = JSONObject(operators).toString()

                        PythonBridge.googleDork(keyword, operatorsJson).fold(
                            onSuccess = { json ->
                                if (json.has("error")) {
                                    error = json.getString("error")
                                } else {
                                    // Format user-friendly output
                                    val formatted = buildString {
                                        appendLine("🔍 Сгенерированный запрос:")
                                        appendLine()
                                        appendLine(json.getString("query"))
                                        appendLine()
                                        appendLine("─".repeat(40))
                                        appendLine()
                                        appendLine("🌐 Готовые ссылки для поиска:")
                                        appendLine()

                                        val searchUrls = json.getJSONObject("search_urls")
                                        appendLine("• Google:")
                                        appendLine("  ${searchUrls.getString("google")}")
                                        appendLine()
                                        appendLine("• Bing:")
                                        appendLine("  ${searchUrls.getString("bing")}")
                                        appendLine()
                                        appendLine("• DuckDuckGo:")
                                        appendLine("  ${searchUrls.getString("duckduckgo")}")
                                        appendLine()
                                        appendLine("• Yandex:")
                                        appendLine("  ${searchUrls.getString("yandex")}")
                                        appendLine()
                                        appendLine("─".repeat(40))
                                        appendLine()
                                        appendLine("💡 Полезные шаблоны для этого сайта:")
                                        appendLine()

                                        val commonDorks = json.getJSONObject("common_dorks")
                                        appendLine("🗂️ Открытые файлы:")
                                        appendLine("  ${commonDorks.getString("exposed_files")}")
                                        appendLine()
                                        appendLine("⚙️ Конфигурационные файлы:")
                                        appendLine("  ${commonDorks.getString("config_files")}")
                                        appendLine()
                                        appendLine("🔐 Страницы входа:")
                                        appendLine("  ${commonDorks.getString("login_pages")}")
                                        appendLine()
                                        appendLine("📝 Лог-файлы:")
                                        appendLine("  ${commonDorks.getString("log_files")}")
                                        appendLine()
                                        appendLine("💾 Резервные копии:")
                                        appendLine("  ${commonDorks.getString("backup_files")}")
                                        appendLine()
                                        appendLine("💡 Скопируйте нужный запрос и используйте в поисковике")
                                    }
                                    result = formatted
                                }
                            },
                            onFailure = { e ->
                                error = e.message ?: "Unknown error"
                            }
                        )

                        isLoading = false
                    }
                },
                enabled = !isLoading && keyword.isNotBlank(),
                isLoading = isLoading
            )

            error?.let {
                ErrorMessage(it)
            }

            result?.let {
                ResultCard(
                    title = "Generated Google Dork",
                    content = it
                )
            }
        }
    }
}

// ==================== Web Crawler Screen ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebCrawlerScreen(navController: NavController) {
    var startUrl by remember { mutableStateOf("") }
    var maxPages by remember { mutableStateOf("10") }
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Web Crawler") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyledTextField(
                value = startUrl,
                onValueChange = { startUrl = it },
                label = "Start URL",
                leadingIcon = Icons.Default.Web
            )

            StyledTextField(
                value = maxPages,
                onValueChange = { maxPages = it.filter { c -> c.isDigit() } },
                label = "Max Pages",
            )

            ActionButton(
                text = "Начать краулинг",
                onClick = {
                    scope.launch {
                        isLoading = true
                        error = null
                        result = null

                        val pages = maxPages.toIntOrNull() ?: 10

                        PythonBridge.webCrawlEnhanced(startUrl, pages).fold(
                            onSuccess = { json ->
                                if (json.has("error")) {
                                    error = json.getString("error")
                                } else {
                                    // Format user-friendly output
                                    val formatted = buildString {
                                        appendLine("🕸️ Результаты краулинга:")
                                        appendLine()
                                        appendLine("🌐 Начальный URL: ${json.getString("start_url")}")
                                        appendLine("📊 Найдено страниц: ${json.getInt("pages_crawled")} / ${json.getInt("max_pages")}")
                                        appendLine()
                                        appendLine("─".repeat(40))
                                        appendLine()
                                        appendLine("📄 Обнаруженные страницы:")
                                        appendLine()

                                        val pagesArray = json.getJSONArray("pages")
                                        for (i in 0 until pagesArray.length()) {
                                            val page = pagesArray.getJSONObject(i)
                                            appendLine("${i + 1}. ${page.getString("title")}")
                                            appendLine("   URL: ${page.getString("url")}")
                                            appendLine("   Статус: ${page.getInt("status")}")
                                            if (i < pagesArray.length() - 1) appendLine()
                                        }

                                        appendLine()
                                        appendLine("💡 Используйте эти URL для дальнейшего анализа")
                                    }
                                    result = formatted
                                }
                            },
                            onFailure = { e ->
                                error = e.message ?: "Unknown error"
                            }
                        )

                        isLoading = false
                    }
                },
                enabled = !isLoading && startUrl.isNotBlank(),
                isLoading = isLoading
            )

            error?.let {
                ErrorMessage(it)
            }

            result?.let {
                ResultCard(
                    title = "Crawled Pages",
                    content = it
                )
            }
        }
    }
}
