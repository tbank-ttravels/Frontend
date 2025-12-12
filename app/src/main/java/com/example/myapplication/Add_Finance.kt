package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import com.example.core_data.model.CreateCategoryRequest
import com.example.core_data.model.ExpenseRequestDTO
import com.example.core_data.model.ExpenseUpdateRequestDTO
import com.example.core_data.network.NetworkResult
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.math.abs

data class ExpenseCategory(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

@Composable
fun Add_Finance(
    trip: Trip,
    tripViewModel: TripViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val backend = remember(context) { BackendProvider.get(context) }
    val scope = rememberCoroutineScope()
    val travelIdLong = remember(trip.id) { trip.id.toLongOrNull() }
    val isoFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
    }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Расходы", "Переводы", "Категории")
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var expenses by remember { mutableStateOf(trip.expenses) }
    var expensesError by remember { mutableStateOf<String?>(null) }
    var isExpensesLoading by remember { mutableStateOf(false) }
    var categories by remember { mutableStateOf<List<ExpenseCategory>>(emptyList()) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoriesError by remember { mutableStateOf<String?>(null) }
    var isCategoriesLoading by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<ExpenseCategory?>(null) }

    fun mapCategoryError(res: NetworkResult<*>, defaultMessage: String): String =
        when (res) {
            is NetworkResult.HttpError -> res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> "Проблемы с сетью"
            is NetworkResult.SerializationError -> "Ошибка обработки ответа"
            else -> defaultMessage
        }

    fun mapExpenseError(res: NetworkResult<*>, defaultMessage: String): String =
        when (res) {
            is NetworkResult.HttpError -> res.error?.message ?: "Ошибка ${res.code}"
            is NetworkResult.NetworkError -> "Проблемы с сетью"
            is NetworkResult.SerializationError -> "Ошибка обработки ответа"
            else -> defaultMessage
        }

    fun refreshExpenses() {
        scope.launch {
            isExpensesLoading = true
            expensesError = null
            val travelId = travelIdLong
            if (travelId == null) {
                expensesError = "Некорректный идентификатор поездки"
                isExpensesLoading = false
                return@launch
            }
            when (val res = backend.getTravelExpenses(travelId)) {
                is NetworkResult.Success -> {
                    val mapped = res.data.expenses.map { exp ->
                        Expense(
                            id = exp.id.toString(),
                            title = exp.name,
                            amount = exp.sum ?: 0.0,
                            category = exp.categoryName.orEmpty(),
                            categoryId = exp.categoryId?.toString(),
                            payerId = exp.payerId.toString(),
                            paidFor = if (exp.participants.size > 1) "Индивидуальные суммы участников" else "Оплата за одного участника",
                            date = formatDateForUi(exp.date),
                            recipientAmounts = exp.participants.associate { it.userId.toString() to abs(it.share ?: 0.0) }
                        )
                    }
                    expenses = mapped
                    tripViewModel.updateTripExpenses(trip.id, mapped)
                }
                else -> expensesError = mapExpenseError(res, "Не удалось загрузить расходы")
            }
            isExpensesLoading = false
        }
    }

    LaunchedEffect(trip.id) {
        refreshExpenses()
        isCategoriesLoading = true
        categoriesError = null
        val travelId = travelIdLong
        if (travelId == null) {
            categoriesError = "Некорректный идентификатор поездки"
            isCategoriesLoading = false
            return@LaunchedEffect
        }
        when (val res = backend.getCategories(travelId)) {
            is NetworkResult.Success -> {
                categories = res.data.items.map { ExpenseCategory(id = it.id.toString(), name = it.name) }
            }
            else -> categoriesError = mapCategoryError(res, "Не удалось загрузить категории")
        }
        isCategoriesLoading = false
    }

    val totalExpenses = expenses.sumOf { it.amount }
    val acceptedParticipants = remember(trip.participants) {
        trip.participants.filter { it.status.toConfirmationStatus() == ConfirmationStatus.ACCEPTED }
    }
    fun buildParticipantShares(
        recipients: Map<String, Double>
    ): Map<Long, Double> = recipients.mapKeys { it.key.toLong() }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFDD2D))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier
                            .clickable { navController.navigateUp() }
                            .padding(end = 16.dp)
                            .size(28.dp),
                        tint = Color(0xFF333333)
                    )
                    Text(
                        text = "Управление финансами",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${totalExpenses.toInt()} ₽",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFFFFDD2D),
                    contentColor = Color(0xFF333333)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> {
                    FloatingActionButton(
                        onClick = { showAddExpenseDialog = true },
                        containerColor = Color(0xFFFFDD2D),
                        contentColor = Color(0xFF333333)
                    ) {
                        Icon(Icons.Filled.Add, "Добавить расход")
                    }
                }
                2 -> {
                    FloatingActionButton(
                        onClick = { showAddCategoryDialog = true },
                        containerColor = Color(0xFFFFDD2D),
                        contentColor = Color(0xFF333333)
                    ) {
                        Icon(Icons.Filled.Add, "Добавить категорию")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> ExpensesTab(
                    expenses = expenses,
                    totalExpenses = totalExpenses,
                    trip = trip,
                    tripViewModel = tripViewModel,
                    categories = categories,
                    isLoading = isExpensesLoading,
                    errorMessage = expensesError,
                    showAddExpenseDialog = showAddExpenseDialog,
                    editingExpense = editingExpense,
                    onEditExpense = { editingExpense = it },
                    onDeleteExpense = { expense ->
                        scope.launch {
                            expensesError = null
                            when (val res = backend.deleteExpense(trip.id.toLong(), expense.id.toLong())) {
                                is NetworkResult.Success -> refreshExpenses()
                                else -> expensesError = mapExpenseError(res, "Не удалось удалить расход")
                            }
                        }
                    },
                    onSaveExpense = { expense, isEditing ->
                        val selectedCategoryId = categories.find { it.name == expense.category || it.id == expense.categoryId }?.id?.toLongOrNull()
                        val payerIdLong = expense.payerId.toLongOrNull()
                        if (selectedCategoryId == null) {
                            expensesError = "Выберите категорию"
                            return@ExpensesTab
                        }
                        if (payerIdLong == null) {
                            expensesError = "Выберите плательщика"
                            return@ExpensesTab
                        }
                        val payerParticipant = acceptedParticipants.find { it.id == expense.payerId }
                        if (payerParticipant == null) {
                            expensesError = "Плательщик должен быть участником поездки"
                            return@ExpensesTab
                        }
                    if (expense.recipientAmounts.isEmpty()) {
                        expensesError = "Укажите участников для траты"
                        return@ExpensesTab
                    }
                    val shares = buildParticipantShares(
                        recipients = expense.recipientAmounts
                    )
                        val travelId = travelIdLong
                        if (travelId == null) {
                            expensesError = "Некорректный идентификатор поездки"
                            return@ExpensesTab
                        }
                        scope.launch {
                            expensesError = null
                            if (isEditing) {
                                val req = ExpenseUpdateRequestDTO(
                                    name = expense.title,
                                    description = null,
                                    date = isoFormatter.format(Date()),
                                    categoryId = selectedCategoryId,
                                    payerId = payerIdLong,
                                    participantShares = shares
                                )
                                when (val res = backend.updateExpense(travelId, expense.id.toLong(), req)) {
                                    is NetworkResult.Success -> refreshExpenses()
                                    else -> expensesError = mapExpenseError(res, "Не удалось обновить расход")
                                }
                            } else {
                                val req = ExpenseRequestDTO(
                                    name = expense.title,
                                    description = null,
                                    payerId = payerIdLong,
                                    date = isoFormatter.format(Date()),
                                    participantShares = shares,
                                    categoryId = selectedCategoryId
                                )
                                when (val res = backend.createExpense(travelId, req)) {
                                    is NetworkResult.Success -> refreshExpenses()
                                    else -> expensesError = mapExpenseError(res, "Не удалось добавить расход")
                                }
                            }
                        }
                        showAddExpenseDialog = false
                        editingExpense = null
                    },
                    onDismissDialog = {
                        showAddExpenseDialog = false
                        editingExpense = null
                    }
                )

                1 -> TransfersScreen(
                    trip = trip,
                    tripViewModel = tripViewModel,
                    navController = navController,
                )

                2 -> CategoriesTab(
                    categories = categories,
                    errorMessage = categoriesError,
                    isLoading = isCategoriesLoading,
                    showAddCategoryDialog = showAddCategoryDialog,
                    editingCategory = editingCategory,
                    onOpenAddDialog = {
                        editingCategory = null
                        showAddCategoryDialog = true
                    },
                    onAddCategory = { category ->
                        val travelId = travelIdLong
                        if (travelId == null) {
                            categoriesError = "Некорректный идентификатор поездки"
                            return@CategoriesTab
                        }
                        if (isCategoriesLoading) return@CategoriesTab
                        categoriesError = null
                        editingCategory = null
                        scope.launch {
                            when (val res = backend.createCategory(travelId, CreateCategoryRequest(category.name))) {
                                is NetworkResult.Success -> {
                                    val newCat = ExpenseCategory(id = res.data.id.toString(), name = res.data.name)
                                    categories = categories + newCat
                                    showAddCategoryDialog = false
                                }
                                else -> categoriesError = mapCategoryError(res, "Не удалось добавить категорию")
                            }
                        }
                    },
                    onEditCategory = { category ->
                        editingCategory = category
                        showAddCategoryDialog = true
                    },
                    onSaveCategory = { category ->
                        val travelId = travelIdLong
                        if (travelId == null) {
                            categoriesError = "Некорректный идентификатор поездки"
                            return@CategoriesTab
                        }
                        if (isCategoriesLoading) return@CategoriesTab
                        categoriesError = null
                        scope.launch {
                            when (val res = backend.editCategory(travelId, category.id.toLong(), com.example.core_data.model.EditCategoryRequest(category.name))) {
                                is NetworkResult.Success -> {
                                    categories = categories.map { if (it.id == category.id) category else it }
                                    showAddCategoryDialog = false
                                    editingCategory = null
                                }
                                else -> categoriesError = mapCategoryError(res, "Не удалось обновить категорию")
                            }
                        }
                    },
                    onDismissDialog = {
                        showAddCategoryDialog = false
                        editingCategory = null
                    }
                )
            }
        }
    }
}

