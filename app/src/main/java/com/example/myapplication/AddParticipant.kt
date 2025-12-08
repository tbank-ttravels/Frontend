package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddParticipantScreen(
    tripId: String?,
    navController: NavHostController,
    tripViewModel: TripViewModel,
    userViewModel: UserViewModel
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val trip by remember(tripId) {
        derivedStateOf {
            tripId?.let { tripViewModel.getTripById(it) }
        }
    }

    val currentUser by userViewModel.userData.collectAsState()

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
            .padding(16.dp)
    ) {
        Text(
            text = "Добавить участника",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF333333),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Имя участника") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Телефон") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        )



        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.navigateUp() },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFBE7),
                    contentColor = Color(0xFF333333)
                )
            ) {
                Text("Отмена", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && tripId != null && trip != null) {
                        val newParticipant = User(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            phone = phone
                        )

                        tripViewModel.addParticipantToTrip(tripId, newParticipant)

                        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

                        tripViewModel.sendInvitation(
                            tripId = tripId,
                            userId = newParticipant.id,
                            invitedBy = currentUser.name
                        )

                        navController.navigateUp()
                    }
                },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFDD2D),
                    contentColor = Color(0xFF333333)
                ),
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text("Отправить приглашение", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}