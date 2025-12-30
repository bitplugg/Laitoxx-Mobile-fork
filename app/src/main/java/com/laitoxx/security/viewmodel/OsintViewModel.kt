package com.laitoxx.security.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laitoxx.security.data.model.IPInfo
import com.laitoxx.security.data.model.SubdomainList
import com.laitoxx.security.data.model.ToolResult
import com.laitoxx.security.data.repository.OsintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OsintViewModel : ViewModel() {

    private val repository = OsintRepository()

    private val _ipInfoState = MutableStateFlow<UiState<IPInfo>>(UiState.Idle)
    val ipInfoState: StateFlow<UiState<IPInfo>> = _ipInfoState.asStateFlow()

    private val _subdomainState = MutableStateFlow<UiState<SubdomainList>>(UiState.Idle)
    val subdomainState: StateFlow<UiState<SubdomainList>> = _subdomainState.asStateFlow()

    private val _emailState = MutableStateFlow<UiState<ToolResult>>(UiState.Idle)
    val emailState: StateFlow<UiState<ToolResult>> = _emailState.asStateFlow()

    private val _phoneState = MutableStateFlow<UiState<ToolResult>>(UiState.Idle)
    val phoneState: StateFlow<UiState<ToolResult>> = _phoneState.asStateFlow()

    fun getIPInfo(target: String) {
        viewModelScope.launch {
            _ipInfoState.value = UiState.Loading
            repository.getIPInfo(target).fold(
                onSuccess = { _ipInfoState.value = UiState.Success(it) },
                onFailure = { _ipInfoState.value = UiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun findSubdomains(domain: String) {
        viewModelScope.launch {
            _subdomainState.value = UiState.Loading
            repository.findSubdomains(domain).fold(
                onSuccess = { _subdomainState.value = UiState.Success(it) },
                onFailure = { _subdomainState.value = UiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun validateEmail(email: String) {
        viewModelScope.launch {
            _emailState.value = UiState.Loading
            repository.validateEmail(email).fold(
                onSuccess = { _emailState.value = UiState.Success(it) },
                onFailure = { _emailState.value = UiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun lookupPhone(phoneNumber: String) {
        viewModelScope.launch {
            _phoneState.value = UiState.Loading
            repository.lookupPhone(phoneNumber).fold(
                onSuccess = { _phoneState.value = UiState.Success(it) },
                onFailure = { _phoneState.value = UiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun resetIPInfo() {
        _ipInfoState.value = UiState.Idle
    }

    fun resetSubdomains() {
        _subdomainState.value = UiState.Idle
    }

    fun resetEmail() {
        _emailState.value = UiState.Idle
    }

    fun resetPhone() {
        _phoneState.value = UiState.Idle
    }
}

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
