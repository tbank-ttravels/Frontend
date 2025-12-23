package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.core_data.network.NetworkResult
import com.example.myapplication.BackendProvider

@Composable
fun AnalyticsTab(trip: Trip) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var totalExpenses by remember { mutableStateOf(0.0) }
    var categoryAnalytics by remember { mutableStateOf<List<CategoryAnalytics>>(emptyList()) }

    fun mapError(res: NetworkResult<*>, defaultMessage: String): String =
        when (res) {
            is NetworkResult.HttpError -> res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> "Проблемы с сетью"
            is NetworkResult.SerializationError -> "Ошибка обработки ответа"
            else -> defaultMessage
        }

    LaunchedEffect(trip.id) {
        isLoading = true
        errorMessage = null
        when (val res = backend.getExpenseReport(trip.id.toLong())) {
            is NetworkResult.Success -> {
                val dto = res.data
                totalExpenses = dto.totalAmount ?: 0.0
                categoryAnalytics = dto.categories.map { cat ->
                    val total = cat.totalAmount ?: 0.0
                    val percent = cat.percentageOfTotal ?: 0.0
                    val participants = cat.participants.associate { p ->
                        val name = listOfNotNull(p.name, p.surname).joinToString(" ").ifBlank { "Участник" }
                        name to (p.expenseAmount ?: 0.0)
                    }
                    CategoryAnalytics(
                        categoryName = cat.name,
                        totalAmount = total,
                        percentage = percent,
                        expenseCount = cat.expenseCount ?: 0,
                        participantSpending = participants
                    )
                }
            }
            else -> errorMessage = mapError(res, "Не удалось загрузить аналитику")
        }
        isLoading = false
    }

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

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color(0xFFFFDD2D)
            )
        }

        errorMessage?.let { msg ->
            Text(
                text = msg,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

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
                            text = "Количество трат",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        if (categoryAnalytics.isNotEmpty()) {
                            Text(
                                text = categoryAnalytics.sumOf { it.expenseCount }.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Анализ по категориям",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (categoryAnalytics.isEmpty() && !isLoading && errorMessage == null) {
            Text(
                text = "Данных по аналитике пока нет",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categoryAnalytics.forEach { category ->
                    CategoryAnalyticsCard(category, totalExpenses)
                }
            }
        }
    }
}

@Composable
fun CategoryAnalyticsCard(
    category: CategoryAnalytics,
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
                        text = category.categoryName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "трат кол-во: ${category.expenseCount}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Text(
                    text = "${category.totalAmount.toInt()} ₽",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = (category.percentage / 100).toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFFFDD2D),
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${category.percentage.toInt()}% от общих расходов",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            if (category.participantSpending.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Расходы участников:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                category.participantSpending.forEach { (participantId, amount) ->
                    ParticipantSpendingRow(
                        participantId = participantId,
                        amount = amount,
                        totalInCategory = category.totalAmount
                    )
                }
            }
        }
    }
}

@Composable
fun ParticipantSpendingRow(
    participantId: String,
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
                text = participantId,
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
