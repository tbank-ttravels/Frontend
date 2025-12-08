package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    data class UserData(
        val name: String = "",
        val phone: String = "",
        val isLoggedIn: Boolean = false,
        val id: String = "",
    )

    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData> = _userData

    init {
        viewModelScope.launch {
            userPreferences.userName.collectLatest { name ->
                userPreferences.userPhone.collectLatest { phone ->
                    _userData.value = UserData(
                        name = name,
                        phone = phone,
                        isLoggedIn = name.isNotEmpty() && phone.isNotEmpty()
                    )
                }
            }
        }
    }

    fun updateUser(name: String, phone: String) {
        viewModelScope.launch {
            userPreferences.saveUser(name, phone)
            _userData.value = UserData(
                name = name,
                phone = phone,
                isLoggedIn = true
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clear()
            _userData.value = UserData()
        }
    }
}