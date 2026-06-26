package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.CustomCategory
import com.example.data.model.Transaction
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ObsidianBlack),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    DashboardScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

// INR formatting helper
fun formatINR(amount: Double): String {
    val isNegative = amount < 0
    val absAmount = kotlin.math.abs(amount)
    val parts = String.format(Locale.US, "%.2f", absAmount).split(".")
    val integerPart = parts[0]
    val decimalPart = parts[1]

    val formattedInteger = if (integerPart.length <= 3) {
        integerPart
    } else {
        val lastThree = integerPart.substring(integerPart.length - 3)
        val remaining = integerPart.substring(0, integerPart.length - 3)
        val buffer = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            if (count > 0 && count % 2 == 0) {
                buffer.append(",")
            }
            buffer.append(remaining[i])
            count++
        }
        buffer.reverse().append(",").append(lastThree).toString()
    }

    val prefix = if (isNegative) "-₹" else "₹"
    return "$prefix$formattedInteger.$decimalPart"
}

@Composable
fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "AccountBalance" -> Icons.Default.AccountBalance
        "ShowChart" -> Icons.Default.ShowChart
        "LaptopMac" -> Icons.Default.LaptopMac
        "Redeem" -> Icons.Default.Redeem
        "Restaurant" -> Icons.Default.Restaurant
        "LocalMall" -> Icons.Default.LocalMall
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "SportsEsports" -> Icons.Default.SportsEsports
        "HomeWork" -> Icons.Default.HomeWork
        "MedicalInformation" -> Icons.Default.MedicalInformation
        "Payments" -> Icons.Default.Payments
        "Wallet" -> Icons.Default.Wallet
        "Category" -> Icons.Default.Category
        else -> Icons.Default.List
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: FinanceViewModel = viewModel(factory = FinanceViewModel.provideFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    // Collect Room Live States
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val balance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val expenseByCategory by viewModel.expenseByCategory.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("history") } // "history", "log", "categories"

    Column(
        modifier = modifier
            .background(ObsidianBlack)
            .padding(horizontal = 16.dp)
    ) {
        // Bento Custom Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                    color = BentoTextMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Aditya Sharma",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                EmeraldGreen,
                                GlowTeal
                            )
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .clickable { /* Profile placeholder */ }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AS",
                    color = ObsidianBlack,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }

        // Hero Stats Card: Linear Gradients + Bento Radiant Aura Glow
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCarbon)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                EmeraldGreen.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = androidx.compose.ui.geometry.Offset(800f, 0f),
                            radius = 500f
                        )
                    )
                    .border(
                        1.dp,
                        BentoBorder,
                        RoundedCornerShape(32.dp)
                    )
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Total Net Worth",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.15f))
                                    .border(1.dp, EmeraldGreen.copy(alpha = 0.25f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "+12.4%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = formatINR(balance),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Updated 2 mins ago",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = BentoTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Elegant Custom Circular Ring Donut Chart representation
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(90.dp)
                    ) {
                        Canvas(modifier = Modifier.size(80.dp)) {
                            val strokeWidthVal = 8.dp.toPx()
                            // Background track ring
                            drawArc(
                                color = Color(0xFF1E2130).copy(alpha = 0.5f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidthVal, cap = StrokeCap.Round)
                            )

                            // Dynamic arc calculation
                            val totalSum = totalIncome + totalExpense
                            if (totalSum > 0) {
                                val incomeRatio = (totalIncome / totalSum).toFloat()
                                val expenseRatio = (totalExpense / totalSum).toFloat()

                                val startAngleIncome = -90f
                                val sweepAngleIncome = incomeRatio * 360f
                                val sweepAngleExpense = expenseRatio * 360f

                                // Draw Income Arc
                                drawArc(
                                    color = EmeraldGreen,
                                    startAngle = startAngleIncome,
                                    sweepAngle = sweepAngleIncome,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidthVal, cap = StrokeCap.Round)
                                )

                                // Draw Expense Arc
                                drawArc(
                                    color = RadiantCoral,
                                    startAngle = startAngleIncome + sweepAngleIncome,
                                    sweepAngle = sweepAngleExpense,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidthVal, cap = StrokeCap.Round)
                                )
                            }
                        }

                        // Text Inside Ring describing savings efficacy
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val savingsPercent = if (totalIncome > 0) {
                                val value = ((totalIncome - totalExpense) / totalIncome) * 100
                                if (value < 0) 0 else value.toInt()
                            } else {
                                0
                            }
                            Text(
                                text = "SAVINGS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$savingsPercent%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Custom High-End Dynamic Bento Pill Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SlateCarbon)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Triple("history", "Home", Icons.Default.Home),
                Triple("log", "Quick Log", Icons.Default.Add),
                Triple("categories", "Categories", Icons.Default.Category)
            )

            tabs.forEach { tabItem ->
                val (tabId, label, icon) = tabItem
                val isSelected = activeTab == tabId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { activeTab = tabId }
                        .padding(vertical = 12.dp)
                        .testTag("tab_$tabId"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) ObsidianBlack else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ObsidianBlack else Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content Area with polished slide anim
        AnimatedContent(
            targetState = activeTab,
            transitionSpec = {
                slideInHorizontally(animationSpec = spring()) { if (targetState == "history") -it else it } togetherWith
                        slideOutHorizontally(animationSpec = spring()) { if (targetState == "history") it else -it }
            },
            modifier = Modifier.weight(1f)
        ) { targetTab ->
            when (targetTab) {
                "history" -> HistoryTab(
                    transactions = transactions,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    onDelete = { viewModel.deleteTransaction(it) }
                )
                "log" -> QuickLogTab(
                    categories = categories,
                    onSubmit = { amount, type, category, desc, date ->
                        viewModel.addTransaction(amount, type, category, desc, date)
                        activeTab = "history" // Move back on successful addition
                    }
                )
                "categories" -> CategoriesTab(
                    categories = categories,
                    viewModel = viewModel
                )
            }
        }
    }
}

