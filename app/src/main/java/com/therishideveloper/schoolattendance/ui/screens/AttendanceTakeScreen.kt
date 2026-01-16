package com.therishideveloper.schoolattendance.ui.screens

import com.therishideveloper.schoolattendance.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therishideveloper.schoolattendance.ui.components.AttendanceBottomSummary
import com.therishideveloper.schoolattendance.ui.components.AttendanceContent
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.ui.components.showToast
import com.therishideveloper.schoolattendance.ui.viewmodels.AttendanceViewModel
import com.therishideveloper.schoolattendance.utils.ClassTypes
import com.therishideveloper.schoolattendance.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceTakeScreen(
    viewModel: AttendanceViewModel,
    classCode: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val attendanceState by viewModel.attendanceState.collectAsState()
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val classType = remember { ClassTypes.fromCode(classCode) }
    val isToday = remember(selectedDateStr) { selectedDateStr == DateTimeUtils.getTodayDate() }
    val displayDate = remember(selectedDateStr) { DateTimeUtils.getDisplayDate(selectedDateStr) }
    val msgAttendanceSuccess = stringResource(R.string.msg_attendance_success)

    LaunchedEffect(classCode, selectedDateStr) {
        viewModel.loadStudentsForAttendance(classCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column (verticalArrangement = Arrangement.spacedBy((-4).dp)){
                        Text(
                            stringResource(classType.stringRes),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(displayDate, fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null
                        )
                    }
                },
                colors = myTopBarColors()
            )
        },
        bottomBar = {
            AttendanceBottomSummary(attendanceState, isToday) { records ->
                viewModel.saveAttendance(records) {
                    showToast(context, msgAttendanceSuccess)
                    onBack()
                }
            }
        }
    ) { padding ->
        AttendanceContent(padding, attendanceState, isToday, viewModel)
    }
}