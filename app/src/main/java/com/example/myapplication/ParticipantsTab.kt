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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.util.UUID

@Composable
fun ParticipantsTab(
    trip: Trip,
    tripViewModel: TripViewModel,
    navController: NavController
) {
    val userViewModel: UserViewModel = viewModel()
    val userData by userViewModel.userData.collectAsState()
    val currentUserPhone = userData.phone

    val isCurrentUserInTrip = currentUserPhone.isNotEmpty() && trip.participants.any { it.phone == currentUserPhone }
    var showLeaveDialog by remember { mutableStateOf(false) }

    val participantStatuses by remember(trip.participants) {
        derivedStateOf {
            getMockParticipantStatuses(trip.participants, currentUserPhone)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Участники",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Button(
                onClick = { navController.navigate("add_participant/${trip.id}") },
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFDD2D),
                    contentColor = Color(0xFF333333)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = "Добавить участника",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Добавить",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (trip.participants.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = Color(0xFFFFDD2D).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = "Добавить участника",
                        tint = Color(0xFFFFDD2D),
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Нет участников",
                    fontSize = 18.sp,
                    color = Color(0xFF333333),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Добавьте первого участника в поездку",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { navController.navigate("add_participant/${trip.id}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD2D),
                        contentColor = Color(0xFF333333)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = "Добавить",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Добавить участника",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                trip.participants.forEach { user ->
                    val status = participantStatuses[user.id] ?: ConfirmationStatus.ACCEPTED
                    val isCurrentUser = user.phone == currentUserPhone
                    val isTripCreator = false

                    ParticipantCardWithConfirmation(
                        user = user,
                        confirmationStatus = status,
                        onDeleteClick = {
                            tripViewModel.removeParticipantFromTrip(trip.id, user.id)
                        },
                        isCurrentUser = isCurrentUser,
                        isTripCreator = isTripCreator
                    )
                }
            }
        }

        if (trip.participants.isNotEmpty() && isCurrentUserInTrip) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { showLeaveDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFFF44336)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,

                )
            ) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = "Выйти из поездки",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Выйти из поездки",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = {
                Text(
                    text = "Выйти из поездки?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Вы уверены, что хотите выйти из поездки?",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "После выхода:",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "• Все ваши данные о расходах останутся в поездке",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "• Вы больше не будете получать уведомления",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "• Чтобы вернуться, нужно будет принять новое приглашение",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isCurrentUserInTrip && currentUserPhone.isNotEmpty()) {
                            val currentUserId = trip.participants.find { it.phone == currentUserPhone }?.id
                            if (currentUserId != null) {
                                tripViewModel.removeParticipantFromTrip(trip.id, currentUserId)
                                navController.navigate("main") {
                                    popUpTo("main") {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                        showLeaveDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Выйти из поездки",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLeaveDialog = false },
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

private fun getMockParticipantStatuses(participants: List<User>, currentUserPhone: String): Map<String, ConfirmationStatus> {
    return participants.associate { participant ->
        val status = when {
            participant.phone == currentUserPhone -> ConfirmationStatus.ACCEPTED
            participants.indexOf(participant) == 1 && participants.size > 1 -> ConfirmationStatus.PENDING
            participants.indexOf(participant) == 2 && participants.size > 2 -> ConfirmationStatus.REJECTED
            else -> ConfirmationStatus.PENDING
        }
        participant.id to status
    }
}