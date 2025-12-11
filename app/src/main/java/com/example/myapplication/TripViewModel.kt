package com.example.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

data class Transfer(
    val fromUserId: String,
    val toUserId: String,
    val amount: Double,
    val id: String = UUID.randomUUID().toString()
)

class TripViewModel : ViewModel() {

    private val baseUrl = "https://your-backend.com/api"

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips

    private val _expenses = MutableStateFlow<Map<String, List<Expense>>>(emptyMap())
    private val _transfers = MutableStateFlow<Map<String, List<Transfer>>>(emptyMap())

    init {
        loadTrips()
    }

    /** -------------------- Trips -------------------- **/
    fun loadTrips() {
        viewModelScope.launch {
            val loadedTrips = withContext(Dispatchers.IO) { fetchTripsNetwork() }
            _trips.value = loadedTrips
        }
    }

    private fun fetchTripsNetwork(): List<Trip> {
        val trips = mutableListOf<Trip>()
        try {
            val url = URL("$baseUrl/travels")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val jsonArray = JSONArray(response)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    trips.add(
                        Trip(
                            id = obj.getString("id"),
                            startTown = obj.getString("startTown"),
                            endTown = obj.getString("endTown"),
                            startDate = obj.getString("startDate"),
                            endDate = obj.getString("endDate"),
                            budget = obj.optString("budget", "0"),
                            participants = emptyList(),
                            expenses = emptyList()
                        )
                    )
                }
            }
            connection.disconnect()
        } catch (e: Exception) {
            Log.e("Network", "Error fetching trips: ${e.localizedMessage}")
        }
        return trips
    }

    fun getTripById(tripId: String): Trip? {
        return _trips.value.find { it.id == tripId }?.copy(
            expenses = _expenses.value[tripId] ?: emptyList()
        )
    }

    fun addTrip(trip: Trip) {
        viewModelScope.launch {
            val createdTrip = withContext(Dispatchers.IO) { createTripNetwork(trip) }
            createdTrip?.let { _trips.value = _trips.value + it }
        }
    }

    private fun createTripNetwork(trip: Trip): Trip? {
        return try {
            val url = URL("$baseUrl/travels")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            val json = JSONObject().apply {
                put("startTown", trip.startTown)
                put("endTown", trip.endTown)
                put("startDate", trip.startDate)
                put("endDate", trip.endDate)
                put("budget", trip.budget)
            }
            OutputStreamWriter(connection.outputStream).use { it.write(json.toString()) }
            if (connection.responseCode == HttpURLConnection.HTTP_CREATED || connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val obj = JSONObject(response)
                Trip(
                    id = obj.getString("id"),
                    startTown = obj.getString("startTown"),
                    endTown = obj.getString("endTown"),
                    startDate = obj.getString("startDate"),
                    endDate = obj.getString("endDate"),
                    budget = obj.optString("budget", "0"),
                    participants = emptyList(),
                    expenses = emptyList()
                )
            } else null
        } catch (e: Exception) {
            Log.e("Network", "Error creating trip: ${e.localizedMessage}")
            null
        }
    }

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) { updateTripNetwork(trip) }
            if (success) _trips.value = _trips.value.map { if (it.id == trip.id) trip else it }
        }
    }

    private fun updateTripNetwork(trip: Trip): Boolean {
        return try {
            val url = URL("$baseUrl/travels/${trip.id}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            val json = JSONObject().apply {
                put("startTown", trip.startTown)
                put("endTown", trip.endTown)
                put("startDate", trip.startDate)
                put("endDate", trip.endDate)
                put("budget", trip.budget)
            }
            OutputStreamWriter(connection.outputStream).use { it.write(json.toString()) }
            val success = connection.responseCode == HttpURLConnection.HTTP_OK
            connection.disconnect()
            success
        } catch (e: Exception) {
            Log.e("Network", "Error updating trip: ${e.localizedMessage}")
            false
        }
    }

    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { deleteTripNetwork(tripId) }
            _trips.value = _trips.value.filter { it.id != tripId }
            _expenses.value = _expenses.value.filterKeys { it != tripId }
            _transfers.value = _transfers.value.filterKeys { it != tripId }
        }
    }

    private fun deleteTripNetwork(tripId: String) {
        try {
            val url = URL("$baseUrl/travels/$tripId")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "DELETE"
            connection.responseCode
            connection.disconnect()
        } catch (e: Exception) {
            Log.e("Network", "Error deleting trip: ${e.localizedMessage}")
        }
    }

    /** -------------------- Expenses -------------------- **/
    fun addExpense(tripId: String, expense: Expense) {
        viewModelScope.launch {
            val addedExpense = withContext(Dispatchers.IO) { addExpenseNetwork(tripId, expense) }
            addedExpense?.let {
                val current = _expenses.value[tripId] ?: emptyList()
                _expenses.value = _expenses.value + (tripId to (current + it))
            }
        }
    }

    private fun addExpenseNetwork(tripId: String, expense: Expense): Expense? {
        return try {
            val url = URL("$baseUrl/travels/$tripId/expenses")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            val json = JSONObject().apply {
                put("title", expense.title)
                put("amount", expense.amount)
                put("category", expense.category)
                put("payerId", expense.payerId)
            }
            OutputStreamWriter(connection.outputStream).use { it.write(json.toString()) }
            if (connection.responseCode == HttpURLConnection.HTTP_CREATED || connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val obj = JSONObject(response)
                Expense(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    amount = obj.getDouble("amount"),
                    category = obj.getString("category"),
                    payerId = obj.getString("payerId"),
                    paidFor = ""
                )
            } else null
        } catch (e: Exception) {
            Log.e("Network", "Error adding expense: ${e.localizedMessage}")
            null
        }
    }

    /** -------------------- Transfers -------------------- **/
    fun addTransfer(tripId: String, transfer: Transfer) {
        viewModelScope.launch {
            val addedTransfer = withContext(Dispatchers.IO) { addTransferNetwork(tripId, transfer) }
            addedTransfer?.let {
                val current = _transfers.value[tripId] ?: emptyList()
                _transfers.value = _transfers.value + (tripId to (current + it))
            }
        }
    }

    private fun addTransferNetwork(tripId: String, transfer: Transfer): Transfer? {
        return try {
            val url = URL("$baseUrl/travels/$tripId/transfers")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            val json = JSONObject().apply {
                put("fromUserId", transfer.fromUserId)
                put("toUserId", transfer.toUserId)
                put("amount", transfer.amount)
            }
            OutputStreamWriter(connection.outputStream).use { it.write(json.toString()) }
            if (connection.responseCode == HttpURLConnection.HTTP_CREATED || connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val obj = JSONObject(response)
                Transfer(
                    id = obj.getString("id"),
                    fromUserId = obj.getString("fromUserId"),
                    toUserId = obj.getString("toUserId"),
                    amount = obj.getDouble("amount")
                )
            } else null
        } catch (e: Exception) {
            Log.e("Network", "Error adding transfer: ${e.localizedMessage}")
            null
        }
    }

    fun getTransfers(tripId: String): List<Transfer> = _transfers.value[tripId] ?: emptyList()

    fun calculateParticipantBalance(userId: String, tripId: String): Double {
        val expenses = _expenses.value[tripId] ?: emptyList()
        val transfers = _transfers.value[tripId] ?: emptyList()
        val paid = expenses.filter { it.payerId == userId }.sumOf { it.amount }
        val owes = expenses.sumOf { it.amount / (getTripById(tripId)?.participants?.size ?: 1) }
        val received = transfers.filter { it.toUserId == userId }.sumOf { it.amount }
        val sent = transfers.filter { it.fromUserId == userId }.sumOf { it.amount }
        return paid + received - owes - sent
    }
}