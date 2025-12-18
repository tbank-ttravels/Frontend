package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.rememberCoroutineScope
import com.example.core_data.model.EditTransferRequest
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun TransfersTab(
    trip: Trip,
    tripViewModel: TripViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    val scope = rememberCoroutineScope()
    var transfers by remember { mutableStateOf<List<Transfer>>(emptyList()) }
    var allParticipantBalances by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var isLoadingTransfers by remember { mutableStateOf(false) }
    var isLoadingDebts by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editingTransfer by remember { mutableStateOf<Transfer?>(null) }
    var editAmount by remember { mutableStateOf("") }
    var isSavingEdit by remember { mutableStateOf(false) }
    val yellow = Color(0xFFFFDD2D)

    val currentUserPhone = remember {
        val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        prefs.getString("user_phone", "") ?: ""
    }

    val currentUserId = remember(currentUserPhone, trip.participants) {
        trip.participants.firstOrNull { it.phone == currentUserPhone }?.id ?: ""
    }

    val participantBalances = remember(allParticipantBalances, currentUserId) {
        if (currentUserId.isNotBlank()) {
            allParticipantBalances.filter { (userId, balance) -> userId != currentUserId && balance != 0.0 }
        } else {
            allParticipantBalances.filter { (_, balance) -> balance != 0.0 }
        }
    }

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
                        amount = it.sum,
                        date = it.date.orEmpty()
                    )
                }
                transfers = mappedTransfers
                tripViewModel.setTransfers(trip.id, mappedTransfers)
            }
            is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
            is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
            else -> errorMessage = "Не удалось загрузить переводы"
        }
        isLoadingTransfers = false
    }

    LaunchedEffect(trip.id) {
        isLoadingDebts = true
        when (val res = backend.getTravelDebts(trip.id.toLong())) {
            is NetworkResult.Success -> {
                val balances = mutableMapOf<String, Double>()
                val acceptedParticipants = trip.participants.filter { it.status?.equals("ACCEPTED", true) == true }
                acceptedParticipants.forEach { balances[it.id] = 0.0 }

                res.data.debts.forEach { balances[it.user.id.toString()] = (balances[it.user.id.toString()] ?: 0.0) + (it.totalAmount ?: 0.0) }
                res.data.creditors.forEach { balances[it.user.id.toString()] = (balances[it.user.id.toString()] ?: 0.0) - (it.totalAmount ?: 0.0) }

                allParticipantBalances = balances
                tripViewModel.setParticipantBalances(trip.id, balances)
            }
            else -> {}
        }
        isLoadingDebts = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Переводы",
                fontSize = 28.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val acceptedParticipants = trip.participants.filter { it.status?.equals("ACCEPTED", true) == true }
            if (acceptedParticipants.size >= 2) {
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
                Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CompareArrows, null, modifier = Modifier.size(64.dp), tint = Color(0xFFBDBDBD))
                    Spacer(Modifier.height(16.dp))
                    Text("Переводы пока не добавлены", fontSize = 18.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(transfers) {
                        TransferItem(
                            transfer = it,
                            trip = trip,
                            onEditClick = {
                                editingTransfer = it
                                editAmount = it.amount.toString()
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        errorMessage?.let { Text(it, color = Color.Red, fontSize = 12.sp) }
    }

    if (editingTransfer != null) {
        AlertDialog(
            onDismissRequest = {
                if (!isSavingEdit) editingTransfer = null
            },
            title = { Text("Редактировать перевод") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = { Text("Сумма") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = yellow,
                            focusedLabelColor = yellow,
                            cursorColor = yellow
                        )
                    )
                    editingTransfer?.date?.takeIf { it.isNotBlank() }?.let { raw ->
                        Text(
                            text = "Дата: ${formatDateForUi(raw)}",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val tr = editingTransfer ?: return@Button
                        val transferId = tr.id.toLongOrNull()
                        val newSum = editAmount.toDoubleOrNull()
                        if (transferId == null || newSum == null) {
                            errorMessage = "Некорректная сумма или идентификатор перевода"
                            return@Button
                        }
                        isSavingEdit = true
                        errorMessage = null
                        scope.launch {
                            when (val res = backend.editTransfer(
                                trip.id.toLong(),
                                transferId,
                                EditTransferRequest(sum = newSum)
                            )) {
                                is NetworkResult.Success -> {
                                    val updated = transfers.map { existing ->
                                        if (existing.id == res.data.id.toString()) {
                                            existing.copy(
                                                amount = res.data.sum,
                                                date = res.data.date.orEmpty()
                                            )
                                        } else existing
                                    }
                                    transfers = updated
                                    tripViewModel.setTransfers(trip.id, updated)
                                    editingTransfer = null
                                }
                                is NetworkResult.HttpError -> errorMessage =
                                    res.error?.message ?: "Ошибка ${res.code}"
                                is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
                                is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
                                else -> errorMessage = "Не удалось обновить перевод"
                            }
                            isSavingEdit = false
                        }
                    },
                    enabled = !isSavingEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = yellow,
                        contentColor = Color(0xFF333333)
                    )
                ) { Text(if (isSavingEdit) "Сохранение..." else "Сохранить") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { editingTransfer = null },
                    enabled = !isSavingEdit,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF333333)
                    ),
                    border = BorderStroke(1.dp, yellow)
                ) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun BalanceCard(participantBalances: Map<String, Double>, trip: Trip) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Баланс участников",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val iOweTo = participantBalances.filter { it.value > 0 }.entries.sortedByDescending { it.value }
            val oweToMe = participantBalances.filter { it.value < 0 }.entries.sortedByDescending { abs(it.value) }

            if (iOweTo.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    Icon(Icons.Filled.ArrowDownward, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Я должен/должна", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4CAF50))
                }
                iOweTo.forEach { (id, value) ->
                    val user = trip.participants.find { it.id == id } ?: return@forEach
                    BalanceRow(displayName(user), value, true)
                }
                if (oweToMe.isNotEmpty()) Spacer(Modifier.height(16.dp))
            }

            if (oweToMe.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    Icon(Icons.Filled.ArrowUpward, null, tint = Color(0xFFF44336), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Должны мне", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF44336))
                }
                oweToMe.forEach { (id, value) ->
                    val user = trip.participants.find { it.id == id } ?: return@forEach
                    BalanceRow(displayName(user), value, false)
                }
            }

            if (iOweTo.isEmpty() && oweToMe.isEmpty()) {
                Text("Нет задолженностей", fontSize = 14.sp, color = Color(0xFF999999), modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun BalanceRow(name: String, value: Double, positive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.Person, contentDescription = name, tint = if (positive) Color(0xFF4CAF50) else Color(0xFFF44336), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
        }
        Text("${abs(value).toInt()} ₽", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (positive) Color(0xFF4CAF50) else Color(0xFFF44336))
    }
}

@Composable
fun TransferItem(
    transfer: Transfer,
    trip: Trip,
    onEditClick: () -> Unit
) {
    val fromUser = trip.participants.find { it.id == transfer.fromUserId }
    val toUser = trip.participants.find { it.id == transfer.toUserId }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${displayName(fromUser)} → ${displayName(toUser)}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (transfer.date.isNotBlank()) {
                    Text(
                        text = formatDateForUi(transfer.date),
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(" ${transfer.amount.toInt()}₽", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Редактировать", tint = Color(0xFF2196F3))
                }
            }
        }
    }
}

