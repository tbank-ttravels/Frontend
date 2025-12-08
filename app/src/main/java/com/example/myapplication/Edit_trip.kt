package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun EditTripScreen(
    tripId: String?,
    navController: NavController,
    tripViewModel: TripViewModel
) {
    var trip by remember { mutableStateOf<Trip?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var startTown by remember { mutableStateOf("") }
    var endTown by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }

    LaunchedEffect(tripId) {
        if (tripId != null) {
            trip = tripViewModel.getTripById(tripId)
            trip?.let {
                startTown = it.startTown
                endTown = it.endTown
                startDate = it.startDate
                endDate = it.endDate
                budget = it.budget
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFDD2D),
                        Color(0xFFFBE885),
                        Color(0xFFF5F5F5)
                    )
                )
            )
    ) {
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = Color(0xFF333333),
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "Редактирование поездки",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF333333),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 140.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp)
                .align(Alignment.BottomCenter)
                .padding(30.dp)
                .border(
                    width = 2.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(
                        topStart = 32.dp,
                        topEnd = 32.dp
                    )
                ),
            shape = RoundedCornerShape(
                topStart = 32.dp,
                topEnd = 32.dp
            ),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = startTown,
                    onValueChange = { startTown = it },
                    label = { Text("Город отправления", fontWeight = FontWeight.ExtraBold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = endTown,
                    onValueChange = { endTown = it },
                    label = { Text("Город прибытия", fontWeight = FontWeight.ExtraBold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Дата начала", fontWeight = FontWeight.ExtraBold) },
                        modifier = Modifier.width(150.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Дата конца", fontWeight = FontWeight.ExtraBold) },
                        modifier = Modifier.width(150.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Планируемый бюджет", fontWeight = FontWeight.ExtraBold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (startTown.isNotEmpty() && endTown.isNotEmpty()) {
                            trip?.let { currentTrip ->
                                val updatedTrip = currentTrip.copy(
                                    startTown = startTown,
                                    endTown = endTown,
                                    startDate = startDate,
                                    endDate = endDate,
                                    budget = budget
                                )
                                tripViewModel.updateTrip(updatedTrip)
                                navController.navigateUp()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD2D),
                        contentColor = Color(0xFF333333)
                    ),
                    enabled = startTown.isNotEmpty() && endTown.isNotEmpty()
                ) {
                    Text("Сохранить изменения", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Button(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 4.dp,
                            color = Color(0xFFFFDD2D),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFFFFF),
                        contentColor = Color(0xFF333333)
                    )
                ) {
                    Text(
                        "Отмена",
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                    )
                }
                if (trip != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFF0F0),
                            contentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Text(
                            "Удалить поездку",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Удалить поездку?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFFD32F2F)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Вы уверены, что хотите удалить поездку?",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "После удаления:",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "• Все данные о поездке будут удалены",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "• Участники потеряют доступ к поездке",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "• Все расходы будут удалены",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "• Это действие нельзя отменить",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        trip?.let {
                            tripViewModel.deleteTrip(it.id)
                            navController.navigate("main") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                        showDeleteDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Удалить",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp
                    )
                ) {
                    Text(
                        text = "Отмена",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}