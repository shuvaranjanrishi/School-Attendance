package com.therishideveloper.schoolattendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import com.therishideveloper.schoolattendance.data.local.model.MonthlyReportModel
import com.therishideveloper.schoolattendance.utils.ClassTypes
import kotlin.collections.find


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportTopBar(title: String, subTitle: String, onMenuClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(subTitle, fontSize = 12.sp, color = Color.White.copy(0.8f))
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = null)
            }
        },
        colors = myTopBarColors()
    )
}

@Composable
fun ReportCard(report: MonthlyReportModel, onClick: () -> Unit) {
    val classType = remember { ClassTypes.fromCode(report.className) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(classType.stringRes),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${String.format("%.1f", report.percentage)}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(0.3f)) // Label space
                TableHeaderCell(
                    text = stringResource(R.string.total),
                    modifier = Modifier.weight(0.23f),
                    color = Color.Gray
                )
                TableHeaderCell(
                    text = stringResource(R.string.present),
                    modifier = Modifier.weight(0.23f),
                    color = Color(0xFF2E7D32)
                )
                TableHeaderCell(
                    text = stringResource(R.string.absent),
                    modifier = Modifier.weight(0.23f),
                    color = Color(0xFFD32F2F)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            GenderDataRow(
                label = stringResource(R.string.male),
                t = report.boysTotal,
                p = report.boysPresent,
                a = report.boysAbsent
            )
            Spacer(modifier = Modifier.height(6.dp))
            GenderDataRow(
                label = stringResource(R.string.female),
                t = report.girlsTotal,
                p = report.girlsPresent,
                a = report.girlsAbsent
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp)
            GenderDataRow(
                label = stringResource(R.string.total),
                t = report.totalAttendance,
                p = report.presentCount,
                a = report.absentCount,
                isBold = true
            )
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier, color: Color) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = color
    )
}

@Composable
fun GenderDataRow(label: String, t: Int, p: Int, a: Int, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.3f),
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isBold) MaterialTheme.colorScheme.onSurface else Color.Gray
        )
        DataValueCell(text = t.toString(), modifier = Modifier.weight(0.23f), isBold = isBold)
        DataValueCell(text = p.toString(), modifier = Modifier.weight(0.23f), isBold = isBold)
        DataValueCell(text = a.toString(), modifier = Modifier.weight(0.23f), isBold = isBold)
    }
}

@Composable
private fun DataValueCell(text: String, modifier: Modifier, isBold: Boolean) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = TextAlign.Center,
        fontSize = 14.sp,
        fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Normal
    )
}

@Composable
fun EmptyStateView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.no_data),
            color = Color.Gray,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun AttendanceTable(attendanceData: List<AttendanceEntity>) {
    val scrollState = rememberScrollState()
    val groupedData = attendanceData.groupBy { it.studentName }
    val days = (1..31).toList()

    Column(modifier = Modifier.fillMaxSize()) {
        // টেবিল হেডার
        Row(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            TableCell(text = "Student Name", width = 120.dp, isHeader = true)
            Row(modifier = Modifier.horizontalScroll(scrollState)) {
                days.forEach { day ->
                    TableCell(text = day.toString(), width = 35.dp, isHeader = true)
                }
            }
        }

        // ছাত্রছাত্রীদের তালিকা
        LazyColumn {
            items(groupedData.keys.toList()) { studentName ->
                val studentRecords = groupedData[studentName] ?: emptyList()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TableCell(text = studentName, width = 120.dp)
                    Row(modifier = Modifier.horizontalScroll(scrollState)) {
                        days.forEach { day ->
                            val record = studentRecords.find {
                                it.date.startsWith(
                                    String.format(
                                        "%02d",
                                        day
                                    )
                                )
                            }
                            val status = when (record?.status) {
                                "Present" -> "P"
                                "Absent" -> "A"
                                else -> "-"
                            }
                            StatusCell(status = status)
                        }
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun TableCell(text: String, width: androidx.compose.ui.unit.Dp, isHeader: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(8.dp),
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        fontSize = 12.sp,
        maxLines = 1,
        textAlign = if (isHeader) TextAlign.Center else TextAlign.Start
    )
}

@Composable
fun StatusCell(status: String) {
    val color = when (status) {
        "P" -> Color(0xFF2E7D32)
        "A" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }
    Text(
        text = status,
        modifier = Modifier
            .width(35.dp)
            .padding(8.dp),
        textAlign = TextAlign.Center,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    )
}

@Composable
fun EmptyStateMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = stringResource(R.string.no_data), color = Color.Gray)
    }
}

@Composable
fun ReportDownloadDialog(
    className: String,
    date: String,
    isExcel: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.download_confirm_title)) },
        text = {
            Column {
                val format = if (isExcel) "Excel" else "PDF"
                Text(stringResource(R.string.download_confirm_msg, "$className ($date) [$format]"))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.file_location_msg),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text(stringResource(R.string.download_btn)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_btn)) }
        }
    )
}