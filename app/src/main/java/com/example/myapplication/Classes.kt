package com.example.myapplication

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
data class TripInvitation(
    val id: String,
    val tripName: String,
    val fromUserName: String,
    val date: String,
    val status: InvitationStatus
)
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val date: String,
    val type: NotificationType,
    val isRead: Boolean
)


enum class ConfirmationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    LEFT
}

enum class NotificationType {
    INVITATION, EXPENSE, MESSAGE, SYSTEM
}
enum class InvitationStatus {
    PENDING, ACCEPTED, REJECTED
}


enum class InvitationAction {
    ACCEPT, REJECT, VIEW_TRIP
}

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
data class CategoryAnalytics(
    val categoryName: String,
    val totalAmount: Double,
    val percentage: Double,
    val expenseCount: Int,
    val participantSpending: Map<String, Double>
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