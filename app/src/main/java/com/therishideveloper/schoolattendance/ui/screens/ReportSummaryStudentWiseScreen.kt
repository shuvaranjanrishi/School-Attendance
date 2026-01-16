package com.therishideveloper.schoolattendance.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.data.local.model.StudentMonthlySummary
import com.therishideveloper.schoolattendance.ui.components.CircularProgress
import com.therishideveloper.schoolattendance.ui.components.EmptyStateView
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.ui.viewmodels.ReportViewModel
import com.therishideveloper.schoolattendance.utils.ClassTypes
import com.therishideveloper.schoolattendance.utils.Constants
import com.therishideveloper.schoolattendance.utils.DateTimeUtils.getFormattedDate
import com.therishideveloper.schoolattendance.utils.localizeDigitsAndLabels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSummaryStudentWiseScreen(
    viewModel: ReportViewModel,
    onMenuClick: () -> Unit,
    onStudentClick: (Int) -> Unit
) {
    val currentMonth by viewModel.selectedMonth.collectAsState()
    val currentYear by viewModel.selectedYear.collectAsState()
    val displayDate = remember(currentMonth, currentYear) {
        getFormattedDate(currentMonth, currentYear)
    }
    val studentSummaries by viewModel.filteredStudentSummaries.collectAsState()
    val selectedClass by viewModel.selectedClassFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy((-4).dp)) {
                        Text(
                            stringResource(R.string.student_report),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(displayDate, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showMonthPicker = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color.White)
                    }
                },
                colors = myTopBarColors()
            )
        }
    ) { paddingValues ->
        if (showMonthPicker) {
            MonthYearPickerDialog(
                initialMonth = currentMonth,
                initialYear = currentYear,
                onDismiss = { showMonthPicker = false },
                onDateSelected = { month, year ->
                    viewModel.updateDate(month, year) // ভিউমডেলের ডাটা আপডেট হবে
                    showMonthPicker = false
                }
            )
        }

        Column(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 3.dp
                )
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearch(it) },
                    placeholder = { Text(stringResource(R.string.hint_search_student)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, null) }
                )


                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedClass == "All",
                        onClick = { viewModel.onClassFilterChanged("All") },
                        label = { Text(stringResource(R.string.all)) },
                        modifier = Modifier.padding(end = 6.dp)
                    )

                    ClassTypes.getAll().forEach { classType ->
                        val className = stringResource(id = classType.stringRes)
                        FilterChip(
                            selected = selectedClass == classType.code,
                            onClick = { viewModel.onClassFilterChanged(classType.code) },
                            label = { Text(className) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }

            // স্টুডেন্ট লিস্ট সেকশন
            if (studentSummaries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 3.dp,
                        bottom = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(studentSummaries) { report ->
                        StudentReportCard(
                            summary = report,
                            onClick = { onStudentClick(report.studentId) }
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("LocalContextResourcesRead", "DiscouragedApi")
@Composable
fun MonthYearPickerDialog(
    initialMonth: String,
    initialYear: String,
    onDismiss: () -> Unit,
    onDateSelected: (String, String) -> Unit
) {
    val context = LocalContext.current
    val monthCodes = Constants.MONTH_CODES
    val yearList = remember { Constants.getDynamicYearRange() }
    val monthDisplayNames = remember(monthCodes, context) {
        monthCodes.map { code ->
            val resName = "${Constants.MONTH_PREFIX}$code"
            val resId = context.resources.getIdentifier(resName, "string", context.packageName)
            if (resId != 0) context.getString(resId) else code
        }
    }
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedYear by remember { mutableStateOf(initialYear) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_month_year),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1.3f)) {
                    ScrollablePicker(
                        items = monthCodes,
                        displayItems = monthDisplayNames,
                        selectedItem = selectedMonth,
                        onItemSelected = { selectedMonth = it }
                    )
                }
                Box(modifier = Modifier.weight(0.9f)) {
                    ScrollablePicker(
                        items = yearList,
                        displayItems = yearList,
                        selectedItem = selectedYear,
                        onItemSelected = { selectedYear = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDateSelected(selectedMonth, selectedYear) }) {
                Text(stringResource(R.string.ok_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollablePicker(
    items: List<String>,
    displayItems: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedIndex = remember(selectedItem, items) {
        items.indexOf(selectedItem).coerceAtLeast(0)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            // displayItems থেকে ইউজারের ভাষায় নাম দেখানো হচ্ছে
            value = displayItems.getOrElse(selectedIndex) { "" },
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(), // এটি খুব গুরুত্বপূর্ণ
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = {
                        Text(text = displayItems[index], fontSize = 14.sp)
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun StudentReportCard(
    summary: StudentMonthlySummary, onClick: () -> Unit
) {
    val classType = remember(summary.classCode) {
        ClassTypes.fromCode(summary.classCode)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // ১. প্রথম রো: নাম এবং ডান পাশে অ্যারো আইকন
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View Details",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = stringResource(R.string.label_roll) + ": ${summary.rollNo.localizeDigitsAndLabels()}  •  " + stringResource(
                    R.string.label_class
                ) + ": ${
                    stringResource(id = classType.stringRes).replace(
                        "Class", ""
                    )
                }", style = MaterialTheme.typography.bodyMedium, color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatusRow(
                        label = stringResource(R.string.total_days),
                        value = summary.totalDays.toString().localizeDigitsAndLabels(),
                        color = Color.Gray
                    )
                    StatusRow(
                        label = stringResource(R.string.present),
                        value = summary.presentCount.toString().localizeDigitsAndLabels(),
                        color = Color(0xFF2E7D32)
                    )
                    StatusRow(
                        label = stringResource(R.string.absent),
                        value = summary.absentCount.toString().localizeDigitsAndLabels(),
                        color = Color(0xFFD32F2F)
                    )
                }

                VerticalDivider(Modifier.size(50.dp), color = Color.Gray, thickness = 1.dp)
                CircularProgress(
                    percentage = summary.attendancePercentage,
                    size = 65,
                    strokeWidth = 6
                )
            }
        }
    }
}

@Composable
fun StatusRow(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = CircleShape,
            color = color
        ) {}
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", fontSize = 13.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}