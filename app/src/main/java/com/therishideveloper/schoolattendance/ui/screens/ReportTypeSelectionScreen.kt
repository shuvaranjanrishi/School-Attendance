package com.therishideveloper.schoolattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.ui.components.AttendanceTable
import com.therishideveloper.schoolattendance.ui.components.EmptyStateMessage
import com.therishideveloper.schoolattendance.ui.components.LoadingOverlay
import com.therishideveloper.schoolattendance.ui.components.ReportDownloadDialog
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.ui.viewmodels.ReportViewModel
import com.therishideveloper.schoolattendance.utils.DateUtils.getFormattedDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportTypeSelectionScreen(
    onMenuClick: () -> Unit,
    onClassWiseClick: () -> Unit,
    onStudentWiseClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("রিপোর্ট সেকশন", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = null)
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
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "কোন ধরণের রিপোর্ট দেখতে চান?",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ১. ক্লাস অনুযায়ী মাসিক উপস্থিতি
            ReportOptionCard(
                title = "ক্লাস অনুযায়ী মাসিক উপস্থিতি",
                description = "প্রতিটি ক্লাসের উপস্থিতির সামারি বা সারাংশ দেখুন",
                icon = Icons.Default.Assessment,
                color = Color(0xFF1976D2),
                onClick = onClassWiseClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ২. স্টুডেন্ট অনুযায়ী মাসিক উপস্থিতি
            ReportOptionCard(
                title = "স্টুডেন্ট অনুযায়ী মাসিক উপস্থিতি",
                description = "ব্যক্তিগতভাবে ছাত্র-ছাত্রীদের হাজিরা ও ক্যালেন্ডার দেখুন",
                icon = Icons.Default.Person,
                color = Color(0xFF388E3C),
                onClick = onStudentWiseClick
            )
        }
    }
}

@Composable
fun ReportOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(15.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(text = title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(text = description, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}