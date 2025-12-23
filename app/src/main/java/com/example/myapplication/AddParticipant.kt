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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    val phoneRegex = remember { Regex("^\\+?7\\d{10}$") }
    val invalidPhoneMessage = "Введите номер в формате +7XXXXXXXXXX"

    val trip by remember(tripId) {
        derivedStateOf {
            tripId?.let { tripViewModel.getTripById(it) }
        }
    }

    val currentUser by userViewModel.userData.collectAsState()
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    val scope = rememberCoroutineScope()

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
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(vertical = 70.dp)

        ) {
            Text(
                text = "Добавить участника",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            errorMessage = null
                            successMessage = null
                        },
                        singleLine = true,
                        label = { Text("Телефон") },
                        placeholder = { Text("+7XXXXXXXXXX") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = null,
                                tint = Color(0xFFFFDD2D)
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        isError = !errorMessage.isNullOrBlank(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (errorMessage.isNullOrBlank()) Color(0xFFFFDD2D) else Color.Red,
                            unfocusedBorderColor = Color(0xFFBDBDBD),
                            focusedLabelColor = Color(0xFF333333),
                            unfocusedLabelColor = Color(0xFF666666),
                            cursorColor = Color(0xFFFFDD2D),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorBorderColor = Color.Red,
                            errorCursorColor = Color.Red,
                            errorLabelColor = Color.Red,
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    when {
                        !errorMessage.isNullOrBlank() -> Text(
                            text = errorMessage.orEmpty(),
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                        !successMessage.isNullOrBlank() -> Text(
                            text = successMessage.orEmpty(),
                            color = Color(0xFF2E7D32),
                            fontSize = 13.sp
                        )
                        else -> Text(
                            text = "Формат: +7XXXXXXXXXX",
                            color = Color(0xFF666666),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF7B7B),
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

                                val trimmed = phone.trim()
                                if (tripId == null || trimmed.isBlank()) {
                                    errorMessage = "Введите телефон"
                                    return@Button
                                }
                                if (!phoneRegex.matches(trimmed)) {
                                    errorMessage = invalidPhoneMessage
                                    return@Button
                                }

                                isLoading = true
                                val invite = InviteRequest(phones = listOf(trimmed))

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
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFDD2D),
                                contentColor = Color(0xFF333333)
                            ),
                            enabled = phone.isNotBlank() && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Отправляем...", fontWeight = FontWeight.SemiBold)
                            } else {
                                Text("Отправить", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
