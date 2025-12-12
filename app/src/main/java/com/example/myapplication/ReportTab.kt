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

@Composable
fun ReportTab(trip: Trip) {
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
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF3F3F3)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoRow(
                    icon = Icons.Filled.Place,
                    title = "Маршрут",
                    value = "${trip.startTown} - ${trip.endTown}"
                )

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

                InfoRow(
                    icon = Icons.Filled.AttachMoney,
                    title = "Бюджет",
                    value = trip.budget
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
                        contentDescription = "Статус",
                        tint = Color(0xFFFFDD2D),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Статус поездки",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tripStatus,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = statusColor,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(statusColor, CircleShape)
                            )
                        }
                    }

                    Button(
                        onClick = { showStatusDialog = true },
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF2FFB5),
                            contentColor = Color(0xFF000000)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Изменить",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showStatusDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFEB3B),
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Изменить статус",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Изменить статус поездки",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = {
                Text(
                    text = "Статус поездки",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    statusOptions.forEach { status ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (tripStatus == status) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                            ),
                            onClick = {
                                tripStatus = status
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            when (status) {
                                                "Активна" -> Color(0xFF4CAF50)
                                                "Остановлена" -> Color(0xFFFF9800)
                                                else -> Color(0xFF666666)
                                            },
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = status,
                                    fontSize = 16.sp,
                                    fontWeight = if (tripStatus == status) FontWeight.Bold else FontWeight.Medium,
                                    color = Color(0xFF333333)
                                )
                                if (tripStatus == status) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Выбрано",
                                        tint = Color(0xFFFFEB3B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showStatusDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEB3B),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showStatusDialog = false },
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
}

@Composable
fun InfoRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color(0xFFFFDD2D),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
