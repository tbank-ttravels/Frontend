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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.core_data.model.AuthLoginRequest
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.delay


sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@Composable
fun AuthScreen(
    navController: NavHostController,
    userViewModel: UserViewModel
)  {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authState by remember { mutableStateOf<AuthState>(AuthState.Idle) }
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }

    LaunchedEffect(authState) {
        when (authState) {
            AuthState.Loading -> {
                val request = AuthLoginRequest(
                    phone = phone.trim(),
                    password = password
                )
                authState = when (val res = backend.login(request)) {
                    is NetworkResult.Success -> AuthState.Success("Авторизация успешна!")
                    is NetworkResult.HttpError -> AuthState.Error(res.error?.message ?: "Ошибка ${res.code}")
                    is NetworkResult.NetworkError -> AuthState.Error("Проблемы с сетью")
                    is NetworkResult.SerializationError -> AuthState.Error("Ошибка обработки ответа")
                    else -> AuthState.Error("Не удалось авторизоваться")
                }
            }
            is AuthState.Success -> {
                val profileResult = backend.getCurrentUser()
                val acc = (profileResult as? NetworkResult.Success)?.data
                val profileName = listOfNotNull(acc?.name, acc?.surname).joinToString(" ").ifBlank { acc?.phone ?: "Пользователь" }
                userViewModel.updateUser(
                    name = acc?.name.orEmpty(),
                    surname = acc?.surname.orEmpty(),
                    phone = acc?.phone.orEmpty().ifBlank { phone.trim() }
                )
                delay(500)
                navController.navigate("profile") {
                    popUpTo("auth") { inclusive = true }
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
            text = "Добро пожаловать!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF333333),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 160.dp)
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
                Text(
                    text = "Войти",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF333333)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        if (authState is AuthState.Error) {
                            authState = AuthState.Idle
                        }
                    },
                    label = { Text("Номер телефона", fontWeight = FontWeight.ExtraBold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = authState is AuthState.Error
                )


                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it

                        if (authState is AuthState.Error) {
                            authState = AuthState.Idle
                        }
                    },
                    label = { Text("Пароль", fontWeight = FontWeight.ExtraBold) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = authState is AuthState.Error

                )


                when (authState) {
                    is AuthState.Error -> {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    is AuthState.Success -> {
                        Text(
                            text = (authState as AuthState.Success).message,
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

                        authState = AuthState.Loading
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD2D),
                        contentColor = Color(0xFF333333)
                    ),
                    enabled = authState != AuthState.Loading &&
                            phone.isNotEmpty() && password.isNotEmpty()
                ) {
                    if (authState == AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF333333),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Войти", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }


                Button(
                    onClick = { navController.navigate("registration") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFFFFF),
                        contentColor = Color(0xFF333333)
                    ),
                    enabled = authState != AuthState.Loading
                ) {
                    Text(
                        "Нет аккаунта? Зарегистрироваться",
                        color = Color(0xFF333333)
                    )
                }
            }
        }
    }
}
