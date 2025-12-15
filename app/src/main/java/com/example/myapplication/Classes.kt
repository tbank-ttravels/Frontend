package com.example.myapplication

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
data class Transfer(
    val id: String = UUID.randomUUID().toString(),
    val fromUserId: String,
    val toUserId: String,
    val amount: Double
)
data class TripInvitation(
    val id: String,
    val tripName: String,
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
    val name: String,
    val description: String? = null,
    val startDate: String,
    val endDate: String? = null,
    val status: String? = null,
    val participants: List<User> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val route: String?
)

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val status: String? = null,
    val role: String? = null
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
    val title: String?,
    val amount: Double,
    val category: String,
    val payerId: String,
    val paidFor: String = "Только себя",
    val date: String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()),
    val name: String
)
