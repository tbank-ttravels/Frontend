package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
    var fromUserId by remember { mutableStateOf<String?>(null) }
    var toUserId by remember { mutableStateOf<String?>(null) }
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(tripId) {
        if (tripId != null) {
            trip = tripViewModel.getTripById(tripId)
        }
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
                trip?.participants?.forEach { participant ->
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
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = participant.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (fromUserId == participant.id) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Выбрано",
                                    tint = Color(0xFF2196F3)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Кому:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                trip?.participants?.forEach { participant ->
                    if (participant.id != fromUserId) {
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
                                    text = participant.name,
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
                    onValueChange = { newValue ->
                        if (newValue.matches(Regex("^\\d*\\.?\\d*$")) || newValue.isEmpty()) {
                            amount = newValue
                        }
                    },
                    label = { Text("Сумма перевода") },
                    placeholder = { Text("Введите сумму") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            Text(text = errorMessage ?: "", color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                    if (trip == null || isSubmitting) return@Button
                    val fromId = fromUserId?.toLongOrNull()
                    val toId = toUserId?.toLongOrNull()
                    val amountVal = amount.toDoubleOrNull()
                    if (fromId == null || toId == null) {
                        errorMessage = "Выберите отправителя и получателя"
                        return@Button
                    }
                    if (amountVal == null || amountVal <= 0) {
                        errorMessage = "Введите корректную сумму"
                        return@Button
                    }
                    errorMessage = null
                    isSubmitting = true
                    val req = CreateTransferRequest(
                        senderId = fromId,
                        recipientId = toId,
                        sum = amountVal
                    )
                    scope.launch {
                        when (val res = backend.createTransfer(trip!!.id.toLong(), req)) {
                            is NetworkResult.Success -> {
                                tripViewModel.addTransfer(trip!!.id, fromUserId!!, toUserId!!, amountVal)
                                navController.navigateUp()
                            }
                            is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
                            is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
                            is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
                            else -> errorMessage = "Не удалось добавить перевод"
                        }
                        isSubmitting = false
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFDD2D),
                    contentColor = Color(0xFF333333)
                ),
                enabled = !isSubmitting
            ) {
                Text("Добавить")
            }
        }
    }
}
