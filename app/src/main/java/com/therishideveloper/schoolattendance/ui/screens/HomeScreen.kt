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
    val context = LocalContext.current
    val todayData by homeViewModel.dashboardSummary.collectAsState(initial = DashboardData(0, 0, 0))
    val monthData by homeViewModel.monthSummary.collectAsState(initial = DashboardData(0, 0, 0))
    val yearData by homeViewModel.yearSummary.collectAsState(initial = DashboardData(0, 0, 0))

    // ১. আপনার অন্য স্ক্রিনগুলোর মতো হুবহু StateFlow থেকে ডেটা নিন
    val selectedDate by homeViewModel.selectedDate.collectAsState()

    // ২. ঠিক যেভাবে আপনি AttendanceTakeScreen-এ লিখেছেন
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

@Composable
fun SummaryCard(
    title: String,
    total: Int,
    present: Int,
    absent: Int
) {
    val totalRecords = present + absent
    val percentage = if (totalRecords > 0) {
        ((present.toFloat() / totalRecords.toFloat()) * 100).toInt()
    } else {
        0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ১. বাম পাশে কলাম অনুযায়ী ছোট সার্কেল এবং লেবেল
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SmallInfoRow(
                        stringResource(R.string.label_total_students),
                        total.toString(),
                        MaterialTheme.colorScheme.primary
                    )
                    SmallInfoRow(
                        stringResource(R.string.label_total_present),
                        present.toString(),
                        Color(0xFF4CAF50)
                    )
                    SmallInfoRow(
                        stringResource(R.string.label_total_absent),
                        absent.toString(),
                        Color(0xFFF44336)
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(100.dp)
                        .padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = Color.LightGray.copy(alpha = 0.5f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = percentage.toFloat() / 100f,
                            modifier = Modifier.size(85.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.8f),
                            strokeWidth = 8.dp,
                            trackColor = Color(0xFFE0E0E0)
                        )
                        Text(
                            text = "$percentage%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.attendance_rate),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun SmallInfoRow(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(35.dp)
                .border(1.5.dp, color.copy(alpha = 0.4f), CircleShape)
                .drawBehind {
                    drawCircle(color = color.copy(alpha = 0.1f), radius = size.minDimension / 2)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
    }
}

//@Composable
//fun CircularInfoItem(
//    label: String,
//    value: String,
//    color: Color
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // একটি সুন্দর গোল বর্ডারের ভেতরে সংখ্যাটি দেখাবে
//        Box(
//            modifier = Modifier
//                .size(70.dp)
//                .border(2.dp, color.copy(alpha = 0.3f), CircleShape)
//                .padding(4.dp)
//                .drawBehind {
//                    drawCircle(
//                        color = color.copy(alpha = 0.1f),
//                        radius = size.minDimension / 2
//                    )
//                },
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = value,
//                fontSize = 20.sp,
//                fontWeight = FontWeight.ExtraBold,
//                color = color
//            )
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // নিচে ছোট করে লেখা (যেমন: মোট ছাত্র)
//        Text(
//            text = label,
//            fontSize = 12.sp,
//            fontWeight = FontWeight.Medium,
//            color = Color.Gray
//        )
//    }
//}
