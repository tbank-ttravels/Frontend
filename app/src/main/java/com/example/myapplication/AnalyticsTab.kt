package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core_data.model.CategoryAnalyticsResponseDTO
import com.example.core_data.model.TravelExpenseAnalyticsDTO
import com.example.core_data.network.NetworkResult

@Composable
fun AnalyticsTab(trip: Trip) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }

    var analytics by remember { mutableStateOf<TravelExpenseAnalyticsDTO?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(trip.id) {
        isLoading = true
        errorMessage = null
        when (val res = backend.getExpenseReport(trip.id.toLong())) {
            is NetworkResult.Success -> analytics = res.data
            is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
            is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
            else -> errorMessage = "Не удалось загрузить аналитику"
        }
        isLoading = false
    }

    val totalExpenses = analytics?.totalAmount ?: 0.0
    val categories = analytics?.categories.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Аналитика расходов",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF3F4F6)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Общие расходы",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = "${totalExpenses.toInt()} ₽",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Количество категорий",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = categories.size.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when {
            isLoading -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Загружаем данные...")
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage.orEmpty(),
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
            categories.isEmpty() -> {
                Text(
                    text = "Нет данных по расходам",
                    color = Color(0xFF666666),
                    fontSize = 14.sp
                )
            }
            else -> {
                Text(
                    text = "Анализ по категориям",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categories.forEach { category ->
                        CategoryAnalyticsCard(category, totalExpenses)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryAnalyticsCard(
    category: CategoryAnalyticsResponseDTO,
    totalExpenses: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = category.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "${category.expenseCount ?: 0} трат",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Text(
                    text = "${(category.totalAmount ?: 0.0).toInt()} ₽",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val percent = category.percentageOfTotal
                ?: if (totalExpenses > 0) ((category.totalAmount ?: 0.0) / totalExpenses * 100) else 0.0
            LinearProgressIndicator(
                progress = (percent / 100).toFloat().coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFFFDD2D),
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${percent.toInt()}% от общих расходов",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            if (category.participants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Расходы участников:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                category.participants.forEach { p ->
                    val fullName = listOfNotNull(p.name, p.surname).joinToString(" ").ifBlank { "Участник" }
                    ParticipantSpendingRow(
                        participantName = fullName,
                        amount = p.expenseAmount ?: 0.0,
                        totalInCategory = category.totalAmount ?: 0.0
                    )
                }
            }
        }
    }
}

@Composable
fun ParticipantSpendingRow(
    participantName: String,
    amount: Double,
    totalInCategory: Double
) {
    val percentage = if (totalInCategory > 0) (amount / totalInCategory * 100).toInt() else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(0xFFFFDD2D), RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = participantName,
                fontSize = 14.sp,
                color = Color(0xFF333333)
            )
            Text(
                text = "$percentage% в категории",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }

        Text(
            text = "${amount.toInt()} ₽",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )
    }
}