@Composable
fun ExpensesTab(
    expenses: List<Expense>,
    totalExpenses: Double,
    trip: Trip,
    tripViewModel: TripViewModel,
    categories: List<ExpenseCategory>,
    isLoading: Boolean,
    errorMessage: String?,
    showAddExpenseDialog: Boolean,
    editingExpense: Expense?,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onSaveExpense: (Expense, Boolean) -> Unit,
    onDismissDialog: () -> Unit
) {
    Column {
        if (isLoading) {
            Text(
                text = "Загружаем расходы...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = Color.Gray
            )
        }
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Red
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Общие расходы",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${totalExpenses.toInt()} ₽",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }

        if (expenses.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AttachMoney,
                    contentDescription = "Нет расходов",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFBDBDBD)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Расходы пока не добавлены",
                    fontSize = 18.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Нажмите + чтобы добавить первый расход",
                    fontSize = 14.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            val categoriesMap = categories.associateBy { it.name }
            val expensesByCategory = expenses.groupBy { it.category }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                expensesByCategory.forEach { (categoryName, categoryExpenses) ->
                    val categoryTotal = categoryExpenses.sumOf { it.amount }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Category,
                                    contentDescription = categoryName,
                                    tint = Color(0xFF757575),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = categoryName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                            }
                            Text(
                                text = "${categoryTotal.toInt()} ₽",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF333333)
                            )
                        }
                    }

                    items(categoryExpenses) { expense ->
                        ExpenseItem(
                            expense = expense,
                            trip = trip,
                            tripViewModel = tripViewModel,
                            onEditClick = { onEditExpense(expense) },
                            onDeleteClick = { onDeleteExpense(expense) }
                        )
                    }
                }
            }
        }

        if (showAddExpenseDialog || editingExpense != null) {
                AddEditExpenseDialog(
                    expense = editingExpense,
                    trip = trip,
                    tripViewModel = tripViewModel,
                    categories = categories,
                    onDismiss = onDismissDialog,
                    onSave = { expense -> onSaveExpense(expense, editingExpense != null) }
                )
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    trip: Trip,
    tripViewModel: TripViewModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val payerName = tripViewModel.getPayerName(trip.id, expense.payerId)

    val paidForIcon = when {
        expense.recipientAmounts.size <= 1 -> Icons.Filled.Person
        else -> Icons.Filled.Group
    }

    val paidForColor = when {
        expense.recipientAmounts.size <= 1 -> Color(0xFF2196F3)
        else -> Color(0xFF4CAF50)
    }
    val paidForText = if (expense.recipientAmounts.size <= 1) {
        "Оплата за одного участника"
    } else {
        "Индивидуальные суммы участников"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Category,
                        contentDescription = expense.category,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = expense.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF333333)
                        )
                        Text(
                            text = "Оплатил: $payerName",
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${expense.amount.toInt()} ₽",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = paidForIcon,
                        contentDescription = "За кого",
                        tint = paidForColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = paidForText,
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Категория: ${expense.category} • ${expense.date}",
                            fontSize = 12.sp,
                            color = Color(0xFF999999),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Редактировать",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Удалить",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить расход?") },
            text = { Text("Вы уверены, что хотите удалить расход \"${expense.title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color(0xFF666666)
                    )
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseDialog(
    expense: Expense?,
    trip: Trip,
    tripViewModel: TripViewModel,
    categories: List<ExpenseCategory>,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    val acceptedParticipants = remember(trip.participants) {
        trip.participants.filter { it.status.toConfirmationStatus() == ConfirmationStatus.ACCEPTED }
    }
    var title by remember { mutableStateOf(expense?.title ?: "") }
    var selectedCategory by remember {
        mutableStateOf(expense?.category ?: categories.firstOrNull()?.name ?: "")
    }
    var selectedPayerId by remember {
        mutableStateOf(expense?.payerId ?: acceptedParticipants.firstOrNull()?.id ?: "")
    }
    var recipientAmounts by remember {
        mutableStateOf<Map<String, String>>(
            expense?.recipientAmounts?.mapValues { it.value.toString() } ?: emptyMap()
        )
    }
    LaunchedEffect(selectedPayerId) {
        if (selectedPayerId.isNotBlank() && recipientAmounts[selectedPayerId].isNullOrBlank()) {
            recipientAmounts = recipientAmounts + (selectedPayerId to "0")
        }
    }
    var localError by remember { mutableStateOf<String?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPayerDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (expense == null) "Добавить расход" else "Редактировать расход",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название расхода") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Filled.AttachMoney, null, tint = Color(0xFFFFDD2D))
                    }
                )

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категория") },
                        leadingIcon = { Icon(Icons.Filled.Category, null, tint = Color(0xFF757575)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                if (categories.isNotEmpty()) {
                                    showCategoryDropdown = true
                                }
                            }
                    )
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        if (categories.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Сначала создайте категории во вкладке 'Категории'") },
                                onClick = {}
                            )
                        } else {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category.name
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (acceptedParticipants.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val selectedPayerName = acceptedParticipants.find { it.id == selectedPayerId }?.name ?: "Не выбран"
                        OutlinedTextField(
                            value = selectedPayerName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Кто оплатил") },
                            leadingIcon = { Icon(Icons.Filled.Person, null, tint = Color(0xFF2196F3)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showPayerDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showPayerDropdown,
                            onDismissRequest = { showPayerDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            acceptedParticipants.forEach { user ->
                                DropdownMenuItem(
                                    text = { Text(user.name) },
                                    onClick = {
                                        selectedPayerId = user.id
                                        showPayerDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        "Добавьте участников, чтобы указать плательщика",
                        color = Color(0xFF999999),
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "За кого платили (укажите суммы)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    acceptedParticipants.forEach { participant ->
                        val isPayer = participant.id == selectedPayerId
                        val amountValue = recipientAmounts[participant.id] ?: ""
                        val isChecked = isPayer || recipientAmounts.containsKey(participant.id)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = isChecked,
                                enabled = !isPayer,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        recipientAmounts = recipientAmounts + (participant.id to amountValue.ifBlank { "0" })
                                    } else {
                                        recipientAmounts = recipientAmounts - participant.id
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isPayer) "${participant.name} (платил)" else participant.name,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = amountValue,
                                onValueChange = { newValue ->
                                    if (newValue.matches(Regex("^\\d*\\.?\\d*$")) || newValue.isEmpty()) {
                                        recipientAmounts = recipientAmounts + (participant.id to newValue)
                                    }
                                },
                                label = { Text("Сумма") },
                                modifier = Modifier.width(140.dp),
                                singleLine = true,
                                enabled = isChecked
                            )
                        }
                    }
                }

                localError?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedRecipients = recipientAmounts.mapNotNull { (id, value) ->
                        val v = value.toDoubleOrNull()
                        if (v != null && v > 0) id to v else null
                    }.toMap()
                    if (title.isBlank() || selectedPayerId.isBlank() || selectedCategory.isBlank()) return@Button
                    if (parsedRecipients.isEmpty()) {
                        localError = "Укажите суммы для участников"
                        return@Button
                    }
                    if (!parsedRecipients.containsKey(selectedPayerId)) {
                        localError = "Добавьте сумму для плательщика"
                        return@Button
                    }
                    val total = parsedRecipients.values.sumOf { it }
                    val newExpense = Expense(
                        id = expense?.id ?: UUID.randomUUID().toString(),
                        title = title,
                        amount = total,
                        category = selectedCategory,
                        payerId = selectedPayerId,
                        paidFor = "Выбранные участники",
                        paidForIds = parsedRecipients.keys.toList(),
                        recipientAmounts = parsedRecipients
                    )
                    onSave(newExpense)
                    localError = null
                },
                enabled = title.isNotBlank() &&
                        selectedPayerId.isNotBlank() &&
                        selectedCategory.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFDD2D),
                    contentColor = Color(0xFF333333)
                )
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF5F5F5),
                    contentColor = Color(0xFF666666)
                )
            ) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun CategoriesTab(
    categories: List<ExpenseCategory>,
    errorMessage: String?,
    isLoading: Boolean,
    showAddCategoryDialog: Boolean,
    editingCategory: ExpenseCategory?,
    onOpenAddDialog: () -> Unit,
    onAddCategory: (ExpenseCategory) -> Unit,
    onSaveCategory: (ExpenseCategory) -> Unit,
    onEditCategory: (ExpenseCategory) -> Unit,
    onDismissDialog: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Категории",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onOpenAddDialog,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFDD2D),
                    contentColor = Color(0xFF333333)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Добавить")
            }
        }
        if (isLoading) {
            Text(
                text = "Загружаем категории...",
                modifier = Modifier.padding(16.dp),
                color = Color.Gray
            )
        }
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Red
            )
        }
        if (categories.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Category,
                    contentDescription = "Нет категорий",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFBDBDBD)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Категории не созданы",
                    fontSize = 18.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Нажмите + чтобы добавить первую категорию",
                    fontSize = 14.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        onEdit = { onEditCategory(category) }
                    )
                }
            }
        }
    }

    if (showAddCategoryDialog || editingCategory != null) {
        AddCategoryDialog(
            category = editingCategory,
            onAddCategory = onAddCategory,
            onSaveCategory = onSaveCategory,
            onDismiss = onDismissDialog
        )
    }
}

