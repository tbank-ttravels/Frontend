package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.core_data.model.EditTravelRequest
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun EditTripScreen(
    tripId: String?,
    navController: NavController,
    tripViewModel: TripViewModel
) {
    var trip by remember { mutableStateOf<Trip?>(null) }

    var tripName by remember { mutableStateOf("") }
    var tripDescription by remember { mutableStateOf("") }
    val displayFormatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val isoFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
    }
    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    val scope = rememberCoroutineScope()

    fun openDateTimePicker(currentMillis: Long?, onPicked: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = currentMillis ?: System.currentTimeMillis()
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        onPicked(cal.timeInMillis)
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LaunchedEffect(tripId) {
        if (tripId != null) {
            trip = tripViewModel.getTripById(tripId)
            trip?.let {
                tripName = it.name
                tripDescription = it.description.orEmpty()
                startDateMillis = parseDisplayToMillis(it.startDate)
                endDateMillis = parseDisplayToMillis(it.endDate)
            }
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
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = Color(0xFF333333),
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "Редактирование поездки",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF333333),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 140.dp)
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
                    value = tripName,
                    onValueChange = { tripName = it },
                    label = { Text("Название поездки", fontWeight = FontWeight.ExtraBold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = tripDescription,
                    onValueChange = { tripDescription = it },
                    label = { Text("Описание", fontWeight = FontWeight.ExtraBold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = startDateMillis?.let { displayFormatter.format(Date(it)) } ?: "Выберите дату начала",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .width(150.dp)
                            .clickable { openDateTimePicker(startDateMillis) { startDateMillis = it } },
                        label = { Text("Дата начала", fontWeight = FontWeight.ExtraBold) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = endDateMillis?.let { displayFormatter.format(Date(it)) } ?: "Выберите дату конца",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .width(150.dp)
                            .clickable { openDateTimePicker(endDateMillis) { endDateMillis = it } },
                        label = { Text("Дата конца", fontWeight = FontWeight.ExtraBold) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(text = errorMessage.orEmpty(), color = Color.Red, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (tripName.isBlank()) {
                            errorMessage = "Введите название"
                            return@Button
                        }
                        if (tripId == null || trip == null || isSubmitting) return@Button
                        isSubmitting = true
                        errorMessage = null
                        val req = EditTravelRequest(
                            name = tripName,
                            description = tripDescription.takeIf { it.isNotBlank() },
                            startDate = startDateMillis?.let { isoFormatter.format(Date(it)) },
                            endDate = endDateMillis?.let { isoFormatter.format(Date(it)) }
                        )
                        scope.launch {
                            when (val res = backend.editTravel(tripId.toLong(), req)) {
                                is NetworkResult.Success -> {
                                    val updatedTrip = Trip(
                                        id = res.data.id.toString(),
                                        name = res.data.name,
                                        description = res.data.description,
                                        startDate = formatDateForUi(res.data.startDate),
                                        endDate = formatDateForUi(res.data.endDate),
                                        status = res.data.status,
                                        participants = trip?.participants.orEmpty(),
                                        expenses = trip?.expenses.orEmpty()
                                    )
                                    tripViewModel.upsertTrip(updatedTrip)
                                    navController.navigateUp()
                                }
                                is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
                                is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
                                is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
                                else -> errorMessage = "Не удалось сохранить поездку"
                            }
                            isSubmitting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD2D),
                        contentColor = Color(0xFF333333)
                    ),
                    enabled = tripName.isNotEmpty() && !isSubmitting
                ) {
                    Text(
                        if (isSubmitting) "Сохраняем..." else "Сохранить изменения",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
                Button(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 4.dp,
                    color = Color(0xFFFFDD2D),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFFFFF),
                        contentColor = Color(0xFF333333)
                    )
                ) {
                    Text(
                        "Отмена",
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                    )
                }
            }
        }
    }
}
private fun parseDisplayToMillis(dateString: String?): Long? {
    if (dateString.isNullOrBlank()) return null
    return try {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        formatter.parse(dateString)?.time
    } catch (_: Exception) {
        null
    }
}
