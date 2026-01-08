package com.therishideveloper.schoolattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therishideveloper.schoolattendance.data.local.model.ClassSummary
import com.therishideveloper.schoolattendance.ui.viewmodels.AttendanceViewModel
import com.therishideveloper.schoolattendance.utils.ClassTypes
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.ui.components.AttendanceDatePicker
import com.therishideveloper.schoolattendance.ui.components.CalendarHeaderCard
import com.therishideveloper.schoolattendance.ui.components.ClassGridCard
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceMainScreen(
    viewModel: AttendanceViewModel,
    onClassClick: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    val summaries by viewModel.classSummaries.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val dateDisplay = remember(selectedDate) { DateUtils.getDisplayDate(selectedDate) }

    AttendanceDatePicker(
        showDialog = showDatePicker,
        initialDate = selectedDate,
        onDismiss = { showDatePicker = false },
        onDateSelected = { viewModel.updateSelectedDate(it) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_attendance),
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            Icons.Default.Menu,
                            null
                        )
                    }
                },
                colors = myTopBarColors()
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            CalendarHeaderCard(dateDisplay) { showDatePicker = true }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(ClassTypes.getAll()) { classType ->
                    val summary = summaries.find { it.className == classType.code }
                        ?: ClassSummary(classType.code, 0, 0, 0)
                    ClassGridCard(classType, summary) {
                        viewModel.loadStudentsForAttendance(classType.code)
                        onClassClick(classType.code)
                    }
                }
            }
        }
    }
}
