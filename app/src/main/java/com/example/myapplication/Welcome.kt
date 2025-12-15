package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.core_data.network.NetworkResult


@Composable
fun Welcome(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val backend = remember { BackendProvider.get(context) }
    LaunchedEffect(Unit) {
        val tokens = backend.tokensStore?.currentTokens()
        if (tokens != null) {
            when (val res = backend.getCurrentUser()) {
                is NetworkResult.Success -> {
                    val acc = res.data
                    userViewModel.updateUser(
                        name = acc.name.orEmpty(),
                        surname = acc.surname.orEmpty(),
                        phone = acc.phone.orEmpty()
                    )
                    navController.navigate("main") {
                        popUpTo("welcome") { inclusive = true }
                    }
                    return@LaunchedEffect
                }
                else -> {
                    backend.tokensStore?.clear()
                    userViewModel.logout()
                }
            }
        }

    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFDD2D)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Flight,
                contentDescription = "Логотип T-Путешествия",
                tint = Color(0xFF333333),
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Добро пожаловать!",
                color = Color(0xFF333333),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "T-Путешествия",
                color = Color(0xFF333333),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}