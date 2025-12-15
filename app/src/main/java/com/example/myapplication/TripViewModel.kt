package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core_data.repository.TTravelsBackend
import com.example.core_data.model.*
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TripViewModel(private val backend: TTravelsBackend) : ViewModel() {
    companion object {
        fun provideFactoryTrip(backend: TTravelsBackend): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
                        return TripViewModel(backend) as T
                    }
                    throw IllegalArgumentException("Незнакомый ViewModel Class")
                }
            }
        }
    }
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    private val _expenses = MutableStateFlow<Map<String, List<Expense>>>(emptyMap())
    private val _transfers = MutableStateFlow<Map<String, List<Transfer>>>(emptyMap())
    private val _balances = MutableStateFlow<Map<String, Map<String, Double>>>(emptyMap())
    private val _categories = MutableStateFlow<Map<String, List<CategoryResponse>>>(emptyMap())

    private val _invitations = MutableStateFlow<List<TripInvitation>>(emptyList())
    val invitations: StateFlow<List<TripInvitation>> = _invitations.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTripsFromBackend()
        loadInvitationsFromBackend()
    }

    fun loadTripsFromBackend() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = backend.getMyTravels()
                when (result) {
                    is NetworkResult.Success -> {
                        val tripsResponse = result.data
                        val tripsList = tripsResponse.travels?.map { travel ->
                            Trip(
                                id = travel.id.toString(),
                                name = travel.name,
                                description = travel.description,
                                startDate = travel.startDate ?: "",
                                endDate = travel.endDate ?: "",
                                participants = emptyList(),
                                expenses = emptyList(),
                                route = travel.description
                            )
                        } ?: emptyList()

                        _trips.value = tripsList

                        tripsList.forEach { trip ->
                            loadExpensesForTrip(trip.id)
                            loadTransfersForTrip(trip.id)
                            loadCategoriesForTrip(trip.id)
                        }
                    }
                    is NetworkResult.HttpError -> {
                        _error.value = "Ошибка загрузки поездок: ${result.code}"
                    }
                    is NetworkResult.NetworkError -> {
                        _error.value = "Нет подключения к интернету"
                    }
                    else -> {
                        _error.value = "Неизвестная ошибка"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun createTripViaBackend(name: String, description: String? = null, startDate: String, endDate: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val request = CreateTravelRequest(
                    name = name,
                    description = description,
                    startDate = startDate,
                    endDate = endDate
                )

                val result = backend.createTravel(request)
                when (result) {
                    is NetworkResult.Success -> {
                        val travel = result.data
                        val newTrip = Trip(
                            id = travel.id.toString(),
                            name = travel.name,
                            description = travel.description,
                            startDate = travel.startDate ?: "",
                            endDate = travel.endDate ?: "",
                            status = null,
                            participants = emptyList(),
                            expenses = emptyList(),
                            route = travel.description
                        )
                        _trips.value = _trips.value + newTrip
                    }
                    is NetworkResult.HttpError -> {
                        _error.value = "Ошибка создания поездки: ${result.code}"
                    }
                    else -> {
                        _error.value = "Не удалось создать поездку"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    private fun loadExpensesForTrip(tripId: String) {
        viewModelScope.launch {
            try {
                val result = backend.getTravelExpenses(tripId.toLong())
                if (result is NetworkResult.Success) {
                    val expenses = result.data.expenses.map { expenseDto ->
                        val participantsMap = expenseDto.participants.associate {
                            it.userId.toString() to (it.share ?: 0.0)
                        }

                        Expense(
                            id = expenseDto.id.toString(),
                            title = expenseDto.name,
                            amount = expenseDto.sum ?: 0.0,
                            category = expenseDto.categoryName ?: expenseDto.categoryId?.toString() ?: "",
                            payerId = expenseDto.payerId.toString(),
                            paidFor = "Все участники",
                            date = expenseDto.date,
                            name = expenseDto.name
                        )
                    }
                    _expenses.value = _expenses.value + (tripId to expenses)
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun loadTransfersForTrip(tripId: String) {
        viewModelScope.launch {
            try {
                val result = backend.getTransfers(tripId.toLong())
                if (result is NetworkResult.Success) {
                    val transfers = result.data.transfers.map { transfer ->
                        Transfer(
                            fromUserId = transfer.senderId.toString(),
                            toUserId = transfer.recipientId.toString(),
                            amount = transfer.sum
                        )
                    }
                    _transfers.value = _transfers.value + (tripId to transfers)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun loadCategoriesForTrip(tripId: String) {
        viewModelScope.launch {
            try {
                val result = backend.getCategories(tripId.toLong())
                if (result is NetworkResult.Success) {
                    _categories.value = _categories.value + (tripId to result.data.items)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun loadInvitationsFromBackend() {
        viewModelScope.launch {
            try {
                val result = backend.getInvites()
                if (result is NetworkResult.Success) {
                    val invites = result.data.invites.map { invite ->
                        TripInvitation(
                            id = invite.inviteId.toString(),
                            tripName = invite.travelName ?: "Неизвестная поездка",
                            date = invite.startDate ?: "",
                            status = InvitationStatus.PENDING
                        )
                    }
                    _invitations.value = invites
                }
            } catch (e: Exception) {
            }
        }
    }

    fun updateTripViaBackend(tripId: String, name: String, description: String?, startDate: String?, endDate: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val request = EditTravelRequest(
                    name = name,
                    description = description,
                    startDate = startDate,
                    endDate = endDate
                )

                val result = backend.editTravel(tripId.toLong(), request)
                when (result) {
                    is NetworkResult.Success -> {
                        val travel = result.data
                        val updatedTrip = Trip(
                            id = travel.id.toString(),
                            name = travel.name,
                            description = travel.description,
                            startDate = travel.startDate ?: "",
                            endDate = travel.endDate ?: "",
                            status = null,
                            participants = emptyList(),
                            expenses = emptyList(),
                            route = travel.description
                        )
                        updateTrip(updatedTrip)
                        loadTripsFromBackend()
                    }
                    is NetworkResult.HttpError -> {
                        _error.value = "Ошибка обновления поездки: ${result.code}"
                    }
                    else -> {
                        _error.value = "Не удалось обновить поездку"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun addExpenseViaBackend(tripId: String, expense: Expense) {
        viewModelScope.launch {
            try {
                val categoryId = try {
                    expense.category.toLong()
                } catch (e: NumberFormatException) {
                    1L
                }

                val request = ExpenseRequestDTO(
                    name = expense.name,
                    description = expense.title,
                    payerId = expense.payerId.toLong(),
                    date = expense.date,
                    participantShares = mapOf(expense.payerId.toLong() to expense.amount),
                    categoryId = categoryId
                )

                val result = backend.createExpense(tripId.toLong(), request)
                if (result is NetworkResult.Success) {
                    loadExpensesForTrip(tripId)
                }
            } catch (e: Exception) {
                addExpense(tripId, expense)
            }
        }
    }

    fun createCategoryForTrip(tripId: String, categoryName: String) {
        viewModelScope.launch {
            try {
                val request = CreateCategoryRequest(name = categoryName)
                val result = backend.createCategory(tripId.toLong(), request)
                if (result is NetworkResult.Success) {
                    loadCategoriesForTrip(tripId)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun addTransferViaBackend(tripId: String, fromUserId: String, toUserId: String, amount: Double) {
        viewModelScope.launch {
            try {
                val request = CreateTransferRequest(
                    senderId = fromUserId.toLong(),
                    recipientId = toUserId.toLong(),
                    sum = amount
                )

                val result = backend.createTransfer(tripId.toLong(), request)
                if (result is NetworkResult.Success) {
                    loadTransfersForTrip(tripId)
                }
            } catch (e: Exception) {
                addTransfer(tripId, fromUserId, toUserId, amount)
            }
        }
    }

    fun inviteMemberViaBackend(tripId: String, phone: String) {
        viewModelScope.launch {
            try {
                val request = InviteRequest(
                    phones = listOf(phone)
                )

                val result = backend.inviteMembers(tripId.toLong(), request)
                if (result is NetworkResult.Success) {
                }
            } catch (e: Exception) {
            }
        }
    }

    fun respondToInvitationViaBackend(invitationId: String, accept: Boolean) {
        viewModelScope.launch {
            try {
                val result = backend.respondToInvite(invitationId.toLong(), accept)
                if (result is NetworkResult.Success) {
                    if (accept) {
                        acceptInvitation(invitationId)
                    } else {
                        rejectInvitationById(invitationId)
                    }
                    loadInvitationsFromBackend()
                }
            } catch (e: Exception) {
                if (accept) {
                    acceptInvitation(invitationId)
                } else {
                    rejectInvitationById(invitationId)
                }
            }
        }
    }

    fun loadDebtsForTrip(tripId: String) {
        viewModelScope.launch {
            try {
                val result = backend.getTravelDebts(tripId.toLong())
                if (result is NetworkResult.Success) {
                    val debts = emptyMap<String, Double>()
                    _balances.value = _balances.value + (tripId to debts)
                }
            } catch (e: Exception) {
                calculateBalancesLocally(tripId)
            }
        }
    }

    private fun calculateBalancesLocally(tripId: String) {
        val expenses = _expenses.value[tripId] ?: emptyList()
        val transfers = _transfers.value[tripId] ?: emptyList()

        val balances = mutableMapOf<String, Double>()

        expenses.forEach { expense ->
            val trip = _trips.value.find { it.id == tripId }
            val participants = trip?.participants ?: emptyList()

            participants.forEach { participant ->
                val current = balances[participant.id] ?: 0.0
                balances[participant.id] = current - expense.amount / participants.size
            }

            val currentPayer = balances[expense.payerId] ?: 0.0
            balances[expense.payerId] = currentPayer + expense.amount
        }

        transfers.forEach { transfer ->
            val fromBalance = balances[transfer.fromUserId] ?: 0.0
            balances[transfer.fromUserId] = fromBalance - transfer.amount

            val toBalance = balances[transfer.toUserId] ?: 0.0
            balances[transfer.toUserId] = toBalance + transfer.amount
        }

        _balances.value = _balances.value + (tripId to balances)
    }

    fun clearError() {
        _error.value = null
    }

    fun addTrip(newTrip: Trip) {
        viewModelScope.launch {
            _trips.value = _trips.value + newTrip
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
            }
        }
    }

    fun confirmParticipation(tripId: String, userId: String) {
        viewModelScope.launch {
        }
    }

    fun rejectInvitation(tripId: String, userId: String) {
        viewModelScope.launch {
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
            _categories.value = _categories.value.filterKeys { it != tripId }
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
        }
    }

    fun updateExpense(tripId: String, expense: Expense) {
        viewModelScope.launch {
            val currentExpenses = _expenses.value[tripId] ?: emptyList()
            val updatedExpenses = currentExpenses.map {
                if (it.id == expense.id) expense else it
            }
            _expenses.value = _expenses.value + (tripId to updatedExpenses)
        }
    }

    fun deleteExpense(tripId: String, expenseId: String) {
        viewModelScope.launch {
            val currentExpenses = _expenses.value[tripId] ?: emptyList()
            val updatedExpenses = currentExpenses.filter { it.id != expenseId }
            _expenses.value = _expenses.value + (tripId to updatedExpenses)
        }
    }

    fun getExpensesForTrip(tripId: String): List<Expense> {
        return _expenses.value[tripId] ?: emptyList()
    }

    fun updateTripExpenses(tripId: String, expenses: List<Expense>) {
        viewModelScope.launch {
            _expenses.value = _expenses.value + (tripId to expenses)
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
        return getTransfersForTrip(tripId)
    }

    fun calculateParticipantBalanceForTrip(userId: String, tripId: String): Double {
        return calculateParticipantBalances(tripId)[userId] ?: 0.0
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
        }
    }

    fun getTransfersForTrip(tripId: String): List<Transfer> {
        return _transfers.value[tripId] ?: emptyList()
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

    fun deleteTransfer(tripId: String, fromUserId: String, toUserId: String, amount: Double) {
        viewModelScope.launch {
            val currentTransfers = _transfers.value[tripId] ?: emptyList()
            val updatedTransfers = currentTransfers.filter { transfer ->
                !(transfer.fromUserId == fromUserId &&
                        transfer.toUserId == toUserId &&
                        transfer.amount == amount)
            }
            _transfers.value = _transfers.value + (tripId to updatedTransfers)
        }
    }

    fun calculateParticipantBalances(tripId: String): Map<String, Double> {
        return _balances.value[tripId] ?: emptyMap()
    }

    fun setParticipantBalances(tripId: String, balances: Map<String, Double>) {
        viewModelScope.launch {
            _balances.value = _balances.value + (tripId to balances)
        }
    }

    fun setExpenses(tripId: String, expenses: List<Expense>) {
        viewModelScope.launch {
            _expenses.value = _expenses.value + (tripId to expenses)
        }
    }

    fun setTransfers(tripId: String, transfers: List<Transfer>) {
        viewModelScope.launch {
            _transfers.value = _transfers.value + (tripId to transfers)
        }
    }

    fun getCategoriesForTrip(tripId: String): List<CategoryResponse> {
        return _categories.value[tripId] ?: emptyList()
    }
}