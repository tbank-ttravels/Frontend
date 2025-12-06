package com.example.myapplication

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val startTown: String,
    val endTown: String,
    val startDate: String,
    val endDate: String,
    val budget: String,
    val participants: List<User> = emptyList(),
    val expenses: List<Expense> = emptyList()
)

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String
)

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: String,
    val payerId: String,
    val paidFor: String = "Только себя",
    val date: String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
)