package com.laitoxx.security.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.laitoxx.security.data.repository.WebSecurityRepository
import com.laitoxx.security.ui.components.*
import com.laitoxx.security.ui.theme.*
import com.laitoxx.security.utils.TextTransformation
import com.laitoxx.security.utils.TextUtils
import com.laitoxx.security.viewmodel.NetworkViewModel
import com.laitoxx.security.viewmodel.OsintViewModel
import com.laitoxx.security.viewmodel.UiState
import kotlinx.coroutines.launch

// Email Validator Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailValidatorScreen(
    navController: NavController,
    viewModel: OsintViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    val emailState by viewModel.emailState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Email Validator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StyledTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    leadingIcon = Icons.Default.Email
                )
            }
            item {
                ActionButton(
                    text = "Validate",
                    onClick = { viewModel.validateEmail(email) },
                    enabled = email.isNotBlank(),
                    icon = Icons.Default.Check
                )
            }
            item {
                when (val state = emailState) {
                    is UiState.Loading -> LoadingIndicator()
                    is UiState.Success -> ResultCard("Result", state.data.data, success = state.data.success)
                    is UiState.Error -> ErrorMessage(state.message) { viewModel.validateEmail(email) }
                    else -> {}
                }
            }
        }
    }
}

// Phone Lookup Screen (deprecated - use EnhancedOsintScreens version)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLookupScreenOld(
    navController: NavController,
    viewModel: OsintViewModel = viewModel()
) {
    var phone by remember { mutableStateOf("") }
    val phoneState by viewModel.phoneState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phone Lookup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StyledTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number",
                    leadingIcon = Icons.Default.Phone
                )
            }
            item {
                ActionButton(
                    text = "Lookup",
                    onClick = { viewModel.lookupPhone(phone) },
                    enabled = phone.isNotBlank(),
                    icon = Icons.Default.Search
                )
            }
            item {
                when (val state = phoneState) {
                    is UiState.Loading -> LoadingIndicator()
                    is UiState.Success -> ResultCard("Phone Information", state.data.data)
                    is UiState.Error -> ErrorMessage(state.message) { viewModel.lookupPhone(phone) }
                    else -> {}
                }
            }
        }
    }
}

// Port Scanner Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortScannerScreen(
    navController: NavController,
    viewModel: NetworkViewModel = viewModel()
) {
    var host by remember { mutableStateOf("") }
    var startPort by remember { mutableStateOf("1") }
    var endPort by remember { mutableStateOf("1000") }
    val portScanState by viewModel.portScanState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Port Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StyledTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = "Host or IP Address",
                    leadingIcon = Icons.Default.Computer
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StyledTextField(
                        value = startPort,
                        onValueChange = { if (it.all { c -> c.isDigit() }) startPort = it },
                        label = "Start Port",
                        modifier = Modifier.weight(1f)
                    )
                    StyledTextField(
                        value = endPort,
                        onValueChange = { if (it.all { c -> c.isDigit() }) endPort = it },
                        label = "End Port",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                ActionButton(
                    text = "Scan Ports",
                    onClick = {
                        viewModel.scanPorts(host, startPort.toIntOrNull() ?: 1, endPort.toIntOrNull() ?: 1000)
                    },
                    enabled = host.isNotBlank() && startPort.isNotBlank() && endPort.isNotBlank(),
                    icon = Icons.Default.Shield
                )
            }
            when (val state = portScanState) {
                is UiState.Loading -> item { LoadingIndicator() }
                is UiState.Success -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Found ${state.data.size} open ports",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (state.data.isEmpty()) Orange else Green,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    items(state.data) { port ->
                        val commonPorts = viewModel.getCommonPorts()
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Port $port", color = White, fontWeight = FontWeight.Bold)
                                Text(commonPorts[port] ?: "Unknown", color = DarkOnSurface)
                            }
                        }
                    }
                }
                is UiState.Error -> item {
                    ErrorMessage(state.message) {
                        viewModel.scanPorts(host, startPort.toIntOrNull() ?: 1, endPort.toIntOrNull() ?: 1000)
                    }
                }
                else -> {}
            }
        }
    }
}

// DNS Lookup Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DNSLookupScreen(
    navController: NavController,
    viewModel: NetworkViewModel = viewModel()
) {
    var domain by remember { mutableStateOf("") }
    val dnsState by viewModel.dnsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DNS Lookup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StyledTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = "Domain Name",
                    leadingIcon = Icons.Default.Dns
                )
            }
            item {
                ActionButton(
                    text = "Lookup",
                    onClick = { viewModel.dnsLookup(domain) },
                    enabled = domain.isNotBlank(),
                    icon = Icons.Default.Search
                )
            }
            item {
                when (val state = dnsState) {
                    is UiState.Loading -> LoadingIndicator()
                    is UiState.Success -> ResultCard("DNS Records", state.data.data)
                    is UiState.Error -> ErrorMessage(state.message) { viewModel.dnsLookup(domain) }
                    else -> {}
                }
            }
        }
    }
}

