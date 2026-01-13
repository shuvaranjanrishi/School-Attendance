package com.therishideveloper.schoolattendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therishideveloper.schoolattendance.ui.components.AttendanceCalendarGrid
import com.therishideveloper.schoolattendance.ui.components.CircularProgress
import com.therishideveloper.schoolattendance.ui.components.LegendItem
import com.therishideveloper.schoolattendance.ui.components.LoadingOverlay
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.ui.viewmodels.ReportViewModel
import com.therishideveloper.schoolattendance.utils.ClassTypes
import com.therishideveloper.schoolattendance.utils.DateUtils.getFormattedDate
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.utils.localizeDigitsAndLabels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportStudentWiseDetailsScreen(
    studentId: Int,
    viewModel: ReportViewModel,
    onBack: () -> Unit
) {
    val attendanceRecords by viewModel.detailedRecords.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()
    val year by viewModel.selectedYear.collectAsState()

    val displayDate = remember(month, year) { getFormattedDate(month, year) }

    // ViewModel থেকে হিসাব করা ডাটা নিয়ে আসা
    val (studentSummary, counts) = remember(attendanceRecords, studentId) {
        viewModel.getStudentDetails(studentId, attendanceRecords)
    }

    val specificStudentRecords = remember(attendanceRecords, studentId) {
        attendanceRecords.filter { it.studentId == studentId }
    }
    val classType = remember(studentSummary?.className) {
        ClassTypes.fromCode(studentSummary?.className ?: "")
    }
    val readableClassName = stringResource(id = classType.stringRes).replace(
        "Class",
        ""
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.attendance_calender), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(displayDate, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = myTopBarColors()
            )
        },
        bottomBar = {
            studentSummary?.let { summary ->
                Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.downloadStudentCalendarPdf(
                                    summary.copy(className = readableClassName),
                                        displayDate,
                                        specificStudentRecords
                                    )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(Modifier.width(5.dp))
                            Text(stringResource(R.string.download_pdf))
                        }

                        // ২. শেয়ার বাটন
                        OutlinedButton(
                            onClick = {
                                viewModel.shareStudentCalendarPdf(
                                    summary.copy(className = readableClassName),
                                    displayDate,
                                    specificStudentRecords
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(5.dp))
                            Text(stringResource(R.string.share_pdf))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (specificStudentRecords.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.no_records_found), color = Color.Gray)
                    }
                } else {
                    // ২. স্টুডেন্ট প্রোফাইল হেডার (সঠিকভাবে রূপান্তর করা হয়েছে)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = studentSummary?.name ?: "Unknown",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text =  stringResource(R.string.label_roll)+": ${studentSummary?.rollNo?.localizeDigitsAndLabels()}",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(4.dp)
                                    .background(Color.LightGray, CircleShape)
                            )

                            // ৩. এখানে stringResource ব্যবহার করা হয়েছে
                            Text(
                                text = stringResource(R.string.label_class)+": $readableClassName",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // স্ট্যাটাস কার্ড
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            StatusRow(
                                label = stringResource(R.string.total_days),
                                value = counts.first.toString().localizeDigitsAndLabels(),
                                color = Color.Black
                            )
                            StatusRow(
                                label = stringResource(R.string.present),
                                value = counts.second.toString().localizeDigitsAndLabels(),
                                color = Color(0xFF2E7D32)
                            )
                            StatusRow(
                                label = stringResource(R.string.absent),
                                value = counts.third.toString().localizeDigitsAndLabels(),
                                color = Color(0xFFD32F2F)
                            )
                        }
                        VerticalDivider(
                            Modifier.height(50.dp),
                            color = Color.Gray,
                            thickness = 1.dp
                        )
                        CircularProgress(
                            percentage = studentSummary?.attendancePercentage ?: 0f,
                            size = 80,
                            strokeWidth = 8
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        LegendItem(label = stringResource(R.string.present), color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(16.dp))
                        LegendItem(label = stringResource(R.string.absent), color = Color(0xFFD32F2F))
                        Spacer(modifier = Modifier.width(16.dp))
                        LegendItem(label = stringResource(R.string.no_class), color = Color.LightGray)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    AttendanceCalendarGrid(records = specificStudentRecords)
                }
            }

            if (isDownloading) {
                LoadingOverlay(isLoading = true, message = stringResource(R.string.pdf_creating_msg))
            }
        }
    }
}
