package com.example.myapplication

import android.content.Context
import com.example.core_data.network.PersistentTokensStore
import com.example.core_data.network.defaultJson
import com.example.core_data.repository.TTravelsBackend

object BackendProvider {

    private const val BASE_URL =
        "https://ttravels.enzolu.ru/api/v1/\n"

    @Volatile
    private var backend: TTravelsBackend? = null

    fun get(context: Context): TTravelsBackend {
        val appContext = context.applicationContext

        return backend ?: synchronized(this) {
            backend ?: TTravelsBackend.create(
                baseUrl = BASE_URL,
                tokensStore = PersistentTokensStore.create(appContext),
                json = defaultJson(),
                configureClient = null
            ).also { backend = it }
        }
    }

    fun get(): TTravelsBackend {
        return backend
            ?: throw IllegalStateException("Запущено")
    }
}
