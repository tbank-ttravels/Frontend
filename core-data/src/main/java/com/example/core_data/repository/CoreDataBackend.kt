package com.example.core_data.repository

import android.content.Context

object CoreDataBackend {
    private var backend: TTravelsBackend? = null

    /**
     * @param context контекст приложения
     * @param baseUrl базовый URL, передается из app
     */
    fun get(context: Context, baseUrl: String): TTravelsBackend {
        val appContext = context.applicationContext
        return backend ?: synchronized(this) {
            backend ?: TTravelsBackend.create(
                baseUrl = baseUrl
            ).also { backend = it }
        }
    }
}
