package com.example.myapplication

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.core_data.model.ExpenseResponseDTO
import com.example.core_data.model.TravelResponse
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.launch

@Composable
fun TripDetailScreen(
    tripId: String?,
    navController: NavHostController,
    tripViewModel: TripViewModel,
    userViewModel: UserViewModel
) {
    var trip by remember { mutableStateOf<Trip?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val userData by userViewModel.userData.collectAsState()
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(tripId) {
        if (tripId == null) return@LaunchedEffect
        isLoading = true
        errorMessage = null
        when (val res = backend.getTravel(tripId.toLong())) {
            is NetworkResult.Success -> {
                val mapped = res.data.toTripUi()
                tripViewModel.upsertTrip(mapped)
                trip = mapped
            }
            is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
            is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
            else -> errorMessage = "Не удалось загрузить поездку"
        }
        if (errorMessage == null) {
            when (val membersRes = backend.getTravelMembers(tripId.toLong())) {
                is NetworkResult.Success -> {
                    val members = membersRes.data.members.map {
                        User(
                            id = it.id.toString(),
                            name = it.name.orEmpty(),
                            surname = it.surname.orEmpty(),
                            phone = it.phone.orEmpty(),
                            status = it.status,
                            role = it.role
                        )
                    }
                    tripViewModel.setParticipants(tripId, members)
                    trip = trip?.copy(participants = members) ?: tripViewModel.getTripById(tripId)
                }
                is NetworkResult.HttpError -> errorMessage = membersRes.error?.message ?: "Ошибка ${membersRes.code}"
                is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
                is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
                else -> errorMessage = "Не удалось загрузить участников"
            }
        }

        if (errorMessage == null) {
            when (val expensesRes = backend.getTravelExpenses(tripId.toLong())) {
                is NetworkResult.Success -> {
                    val mappedExpenses = expensesRes.data.expenses.map { it.toExpenseUi() }
                    tripViewModel.setExpenses(tripId, mappedExpenses)
                    // Триггерим обновление trip в StateFlow, чтобы все табы увидели новые расходы
                    tripViewModel.getTripById(tripId)?.let { current ->
                        tripViewModel.updateTrip(current.copy(expenses = mappedExpenses))
                    }
                }
                is NetworkResult.HttpError -> {
                    // Не блокируем экран поездки, просто покажем ошибку в шапке
                    errorMessage = expensesRes.error?.message ?: "Ошибка ${expensesRes.code}"
                }
                is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
                is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
                else -> {}
            }
        }
        isLoading = false
    }

    val tripsState by tripViewModel.trips.collectAsState()
    LaunchedEffect(tripsState) {
        if (tripId != null) {
            trip = tripViewModel.getTripById(tripId)
        }
    }
    val isOwner = remember(trip, userData) {
        trip?.participants?.any { it.phone == userData.phone && it.role.equals("OWNER", ignoreCase = true) } == true
    }
    val isClosed = trip?.status.equals("CLOSED", ignoreCase = true)
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (trip == null) {
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
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFFFFDD2D)
            )
        }
        return
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

        var selectedTab by remember { mutableIntStateOf(0) }
        val tabs = listOf("Участники", "Финансы", "Отчет", "Аналитика")

        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF333333),
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = Color(0xFFFFDD2D),
                trackColor = Color(0xFF70631C)
            )
        }
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = trip!!.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = "Даты",
                                tint = Color(0xFFFFDD2D),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val dateText = buildString {
                                append(trip!!.startDate)
                                if (!trip!!.endDate.isNullOrBlank()) {
                                    append(" - ")
                                    append(trip!!.endDate)
                                }
                            }
                            Text(
                                text = dateText,
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        trip!!.description?.takeIf { it.isNotBlank() }?.let { desc ->
                            Text(
                                text = desc,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF666666)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${trip!!.participants.size} участников",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            when (selectedTab) {
                0 -> ParticipantsTab(
                    trip = trip!!,
                    tripViewModel = tripViewModel,
                    navController = navController,
                    canEdit = !isClosed
                )
                1 -> FinanceTab(trip = trip!!, tripViewModel = tripViewModel, navController = navController)
                2 -> ReportTab(trip = trip!!, isOwner = isOwner, tripViewModel = tripViewModel)
                3 -> AnalyticsTab(trip = trip!!)

            }
        }

        if (isOwner) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate("edit_trip/${trip!!.id}") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isClosed) Color(0xFFF5F5F5) else Color(0xFFFFDD2D),
                        contentColor = Color(0xFF333333)
                    ),
                    enabled = !isClosed,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Редактировать",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isClosed) "Недоступно (закрыта)" else "Редактировать",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9A9A),
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Удалить",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Удалить")
                }
            }
        }

        Button(
            onClick = { navController.navigate("main") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color(0xFF333333)
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "Список",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Все поездки",
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (showDeleteDialog && trip != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить поездку?") },
            text = { Text("Это действие удалит поездку без возможности восстановления.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            when (backend.deleteTravel(trip!!.id.toLong())) {
                                is NetworkResult.Success -> {
                                    tripViewModel.deleteTrip(trip!!.id)
                                    navController.navigate("main")
                                }
                                is NetworkResult.HttpError -> errorMessage = "Не удалось удалить поездку"
                                is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
                                is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
                                else -> errorMessage = "Не удалось удалить поездку"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

private fun TravelResponse.toTripUi(): Trip =
    Trip(
        id = id.toString(),
        name = name,
        description = description,
        startDate = formatDateForUi(startDate),
        endDate = formatDateForUi(endDate),
        status = status,
        participants = emptyList(),
        expenses = emptyList()
    )

private fun ExpenseResponseDTO.toExpenseUi(): Expense {
    val paidForText = when {
        participants.isEmpty() -> "Только себя"
        participants.size == 1 -> {
            val target = participants.first()
            val fullName = listOfNotNull(target.name, target.surname).joinToString(" ").ifBlank { null }
            fullName?.let { "За: $it" } ?: "Только себя"
        }
        else -> {
            val names = participants.mapNotNull { p ->
                listOfNotNull(p.name, p.surname).joinToString(" ").takeIf { it.isNotBlank() }
            }
            if (names.isNotEmpty()) "За: ${names.joinToString(", ")}" else "За участников"
        }
    }

    val shares = participants
        .mapNotNull { p -> p.userId.toString().takeIf { p.share != null }?.let { it to (p.share ?: 0.0) } }
        .toMap()

    return Expense(
        id = id.toString(),
        title = name,
        amount = sum ?: 0.0,
        category = categoryName ?: "",
        payerId = payerId.toString(),
        paidFor = paidForText,
        date = date,
        participantShares = shares
    )
}
