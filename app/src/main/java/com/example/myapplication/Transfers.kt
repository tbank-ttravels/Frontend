package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TransfersTab(
    trip: Trip,
    tripViewModel: TripViewModel
) {
    var transfers by remember { mutableStateOf<List<Transfer>>(emptyList()) }
    var participantBalances by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }

    LaunchedEffect(trip.expenses) {
        participantBalances = calculateParticipantBalances(trip)
        transfers = calculateTransfers(trip)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (trip.participants.size < 2) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CompareArrows,
                    contentDescription = "Нет переводов",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFBDBDBD)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Нет участников для расчетов",
                    fontSize = 18.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Добавьте минимум 2 участника",
                    fontSize = 14.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else if (transfers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CompareArrows,
                    contentDescription = "Нет переводов",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Нет долгов между участниками",
                    fontSize = 18.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Все расчеты сбалансированы",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Баланс участников",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    val sortedEntries = participantBalances.entries.sortedByDescending { it.value }

                    sortedEntries.forEach { entry ->
                        val participantId = entry.key
                        val balance = entry.value
                        val participant = trip.participants.find { it.id == participantId }
                        participant?.let { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = user.name,
                                        tint = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = user.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF333333)
                                        )
                                        Text(
                                            text = if (balance >= 0) "Должны ему/ей" else "Должен/должна",
                                            fontSize = 12.sp,
                                            color = Color(0xFF666666),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = formatCurrency(balance),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Необходимые переводы",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn {
                        items(transfers) { transfer ->
                            TransferItem(transfer = transfer, trip = trip)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransferItem(
    transfer: Transfer,
    trip: Trip
) {
    val fromUser = trip.participants.find { it.id == transfer.fromUserId }
    val toUser = trip.participants.find { it.id == transfer.toUserId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "От",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = fromUser?.name ?: "Неизвестный",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333333)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Кому",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = toUser?.name ?: "Неизвестный",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333333)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatCurrency(transfer.amount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "должен перевести",
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

fun calculateParticipantBalances(trip: Trip): Map<String, Double> {
    val totalPaid = mutableMapOf<String, Double>()
    val totalDebt = mutableMapOf<String, Double>()

    trip.participants.forEach { participant ->
        totalPaid[participant.id] = 0.0
        totalDebt[participant.id] = 0.0
    }

    trip.expenses.forEach { expense ->
        if (expense.paidFor == "Поровну между всеми") {
            val payerId = expense.payerId
            val sharePerPerson = expense.amount / trip.participants.size

            totalPaid[payerId] = totalPaid[payerId]!! + expense.amount

            trip.participants.forEach { participant ->
                if (participant.id != payerId) {
                    totalDebt[participant.id] = totalDebt[participant.id]!! + sharePerPerson
                }
            }
        }
    }

    val balances = mutableMapOf<String, Double>()
    trip.participants.forEach { participant ->
        val paid = totalPaid[participant.id] ?: 0.0
        val debt = totalDebt[participant.id] ?: 0.0
        balances[participant.id] = paid - debt
    }

    return balances
}

fun calculateParticipantBalance(userId: String, trip: Trip): Double {
    val balances = calculateParticipantBalances(trip)
    return balances[userId] ?: 0.0
}

fun calculateTransfers(trip: Trip): List<Transfer> {
    val allTransfers = mutableListOf<Transfer>()

    trip.expenses.forEach { expense ->
        if (expense.paidFor == "Поровну между всеми") {
            val payerId = expense.payerId
            val sharePerPerson = expense.amount / trip.participants.size

            trip.participants.forEach { participant ->
                if (participant.id != payerId) {
                    allTransfers.add(Transfer(
                        fromUserId = participant.id,
                        toUserId = payerId,
                        amount = sharePerPerson
                    ))
                }
            }
        }
    }

    return allTransfers
}