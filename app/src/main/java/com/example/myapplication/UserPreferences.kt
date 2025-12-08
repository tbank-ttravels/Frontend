package com.example.myapplication
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_preferences")
class UserPreferences(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PHONE = stringPreferencesKey("user_phone")
    }

    suspend fun saveUser(name: String, phone: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME] = name
            preferences[USER_PHONE] = phone
        }
    }

    val userName: Flow<String> = dataStore.data
        .map { preferences -> preferences[USER_NAME] ?: "" }

    val userPhone: Flow<String> = dataStore.data
        .map { preferences -> preferences[USER_PHONE] ?: "" }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}