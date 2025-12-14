package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.core_data.model.InviteRequest
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.launch

@Composable
fun AddParticipantScreen(
    tripId: String?,
    navController: NavHostController,
    tripViewModel: TripViewModel,
    userViewModel: UserViewModel
) {
    var phone by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val trip by remember(tripId) {
        derivedStateOf {
            tripId?.let { tripViewModel.getTripById(it) }
        }
    }

    val currentUser by userViewModel.userData.collectAsState()
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    val scope = rememberCoroutineScope()

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
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Телефон") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        )

        if (!errorMessage.isNullOrBlank()) {
            Text(text = errorMessage.orEmpty(), color = Color.Red, fontSize = 14.sp)
        }
        if (!successMessage.isNullOrBlank()) {
            Text(text = successMessage.orEmpty(), color = Color(0xFF4CAF50), fontSize = 14.sp)
        }


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
                    if (isLoading) return@Button
                    errorMessage = null
                    successMessage = null
                    if (phone.isBlank() || tripId == null) {
                        errorMessage = "Введите телефон"
                        return@Button
                    }
                    isLoading = true
                    val invite = InviteRequest(phones = listOf(phone.trim()))
                    scope.launch {
                        val res = backend.inviteMembers(tripId.toLong(), invite)
                        if (res is NetworkResult.Success) {
                            successMessage = "Приглашение отправлено"
                            phone = ""
                        } else {
                            errorMessage = when (res) {
                                is NetworkResult.HttpError -> res.error?.message ?: "Ошибка ${res.code}"
                                is NetworkResult.NetworkError -> "Проблемы с сетью"
                                is NetworkResult.SerializationError -> "Ошибка обработки ответа"
                                else -> "Не удалось отправить приглашение"
                            }
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFDD2D),
                    contentColor = Color(0xFF333333)
                ),
                enabled = phone.isNotBlank() && !isLoading
            ) {
                Text(if (isLoading) "Отправляем..." else "Отправить приглашение", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