// ============== BENTO INCOME-SPEND PROGRESS DISPLAY ==============
@Composable
fun IncomeSpendRow(totalIncome: Double, totalExpense: Double) {
    val totalSum = totalIncome + totalExpense
    val incomeRatio = if (totalSum > 0) (totalIncome / totalSum).toFloat() else 0.5f
    val spentRatio = if (totalSum > 0) (totalExpense / totalSum).toFloat() else 0.5f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Income Bento Block
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(SlateCarbon)
                .border(1.dp, BentoBorder, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "INCOME",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldGreen,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatINR(totalIncome),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(incomeRatio)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    EmeraldGreen,
                                    EmeraldGreen.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }
        }

        // Spent Bento Block
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(SlateCarbon)
                .border(1.dp, BentoBorder, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "SPENT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = RadiantCoral,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatINR(totalExpense),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(spentRatio)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    RadiantCoral,
                                    RadiantCoral.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }
        }
    }
}

// ============== BENTO MONTHLY VELOCITY VISUALIZATION CHART ==============
@Composable
fun MonthlyVelocityChart() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SlateCarbon)
            .border(1.dp, BentoBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monthly Velocity",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.3).sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(EmeraldGreen))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CSS-based stylized interactive bar graph from the Bento mockup design
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            val heightsFraction = listOf(0.40f, 0.60f, 0.45f, 0.85f, 0.55f, 0.30f, 0.70f)
            heightsFraction.forEachIndexed { idx, frac ->
                val isHeroBar = idx == 3 // 4th bar is emerald hero bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(frac)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            if (isHeroBar) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        EmeraldGreen,
                                        GlowTeal
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color.White.copy(alpha = 0.04f)
                                    )
                                )
                            }
                        )
                )
            }
        }
    }
}

// ============== HISTORY TAB COMPOSABLE ==============
@Composable
fun HistoryTab(
    transactions: List<Transaction>,
    totalIncome: Double,
    totalExpense: Double,
    onDelete: (Transaction) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Item 1: Income / Spend Row
        item {
            IncomeSpendRow(totalIncome = totalIncome, totalExpense = totalExpense)
        }

        // Item 2: Monthly Velocity Chart
        item {
            MonthlyVelocityChart()
        }

        // Item 3: Recent Activity Label Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldGreen,
                    modifier = Modifier.clickable { /* Interactions */ }
                )
            }
        }

        // Item 4+: List Entries
        if (transactions.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SlateCarbon)
                            .border(1.dp, BentoBorder, CircleShape)
                            .padding(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty Log info",
                            tint = TextSecondary,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No records logged yet",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap 'Quick Log' tab to record transactions.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(transactions, key = { it.id }) { txn ->
                TransactionItemRow(
                    transaction = txn,
                    onDelete = { onDelete(txn) }
                )
            }
        }

        // Scroll safety padding
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TransactionItemRow(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = df.format(Date(transaction.date))
    val isIncome = transaction.type == "INCOME"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCarbon)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BentoBorder, RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge in Rounded Square
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isIncome) EmeraldGreen.copy(alpha = 0.12f) else RadiantCoral.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(transaction.category),
                    contentDescription = transaction.category,
                    tint = if (isIncome) EmeraldGreen else RadiantCoral,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description.ifEmpty { transaction.category },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = transaction.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoTextMuted
                    )
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.1f)
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = BentoTextMuted
                    )
                }
            }

            // Price metric + Delete button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${if (isIncome) "+" else "-"} ₹${String.format(Locale.US, "%,.2f", transaction.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncome) EmeraldGreen else RadiantCoral
                )
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete entry",
                    tint = RadiantCoral.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onDelete)
                        .testTag("delete_txn_${transaction.id}")
                )
            }
        }
    }
}

