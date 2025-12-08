package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnalyticsTab(trip: Trip) {
    val totalExpenses = 26000.0
    val scrollState = rememberScrollState()

    val categoryAnalytics = listOf(
        CategoryAnalytics(
            categoryName = "Проживание",
            totalAmount = 150000.0,
            percentage = 45.3,
            expenseCount = 1,
            participantSpending = mapOf(
                "Игорь" to 150000.0
            )
        ),
        CategoryAnalytics(
            categoryName = "Транспорт",
            totalAmount = 6000.0,
            percentage = 32.1,
            expenseCount = 1,
            participantSpending = mapOf(
                "Татьяна" to 6000.0
            )
        ),
        CategoryAnalytics(
            categoryName = "Еда",
            totalAmount = 5000.0,
            percentage = 22.6,
            expenseCount = 1,
            participantSpending = mapOf(
                "Светлана" to 5000.0
            )
        )
    )

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
                            text = "Количество трат",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = "3",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
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
                        text = "${category.expenseCount} трат",
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