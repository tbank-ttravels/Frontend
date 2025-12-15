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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.core_data.network.NetworkResult
import java.util.UUID
import kotlin.math.absoluteValue

@Composable
fun TransfersScreen(
    trip: Trip,
    tripViewModel: TripViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    var transfers by remember { mutableStateOf<List<Transfer>>(emptyList()) }
    var participantBalances by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(trip.id) {
        isLoading = true
        errorMessage = null

        when (val res = backend.getTransfers(trip.id.toLong())) {
            is NetworkResult.Success -> {
                val mapped = res.data.transfers.map {
                    Transfer(
                        id = (it.id ?: UUID.randomUUID().mostSignificantBits.absoluteValue).toString(),
                        fromUserId = it.senderId.toString(),
                        toUserId = it.recipientId.toString(),
                        amount = it.sum
                    )
                }
                transfers = mapped
                tripViewModel.setTransfers(trip.id, mapped)
            }

            is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
            is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
            else -> errorMessage = "Не удалось загрузить переводы"
        }

        when (val debtsRes = backend.getTravelDebts(trip.id.toLong())) {
            is NetworkResult.Success -> {
                val map = mutableMapOf<String, Double>()
                // Положительное значение — этому участнику должны, отрицательное — участник должен
                debtsRes.data.creditors.forEach { cred ->
                    map[cred.user.id.toString()] = cred.totalAmount ?: 0.0
                }
                debtsRes.data.debts.forEach { debt ->
                    map[debt.user.id.toString()] = -(debt.totalAmount ?: 0.0)
                }
                participantBalances = map
            }

            is NetworkResult.HttpError -> errorMessage = errorMessage ?: debtsRes.error?.message
                ?: "Ошибка ${debtsRes.code}"

            is NetworkResult.NetworkError -> errorMessage = errorMessage ?: "Проблемы с сетью"
            is NetworkResult.SerializationError -> errorMessage = errorMessage ?: "Ошибка обработки ответа"
            else -> if (participantBalances.isEmpty()) errorMessage = errorMessage ?: "Не удалось загрузить балансы"
        }
        isLoading = false
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
        } else if (transfers.isEmpty() && !isLoading) {
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
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Переводов пока нет",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            if (errorMessage != null) {
                Text(text = errorMessage ?: "", color = Color.Red)
            }
            BalanceCard(participantBalances, trip)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(transfers) { transfer ->
                    TransferItem(transfer, trip)
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Долги участников",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val sortedEntries = participantBalances.entries.sortedByDescending { it.value }

            sortedEntries.forEach { entry ->
                val participantId = entry.key
                val balance = entry.value
                val isPositive = balance >= 0
                val amountText = (if (isPositive) "+" else "-") + balance.absoluteValue.toInt() + " ₽"
                val directionText = if (!isPositive) "Ему/ей должны" else "Он/она должен(а)"
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
                                tint = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336),
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
                                    text = directionText,
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = amountText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
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
    trip: Trip
) {
    val fromUser = trip.participants.find { it.id == transfer.fromUserId }
    val toUser = trip.participants.find { it.id == transfer.toUserId }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${fromUser?.name ?: "Неизвестный"} → ${toUser?.name ?: "Неизвестный"}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Перевод",
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93)
                )
            }
            Text(
                text = "${transfer.amount.toInt()} ₽",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        }
    }
}
