package com.therishideveloper.schoolattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.ui.components.EmptyStateView
import com.therishideveloper.schoolattendance.ui.components.ReportCard
import com.therishideveloper.schoolattendance.ui.components.ReportTopBar
import com.therishideveloper.schoolattendance.ui.viewmodels.ReportViewModel
import com.therishideveloper.schoolattendance.utils.DateUtils.getFormattedDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    onMenuClick: () -> Unit,
    onClassClick: (String) -> Unit
) {
    val currentMonth by viewModel.selectedMonth.collectAsState()
    val currentYear by viewModel.selectedYear.collectAsState()
    val displayDate =
        remember(currentMonth, currentYear) { getFormattedDate(currentMonth, currentYear) }
    val reportData by viewModel.monthlyReport.collectAsState()

    Scaffold(
        topBar = {
            ReportTopBar(
                title = stringResource(R.string.monthly_report_title),
                subTitle = displayDate,
                onMenuClick = onMenuClick
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (reportData.isEmpty()) {
                EmptyStateView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reportData) { data ->
                        ReportCard(
                            report = data,
                            onClick = { onClassClick(data.className) }
                        )
                    }
                }
            }
        }
    }
}
