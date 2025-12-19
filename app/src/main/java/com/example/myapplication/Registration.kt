package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.core_data.model.AuthRegisterRequest
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.delay

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val message: String) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

@Composable
fun Registration(
    navController: NavController,
    userViewModel: UserViewModel
) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var registrationState by remember { mutableStateOf<RegistrationState>(RegistrationState.Idle) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    val phoneRegex = remember { Regex("^\\+?7\\d{10}$") }
    val invalidPhoneMessage = "Введите номер в формате +7XXXXXXXXXX"
    val context = LocalContext.current

    LaunchedEffect(registrationState) {
        when(registrationState){
            is RegistrationState.Success -> {
                userViewModel.updateUser(name.trim(), surname.trim(), phone.trim())
                delay(1000)
                navController.navigate("profile") {
                    popUpTo("registration") { inclusive = true }
                }
            }
            is RegistrationState.Loading -> {
                val request = AuthRegisterRequest(
                    phone = phone.trim(),
                    name = name.trim(),
                    surname = surname.trim(),
                    password = password
                )
                registrationState = when (val res = BackendProvider.get(context).register(request)) {
                    is NetworkResult.Success -> RegistrationState.Success("Аккаунт успешно создан! Добро пожаловать, ${request.name}!")
                    is NetworkResult.HttpError -> RegistrationState.Error(res.error?.message ?: "Ошибка ${res.code}")
                    is NetworkResult.NetworkError -> RegistrationState.Error("Проблемы с сетью")
                    is NetworkResult.SerializationError -> RegistrationState.Error("Ошибка обработки ответа")
                    else -> RegistrationState.Error("Не удалось зарегистрироваться")
                }
            }
            is RegistrationState.Error -> {
                if (!phoneRegex.matches(phone.trim())) {
                    phoneError = invalidPhoneMessage
                }
            }
            else -> Unit
        }
    }

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
    ) {
        Text(
            text = "Регистрация",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF333333),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp)
                .align(Alignment.BottomCenter)
                .padding(30.dp)
                .border(
                    width = 2.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(
                        topStart = 32.dp,
                        topEnd = 32.dp
                    )
                ),
            shape = RoundedCornerShape(
                topStart = 32.dp,
                topEnd = 32.dp
            ),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { newName ->
                        name = newName
                        if (registrationState is RegistrationState.Error) {
                            registrationState = RegistrationState.Idle
                        }
                    },
                    label = { Text("Имя", fontWeight = FontWeight.ExtraBold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = registrationState is RegistrationState.Error
                )

                OutlinedTextField(
                    value = surname,
                    onValueChange = { newSurname ->
                        surname = newSurname
                        if (registrationState is RegistrationState.Error) {
                            registrationState = RegistrationState.Idle
                        }
                    },
                    label = { Text("Фамилия", fontWeight = FontWeight.ExtraBold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = registrationState is RegistrationState.Error
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { newPhone ->
                        phone = newPhone
                        phoneError = null
                        if (registrationState is RegistrationState.Error) {
                            registrationState = RegistrationState.Idle
                        }
                    },
                    label = { Text("Номер телефона", fontWeight = FontWeight.ExtraBold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = phoneError != null || registrationState is RegistrationState.Error
                )
                if (!phoneError.isNullOrBlank()) {
                    Text(
                        text = phoneError.orEmpty(),
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                } else {
                    Text(
                        text = "Формат: +7XXXXXXXXXX",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { newPassword ->
                        password = newPassword
                        if (registrationState is RegistrationState.Error) {
                            registrationState = RegistrationState.Idle
                        }
                    },
                    label = { Text("Пароль", fontWeight = FontWeight.ExtraBold) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = registrationState is RegistrationState.Error
                )

                when (registrationState) {
                    is RegistrationState.Error -> {
                        Text(
                            text = (registrationState as RegistrationState.Error).message,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    is RegistrationState.Success -> {
                        Text(
                            text = (registrationState as RegistrationState.Success).message,
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    else -> {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val trimmedPhone = phone.trim()
                        if (!phoneRegex.matches(trimmedPhone)) {
                            phoneError = invalidPhoneMessage
                            registrationState = RegistrationState.Idle
                            return@Button
                        }
                        phoneError = null
                        registrationState = RegistrationState.Loading
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD2D),
                        contentColor = Color(0xFF333333)
                    ),
                    enabled = registrationState != RegistrationState.Loading &&
                            name.isNotEmpty() && surname.isNotEmpty() &&
                            phone.isNotEmpty() && password.isNotEmpty()
                ) {
                    if (registrationState == RegistrationState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF333333),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Создать аккаунт", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }

                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFFFFF),
                        contentColor = Color(0xFF333333)
                    ),
                    enabled = registrationState != RegistrationState.Loading
                ) {
                    Text(
                        "Уже есть аккаунт? Войти",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF333333)
                    )
                }
            }
        }
    }
}
