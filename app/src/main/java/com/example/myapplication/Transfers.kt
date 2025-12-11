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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun TransfersTab(
    trip: Trip,
    tripViewModel: TripViewModel,
    navController: NavController
) {
    val transfers by remember(trip.id) {
        derivedStateOf { tripViewModel.getTransfersForTrip(trip.id) }
    }

    val participantBalances by remember(trip.id) {
        derivedStateOf { tripViewModel.calculateParticipantBalances(trip.id) }
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
            Text(
                text = "Переводы",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            if (trip.participants.size >= 2) {
                Button(
                    onClick = { navController.navigate("add_transfer/${trip.id}") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD2D),
                        contentColor = Color(0xFF333333)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Добавить перевод",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Добавить")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (trip.participants.size < 2) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate("add_participant/${trip.id}") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD2D),
                        contentColor = Color(0xFF333333)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = "Добавить участника",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить участника")
                }
            }
        } else if (transfers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { navController.navigate("add_transfer/${trip.id}") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Добавить перевод",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить перевод вручную")
                }
            }
        } else {
            BalanceCard(participantBalances, trip)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Необходимые переводы",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Button(
                    onClick = { navController.navigate("add_transfer/${trip.id}") },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color(0xFF333333)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Добавить перевод",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(transfers) { transfer ->
                    ClickableTransferItem(
                        transfer = transfer,
                        trip = trip,
                        tripViewModel = tripViewModel,
                        navController = navController,
                        onTransferCompleted = { completedTransfer ->
                            tripViewModel.deleteTransfer(
                                tripId = trip.id,
                                fromUserId = completedTransfer.fromUserId,
                                toUserId = completedTransfer.toUserId,
                                amount = completedTransfer.amount
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun BalanceCard(
    participantBalances: Map<String, Double>,
    trip: Trip
) {
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
                            text = "${balance.toInt()} ₽",
                            fontSize = 16.sp,
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
fun ClickableTransferItem(
    transfer: Transfer,
    trip: Trip,
    navController: NavController,
    onTransferCompleted: (Transfer) -> Unit,
    tripViewModel: TripViewModel
) {
    var showTransferDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val fromUser = trip.participants.find { it.id == transfer.fromUserId }
    val toUser = trip.participants.find { it.id == transfer.toUserId }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { showTransferDialog = true }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
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
                        text = "${transfer.amount.toInt()} ₽",
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

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showTransferDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFDD2D),
                    contentColor = Color(0xFF333333)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.CompareArrows,
                    contentDescription = "Перевести",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выполнить перевод")
            }
        }
    }

    if (showTransferDialog) {
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = {
                Text(
                    text = "Перевод средств",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "${fromUser?.name} должен перевести",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "${transfer.amount.toInt()} ₽",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "${toUser?.name}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Комментарий (необязательно)",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTransferDialog = false
                        showSuccessDialog = true
                        onTransferCompleted(transfer)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Подтвердить",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Подтвердить перевод")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showTransferDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp
                    )
                ) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    text = "Перевод выполнен!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF4CAF50)
                )
            },
            text = {
                Column {
                    Text(
                        text = "${fromUser?.name} перевел",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "${transfer.amount.toInt()} ₽",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "${toUser?.name}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "Перевод удален из списка необходимых",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Text("ОК")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

private fun getMockParticipantBalances(trip: Trip): Map<String, Double> {
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

private fun getMockTransfers(trip: Trip): List<Transfer> {
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

fun calculateParticipantBalances(trip: Trip): Map<String, Double> {
    return getMockParticipantBalances(trip)
}

fun calculateParticipantBalance(userId: String, trip: Trip): Double {
    return getMockParticipantBalances(trip)[userId] ?: 0.0
}

fun calculateTransfers(trip: Trip): List<Transfer> {
    return getMockTransfers(trip)
}
