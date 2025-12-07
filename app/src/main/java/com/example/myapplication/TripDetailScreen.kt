package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.core_data.network.NetworkResult

@Composable
fun CreateTab(
    navController: NavController,
    tripViewModel: TripViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val trips by tripViewModel.trips.collectAsState()
    val userData by userViewModel.userData.collectAsState()
    var pendingInvitationsCount by remember { mutableStateOf(0) }
    var hasNewNotifications by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }

    LaunchedEffect(Unit) {
        isLoading = true
        loadError = null
        val invitesRes = backend.getInvites()
        pendingInvitationsCount = if (invitesRes is NetworkResult.Success) {
            invitesRes.data.invites.size
        } else 0

        when (val travelsRes = backend.getMyTravels()) {
            is NetworkResult.Success -> {
                val mapped = travelsRes.data.travels.map { travel ->
                    Trip(
                        id = travel.id.toString(),
                        name = travel.name,
                        description = travel.description,
                        startDate = formatDateForUi(travel.startDate),
                        endDate = formatDateForUi(travel.endDate),
                        status = travel.travelStatus,
                        participants = emptyList(),
                        expenses = emptyList()
                    )
                }
                tripViewModel.replaceTrips(mapped)
                loadError = null
            }
            is NetworkResult.HttpError -> loadError = travelsRes.error?.message ?: "Ошибка ${travelsRes.code}"
            is NetworkResult.NetworkError -> loadError = "Проблемы с сетью"
            is NetworkResult.SerializationError -> loadError = "Ошибка обработки ответа"
            else -> loadError = "Не удалось загрузить поездки"
        }
        isLoading = false
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
                .padding(horizontal = 25.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigate("invitations") },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MailOutline,
                            contentDescription = "Приглашения",
                            tint = Color(0xFF333333),
                            modifier = Modifier.size(28.dp)
                        )
                        if (pendingInvitationsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color(0xFFFF5252), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pendingInvitationsCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { navController.navigate("notifications") },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Уведомления",
                            tint = Color(0xFF333333),
                            modifier = Modifier.size(28.dp)
                        )
                        if (hasNewNotifications) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(12.dp)
                                    .background(Color(0xFFFF5252), CircleShape)
                            )
                        }
                    }
                }
            }
        }
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp),
                color = Color(0xFFFFDD2D),
                trackColor = Color(0x33FFFFDD2D)
            )
        }
        loadError?.let { err ->
            Text(
                text = err,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 4.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Куда отправимся?",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flight,
                            contentDescription = "Создать",
                            modifier = Modifier.size(60.dp),
                            tint = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { navController.navigate("create_trip") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFDD2D),
                                contentColor = Color(0xFF333333)
                            )
                        ) {
                            Text(
                                "Создать поездку",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Card(
                    onClick = { navController.navigate("invitations") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MailOutline,
                                contentDescription = "Приглашения",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "У вас есть приглашения",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = "$pendingInvitationsCount ожидают подтверждения",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }

                        if (pendingInvitationsCount > 0) {
                            Badge(
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = pendingInvitationsCount.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Перейти",
                            tint = Color(0xFF999999),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
                .border(
                    width = 2.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(16.dp)
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
                    .padding(32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Мои поездки",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color(0xFF333333)
                    )

                    Text(
                        text = "${trips.size} шт.",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (trips.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FlightTakeoff,
                            contentDescription = "Нет поездок",
                            tint = Color(0xFFBDBDBD),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "У вас пока нет поездок",
                            color = Color(0xFF666666),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Создайте первую поездку или примите приглашение!",
                            color = Color(0xFF999999),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(trips) { trip ->
                            TripCard(
                                trip = trip,
                                onClick = {
                                    navController.navigate("trip_detail/${trip.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}