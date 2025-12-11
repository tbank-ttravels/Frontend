package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun  ParticipantCardWithConfirmation(
    user: User,
    confirmationStatus: ConfirmationStatus,
    onDeleteClick: () -> Unit,
    isCurrentUser: Boolean,
    isTripCreator: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (confirmationStatus) {
                ConfirmationStatus.PENDING -> Color(0xFFFFF8E1)
                ConfirmationStatus.ACCEPTED -> Color(0xFFE8F5E9)
                ConfirmationStatus.REJECTED -> Color(0xFFFFEBEE)
                ConfirmationStatus.LEFT -> Color(0xFFF5F5F5)
            }
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
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = when (confirmationStatus) {
                                ConfirmationStatus.PENDING -> Color(0xFFFFF3E0)
                                ConfirmationStatus.ACCEPTED -> Color(0xFFC8E6C9)
                                ConfirmationStatus.REJECTED -> Color(0xFFFFCDD2)
                                ConfirmationStatus.LEFT -> Color(0xFFE0E0E0)
                            },
                            shape = CircleShape
                        )
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (confirmationStatus) {
                            ConfirmationStatus.PENDING -> Color(0xFFF57C00)
                            ConfirmationStatus.ACCEPTED -> Color(0xFF4CAF50)
                            ConfirmationStatus.REJECTED -> Color(0xFFF44336)
                            ConfirmationStatus.LEFT -> Color(0xFF9E9E9E)
                        }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    when (confirmationStatus) {
                                        ConfirmationStatus.PENDING -> Color(0xFFFFF3E0)
                                        ConfirmationStatus.ACCEPTED -> Color(0xFFE8F5E9)
                                        ConfirmationStatus.REJECTED -> Color(0xFFFFEBEE)
                                        ConfirmationStatus.LEFT -> Color(0xFFF5F5F5)
                                    },
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = when (confirmationStatus) {
                                    ConfirmationStatus.PENDING -> "Ожидает"
                                    ConfirmationStatus.ACCEPTED -> "Принял"
                                    ConfirmationStatus.REJECTED -> "Отклонил"
                                    ConfirmationStatus.LEFT -> "Вышел"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (confirmationStatus) {
                                    ConfirmationStatus.PENDING -> Color(0xFFF57C00)
                                    ConfirmationStatus.ACCEPTED -> Color(0xFF4CAF50)
                                    ConfirmationStatus.REJECTED -> Color(0xFFF44336)
                                    ConfirmationStatus.LEFT -> Color(0xFF9E9E9E)
                                }
                            )
                        }
                    }

                    Text(
                        text = user.phone,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    if (confirmationStatus == ConfirmationStatus.PENDING) {
                        Text(
                            text = "Ожидает подтверждения участия",
                            fontSize = 12.sp,
                            color = Color(0xFFF57C00),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            when (confirmationStatus) {
                ConfirmationStatus.PENDING -> {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = "Ожидает",
                        tint = Color(0xFFF57C00),
                        modifier = Modifier.size(24.dp)
                    )
                }
                else -> {
                    if (isTripCreator && !isCurrentUser && confirmationStatus != ConfirmationStatus.LEFT) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFFE0E0), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Удалить",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
