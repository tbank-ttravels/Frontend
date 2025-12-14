package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core_data.repository.TTravelsBackend
import com.example.core_data.model.AccountResponse
import com.example.core_data.model.UpdateAccountRequest
import com.example.core_data.model.LogoutRequest
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val backend: TTravelsBackend) : ViewModel() {

    companion object {
        fun provideFactory(backend: TTravelsBackend): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                        return UserViewModel(backend) as T
                    }
                    throw IllegalArgumentException("Незнакомый ViewModel Class")
                }
            }
        }
    }

    data class UserData(
        val name: String = "",
        val surname: String = "",
        val phone: String = "",
        val isLoggedIn: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        val errorCode: Int? = null,
        val hasValidToken: Boolean = false
    )

    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData> = _userData.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val tokens = backend.tokensStore?.currentTokens()
        val hasToken = tokens?.accessToken != null

        _userData.value = _userData.value.copy(
            hasValidToken = hasToken
        )

        if (hasToken) {
            loadCurrentUser()
        } else {
            _userData.value = UserData(isLoggedIn = false, hasValidToken = false)
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _userData.value = _userData.value.copy(
                isLoading = true,
                error = null,
                errorCode = null
            )

            try {
                val result = backend.getCurrentUser()
                handleUserResult(result)
            } catch (e: Exception) {
                _userData.value = UserData(
                    isLoggedIn = false,
                    hasValidToken = false,
                    isLoading = false,
                    error = "Неожиданная ошибка: ${e.localizedMessage ?: "неизвестно"}"
                )
                backend.tokensStore?.clear()
            }
        }
    }

    private fun handleUserResult(result: NetworkResult<AccountResponse>) {
        when (result) {
            is NetworkResult.Success -> {
                val account = result.data
                _userData.value = UserData(
                    name = account.name ?: "",
                    surname = account.surname ?: "",
                    phone = account.phone ?: "",
                    isLoggedIn = true,
                    hasValidToken = true,
                    isLoading = false
                )
            }
            is NetworkResult.HttpError -> {
                val (errorMessage, shouldLogout) = when (result.code) {
                    401 -> "Сессия истекла. Пожалуйста, войдите снова." to true
                    403 -> "Доступ запрещен" to true
                    404 -> "Пользователь не найден" to true
                    429 -> "Слишком много запросов. Подождите немного" to false
                    500 -> "Ошибка на сервере. Попробуйте позже" to false
                    502, 503, 504 -> "Сервер временно недоступен" to false
                    else -> "Ошибка сервера (${result.code})" to false
                }

                if (shouldLogout) {
                    backend.tokensStore?.clear()
                }

                _userData.value = UserData(
                    isLoggedIn = false,
                    hasValidToken = !shouldLogout,
                    isLoading = false,
                    error = errorMessage,
                    errorCode = result.code
                )
            }
            is NetworkResult.NetworkError -> {
                _userData.value = _userData.value.copy(
                    isLoading = false,
                    error = "Нет подключения к интернету. Проверьте соединение."
                )
            }
            is NetworkResult.SerializationError -> {
                _userData.value = UserData(
                    isLoggedIn = false,
                    hasValidToken = false,
                    isLoading = false,
                    error = "Ошибка обработки данных сервера"
                )
                backend.tokensStore?.clear()
            }
            is NetworkResult.UnknownError -> {
                _userData.value = UserData(
                    isLoggedIn = false,
                    hasValidToken = false,
                    isLoading = false,
                    error = "Неизвестная ошибка"
                )
                backend.tokensStore?.clear()
            }
        }
    }

    fun updateUser(name: String, surname: String, phone: String) {
        _userData.value = _userData.value.copy(
            name = name,
            surname = surname,
            phone = phone,
            isLoggedIn = true,
            hasValidToken = true
        )
    }

    fun updateAccount(name: String, surname: String) {
        viewModelScope.launch {
            _userData.value = _userData.value.copy(
                isLoading = true,
                error = null,
                errorCode = null
            )

            try {
                val request = UpdateAccountRequest(
                    newName = name.takeIf { it.isNotBlank() },
                    newSurname = surname.takeIf { it.isNotBlank() }
                )
                val result = backend.updateAccount(request)
                handleUpdateResult(result, name, surname)
            } catch (e: Exception) {
                _userData.value = _userData.value.copy(
                    isLoading = false,
                    error = "Ошибка обновления профиля: ${e.localizedMessage ?: "неизвестно"}"
                )
            }
        }
    }

    private fun handleUpdateResult(
        result: NetworkResult<AccountResponse>,
        requestedName: String,
        requestedSurname: String
    ) {
        when (result) {
            is NetworkResult.Success -> {
                val account = result.data
                _userData.value = UserData(
                    name = account.name ?: requestedName,
                    surname = account.surname ?: requestedSurname,
                    phone = account.phone ?: _userData.value.phone,
                    isLoggedIn = true,
                    hasValidToken = true,
                    isLoading = false
                )
            }
            is NetworkResult.HttpError -> {
                val errorMessage = when (result.code) {
                    400 -> "Некорректные данные. Проверьте введенные значения."
                    401 -> {
                        backend.tokensStore?.clear()
                        "Сессия истекла. Пожалуйста, войдите снова."
                    }
                    403 -> "Нет прав для изменения профиля"
                    422 -> "Неверный формат данных"
                    else -> "Не удалось обновить профиль (ошибка ${result.code})"
                }

                _userData.value = _userData.value.copy(
                    isLoading = false,
                    error = errorMessage,
                    errorCode = result.code
                )
            }
            is NetworkResult.NetworkError -> {
                _userData.value = _userData.value.copy(
                    isLoading = false,
                    error = "Нет подключения к интернету. Изменения не сохранены."
                )
            }
            is NetworkResult.SerializationError -> {
                _userData.value = _userData.value.copy(
                    isLoading = false,
                    error = "Ошибка обработки ответа сервера"
                )
            }
            is NetworkResult.UnknownError -> {
                _userData.value = _userData.value.copy(
                    isLoading = false,
                    error = "Не удалось сохранить изменения"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _userData.value = _userData.value.copy(
                isLoading = true,
                error = null,
                errorCode = null
            )

            try {
                val tokens = backend.tokensStore?.currentTokens()
                if (tokens?.refreshToken != null) {
                    val request = LogoutRequest(tokens.refreshToken)
                    val result = backend.logout(request)
                    handleLogoutResult(result)
                } else {
                    clearUserData()
                }
            } catch (e: Exception) {
                clearUserData()
            }
        }
    }

    private fun handleLogoutResult(result: NetworkResult<Unit>) {
        when (result) {
            is NetworkResult.Success -> {
                clearUserData("Вы успешно вышли из системы")
            }
            is NetworkResult.HttpError -> {
                val message = when (result.code) {
                    401 -> "Сессия уже истекла"
                    else -> "Ошибка выхода (${result.code})"
                }
                clearUserData(message)
            }
            is NetworkResult.NetworkError -> {
                clearUserData("Нет подключения, но вы вышли локально")
            }
            else -> {
                clearUserData("Ошибка при выходе")
            }
        }
    }

    private fun clearUserData(errorMessage: String? = null) {
        backend.tokensStore?.clear()
        _userData.value = UserData(
            isLoggedIn = false,
            hasValidToken = false,
            isLoading = false,
            error = errorMessage
        )
    }

}