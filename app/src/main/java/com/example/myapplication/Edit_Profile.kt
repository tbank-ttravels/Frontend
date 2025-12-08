package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.core_data.model.ChangePasswordRequest
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun EditProfileScreen(
    navController: NavHostController,
    userViewModel: UserViewModel
) {
    val userData by userViewModel.userData.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(userData.name) }
    var surname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(userData.phone) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        isLoading = true
        when (val res = BackendProvider.get(context).getCurrentUser()) {
            is NetworkResult.Success -> {
                name = res.data.name.orEmpty()
                surname = res.data.surname.orEmpty()
                phone = res.data.phone.orEmpty()
                errorMessage = null
            }
            is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
            is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
            else -> errorMessage = "Не удалось загрузить профиль"
        }
        isLoading = false
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
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color(0xFF333333)
                )
            }
            Text(
                text = "Редактирование профиля",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Имя",
                            tint = Color(0xFFFFDD2D)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                    ,
                    readOnly = true
                )

                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Фамилия") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Фамилия",
                            tint = Color(0xFFFFDD2D)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Телефон") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Phone,
                            contentDescription = "Телефон",
                            tint = Color(0xFFFFDD2D)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Текущий пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Новый пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it },
                    label = { Text("Повторите новый пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                if (!successMessage.isNullOrBlank()) {
                    Text(
                        text = successMessage ?: "",
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                errorMessage = null
                successMessage = null
                if (newPassword != repeatPassword) {
                    errorMessage = "Пароли не совпадают"
                    return@Button
                }
                scope.launch {
                    isLoading = true
                    val req = ChangePasswordRequest(
                        currentPassword = currentPassword,
                        newPassword = newPassword
                    )
                    when (val res = BackendProvider.get(context).changePassword(req)) {
                        is NetworkResult.Success -> {
                            successMessage = "Пароль успешно изменён"
                            currentPassword = ""
                            newPassword = ""
                            repeatPassword = ""
                            errorMessage = null
                        }
                        is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
                        is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
                        is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
                        else -> errorMessage = "Не удалось изменить пароль"
                    }
                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFDD2D),
                contentColor = Color(0xFF333333)
            ),
            enabled = currentPassword.isNotBlank() &&
                    newPassword.isNotBlank() &&
                    repeatPassword.isNotBlank() &&
                    !isLoading
        ) {
            Text(
                "Изменить пароль",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Button(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF333333)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp
            )
        ) {
            Text(
                "Отмена",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
