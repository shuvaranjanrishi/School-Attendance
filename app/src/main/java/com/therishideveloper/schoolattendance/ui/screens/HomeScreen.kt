package com.therishideveloper.schoolattendance.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.data.local.model.DashboardData
import com.therishideveloper.schoolattendance.ui.components.SummaryCard
import com.therishideveloper.schoolattendance.ui.components.VerticalSpace
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.ui.viewmodels.HomeViewModel
import com.therishideveloper.schoolattendance.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onMenuClick: () -> Unit,
) {
    val todayData by homeViewModel.dashboardSummary.collectAsState(initial = DashboardData(0, 0, 0))
    val monthData by homeViewModel.monthSummary.collectAsState(initial = DashboardData(0, 0, 0))
    val yearData by homeViewModel.yearSummary.collectAsState(initial = DashboardData(0, 0, 0))
    val selectedDate by homeViewModel.selectedDate.collectAsState()
    val displayDate = remember(selectedDate) { DateUtils.getDisplayDate(selectedDate) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.dashboard), fontWeight = FontWeight.ExtraBold)
                        Text(
                            text = displayDate,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu))
                    }
                },
                colors = myTopBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), // স্ক্রল করার জন্য
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VerticalSpace(12)
            SummaryCard(
                stringResource(R.string.today_attendance),
                todayData.total,
                todayData.present,
                todayData.absent
            )
            SummaryCard(
                stringResource(R.string.monthly_attendance),
                monthData.total,
                monthData.present,
                monthData.absent
            )
            SummaryCard(
                stringResource(R.string.yearly_attendance),
                yearData.total,
                yearData.present,
                yearData.absent
            )
        }
    }
}

