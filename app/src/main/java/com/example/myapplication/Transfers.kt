package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.core_data.network.NetworkResult

@Composable
fun TransfersTab(
    trip: Trip,
    tripViewModel: TripViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    var isLoadingTransfers by remember { mutableStateOf(false) }
    var isLoadingDebts by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val transfers by remember(trip.id) {
        derivedStateOf { tripViewModel.getTransfersForTrip(trip.id) }
    }

    val participantBalances by remember(trip.id) {
        derivedStateOf { tripViewModel.calculateParticipantBalances(trip.id) }
    }

    // Загрузка переводов
    LaunchedEffect(trip.id) {
        isLoadingTransfers = true
        errorMessage = null
        when (val res = backend.getTransfers(trip.id.toLong())) {
            is NetworkResult.Success -> {
                val mappedTransfers = res.data.transfers.map {
                    Transfer(
                        id = it.id.toString(),
                        fromUserId = it.senderId.toString(),
                        toUserId = it.recipientId.toString(),
                        amount = it.sum
                    )
                }
                tripViewModel.setTransfers(trip.id, mappedTransfers)
            }
            is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
            is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
            else -> errorMessage = "Не удалось загрузить переводы"
        }
        isLoadingTransfers = false
    }

    // Загрузка долгов
    LaunchedEffect(trip.id) {
        isLoadingDebts = true
        when (val res = backend.getTravelDebts(trip.id.toLong())) {
            is NetworkResult.Success -> {
                val balances = mutableMapOf<String, Double>()

                // Инициализируем всех участников с нулевым балансом
                trip.participants.forEach { participant ->
                    balances[participant.id] = 0.0
                }

                // Обрабатываем долги (должники)
                res.data.debts.forEach {
                    val userId = it.user.id.toString()
                    balances[userId] = (balances[userId] ?: 0.0) - (it.totalAmount ?: 0.0)
                }

                // Обрабатываем кредиторов (кому должны)
                res.data.creditors.forEach {
                    val userId = it.user.id.toString()
                    balances[userId] = (balances[userId] ?: 0.0) + (it.totalAmount ?: 0.0)
                }

                tripViewModel.setParticipantBalances(trip.id, balances)
            }
            else -> {}
        }
        isLoadingDebts = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Переводы", fontSize = 28.sp)

            if (trip.participants.size >= 2) {
                Button(
                    onClick = { navController.navigate("add_transfer/${trip.id}") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD2D),
                        contentColor = Color(0xFF333333)
                    )
                ) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Добавить")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isLoadingTransfers || isLoadingDebts) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFFDD2D))
            }
        } else {

            if (participantBalances.isNotEmpty()) {
                BalanceCard(participantBalances, trip)
                Spacer(Modifier.height(16.dp))
            }

            if (transfers.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.CompareArrows,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFBDBDBD)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Переводы пока не добавлены", fontSize = 18.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(transfers) {
                        TransferItem(it, trip, tripViewModel)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        errorMessage?.let {
            Text(it, color = Color.Red, fontSize = 12.sp)
        }
    }
}

/* ---------------- COMPONENTS ---------------- */

@Composable
fun BalanceCard(
    participantBalances: Map<String, Double>,
    trip: Trip
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(
                "Баланс участников",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            participantBalances
                .entries
                .sortedByDescending { it.value }
                .forEach { entry ->

                    val user = trip.participants.find { it.id == entry.key }
                    val balance = entry.value

                    user?.let {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Person,
                                    null,
                                    tint = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(it.name)
                            }

                            Text(
                                "${balance.toInt()} ₽",
                                fontWeight = FontWeight.Bold,
                                color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    }
                }
        }
    }
}

@Composable
fun TransferItem(
    transfer: Transfer,
    trip: Trip,
    tripViewModel: TripViewModel
) {
    val fromUser = trip.participants.find { it.id == transfer.fromUserId }
    val toUser = trip.participants.find { it.id == transfer.toUserId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${fromUser?.name ?: "Неизвестный"} → ${toUser?.name ?: "Неизвестный"}")
            Text("${transfer.amount.toInt()} ₽")
        }
    }
}
