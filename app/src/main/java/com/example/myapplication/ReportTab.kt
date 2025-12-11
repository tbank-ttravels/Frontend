package com.example.myapplication
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable

@Composable
fun ReportTab(trip: Trip, isOwner: Boolean) {
    var tripStatus by remember { mutableStateOf("Активна") }
    var showStatusDialog by remember { mutableStateOf(false) }
    val statusOptions = listOf("Активна", "Остановлена")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Отчет по поездке",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3F3))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                InfoRow(
                    icon = Icons.Filled.DateRange,
                    title = "Продолжительность",
                    value = "${trip.startDate} - ${trip.endDate}"
                )

                InfoRow(
                    icon = Icons.Filled.Group,
                    title = "Количество участников",
                    value = trip.participants.size.toString()
                )

                val totalExpenses = trip.expenses.sumOf { it.amount }

                InfoRow(
                    icon = Icons.Filled.Money,
                    title = "Общие расходы",
                    value = "$totalExpenses ₽"
                )

                val statusColor = when (tripStatus) {
                    "Активна" -> Color(0xFF4CAF50)
                    "Остановлена" -> Color(0xFFFF9800)
                    else -> Color(0xFF666666)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = null,
                        tint = Color(0xFFFFDD2D),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Статус поездки", fontSize = 14.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tripStatus,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = statusColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(statusColor, CircleShape)
                            )
                        }
                    }

                    if (isOwner) {
                        Button(
                            onClick = { showStatusDialog = true },
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF2FFB5)
                            )
                        ) {
                            Text("Изменить", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isOwner) {
            Button(
                onClick = { showStatusDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B))
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Изменить статус")
            }
        }
    }

    if (showStatusDialog && isOwner) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Статус поездки") },
            text = {
                Column {
                    statusOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { tripStatus = option },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = tripStatus == option,
                                onClick = { tripStatus = option }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showStatusDialog = false }) {
                    Text("Сохранить")
                }
            }
        )
    }
}

@Composable
fun InfoRow(icon: ImageVector, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color(0xFFFFDD2D))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 14.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}
