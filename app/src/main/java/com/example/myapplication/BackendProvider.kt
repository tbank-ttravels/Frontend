package com.example.myapplication

import android.content.Context
import com.example.core_data.network.NetworkDefaults
import com.example.core_data.network.PersistentTokensStore
import com.example.core_data.repository.TTravelsBackend
import com.example.myapplication.BuildConfig

object BackendProvider {
    @Volatile
    private var backend: TTravelsBackend? = null

    /**
     * @param context контекст приложения
     * @param baseUrl базовый URL
     */
    fun get(context: Context, baseUrl: String = BuildConfig.BASE_URL): TTravelsBackend {
        val appContext = context.applicationContext
        val finalUrl = baseUrl.ifBlank { NetworkDefaults.DEFAULT_BASE_URL }
        return backend ?: synchronized(this) {
            backend ?: TTravelsBackend.create(
                baseUrl = finalUrl,
                tokensStore = PersistentTokensStore.create(appContext),
                json = com.example.core_data.network.defaultJson(),
                configureClient = null
            ).also { backend = it }
        }
    }


    fun get(): TTravelsBackend {
        return backend ?: throw IllegalStateException("Запущено")
    }
}
