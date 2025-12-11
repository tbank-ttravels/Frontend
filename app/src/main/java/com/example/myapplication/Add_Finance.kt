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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import java.util.UUID

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
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Расходы", "Переводы", "Категории")
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var expenses by remember { mutableStateOf(trip.expenses) }
    var categories by remember { mutableStateOf<List<ExpenseCategory>>(emptyList()) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(trip.expenses) {
        expenses = trip.expenses
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val updatedTrip = tripViewModel.getTripById(trip.id)
            updatedTrip?.let {
                expenses = it.expenses
            }
        }
    }

    LaunchedEffect(trip.id) {
        categories = getCategoriesForTrip(trip.id)
    }

    val totalExpenses = expenses.sumOf { it.amount }

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
                    showAddExpenseDialog = showAddExpenseDialog,
                    editingExpense = editingExpense,
                    onEditExpense = { editingExpense = it },
                    onDeleteExpense = { expense ->
                        tripViewModel.deleteExpense(trip.id, expense.id)
                        val updatedTrip = tripViewModel.getTripById(trip.id)
                        updatedTrip?.let {
                            expenses = it.expenses
                        }
                    },
                    onSaveExpense = { expense ->
                        if (editingExpense != null) {
                            tripViewModel.updateExpense(trip.id, expense)
                        } else {
                            tripViewModel.addExpense(trip.id, expense)
                        }
                        val updatedTrip = tripViewModel.getTripById(trip.id)
                        updatedTrip?.let {
                            expenses = it.expenses
                        }
                        showAddExpenseDialog = false
                        editingExpense = null
                    },
                    onDismissDialog = {
                        showAddExpenseDialog = false
                        editingExpense = null
                    }
                )

                1 -> TransfersTab(
                    trip = trip,
                    navController = navController,
                    tripViewModel = tripViewModel
                )

                2 -> CategoriesTab(
                    categories = categories,
                    showAddCategoryDialog = showAddCategoryDialog,
                    onAddCategory = { category ->
                        categories = categories + category
                        saveCategoriesForTrip(trip.id, categories)
                        showAddCategoryDialog = false
                    },
                    onDeleteCategory = { category ->
                        categories = categories.filter { it.id != category.id }
                        saveCategoriesForTrip(trip.id, categories)
                    },
                    onDismissDialog = { showAddCategoryDialog = false }
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
    showAddExpenseDialog: Boolean,
    editingExpense: Expense?,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onSaveExpense: (Expense) -> Unit,
    onDismissDialog: () -> Unit
) {
    Column {
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
                onSave = onSaveExpense
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
        expense.paidFor == "Только себя" -> Icons.Filled.Person
        expense.paidFor.startsWith("За: ") -> Icons.Filled.Person
        else -> Icons.Filled.Group
    }

    val paidForColor = when {
        expense.paidFor == "Только себя" -> Color(0xFF2196F3)
        expense.paidFor.startsWith("За: ") -> Color(0xFF9C27B0)
        else -> Color(0xFF4CAF50)
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
                            text = "За: ${expense.paidFor}",
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
    var title by remember { mutableStateOf(expense?.title ?: "") }
    var amount by remember { mutableStateOf(expense?.amount?.toString() ?: "") }
    var selectedCategory by remember {
        mutableStateOf(expense?.category ?: categories.firstOrNull()?.name ?: "")
    }
    var selectedPayerId by remember {
        mutableStateOf(expense?.payerId ?: trip.participants.firstOrNull()?.id ?: "")
    }
    var selectedPaidFor by remember { mutableStateOf(expense?.paidFor ?: "Только себя") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPaidForDropdown by remember { mutableStateOf(false) }
    var showPayerDropdown by remember { mutableStateOf(false) }

    val paidForOptions = remember(trip.id) {
        val baseOptions = listOf("Только себя", "Поровну между всеми")
        val participantOptions = trip.participants.map { "За: ${it.name}" }
        baseOptions + participantOptions
    }

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.matches(Regex("^\\d*\\.?\\d*$")) || newValue.isEmpty()) {
                            amount = newValue
                        }
                    },
                    label = { Text("Сумма (руб.)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Filled.AttachMoney, null)
                    }
                )

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            label = { Text("Категория") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Category,
                                    contentDescription = null,
                                    tint = Color(0xFF757575)
                                )
                            },
                            trailingIcon = {
                                if (categories.isEmpty()) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        "Нет категорий",
                                        tint = Color(0xFFFF9800)
                                    )
                                }
                            }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable {
                                    if (categories.isNotEmpty()) {
                                        showCategoryDropdown = true
                                    }
                                }
                                .background(Color.Transparent)
                        )
                    }

                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        if (categories.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text("Сначала создайте категории во вкладке 'Категории'")
                                },
                                onClick = {}
                            )
                        } else {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Category,
                                                contentDescription = null,
                                                tint = Color(0xFF757575),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(category.name)
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = category.name
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        OutlinedTextField(
                            value = selectedPaidFor,
                            onValueChange = {},
                            label = { Text("За кого оплачено") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false,
                            leadingIcon = {
                                Icon(Icons.Filled.Group, null, tint = Color(0xFF4CAF50))
                            }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { showPaidForDropdown = true }
                                .background(Color.Transparent)
                        )
                    }

                    DropdownMenu(
                        expanded = showPaidForDropdown,
                        onDismissRequest = { showPaidForDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        paidForOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val icon = when {
                                            option == "Только себя" -> Icons.Filled.Person
                                            option.startsWith("За: ") -> Icons.Filled.Person
                                            else -> Icons.Filled.Group
                                        }
                                        val tint = when {
                                            option == "Только себя" -> Color(0xFF2196F3)
                                            option.startsWith("За: ") -> Color(0xFF9C27B0)
                                            else -> Color(0xFF4CAF50)
                                        }

                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = tint,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(option)
                                    }
                                },
                                onClick = {
                                    selectedPaidFor = option
                                    showPaidForDropdown = false
                                }
                            )
                        }
                    }
                }

                if (trip.participants.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val selectedPayerName = trip.participants.find { it.id == selectedPayerId }?.name ?: "Не выбран"

                        Column {
                            OutlinedTextField(
                                value = selectedPayerName,
                                onValueChange = {},
                                label = { Text("Кто оплатил") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                enabled = false,
                                leadingIcon = {
                                    Icon(Icons.Filled.Person, null, tint = Color(0xFF2196F3))
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clickable { showPayerDropdown = true }
                                    .background(Color.Transparent)
                            )
                        }

                        DropdownMenu(
                            expanded = showPayerDropdown,
                            onDismissRequest = { showPayerDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            trip.participants.forEach { user ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Person,
                                                contentDescription = null,
                                                tint = Color(0xFF2196F3),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(user.name)
                                        }
                                    },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && amount.isNotBlank() && amount.toDoubleOrNull() != null &&
                        selectedPayerId.isNotBlank() && selectedCategory.isNotBlank()) {
                        val newExpense = Expense(
                            id = expense?.id ?: UUID.randomUUID().toString(),
                            title = title,
                            amount = amount.toDouble(),
                            category = selectedCategory,
                            payerId = selectedPayerId,
                            paidFor = selectedPaidFor
                        )
                        onSave(newExpense)
                    }
                },
                enabled = title.isNotBlank() &&
                        amount.isNotBlank() &&
                        amount.toDoubleOrNull() != null &&
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
    showAddCategoryDialog: Boolean,
    onAddCategory: (ExpenseCategory) -> Unit,
    onDeleteCategory: (ExpenseCategory) -> Unit,
    onDismissDialog: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
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
                        onDelete = { onDeleteCategory(category) }
                    )
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onAddCategory = onAddCategory,
            onDismiss = onDismissDialog
        )
    }
}

@Composable
fun CategoryItem(
    category: ExpenseCategory,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(32.dp)
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить категорию?") },
            text = {
                Text("Вы уверены, что хотите удалить категорию \"${category.name}\"? Все расходы в этой категории станут без категории.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
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

@Composable
fun AddCategoryDialog(
    onAddCategory: (ExpenseCategory) -> Unit,
    onDismiss: () -> Unit
) {
    var categoryName by remember { mutableStateOf("") }

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
                    if (categoryName.isNotBlank()) {
                        onAddCategory(ExpenseCategory(name = categoryName))
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

private fun getCategoriesForTrip(tripId: String): List<ExpenseCategory> {
    return emptyList()
}

private fun saveCategoriesForTrip(tripId: String, categories: List<ExpenseCategory>) {
}