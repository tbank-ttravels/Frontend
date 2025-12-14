package com.example.myapplication
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.core_data.model.CreateTravelRequest
import com.example.core_data.network.NetworkResult
import com.example.myapplication.formatDateForUi
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun Create_trip(
    navController: NavController,
    tripViewModel: TripViewModel = viewModel()
)  {
    val context = LocalContext.current
    val displayFormatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val isoFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
    }
    var travelName by remember { mutableStateOf("") }
    var travelDescription by remember { mutableStateOf("") }
    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    val backend = remember(context) { BackendProvider.get(context) }
    val scope = rememberCoroutineScope()

    fun formatForDisplay(millis: Long?): String =
        millis?.let { displayFormatter.format(Date(it)) } ?: ""

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
            text = "Путешествуй вместе с нами!",
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
                .padding( 30.dp)
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
                    value = travelName,
                    onValueChange = { travelName = it },
                    label = { Text("Название поездки", fontWeight = FontWeight.ExtraBold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = travelDescription,
                    onValueChange = { travelDescription = it },
                    label = { Text("Описание (необязательно)", fontWeight = FontWeight.ExtraBold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                val dateFieldColors = TextFieldDefaults.colors(
                    disabledTextColor = Color(0xFF333333),
                    disabledIndicatorColor = Color.Gray,
                    disabledLabelColor = Color(0xFF666666),
                    disabledPlaceholderColor = Color(0xFF999999),
                    disabledTrailingIconColor = Color(0xFF333333),
                    disabledContainerColor = Color.Transparent
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openDateTimePicker(startDateMillis) { startDateMillis = it } }
                ) {
                    OutlinedTextField(
                        value = formatForDisplay(startDateMillis).ifBlank { "Выберите дату и время начала" },
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Дата начала", fontWeight = FontWeight.ExtraBold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Выбрать дату начала"
                            )
                        },
                        colors = dateFieldColors
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openDateTimePicker(endDateMillis) { endDateMillis = it } }
                ) {
                    OutlinedTextField(
                        value = formatForDisplay(endDateMillis).ifBlank { "Выберите дату и время конца" },
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Дата конца", fontWeight = FontWeight.ExtraBold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Выбрать дату конца"
                            )
                        },
                        colors = dateFieldColors
                    )
                }



                Spacer(modifier = Modifier.height(16.dp))

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = {
                        if (isSubmitting) return@Button
                        errorMessage = null
                        when {
                            travelName.isBlank() -> {
                                errorMessage = "Введите название поездки"
                            }
                            startDateMillis == null -> {
                                errorMessage = "Выберите дату и время начала"
                            }
                            endDateMillis != null && startDateMillis != null && endDateMillis!! < startDateMillis!! -> {
                                errorMessage = "Дата конца должна быть позже даты начала"
                            }
                            else -> {
                                isSubmitting = true
                                val request = CreateTravelRequest(
                                    name = travelName.trim(),
                                    description = travelDescription.takeIf { it.isNotBlank() },
                                    startDate = isoFormatter.format(Date(startDateMillis!!)),
                                    endDate = endDateMillis?.let { isoFormatter.format(Date(it)) }
                                )
                                scope.launch {
                                    when (val res = backend.createTravel(request)) {
                                        is NetworkResult.Success -> {
                                            val mapped = Trip(
                                                id = res.data.id.toString(),
                                                name = res.data.name,
                                                description = res.data.description,
                                                startDate = formatDateForUi(res.data.startDate),
                                                endDate = formatDateForUi(res.data.endDate),
                                                status = res.data.status,
                                                participants = emptyList(),
                                                expenses = emptyList()
                                            )
                                            tripViewModel.upsertTrip(mapped)
                                            navController.navigate("main") {
                                                popUpTo("create_trip") { inclusive = true }
                                            }
                                        }
                                        is NetworkResult.HttpError -> errorMessage = res.error?.message ?: "Ошибка ${res.code}"
                                        is NetworkResult.NetworkError -> errorMessage = "Проблемы с сетью"
                                        is NetworkResult.SerializationError -> errorMessage = "Ошибка обработки ответа"
                                        else -> errorMessage = "Не удалось создать поездку"
                                    }
                                    isSubmitting = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD2D),
                        contentColor = Color(0xFF333333)
                    ),
                    enabled = !isSubmitting
                ) {
                    Text(
                        if (isSubmitting) "Сохраняем..." else "Создать",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }



                Button(
                    onClick = { navController.navigate("main") },
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
                        "Список поездок",
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                    )
                }



            }

        }
    }
}