// ============== QUICK LOG TAB (FORM) ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogTab(
    categories: List<CustomCategory>,
    onSubmit: (amount: Double, type: String, category: String, description: String, date: Long) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var txnType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var selectedCategory by remember { mutableStateOf("") }
    var descriptionStr by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var expandedDropdown by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val filteredCategories = categories.filter { it.type == txnType }

    // Auto select first entry of filtered list if current selected is invalid
    LaunchedEffect(filteredCategories) {
        if (filteredCategories.isNotEmpty()) {
            selectedCategory = filteredCategories[0].name
        } else {
            selectedCategory = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "RECORD TRANSACTION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        // Segmented Type Option Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SlateCarbon)
                .padding(4.dp)
        ) {
            // Expense Pill Select option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (txnType == "EXPENSE") RadiantCoral else Color.Transparent)
                    .clickable { txnType = "EXPENSE" }
                    .padding(vertical = 12.dp)
                    .testTag("form_type_expense"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Expense",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (txnType == "EXPENSE") ObsidianBlack else Color.White
                )
            }

            // Income Pill Select option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (txnType == "INCOME") EmeraldGreen else Color.Transparent)
                    .clickable { txnType = "INCOME" }
                    .padding(vertical = 12.dp)
                    .testTag("form_type_income"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Income",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (txnType == "INCOME") ObsidianBlack else Color.White
                )
            }
        }

        // Beautiful Display Amount Input
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SlateCarbon)
                .border(1.dp, Color(0xFF232535), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "TRANSACTION VALUE (INR)",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldPremium,
                    modifier = Modifier.padding(end = 6.dp)
                )
                TextField(
                    value = amountStr,
                    onValueChange = { input ->
                        // Only numerical filters
                        if (input.all { it.isDigit() || it == '.' }) {
                            amountStr = input
                        }
                    },
                    placeholder = {
                        Text(
                            text = "0.00",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextSecondary.copy(alpha = 0.4f)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_amount_input"),
                    singleLine = true
                )
            }
        }

        // Category Menu Dropdown select
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Category Selector Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SlateCarbon)
                    .border(1.dp, Color(0xFF232535), RoundedCornerShape(16.dp))
                    .clickable { expandedDropdown = true }
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "CLASSIFICATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCategory.ifEmpty { "Select..." },
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown toggle icon",
                            tint = GoldPremium,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier
                        .background(SlateCarbon)
                        .border(1.dp, DarkGray, RoundedCornerShape(8.dp))
                ) {
                    if (filteredCategories.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No custom slots. Add one in stats tab", color = TextSecondary, fontSize = 12.sp) },
                            onClick = { expandedDropdown = false }
                        )
                    } else {
                        filteredCategories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(category.iconName),
                                            contentDescription = category.name,
                                            tint = GoldPremium,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(text = category.name, color = Color.White)
                                    }
                                },
                                onClick = {
                                    selectedCategory = category.name
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Calendar Selection Box Area
            val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SlateCarbon)
                    .border(1.dp, Color(0xFF232535), RoundedCornerShape(16.dp))
                    .clickable { showDatePickerDialog = true }
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "RECORD DATE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = df.format(Date(selectedDate)),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Date icon status",
                            tint = GlowTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Description Input Form Field
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SlateCarbon)
                .border(1.dp, Color(0xFF232535), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "NARRATION / PURPOSE",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            TextField(
                value = descriptionStr,
                onValueChange = { descriptionStr = it },
                placeholder = {
                    Text(
                        text = "Describe transaction notes here...",
                        color = TextSecondary.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 14.sp,
                    color = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_description_input"),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Confirm Log major CTA Button
        val isButtonActive = amountStr.isNotEmpty() && selectedCategory.isNotEmpty()
        Button(
            onClick = {
                val valueAmount = amountStr.toDoubleOrNull()
                if (valueAmount != null && valueAmount > 0) {
                    onSubmit(
                        valueAmount,
                        txnType,
                        selectedCategory,
                        descriptionStr,
                        selectedDate
                    )
                }
            },
            enabled = isButtonActive,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .border(
                    1.dp,
                    if (isButtonActive) GoldPremium else Color.Transparent,
                    RoundedCornerShape(16.dp)
                )
                .testTag("submit_transaction_btn"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (txnType == "EXPENSE") RadiantCoral else EmeraldGreen,
                disabledContainerColor = DarkGray
            )
        ) {
            Text(
                text = "PROCEED TRANSACTION",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = if (isButtonActive) ObsidianBlack else TextSecondary
            )
        }

        // Date Picker M3 Dialog integration
        if (showDatePickerDialog) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate
            )
            DatePickerDialog(
                onDismissRequest = { showDatePickerDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                selectedDate = it
                            }
                            showDatePickerDialog = false
                        }
                    ) {
                        Text("SELECT", color = GoldPremium, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePickerDialog = false }) {
                        Text("CANCEL", color = TextSecondary)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

// ============== CATEGORIES TAB COMPOSABLE ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesTab(
    categories: List<CustomCategory>,
    viewModel: FinanceViewModel
) {
    var newCategoryName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var selectedIconName by remember { mutableStateOf("LaptopMac") }

    val availableIcons = listOf(
        Pair("Restaurant", Icons.Default.Restaurant),
        Pair("LocalMall", Icons.Default.LocalMall),
        Pair("DirectionsCar", Icons.Default.DirectionsCar),
        Pair("ShoppingCart", Icons.Default.ShoppingCart),
        Pair("SportsEsports", Icons.Default.SportsEsports),
        Pair("HomeWork", Icons.Default.HomeWork),
        Pair("MedicalInformation", Icons.Default.MedicalInformation),
        Pair("Payments", Icons.Default.Payments),
        Pair("AccountBalance", Icons.Default.AccountBalance),
        Pair("ShowChart", Icons.Default.ShowChart),
        Pair("LaptopMac", Icons.Default.LaptopMac),
        Pair("Redeem", Icons.Default.Redeem),
        Pair("Wallet", Icons.Default.Wallet)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section: Add Custom Category
        Text(
            text = "ASSEMBLE NEW CATEGORY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCarbon)
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, Color(0xFF232535), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name Input Field
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianBlack)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "CATEGORY TITLE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    TextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        placeholder = { Text("e.g. Subscriptions, Food delivery...", color = TextSecondary.copy(alpha = 0.4f), fontSize = 12.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color.White),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_title_input")
                    )
                }

                // Type Option Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedType == "EXPENSE") RadiantCoral.copy(alpha = 0.2f) else DarkGray.copy(alpha = 0.3f))
                            .border(1.dp, if (selectedType == "EXPENSE") RadiantCoral else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { selectedType = "EXPENSE" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Expense Slot", color = if (selectedType == "EXPENSE") RadiantCoral else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedType == "INCOME") EmeraldGreen.copy(alpha = 0.2f) else DarkGray.copy(alpha = 0.3f))
                            .border(1.dp, if (selectedType == "INCOME") EmeraldGreen else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { selectedType = "INCOME" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Income Slot", color = if (selectedType == "INCOME") EmeraldGreen else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Icon selection bar
                Text(text = "SELECT DESIGN ICON", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableIcons) { iconPair ->
                        val (iconId, iconVec) = iconPair
                        val isSelected = selectedIconName == iconId
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) GoldPremium else ObsidianBlack)
                                .border(1.dp, if (isSelected) Color.Transparent else DarkGray, CircleShape)
                                .clickable { selectedIconName = iconId }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = iconId,
                                tint = if (isSelected) ObsidianBlack else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // SUBMIT CATEGORY SLOT
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addCategory(
                                name = newCategoryName.trim(),
                                type = selectedType,
                                iconName = selectedIconName
                            )
                            newCategoryName = "" // Reset state
                        }
                    },
                    enabled = newCategoryName.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPremium,
                        disabledContainerColor = DarkGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_category_btn")
                ) {
                    Text("REGISTER CATEGORY", color = ObsidianBlack, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Section: Active Categories
        Text(
            text = "CREATED CATEGORIES GRID",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCarbon)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianBlack),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(cat.iconName),
                                    contentDescription = cat.name,
                                    tint = GoldPremium,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(text = cat.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = cat.type,
                                    color = if (cat.type == "INCOME") EmeraldGreen else RadiantCoral,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Delete specific Category if custom created
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete custom category",
                            tint = RadiantCoral.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { viewModel.deleteCategory(cat) }
                                .testTag("delete_category_${cat.id}")
                        )
                    }
                }
            }
        }
    }
}
