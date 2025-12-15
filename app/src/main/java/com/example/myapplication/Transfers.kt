package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.core_data.network.NetworkResult

@Composable
fun TransfersTab(
    trip: Trip,
    tripViewModel: TripViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    var isLoadingTransfers by remember { mutableStateOf(false) }
    var isLoadingDebts by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val transfers by remember(trip.id) {
        derivedStateOf { tripViewModel.getTransfersForTrip(trip.id) }
    }

    val allParticipantBalances by remember(trip.id) {
        derivedStateOf { tripViewModel.calculateParticipantBalances(trip.id) }
    }
    
    // Получаем phone текущего пользователя из SharedPreferences
    val currentUserPhone = remember {
        val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        prefs.getString("user_phone", "") ?: ""
    }
    
    // Находим ID текущего пользователя по phone
    val currentUserId = remember(currentUserPhone, trip.participants) {
        trip.participants.firstOrNull { it.phone == currentUserPhone }?.id ?: ""
    }
    
    // Фильтруем баланс - показываем всех участников с ненулевым балансом (исключая текущего пользователя)
    val participantBalances = remember(allParticipantBalances, currentUserId) {
        if (currentUserId.isNotBlank()) {
            // Исключаем собственный баланс и показываем всех участников с ненулевым балансом
            allParticipantBalances.filter { (userId, balance) ->
                userId != currentUserId && balance != 0.0
            }
        } else {
            // Если не удалось определить текущего пользователя, показываем всех с ненулевым балансом
            allParticipantBalances.filter { (_, balance) -> balance != 0.0 }
        }
    }

    // Загрузка переводов
    LaunchedEffect(trip.id) {
        isLoadingTransfers = true
        errorMessage = null
        when (val res = backend.getTransfers(trip.id.toLong())) {
            is NetworkResult.Success -> {
                val mappedTransfers = res.data.transfers.map {
                    Transfer(
                        id = it.id.toString(),
                        fromUserId = it.senderId.toString(),
                        toUserId = it.recipientId.toString(),
                        amount = it.sum
                    )
                }
                tripViewModel.setTransfers(trip.id, mappedTransfers)
            }
            is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
            is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
            else -> errorMessage = "Не удалось загрузить переводы"
        }
        isLoadingTransfers = false
    }

    // Загрузка долгов
    LaunchedEffect(trip.id) {
        isLoadingDebts = true
        when (val res = backend.getTravelDebts(trip.id.toLong())) {
            is NetworkResult.Success -> {
                val balances = mutableMapOf<String, Double>()
                
                // Фильтруем только участников со статусом ACCEPTED
                val acceptedParticipants = trip.participants.filter { 
                    it.status?.equals("ACCEPTED", ignoreCase = true) == true 
                }

                // Инициализируем всех ACCEPTED участников с нулевым балансом
                acceptedParticipants.forEach { participant ->
                    balances[participant.id] = 0.0
                }

                // debts - кому должна я (список тех, кому я должна)
                // creditors - кто должен мне (список тех, кто должен мне)
                // В контексте баланса участников относительно текущего пользователя:
                // Если debts содержит пользователя X с суммой Y, это означает что я должна X сумму Y
                //   Значит, X имеет положительный баланс +Y (X должен получить от меня Y)
                // Если creditors содержит пользователя X с суммой Y, это означает что X должен мне сумму Y
                //   Значит, X имеет отрицательный баланс -Y (X должен мне Y)
                
                // Обрабатываем долги (debts) - те, кому должна я
                res.data.debts.forEach {
                    val userId = it.user.id.toString()
                    // Если я должна X сумму Y, то X имеет положительный баланс +Y
                    balances[userId] = (balances[userId] ?: 0.0) + (it.totalAmount ?: 0.0)
                }

                // Обрабатываем кредиторов (creditors) - кто должен мне
                res.data.creditors.forEach {
                    val userId = it.user.id.toString()
                    // Если X должен мне сумму Y, то X имеет отрицательный баланс -Y
                    balances[userId] = (balances[userId] ?: 0.0) - (it.totalAmount ?: 0.0)
                }

                tripViewModel.setParticipantBalances(trip.id, balances)
            }
            else -> {}
        }
        isLoadingDebts = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Переводы", fontSize = 28.sp)

            // Фильтруем только ACCEPTED участников для переводов
            val acceptedParticipants = trip.participants.filter { 
                it.status?.equals("ACCEPTED", ignoreCase = true) == true 
            }
            
            if (acceptedParticipants.size >= 2) {
                Button(
                    onClick = { navController.navigate("add_transfer/${trip.id}") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD2D),
                        contentColor = Color(0xFF333333)
                    )
                ) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Добавить")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isLoadingTransfers || isLoadingDebts) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFFDD2D))
            }
        } else {

            if (participantBalances.isNotEmpty()) {
                BalanceCard(participantBalances, trip)
                Spacer(Modifier.height(16.dp))
            }

            if (transfers.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.CompareArrows,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFBDBDBD)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Переводы пока не добавлены", fontSize = 18.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(transfers) {
                        TransferItem(it, trip, tripViewModel)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        errorMessage?.let {
            Text(it, color = Color.Red, fontSize = 12.sp)
        }
    }
}


@Composable
fun BalanceCard(
    participantBalances: Map<String, Double>,
    trip: Trip
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Баланс участников",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val iOweTo = participantBalances.filter { it.value > 0 }.entries.sortedByDescending { it.value }
            val oweToMe = participantBalances.filter { it.value < 0 }.entries.sortedByDescending { kotlin.math.abs(it.value) }
            

            if (iOweTo.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Я должен/должна",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                iOweTo.forEach { entry ->
                    val participantId = entry.key
                    val balance = entry.value
                    val participant = trip.participants.find { it.id == participantId }
                    participant?.let { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = user.name,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = user.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333333)
                                )
                            }
                            Text(
                                text = "${balance.toInt()} ₽",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
                
                if (oweToMe.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Секция "Кто должен мне"
            if (oweToMe.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Должны мне",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF44336)
                    )
                }
                
                oweToMe.forEach { entry ->
                    val participantId = entry.key
                    val balance = entry.value
                    val participant = trip.participants.find { it.id == participantId }
                    participant?.let { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = user.name,
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = user.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333333)
                                )
                            }
                            Text(
                                text = "${kotlin.math.abs(balance).toInt()} ₽",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF44336)
                            )
                        }
                    }
                }
            }
            
            // Если нет балансов
            if (iOweTo.isEmpty() && oweToMe.isEmpty()) {
                Text(
                    text = "Нет задолженностей",
                    fontSize = 14.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun TransferItem(
    transfer: Transfer,
    trip: Trip,
    tripViewModel: TripViewModel
) {
    val fromUser = trip.participants.find { it.id == transfer.fromUserId }
    val toUser = trip.participants.find { it.id == transfer.toUserId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${fromUser?.name ?: "Неизвестный"} перевел  ${toUser?.name ?: "Неизвестный"}")
            Text(" ${transfer.amount.toInt()}₽")
        }
    }
}
