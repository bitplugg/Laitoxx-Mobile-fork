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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.laitoxx.security.data.model.*
import com.laitoxx.security.data.network.EnhancedOsintService
import com.laitoxx.security.data.network.PhoneInfoService
import com.laitoxx.security.ui.components.*
import com.laitoxx.security.viewmodel.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ==================== ViewModel ====================

class EnhancedOsintViewModel : ViewModel() {
    private val enhancedService = EnhancedOsintService()
    private val phoneService = PhoneInfoService()

    private val _enhancedIPState = MutableStateFlow<UiState<EnhancedIPInfo>>(UiState.Idle)
    val enhancedIPState: StateFlow<UiState<EnhancedIPInfo>> = _enhancedIPState

    private val _macState = MutableStateFlow<UiState<MACInfo>>(UiState.Idle)
    val macState: StateFlow<UiState<MACInfo>> = _macState

    private val _whoisState = MutableStateFlow<UiState<WhoisInfo>>(UiState.Idle)
    val whoisState: StateFlow<UiState<WhoisInfo>> = _whoisState

    private val _phoneState = MutableStateFlow<UiState<PhoneInfo>>(UiState.Idle)
    val phoneState: StateFlow<UiState<PhoneInfo>> = _phoneState

    fun getEnhancedIPInfo(ip: String) {
        viewModelScope.launch {
            _enhancedIPState.value = UiState.Loading
            val result = enhancedService.getEnhancedIPInfo(ip)
            _enhancedIPState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Ошибка получения данных") }
            )
        }
    }

    fun getMACInfo(mac: String) {
        viewModelScope.launch {
            _macState.value = UiState.Loading
            val result = enhancedService.getMACInfo(mac)
            _macState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Ошибка получения данных") }
            )
        }
    }

    fun getWhoisInfo(resource: String) {
        viewModelScope.launch {
            _whoisState.value = UiState.Loading
            val result = enhancedService.getWhoisInfo(resource)
            _whoisState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Ошибка получения данных") }
            )
        }
    }

    fun getPhoneInfo(phone: String) {
        viewModelScope.launch {
            _phoneState.value = UiState.Loading
            val result = phoneService.getPhoneInfo(phone)
            _phoneState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Ошибка получения данных") }
            )
        }
    }
}

// ==================== Enhanced IP Screen ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedIPInfoScreen(
    navController: NavController,
    viewModel: EnhancedOsintViewModel = viewModel()
) {
    var target by remember { mutableStateOf("") }
    val state by viewModel.enhancedIPState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IP Lookup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                    label = "IP адрес или домен",
                    placeholder = "8.8.8.8",
                    leadingIcon = Icons.Default.Public
                )
            }

            item {
                ActionButton(
                    text = "Поиск",
                    onClick = { viewModel.getEnhancedIPInfo(target) },
                    enabled = target.isNotBlank(),
                    icon = Icons.Default.Search
                )
            }

            item {
                when (val currentState = state) {
                    is UiState.Loading -> LoadingIndicator()
                    is UiState.Success -> {
                        EnhancedResultCard(
                            title = "Информация об IP",
                            readableContent = currentState.data.toReadableString(),
                            rawJsonData = currentState.data.rawData
                        )
                    }
                    is UiState.Error -> {
                        ErrorMessage(
                            message = currentState.message,
                            onRetry = { viewModel.getEnhancedIPInfo(target) }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

// ==================== MAC Lookup Screen ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MACLookupScreen(
    navController: NavController,
    viewModel: EnhancedOsintViewModel = viewModel()
) {
    var mac by remember { mutableStateOf("") }
    val state by viewModel.macState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MAC Address Lookup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
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
                    value = mac,
                    onValueChange = { mac = it },
                    label = "MAC адрес",
                    placeholder = "44:38:39:ff:ef:57",
                    leadingIcon = Icons.Default.NetworkCheck
                )
            }

            item {
                ActionButton(
                    text = "Поиск",
                    onClick = { viewModel.getMACInfo(mac) },
                    enabled = mac.isNotBlank(),
                    icon = Icons.Default.Search
                )
            }

            item {
                when (val currentState = state) {
                    is UiState.Loading -> LoadingIndicator()
                    is UiState.Success -> {
                        EnhancedResultCard(
                            title = "Информация о MAC адресе",
                            readableContent = currentState.data.toReadableString(),
                            rawJsonData = currentState.data.rawData
                        )
                    }
                    is UiState.Error -> {
                        ErrorMessage(
                            message = currentState.message,
                            onRetry = { viewModel.getMACInfo(mac) }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

// ==================== WHOIS Screen ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhoisLookupScreen(
    navController: NavController,
    viewModel: EnhancedOsintViewModel = viewModel()
) {
    var resource by remember { mutableStateOf("") }
    val state by viewModel.whoisState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WHOIS Lookup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
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
                    value = resource,
                    onValueChange = { resource = it },
                    label = "IP / Домен / ASN",
                    placeholder = "example.com или 8.8.8.8 или AS15169",
                    leadingIcon = Icons.Default.Info
                )
            }

            item {
                ActionButton(
                    text = "WHOIS Lookup",
                    onClick = { viewModel.getWhoisInfo(resource) },
                    enabled = resource.isNotBlank(),
                    icon = Icons.Default.Search
                )
            }

            item {
                when (val currentState = state) {
                    is UiState.Loading -> LoadingIndicator()
                    is UiState.Success -> {
                        EnhancedResultCard(
                            title = "WHOIS / RDAP информация",
                            readableContent = currentState.data.toReadableString(),
                            rawJsonData = currentState.data.rawData
                        )
                    }
                    is UiState.Error -> {
                        ErrorMessage(
                            message = currentState.message,
                            onRetry = { viewModel.getWhoisInfo(resource) }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

// ==================== Phone Lookup Screen ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLookupScreen(
    navController: NavController,
    viewModel: EnhancedOsintViewModel = viewModel()
) {
    var phone by remember { mutableStateOf("") }
    val state by viewModel.phoneState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phone Number Lookup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
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
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Номер телефона",
                    placeholder = "+1234567890",
                    leadingIcon = Icons.Default.Phone
                )
            }

            item {
                Text(
                    text = "ℹ️ Используйте международный формат: +код_страны номер",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            item {
                ActionButton(
                    text = "Поиск",
                    onClick = { viewModel.getPhoneInfo(phone) },
                    enabled = phone.isNotBlank(),
                    icon = Icons.Default.Search
                )
            }

            item {
                when (val currentState = state) {
                    is UiState.Loading -> LoadingIndicator()
                    is UiState.Success -> {
                        EnhancedResultCard(
                            title = "Информация о номере телефона",
                            readableContent = currentState.data.toReadableString(),
                            rawJsonData = currentState.data.rawData
                        )
                    }
                    is UiState.Error -> {
                        ErrorMessage(
                            message = currentState.message,
                            onRetry = { viewModel.getPhoneInfo(phone) }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
