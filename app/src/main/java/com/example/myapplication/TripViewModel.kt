package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID

data class Transfer(
    val fromUserId: String,
    val toUserId: String,
    val amount: Double
)

class TripViewModel : ViewModel() {
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips

    private val _expenses = MutableStateFlow<Map<String, List<Expense>>>(emptyMap())

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

    init {
        println("TripViewModel создан")

        viewModelScope.launch {
            val testTrip = Trip(
                startTown = "Москва",
                endTown = "Санкт-Петербург",
                startDate = "15.01.2024",
                endDate = "20.01.2024",
                budget = "50000",
                participants = listOf(
                    User(name = "Татьяна", phone = "89345678978"),
                    User(name = "Игорь", phone = "89457899068"),
                    User(name = "Светлана", phone = "89324567890")
                )
            )
            addTrip(testTrip)

            delay(100)

            val tripId = _trips.value.firstOrNull()?.id ?: ""
            if (tripId.isNotEmpty()) {
                val trip = getTripById(tripId)
                trip?.participants?.let { participants ->
                    if (participants.isNotEmpty()) {
                        addExpense(
                            tripId = tripId,
                            expense = Expense(
                                title = "Билеты на поезд",
                                amount = 6000.0,
                                category = "Транспорт",
                                payerId = participants[0].id,
                                paidFor = "За всех участников"
                            )
                        )

                        addExpense(
                            tripId = tripId,
                            expense = Expense(
                                title = "Отель",
                                amount = 15000.0,
                                category = "Проживание",
                                payerId = participants[1].id,
                                paidFor = "За всех участников"
                            )
                        )

                        addExpense(
                            tripId = tripId,
                            expense = Expense(
                                title = "Ужин в ресторане",
                                amount = 5000.0,
                                category = "Еда",
                                payerId = participants[2].id,
                                paidFor = "За: Татьяна"
                            )
                        )
                    }
                }
            }
        }
    }

    fun addTrip(newTrip: Trip) {
        viewModelScope.launch {
            println("Добавляем поездку: ${newTrip.startTown} -> ${newTrip.endTown}")
            _trips.value = _trips.value + newTrip
            println("Теперь поездок: ${_trips.value.size}")
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
            println("Обновлены все расходы для поездки $tripId: ${expenses.size} расходов")
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
        return calculateTransfers(trip)
    }

    fun calculateParticipantBalanceForTrip(userId: String, tripId: String): Double {
        val trip = getTripById(tripId) ?: return 0.0
        return calculateParticipantBalance(userId, trip)
    }
}