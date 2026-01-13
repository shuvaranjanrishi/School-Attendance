package com.therishideveloper.schoolattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.ui.components.AttendanceTable
import com.therishideveloper.schoolattendance.ui.components.EmptyStateMessage
import com.therishideveloper.schoolattendance.ui.components.LoadingOverlay
import com.therishideveloper.schoolattendance.ui.components.ReportDownloadDialog
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.ui.viewmodels.ReportViewModel
import com.therishideveloper.schoolattendance.utils.ClassTypes
import com.therishideveloper.schoolattendance.utils.DateUtils.getFormattedDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassWiseDetailsReportScreen(
    onBack: () -> Unit,
    className: String,
    month: String,
    year: String,
    viewModel: ReportViewModel
) {
    val displayDate = remember(month, year) { getFormattedDate(month, year) }
    val allAttendanceData by viewModel.detailedRecords.collectAsState()
    val filteredData = remember(allAttendanceData, className) {
        allAttendanceData.filter { it.className == className }
    }
    val isDownloading by viewModel.isDownloading.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showDownloadConfirm by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableIntStateOf(0) }
    val classTitle = remember(className) {
        val classType = ClassTypes.fromCode(className)
        classType.stringRes
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${stringResource(id = classTitle)} - $displayDate",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Download")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Download as PDF") },
                                onClick = {
                                    showMenu = false; selectedFormat = 0; showDownloadConfirm = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Download as Excel (.xlsx)") },
                                onClick = {
                                    showMenu = false; selectedFormat = 1; showDownloadConfirm = true
                                }
                            )
                        }
                    }
                },
                colors = myTopBarColors()
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (filteredData.isEmpty()) {
                EmptyStateMessage()
            } else {
                AttendanceTable(filteredData)
            }

            if (showDownloadConfirm) {
                ReportDownloadDialog(
                    className = className,
                    date = displayDate,
                    isExcel = selectedFormat == 1,
                    onConfirm = {
                        showDownloadConfirm = false
                        if (selectedFormat == 0) viewModel.generateMonthlyReportPdf(
                            className,
                            displayDate
                        )
                        else viewModel.generateMonthlyReportExcel(className, displayDate)
                    },
                    onDismiss = { showDownloadConfirm = false }
                )
            }

            LoadingOverlay(
                isLoading = isDownloading,
                message = stringResource(id = R.string.pdf_creating_msg)
            )
        }
    }
}