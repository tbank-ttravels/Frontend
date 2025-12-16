package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ParticipantsTab(
    trip: Trip,
    tripViewModel: TripViewModel,
    navController: NavController,
    canEdit: Boolean = true
) {
    val userViewModel: UserViewModel = viewModel()
    val userData by userViewModel.userData.collectAsState()
    val currentUserPhone = userData.phone
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    val scope = rememberCoroutineScope()

    var showLeaveDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(trip.id) {
        isLoading = true
        errorMessage = null
        when (val res = backend.getTravelMembers(trip.id.toLong())) {
            is NetworkResult.Success -> {
                val members = res.data.members.map {
                    User(
                        id = it.id.toString(),
                        name = it.name.orEmpty(),
                        phone = it.phone.orEmpty(),
                        status = it.status,
                        role = it.role
                    )
                }
                tripViewModel.setParticipants(trip.id, members)
            }
            is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
            is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
            else -> errorMessage = "Не удалось загрузить участников"
        }
        isLoading = false
    }

    val isCurrentUserInTrip = currentUserPhone.isNotEmpty() && trip.participants.any { it.phone == currentUserPhone }
    val participantStatuses by remember(trip.participants) {
        derivedStateOf {
            trip.participants.associate { participant ->
                participant.id to participant.status.toConfirmationStatus()
            }
        }
    }
    val isOwner = remember(trip.participants, currentUserPhone) {
        trip.participants.any { it.phone == currentUserPhone && it.role.equals("OWNER", ignoreCase = true) }
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

            if (canEdit) {
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
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (!errorMessage.isNullOrBlank()) {
            Text(text = errorMessage ?: "", color = Color.Red, fontSize = 13.sp)
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
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                trip.participants.forEach { user ->
                    val status = participantStatuses[user.id] ?: ConfirmationStatus.ACCEPTED
                    val isCurrentUser = user.phone == currentUserPhone

                    ParticipantCardWithConfirmation(
                        user = user,
                        confirmationStatus = status,
                        onDeleteClick = {
                            if (!canEdit) return@ParticipantCardWithConfirmation
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val res = backend.kickMember(trip.id.toLong(), user.id.toLong())
                                if (res is NetworkResult.Success) {
                                    when (val refresh = backend.getTravelMembers(trip.id.toLong())) {
                                        is NetworkResult.Success -> {
                                            val members = refresh.data.members.map {
                                                User(
                                                    id = it.id.toString(),
                                                    name = it.name.orEmpty(),
                                                    phone = it.phone.orEmpty(),
                                                    status = it.status
                                                )
                                            }
                                            tripViewModel.setParticipants(trip.id, members)
                                        }
                                        else -> Unit
                                    }
                                } else {
                                    errorMessage = when (res) {
                                        is NetworkResult.HttpError -> res.error?.message ?: "Ошибка ${res.code}"
                                        is NetworkResult.NetworkError -> "Проблемы с сетью"
                                        is NetworkResult.SerializationError -> "Ошибка обработки ответа"
                                        else -> "Не удалось удалить участника"
                                    }
                                }
                                isLoading = false
                            }
                        },
                        isCurrentUser = isCurrentUser,
                        isTripCreator = isOwner && canEdit
                    )
                }
            }
        }

        if (trip.participants.isNotEmpty() && isCurrentUserInTrip && !isOwner && canEdit) {
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
                border = ButtonDefaults.outlinedButtonBorder(
                    enabled = true
                ).copy(
                    width = 1.dp
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
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
                            isLoading = true
                            scope.launch {
                                val res = backend.leaveTravel(trip.id.toLong())
                                if (res is NetworkResult.Success) {
                                    when (val refresh = backend.getTravelMembers(trip.id.toLong())) {
                                        is NetworkResult.Success -> {
                                            val members = refresh.data.members.map {
                                                User(
                                                    id = it.id.toString(),
                                                    name = it.name.orEmpty(),
                                                    phone = it.phone.orEmpty(),
                                                    status = it.status
                                                )
                                            }
                                            tripViewModel.setParticipants(trip.id, members)
                                        }
                                        else -> Unit
                                    }
                                    navController.navigate("main") {
                                        popUpTo("main") {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                } else {
                                    errorMessage = "Не удалось выйти из поездки"
                                }
                                isLoading = false
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
                    border = ButtonDefaults.outlinedButtonBorder(
                        enabled = true
                    ).copy(
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
private fun String?.toConfirmationStatus(): ConfirmationStatus =
    when (this?.uppercase(Locale.getDefault())) {
        "INVITED" -> ConfirmationStatus.PENDING
        "ACCEPTED" -> ConfirmationStatus.ACCEPTED
        "REJECTED" -> ConfirmationStatus.REJECTED
        "LEAVE" -> ConfirmationStatus.LEFT
        else -> ConfirmationStatus.PENDING
    }
