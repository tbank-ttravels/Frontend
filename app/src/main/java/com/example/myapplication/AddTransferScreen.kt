package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import com.example.core_data.model.CreateTransferRequest
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.launch

@Composable
fun AddTransferScreen(
    tripId: String?,
    navController: NavController,
    tripViewModel: TripViewModel
) {
    var trip by remember { mutableStateOf<Trip?>(null) }
    var fromUserId by remember { mutableStateOf("") }
    var toUserId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    val scope = rememberCoroutineScope()
    
    val scrollState = rememberScrollState()

    fun mapError(res: NetworkResult<*>, defaultMessage: String): String =
        when (res) {
            is NetworkResult.HttpError -> res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> "Проблемы с сетью"
            is NetworkResult.SerializationError -> "Ошибка обработки ответа"
            else -> defaultMessage
        }

    LaunchedEffect(tripId) {
        if (tripId != null) {
            trip = tripViewModel.getTripById(tripId)
            // Фильтруем только участников со статусом ACCEPTED
            val acceptedParticipants = trip?.participants?.filter { 
                it.status?.equals("ACCEPTED", ignoreCase = true) == true 
            } ?: emptyList()
            
            acceptedParticipants.firstOrNull()?.let {
                fromUserId = it.id
            }
            acceptedParticipants.getOrNull(1)?.let {
                toUserId = it.id
            }
        }
    }
    
    // Фильтруем только ACCEPTED участников
    val acceptedParticipants = remember(trip) {
        trip?.participants?.filter { 
            it.status?.equals("ACCEPTED", ignoreCase = true) == true 
        } ?: emptyList()
    }

    if (trip == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Добавить перевод",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "От кого:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                acceptedParticipants.forEach { participant ->
                    val participantName = displayName(participant)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (fromUserId == participant.id) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                        ),
                        onClick = { fromUserId = participant.id }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Участник",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = participantName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (fromUserId == participant.id) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Выбрано",
                                    tint = Color(0xFFFFF292)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Кому:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                acceptedParticipants.forEach { participant ->
                    if (participant.id != fromUserId) {
                        val participantName = displayName(participant)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (toUserId == participant.id) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
                            ),
                            onClick = { toUserId = participant.id }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Участник",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = participantName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (toUserId == participant.id) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Выбрано",
                                        tint = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Сумма перевода") },
                    placeholder = { Text("Введите сумму") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        errorMessage?.let { message ->
            Text(
                text = message,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { navController.navigateUp() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF5F5F5),
                    contentColor = Color(0xFF333333)
                )
            ) {
                Text("Отмена")
            }

            Button(
                onClick = {
                    if (tripId == null) return@Button
                    if (fromUserId.isNotEmpty() && toUserId.isNotEmpty() && amount.isNotEmpty()) {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        scope.launch {
                            isSaving = true
                            errorMessage = null
                            val sender = fromUserId.toLongOrNull()
                            val recipient = toUserId.toLongOrNull()
                            if (sender == null || recipient == null) {
                                errorMessage = "Не удалось определить участников перевода"
                                isSaving = false
                                return@launch
                            }
                            when (val res = backend.createTransfer(
                                tripId.toLong(),
                                CreateTransferRequest(
                                    senderId = sender,
                                    recipientId = recipient,
                                    sum = amountValue
                                )
                            )) {
                                is NetworkResult.Success -> {
                                    val mapped = Transfer(
                                        id = res.data.id.toString(),
                                        fromUserId = res.data.senderId.toString(),
                                        toUserId = res.data.recipientId.toString(),
                                        amount = res.data.sum,
                                        date = res.data.date.orEmpty()
                                    )
                                    val updated = tripViewModel.getTransfersForTrip(tripId) + mapped
                                    tripViewModel.setTransfers(tripId, updated)
                                    navController.navigateUp()
                                }
                                else -> errorMessage = mapError(res, "Не удалось создать перевод")
                            }
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFDD2D),
                    contentColor = Color(0xFF333333)
                ),
                enabled = fromUserId.isNotEmpty() && toUserId.isNotEmpty() && amount.isNotEmpty() && !isSaving
            ) {
                Text(if (isSaving) "Сохранение..." else "Добавить")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
