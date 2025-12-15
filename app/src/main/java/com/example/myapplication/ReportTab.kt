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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.launch

@Composable
fun ReportTab(
    trip: Trip,
    isOwner: Boolean = false
) {
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    val scope = rememberCoroutineScope()
    
    var tripStatus by remember { mutableStateOf(trip.status ?: "Активна") }
    var showStatusDialog by remember { mutableStateOf(false) }
    var isLoadingStatus by remember { mutableStateOf(false) }
    var statusError by remember { mutableStateOf<String?>(null) }
    val statusOptions = listOf("Активна", "Остановлена")
    val totalExpenses = trip.expenses.sumOf { it.amount }
    
    fun mapError(res: NetworkResult<*>, defaultMessage: String): String =
        when (res) {
            is NetworkResult.HttpError -> res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> "Проблемы с сетью"
            is NetworkResult.SerializationError -> "Ошибка обработки ответа"
            else -> defaultMessage
        }

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
                    icon = Icons.Filled.Group,
                    title = "Количество участников",
                    value = "${trip.participants.size} чел."
                )

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

                    if (isOwner) {
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
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isOwner) {
            Button(
                onClick = { showStatusDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEB3B),
                    contentColor = Color.Black
                ),
                enabled = !isLoadingStatus
            ) {
                if (isLoadingStatus) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.Black
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Изменить статус",
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Изменить статус поездки",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            statusError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
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
                    onClick = {
                        val newStatus = tripStatus
                        val currentStatus = trip.status ?: "Активна"
                        
                        if (newStatus != currentStatus) {
                            isLoadingStatus = true
                            statusError = null
                            scope.launch {
                                val result = when {
                                    currentStatus == "Активна" && newStatus == "Остановлена" -> {
                                        backend.closeTravel(trip.id.toLong())
                                    }
                                    currentStatus == "Остановлена" && newStatus == "Активна" -> {
                                        backend.reopenTravel(trip.id.toLong())
                                    }
                                    else -> {
                                        NetworkResult.Success(Unit)
                                    }
                                }
                                
                                when (result) {
                                    is NetworkResult.Success -> {
                                        showStatusDialog = false
                                    }
                                    else -> {
                                        statusError = mapError(result, "Не удалось изменить статус поездки")
                                    }
                                }
                                isLoadingStatus = false
                            }
                        } else {
                            showStatusDialog = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEB3B),
                        contentColor = Color.Black
                    ),
                    enabled = !isLoadingStatus
                ) {
                    Text(if (isLoadingStatus) "Сохранение..." else "Сохранить")
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
