package com.laitoxx.security.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laitoxx.security.data.model.ToolResult
import com.laitoxx.security.data.repository.NetworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NetworkViewModel : ViewModel() {

    private val repository = NetworkRepository()

    private val _portScanState = MutableStateFlow<UiState<List<Int>>>(UiState.Idle)
    val portScanState: StateFlow<UiState<List<Int>>> = _portScanState.asStateFlow()

    private val _dnsState = MutableStateFlow<UiState<ToolResult>>(UiState.Idle)
    val dnsState: StateFlow<UiState<ToolResult>> = _dnsState.asStateFlow()

    private val _pingState = MutableStateFlow<UiState<ToolResult>>(UiState.Idle)
    val pingState: StateFlow<UiState<ToolResult>> = _pingState.asStateFlow()

    fun scanPorts(host: String, startPort: Int, endPort: Int) {
        viewModelScope.launch {
            _portScanState.value = UiState.Loading
            repository.scanPortRange(host, startPort, endPort).fold(
                onSuccess = { _portScanState.value = UiState.Success(it) },
                onFailure = { _portScanState.value = UiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun dnsLookup(domain: String) {
        viewModelScope.launch {
            _dnsState.value = UiState.Loading
            repository.dnsLookup(domain).fold(
                onSuccess = { _dnsState.value = UiState.Success(it) },
                onFailure = { _dnsState.value = UiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun ping(host: String) {
        viewModelScope.launch {
            _pingState.value = UiState.Loading
            repository.ping(host).fold(
                onSuccess = { _pingState.value = UiState.Success(it) },
                onFailure = { _pingState.value = UiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun resetPortScan() {
        _portScanState.value = UiState.Idle
    }

    fun resetDns() {
        _dnsState.value = UiState.Idle
    }

    fun resetPing() {
        _pingState.value = UiState.Idle
    }

    fun getCommonPorts() = repository.getCommonPorts()
}
