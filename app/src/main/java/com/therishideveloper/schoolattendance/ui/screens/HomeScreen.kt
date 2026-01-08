package com.therishideveloper.schoolattendance.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    totalStudents: Int,
    presentCount: Int,
    absentCount: Int,
    onStartAttendance: () -> Unit // হাজিরা নিতে এই বাটনে ক্লিক করবেন
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("School 24", fontWeight = FontWeight.ExtraBold) },
                colors = myTopBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // আজকের তারিখ
            Text(
                text = DateUtils.getDisplayDate(DateUtils.getTodayDate()),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // বড় সামারি কার্ড
            MainSummaryCard(totalStudents, presentCount, absentCount)

            Spacer(modifier = Modifier.weight(1f))

            // হাজিরা শুরু করার বড় বাটন
            Button(
                onClick = onStartAttendance,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("আজকের হাজিরা শুরু করুন", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MainSummaryCard(total: Int, present: Int, absent: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("আজকের পরিস্থিতি", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CircularInfoItem("মোট ছাত্র", total.toString(), MaterialTheme.colorScheme.primary)
                CircularInfoItem("উপস্থিত", present.toString(), Color(0xFF4CAF50))
                CircularInfoItem("অনুপস্থিত", absent.toString(), Color(0xFFF44336))
            }
        }
    }
}

@Composable
fun CircularInfoItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // একটি সুন্দর গোল বর্ডারের ভেতরে সংখ্যাটি দেখাবে
        Box(
            modifier = Modifier
                .size(70.dp)
                .border(2.dp, color.copy(alpha = 0.3f), CircleShape)
                .padding(4.dp)
                .drawBehind {
                    drawCircle(
                        color = color.copy(alpha = 0.1f),
                        radius = size.minDimension / 2
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // নিচে ছোট করে লেখা (যেমন: মোট ছাত্র)
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
    }
}
