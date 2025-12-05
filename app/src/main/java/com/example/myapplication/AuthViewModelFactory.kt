package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core_data.repository.TTravelsBackend

class AuthViewModelFactory(
    private val backend: TTravelsBackend
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(backend) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}