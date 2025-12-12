package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class Transfer(
    val id: String = UUID.randomUUID().toString(),
    val fromUserId: String,
    val toUserId: String,
    val amount: Double
)

class TripViewModel : ViewModel() {
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips

    private val _expenses = MutableStateFlow<Map<String, List<Expense>>>(emptyMap())
    private val _transfers = MutableStateFlow<Map<String, List<Transfer>>>(emptyMap())

    private val _invitations = MutableStateFlow<List<TripInvitation>>(emptyList())
    val invitations: StateFlow<List<TripInvitation>> = _invitations

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    companion object {
        val expenseCategories = listOf(
            "Транспорт",
            "Проживание",
            "Еда",
            "Развлечения",
            "Покупки",
            "Прочее"
        )
    }

    fun addTrip(newTrip: Trip) {
        viewModelScope.launch {
            println("Добавляем поездку: ${newTrip.name}")
            _trips.value = _trips.value + newTrip
            println("Теперь поездок: ${_trips.value.size}")
        }
    }

    fun sendInvitation(tripId: String, userId: String, invitedBy: String) {
        viewModelScope.launch {
            val trip = getTripById(tripId)
            if (trip != null) {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val invitation = TripInvitation(
                    id = UUID.randomUUID().toString(),
                    tripName = trip.name,
                    date = dateFormat.format(Date()),
                    status = InvitationStatus.PENDING
                )
                _invitations.value = _invitations.value + invitation

                val notification = Notification(
                    id = UUID.randomUUID().toString(),
                    title = "Приглашение в поездку",
                    message = "Вас приглашают в поездку ${trip.name}",
                    date = dateFormat.format(Date()),
                    type = NotificationType.INVITATION,
                    isRead = false
                )
                _notifications.value = _notifications.value + notification

                println("Отправлено приглашение пользователю $userId в поездку $tripId")
            }
        }
    }

    fun confirmParticipation(tripId: String, userId: String) {
        viewModelScope.launch {
            println("Пользователь $userId подтвердил участие в поездке $tripId")
        }
    }

    fun rejectInvitation(tripId: String, userId: String) {
        viewModelScope.launch {
            println("Пользователь $userId отклонил приглашение в поездку $tripId")
        }
    }

    fun getInvitationById(invitationId: String): TripInvitation? {
        return _invitations.value.find { it.id == invitationId }
    }

    fun acceptInvitation(invitationId: String) {
        viewModelScope.launch {
            _invitations.value = _invitations.value.map { invitation ->
                if (invitation.id == invitationId) {
                    invitation.copy(status = InvitationStatus.ACCEPTED)
                } else {
                    invitation
                }
            }
        }
    }

    fun rejectInvitationById(invitationId: String) {
        viewModelScope.launch {
            _invitations.value = _invitations.value.map { invitation ->
                if (invitation.id == invitationId) {
                    invitation.copy(status = InvitationStatus.REJECTED)
                } else {
                    invitation
                }
            }
        }
    }

