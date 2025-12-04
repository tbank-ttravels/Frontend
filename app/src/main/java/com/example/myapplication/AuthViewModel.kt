package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core_data.model.AuthLoginRequest
import com.example.core_data.network.NetworkResult
import com.example.core_data.repository.TTravelsBackend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val backend: TTravelsBackend
) : ViewModel() {

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = backend.login(AuthLoginRequest(phone, password))) {

                is NetworkResult.Success -> {
                    _authState.value = AuthState.Success
                }

                is NetworkResult.HttpError -> {
                    _authState.value = AuthState.Error(
                        result.error?.message ?: "HTTP ошибка ${result.code}"
                    )
                }

                is NetworkResult.NetworkError -> {
                    _authState.value = AuthState.Error("Ошибка сети")
                }

                is NetworkResult.SerializationError -> {
                    _authState.value = AuthState.Error("Ошибка обработки данных")
                }

                is NetworkResult.UnknownError -> {
                    _authState.value = AuthState.Error(
                        result.throwable.localizedMessage ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }
}
