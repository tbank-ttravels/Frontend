package com.example.myapplication

import androidx.compose.foundation.layout.*
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
fun AddTransferScreen(
    tripId: String?,
    navController: NavController,
    tripViewModel: TripViewModel
) {
    var trip by remember { mutableStateOf<Trip?>(null) }
    var fromUserId by remember { mutableStateOf("") }
    var toUserId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    LaunchedEffect(tripId) {
        if (tripId != null) {
            trip = tripViewModel.getTripById(tripId)
            trip?.participants?.firstOrNull()?.let {
                fromUserId = it.id
            }
            trip?.participants?.getOrNull(1)?.let {
                toUserId = it.id
            }
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
                                tint = Color(0xFFF44336),
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
                    onValueChange = { amount = it },
                    label = { Text("Сумма перевода") },
                    placeholder = { Text("Введите сумму") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
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
                    if (fromUserId.isNotEmpty() && toUserId.isNotEmpty() && amount.isNotEmpty()) {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        tripViewModel.addTransfer(
                            tripId = tripId ?: "",
                            fromUserId = fromUserId,
                            toUserId = toUserId,
                            amount = amountValue
                        )

                        navController.navigateUp()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFDD2D),
                    contentColor = Color(0xFF333333)
                ),
                enabled = fromUserId.isNotEmpty() && toUserId.isNotEmpty() && amount.isNotEmpty()
            ) {
                Text("Добавить")
            }
        }
    }
}