    fun getTripById(tripId: String): Trip? {
        val trip = _trips.value.find { it.id == tripId }
        return trip?.copy(
            expenses = _expenses.value[tripId] ?: emptyList()
        )
    }

    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            _trips.value = _trips.value.filter { it.id != tripId }
            _expenses.value = _expenses.value.filterKeys { it != tripId }
            _transfers.value = _transfers.value.filterKeys { it != tripId }
        }
    }

    fun updateTrip(updatedTrip: Trip) {
        viewModelScope.launch {
            _trips.value = _trips.value.map { trip ->
                if (trip.id == updatedTrip.id) updatedTrip else trip
            }
        }
    }

    fun addExpense(tripId: String, expense: Expense) {
        viewModelScope.launch {
            val currentExpenses = _expenses.value[tripId] ?: emptyList()
            _expenses.value = _expenses.value + (tripId to (currentExpenses + expense))
            println("Добавлен расход для поездки $tripId: ${expense.title}")
        }
    }

    fun updateExpense(tripId: String, expense: Expense) {
        viewModelScope.launch {
            val currentExpenses = _expenses.value[tripId] ?: emptyList()
            val updatedExpenses = currentExpenses.map {
                if (it.id == expense.id) expense else it
            }
            _expenses.value = _expenses.value + (tripId to updatedExpenses)
            println("Обновлен расход для поездки $tripId: ${expense.title}")
        }
    }

    fun deleteExpense(tripId: String, expenseId: String) {
        viewModelScope.launch {
            val currentExpenses = _expenses.value[tripId] ?: emptyList()
            val updatedExpenses = currentExpenses.filter { it.id != expenseId }
            _expenses.value = _expenses.value + (tripId to updatedExpenses)
            println("Удален расход $expenseId из поездки $tripId")
        }
    }

    fun getExpensesForTrip(tripId: String): List<Expense> {
        return _expenses.value[tripId] ?: emptyList()
    }

    fun updateTripExpenses(tripId: String, expenses: List<Expense>) {
        viewModelScope.launch {
            _expenses.value = _expenses.value + (tripId to expenses)
            _trips.value = _trips.value.map { trip ->
                if (trip.id == tripId) trip.copy(expenses = expenses) else trip
            }
        }
    }

    fun addParticipantToTrip(tripId: String, user: User) {
        viewModelScope.launch {
            _trips.value = _trips.value.map { trip ->
                if (trip.id == tripId) {
                    val userWithId = if (user.id.isEmpty()) {
                        user.copy(id = UUID.randomUUID().toString())
                    } else {
                        user
                    }
                    val updatedParticipants = trip.participants.toMutableList().apply {
                        add(userWithId)
                    }
                    trip.copy(participants = updatedParticipants)
                } else {
                    trip
                }
            }
        }
    }

    fun removeParticipantFromTrip(tripId: String, userId: String) {
        viewModelScope.launch {
            _trips.value = _trips.value.map { trip ->
                if (trip.id == tripId) {
                    val updatedParticipants = trip.participants.filter { it.id != userId }
                    trip.copy(participants = updatedParticipants)
                } else {
                    trip
                }
            }
        }
    }

    fun getUserById(userId: String, tripId: String): User? {
        val trip = getTripById(tripId)
        return trip?.participants?.find { it.id == userId }
    }

    fun getPayerName(tripId: String, payerId: String): String {
        return getUserById(payerId, tripId)?.name ?: "Неизвестный"
    }

    fun calculateTransfersForTrip(tripId: String): List<Transfer> {
        val trip = getTripById(tripId) ?: return emptyList()
        return getMockTransfers(trip)
    }

    fun calculateParticipantBalanceForTrip(userId: String, tripId: String): Double {
        val trip = getTripById(tripId) ?: return 0.0
        return getMockParticipantBalances(trip)[userId] ?: 0.0
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            _notifications.value = _notifications.value.map { notification ->
                if (notification.id == notificationId) {
                    notification.copy(isRead = true)
                } else {
                    notification
                }
            }
        }
    }

    fun getUnreadNotificationsCount(): Int {
        return _notifications.value.count { !it.isRead }
    }
    fun addTransfer(tripId: String, fromUserId: String, toUserId: String, amount: Double) {
        viewModelScope.launch {
            val transfer = Transfer(
                fromUserId = fromUserId,
                toUserId = toUserId,
                amount = amount
            )
            val currentTransfers = _transfers.value[tripId] ?: emptyList()
            _transfers.value = _transfers.value + (tripId to (currentTransfers + transfer))
            println("Добавлен перевод в поездку $tripId: ${fromUserId} -> ${toUserId} ${amount}₽")
        }
    }

    fun getTransfersForTrip(tripId: String): List<Transfer> {
        return _transfers.value[tripId] ?: emptyList()
    }

    fun setTransfers(tripId: String, transfers: List<Transfer>) {
        viewModelScope.launch {
            _transfers.value = _transfers.value + (tripId to transfers)
        }
    }

    fun setParticipants(tripId: String, participants: List<User>) {
        viewModelScope.launch {
            _trips.value = _trips.value.map { trip ->
                if (trip.id == tripId) trip.copy(participants = participants) else trip
            }
        }
    }

    fun upsertTrip(trip: Trip) {
        viewModelScope.launch {
            val existing = _trips.value.any { it.id == trip.id }
            _trips.value = if (existing) {
                _trips.value.map { if (it.id == trip.id) trip else it }
            } else {
                _trips.value + trip
            }
        }
    }

    fun replaceTrips(trips: List<Trip>) {
        viewModelScope.launch {
            _trips.value = trips
            _expenses.value = emptyMap()
        }
    }

    fun getMockParticipantBalances(trip: Trip): Map<String, Double> {
        return if (trip.participants.size >= 2) {
            val balances = mutableMapOf<String, Double>()
            trip.participants.forEachIndexed { index, participant ->
                when (index) {
                    0 -> balances[participant.id] = 1500.0
                    1 -> balances[participant.id] = -800.0
                    2 -> if (trip.participants.size > 2) balances[participant.id] = -700.0
                    else -> balances[participant.id] = 0.0
                }
            }
            balances
        } else {
            emptyMap()
        }
    }

    fun getMockTransfers(trip: Trip): List<Transfer> {
        return if (trip.participants.size >= 2) {
            val transfers = mutableListOf<Transfer>()

            when (trip.participants.size) {
                2 -> {
                    if (trip.participants.size >= 2) {
                        transfers.add(
                            Transfer(
                                fromUserId = trip.participants[1].id,
                                toUserId = trip.participants[0].id,
                                amount = 800.0
                            )
                        )
                    }
                }
                else -> {
                    if (trip.participants.size >= 3) {
                        transfers.add(
                            Transfer(
                                fromUserId = trip.participants[1].id,
                                toUserId = trip.participants[0].id,
                                amount = 800.0
                            )
                        )
                        transfers.add(
                            Transfer(
                                fromUserId = trip.participants[2].id,
                                toUserId = trip.participants[0].id,
                                amount = 700.0
                            )
                        )
                    }
                    if (trip.participants.size >= 4) {
                        transfers.add(
                            Transfer(
                                fromUserId = trip.participants[3].id,
                                toUserId = trip.participants[0].id,
                                amount = 500.0
                            )
                        )
                    }
                }
            }

            transfers
        } else {
            emptyList()
        }
    }
    fun deleteTransfer(tripId: String, fromUserId: String, toUserId: String, amount: Double) {
        viewModelScope.launch {
            val currentTransfers = _transfers.value[tripId] ?: emptyList()
            val updatedTransfers = currentTransfers.filter { transfer ->
                !(transfer.fromUserId == fromUserId &&
                        transfer.toUserId == toUserId &&
                        transfer.amount == amount)
            }
            _transfers.value = _transfers.value + (tripId to updatedTransfers)
            println("Удален перевод из поездки $tripId: ${fromUserId} -> ${toUserId} ${amount}₽")
        }
    }
}