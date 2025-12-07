package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.core_data.model.InvitesItem
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.myapplication.formatDateForUi


@Composable
fun InvitationsScreen(
    navController: NavHostController
) {
    var invitations by remember { mutableStateOf<List<TripInvitation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var respondingId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        loading = true
        errorMessage = null
        when (val res = backend.getInvites()) {
            is NetworkResult.Success -> {
                invitations = res.data.invites.map { it.toTripInvitation() }
                errorMessage = null
            }
            is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
            is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
            else -> errorMessage = "Не удалось загрузить приглашения"
        }
        loading = false
    }

    Column(
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color(0xFF333333)
                )
            }
            Text(
                text = "Приглашения в поездки",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.weight(1f)
            )
        }

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFDD2D)
                    )
                }
            }
            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = errorMessage ?: "Ошибка",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            loading = true
                            errorMessage = null
                            scope.launch {
                                when (val res = backend.getInvites()) {
                                    is NetworkResult.Success -> {
                                        invitations = res.data.invites.map { it.toTripInvitation() }
                                        errorMessage = null
                                    }
                                    is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
                                    is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
                                    is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
                                    else -> errorMessage = "Не удалось загрузить приглашения"
                                }
                                loading = false
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFDD2D),
                            contentColor = Color(0xFF333333)
                        )
                    ) {
                        Text("Повторить")
                    }
                }
            }
            invitations.isEmpty() -> {
                EmptyInvitationsState()
            }
            else -> {
                InvitationsList(
                    invitations = invitations,
                    respondingId = respondingId,
                    onInvitationAction = { invitation, action ->
                        when (action) {
                            InvitationAction.ACCEPT -> {
                                if (respondingId != null) return@InvitationsList
                                respondingId = invitation.id
                                scope.launch {
                                    val inviteId = invitation.id.toLongOrNull()
                                    val res = inviteId?.let { backend.respondToInvite(it, true) }
                                    if (res is NetworkResult.HttpError || res is NetworkResult.NetworkError || res is NetworkResult.SerializationError) {
                                        errorMessage = "Не удалось принять приглашение"
                                    } else {
                                        invitations = invitations.map {
                                            if (it.id == invitation.id) it.copy(status = InvitationStatus.ACCEPTED)
                                            else it
                                        }
                                    }
                                    respondingId = null
                                }
                            }
                            InvitationAction.REJECT -> {
                                if (respondingId != null) return@InvitationsList
                                respondingId = invitation.id
                                scope.launch {
                                    val inviteId = invitation.id.toLongOrNull()
                                    val res = inviteId?.let { backend.respondToInvite(it, false) }
                                    if (res is NetworkResult.HttpError || res is NetworkResult.NetworkError || res is NetworkResult.SerializationError) {
                                        errorMessage = "Не удалось отклонить приглашение"
                                    } else {
                                        invitations = invitations.map {
                                            if (it.id == invitation.id) it.copy(status = InvitationStatus.REJECTED)
                                            else it
                                        }
                                    }
                                    respondingId = null
                                }
                            }
                            InvitationAction.VIEW_TRIP -> {
                                navController.navigate("trip_detail/${invitation.tripName}")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyInvitationsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MailOutline,
            contentDescription = "Нет приглашений",
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Приглашений пока нет",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666)
        )
        Text(
            text = "Вас пока не приглашали в поездки",
            fontSize = 14.sp,
            color = Color(0xFF999999),
            modifier = Modifier.padding(top = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun InvitationsList(
    invitations: List<TripInvitation>,
    respondingId: String?,
    onInvitationAction: (TripInvitation, InvitationAction) -> Unit
) {
    val pendingInvitations = invitations.filter { it.status == InvitationStatus.PENDING }
    val acceptedInvitations = invitations.filter { it.status == InvitationStatus.ACCEPTED }
    val rejectedInvitations = invitations.filter { it.status == InvitationStatus.REJECTED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (pendingInvitations.isNotEmpty()) {
            item {
                Text(
                    text = "Ожидают ответа (${pendingInvitations.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(pendingInvitations) { invitation ->
                PendingInvitationCard(
                    invitation = invitation,
                    isProcessing = respondingId == invitation.id,
                    onAccept = { onInvitationAction(invitation, InvitationAction.ACCEPT) },
                    onReject = { onInvitationAction(invitation, InvitationAction.REJECT) }
                )
            }
        }
        if (acceptedInvitations.isNotEmpty()) {
            item {
                Text(
                    text = "Принятые (${acceptedInvitations.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(acceptedInvitations) { invitation ->
                InvitationHistoryCard(
                    invitation = invitation,
                    status = "Принято",
                    statusColor = Color(0xFF4CAF50),
                    onViewTrip = { onInvitationAction(invitation, InvitationAction.VIEW_TRIP) }
                )
            }
        }
        if (rejectedInvitations.isNotEmpty()) {
            item {
                Text(
                    text = "Отклоненные (${rejectedInvitations.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(rejectedInvitations) { invitation ->
                InvitationHistoryCard(
                    invitation = invitation,
                    status = "Отклонено",
                    statusColor = Color(0xFFF44336),
                    onViewTrip = { onInvitationAction(invitation, InvitationAction.VIEW_TRIP) }
                )
            }
        }
    }
}

@Composable
fun PendingInvitationCard(
    invitation: TripInvitation,
    isProcessing: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFF8E1), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flight,
                        contentDescription = "Поездка",
                        tint = Color(0xFFF57C00),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = invitation.tripName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = invitation.date,
                        fontSize = 12.sp,
                        color = Color(0xFF888888),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Новое",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF57C00)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Принять",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Принять")
                    }
                }

                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color(0xFFF44336)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp
                    ),
                    enabled = !isProcessing
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Отклонить",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Отклонить")
                }
            }
        }
    }
}

@Composable
fun InvitationHistoryCard(
    invitation: TripInvitation,
    status: String,
    statusColor: Color,
    onViewTrip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = invitation.tripName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "От",
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = invitation.date,
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))


                }
        }
    }
}

private fun InvitesItem.toTripInvitation(): TripInvitation =
    TripInvitation(
        id = inviteId.toString(),
        tripName = travelName ?: "Поездка",
        date = listOfNotNull(startDate, endDate).firstOrNull()?.let { formatDateForUi(it) } ?: "",
        status = InvitationStatus.PENDING
    )