// Ping Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PingScreen(
    navController: NavController,
    viewModel: NetworkViewModel = viewModel()
) {
    var host by remember { mutableStateOf("") }
    val pingState by viewModel.pingState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ping", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StyledTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = "Host or IP Address",
                    leadingIcon = Icons.Default.SignalCellularAlt
                )
            }
            item {
                ActionButton(
                    text = "Ping",
                    onClick = { viewModel.ping(host) },
                    enabled = host.isNotBlank(),
                    icon = Icons.Default.NetworkPing
                )
            }
            item {
                when (val state = pingState) {
                    is UiState.Loading -> LoadingIndicator()
                    is UiState.Success -> ResultCard("Ping Result", state.data.data, Modifier, state.data.success)
                    is UiState.Error -> ErrorMessage(state.message) { viewModel.ping(host) }
                    else -> {}
                }
            }
        }
    }
}

// URL Checker Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun URLCheckerScreen(navController: NavController) {
    var url by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val repository = remember { WebSecurityRepository() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("URL Checker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StyledTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = "URL to Check",
                    leadingIcon = Icons.Default.Link
                )
            }
            item {
                ActionButton(
                    text = "Check URL",
                    onClick = {
                        isLoading = true
                        scope.launch {
                            repository.checkURL(url).fold(
                                onSuccess = { result = it.data; isLoading = false },
                                onFailure = { result = "Error: ${it.message}"; isLoading = false }
                            )
                        }
                    },
                    enabled = url.isNotBlank() && !isLoading,
                    icon = Icons.Default.Security
                )
            }
            if (isLoading) {
                item { LoadingIndicator() }
            } else if (result.isNotEmpty()) {
                item { ResultCard("URL Information", result) }
            }
        }
    }
}

// Admin Finder Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFinderScreen(navController: NavController) {
    var baseUrl by remember { mutableStateOf("") }
    var foundPages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val repository = remember { WebSecurityRepository() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Finder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StyledTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = "Base URL",
                    leadingIcon = Icons.Default.AdminPanelSettings
                )
            }
            item {
                ActionButton(
                    text = "Find Admin Pages",
                    onClick = {
                        isLoading = true
                        scope.launch {
                            repository.findAdminPages(baseUrl).fold(
                                onSuccess = { foundPages = it; isLoading = false },
                                onFailure = { foundPages = listOf("Error: ${it.message}"); isLoading = false }
                            )
                        }
                    },
                    enabled = baseUrl.isNotBlank() && !isLoading,
                    icon = Icons.Default.Search
                )
            }
            when {
                isLoading -> item { LoadingIndicator() }
                foundPages.isNotEmpty() -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Text(
                                "Found ${foundPages.size} admin pages",
                                modifier = Modifier.padding(16.dp),
                                color = Green,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    items(foundPages) { page ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Text(page, modifier = Modifier.padding(12.dp), color = DarkOnSurface)
                        }
                    }
                }
            }
        }
    }
}

// Text Transformer Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextTransformerScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var selectedTransform by remember { mutableStateOf(TextTransformation.UPPERCASE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Text Transformer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StyledTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = "Input Text",
                    maxLines = 5,
                    singleLine = false
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Transformation Type", color = White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextTransformation.values().forEach { transform ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedTransform == transform,
                                    onClick = { selectedTransform = transform },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentRed)
                                )
                                Text(transform.name, color = DarkOnSurface)
                            }
                        }
                    }
                }
            }
            item {
                ActionButton(
                    text = "Transform",
                    onClick = { outputText = TextUtils.transformText(inputText, selectedTransform) },
                    enabled = inputText.isNotBlank(),
                    icon = Icons.Default.Transform
                )
            }
            if (outputText.isNotEmpty()) {
                item { ResultCard("Result", outputText) }
            }
        }
    }
}

// Hash Generator Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashGeneratorScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    var md5Hash by remember { mutableStateOf("") }
    var sha1Hash by remember { mutableStateOf("") }
    var sha256Hash by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hash Generator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StyledTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = "Text to Hash",
                    maxLines = 3,
                    singleLine = false
                )
            }
            item {
                ActionButton(
                    text = "Generate Hashes",
                    onClick = {
                        md5Hash = TextUtils.generateHash(inputText, "MD5")
                        sha1Hash = TextUtils.generateHash(inputText, "SHA-1")
                        sha256Hash = TextUtils.generateHash(inputText, "SHA-256")
                    },
                    enabled = inputText.isNotBlank(),
                    icon = Icons.Default.Tag
                )
            }
            if (md5Hash.isNotEmpty()) {
                item { ResultCard("MD5", md5Hash) }
                item { ResultCard("SHA-1", sha1Hash) }
                item { ResultCard("SHA-256", sha256Hash) }
            }
        }
    }
}

// Base64 Encoder Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Base64EncoderScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var isEncoding by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Base64 Encoder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StyledTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = "Input Text",
                    maxLines = 5,
                    singleLine = false
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton(
                        text = "Encode",
                        onClick = {
                            isEncoding = true
                            outputText = TextUtils.encodeBase64(inputText)
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        text = "Decode",
                        onClick = {
                            isEncoding = false
                            outputText = TextUtils.decodeBase64(inputText)
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (outputText.isNotEmpty()) {
                item {
                    ResultCard(
                        if (isEncoding) "Encoded Result" else "Decoded Result",
                        outputText
                    )
                }
            }
        }
    }
}
