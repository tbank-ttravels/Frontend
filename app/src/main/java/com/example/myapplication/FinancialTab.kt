package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun FinanceTab(
    trip: Trip,
    tripViewModel: TripViewModel,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Финансы",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        val totalExpenses = trip.expenses.sumOf { it.amount }
        val budget = trip.budget.toDoubleOrNull() ?: 0.0

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
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
                            text = "Общие расходы",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = "$totalExpenses ₽",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Бюджет",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = "${trip.budget}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (budget > 0) {
                    val progress = (totalExpenses / budget).coerceIn(0.0, 1.0)
                    LinearProgressIndicator(
                        progress = progress.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (progress > 0.9) Color(0xFFF44336) else Color(0xFF4CAF50),
                        trackColor = Color(0xFFE0E0E0)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Использовано ${(progress * 100).toInt()}% бюджета",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (trip.expenses.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Money,
                    contentDescription = "Нет расходов",
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Расходы пока не добавлены",
                    fontSize = 18.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Добавьте ваши траты",
                    fontSize = 14.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            Text(
                text = "Статьи расходов (${trip.expenses.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                trip.expenses.forEach { expense ->
                    ExpenseItem(expense = expense)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("add_finance/${trip.id}") },
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
                imageVector = Icons.Filled.Add,
                contentDescription = "Добавить",
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Управление финансами",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
