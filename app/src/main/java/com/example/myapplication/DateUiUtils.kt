package com.example.myapplication

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val displayDateTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
private val displayDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

/**
 * Приводит дату  к читабельному виду.
 * Поддерживает ISO-подобные форматы и dd.MM.yyyy (c/без времени).
 * Если не получилось распарсить - возвращает исходную строку.
 */
fun formatDateForUi(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val trimmed = raw.trim()
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" to true,
        "yyyy-MM-dd'T'HH:mm:ssXXX" to true,
        "yyyy-MM-dd'T'HH:mm:ss'Z'" to true,
        "yyyy-MM-dd'T'HH:mm:ss" to true,
        "yyyy-MM-dd" to false,
        "dd.MM.yyyy HH:mm" to true,
        "dd.MM.yyyy" to false
    )

    patterns.forEach { (pattern, hasTime) ->
        val parser = SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        try {
            val parsed = parser.parse(trimmed)
            if (parsed != null) {
                return if (hasTime) displayDateTime.format(parsed) else displayDate.format(parsed)
            }
        } catch (_: ParseException) {
        }
    }
    return trimmed
}