@Composable
fun CategoryItem(
    category: ExpenseCategory,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Category,
                contentDescription = category.name,
                tint = Color(0xFF757575),
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
            }

            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Редактировать",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    category: ExpenseCategory? = null,
    onAddCategory: (ExpenseCategory) -> Unit,
    onSaveCategory: (ExpenseCategory) -> Unit,
    onDismiss: () -> Unit
) {
    var categoryName by remember { mutableStateOf(category?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Добавить категорию",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Название категории") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Filled.Category, null, tint = Color(0xFFFFDD2D))
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isBlank()) return@Button
                    if (category == null) {
                        onAddCategory(ExpenseCategory(name = categoryName))
                    } else {
                        onSaveCategory(category.copy(name = categoryName))
                    }
                },
                enabled = categoryName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFDD2D),
                    contentColor = Color(0xFF333333)
                )
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF5F5F5),
                    contentColor = Color(0xFF666666)
                )
            ) {
                Text("Отмена")
            }
        }
    )
}

private fun String?.toConfirmationStatus(): ConfirmationStatus =
    when (this?.uppercase(Locale.getDefault())) {
        "INVITED" -> ConfirmationStatus.PENDING
        "ACCEPTED" -> ConfirmationStatus.ACCEPTED
        "REJECTED" -> ConfirmationStatus.REJECTED
        "LEAVE" -> ConfirmationStatus.LEFT
        else -> ConfirmationStatus.PENDING
    }
