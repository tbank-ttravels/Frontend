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
        val surname: String = "",
        val phone: String = "",
        val isLoggedIn: Boolean = false,
        val id: String = "",
    )

    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData> = _userData

    init {
        viewModelScope.launch {
            userPreferences.userName.collectLatest { name ->
                userPreferences.userSurname.collectLatest { surname ->
                    userPreferences.userPhone.collectLatest { phone ->
                        _userData.value = UserData(
                            name = name,
                            surname = surname,
                            phone = phone,
                            isLoggedIn = phone.isNotEmpty()
                        )
                    }
                }
            }
        }
    }

    fun updateUser(name: String, surname: String, phone: String) {
        viewModelScope.launch {
            val current = _userData.value
            val nameToStore = if (name.isNotBlank()) name else current.name
            val surnameToStore = if (surname.isNotBlank()) surname else current.surname
            val phoneToStore = if (phone.isNotBlank()) phone else current.phone

            userPreferences.saveUser(nameToStore, surnameToStore, phoneToStore)
            _userData.value = UserData(
                name = nameToStore,
                surname = surnameToStore,
                phone = phoneToStore,
                isLoggedIn = phoneToStore.isNotEmpty()
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
