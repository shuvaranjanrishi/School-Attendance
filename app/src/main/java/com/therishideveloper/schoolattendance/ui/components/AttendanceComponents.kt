package com.therishideveloper.schoolattendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import com.therishideveloper.schoolattendance.data.local.model.ClassSummary
import com.therishideveloper.schoolattendance.ui.viewmodels.AttendanceViewModel
import com.therishideveloper.schoolattendance.utils.ClassTypes
import com.therishideveloper.schoolattendance.utils.Result


@Composable
fun CalendarHeaderCard(dateDisplay: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.4f
            )
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(dateDisplay, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.label_change_date),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.btn_change),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ClassGridCard(
    classType: ClassTypes,
    summary: ClassSummary,
    onClick: () -> Unit
) {
    val isTaken = summary.isTaken

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTaken) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // স্ট্যাটাস ডট
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (isTaken) Color(0xFF2E7D32) else Color.LightGray,
                            CircleShape
                        )
                )
            }

            Text(
                text = stringResource(classType.stringRes),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isTaken) Color(0xFF2E7D32) else Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow(
                stringResource(R.string.label_total_students),
                summary.totalStudents.toString(),
                Color.DarkGray
            )
            SummaryRow(
                stringResource(R.string.label_total_present),
                summary.totalPresent.toString(),
                Color(0xFF2E7D32)
            )
            SummaryRow(
                stringResource(R.string.label_total_absent),
                summary.totalAbsent.toString(),
                Color.Red
            )
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun AttendanceStudentRow(
    name: String,
    roll: String,
    status: String,
    enabled: Boolean = true,
    onToggle: () -> Unit
) {
    val isPresent = status == "Present"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPresent) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    stringResource(R.string.pdf_label_roll) + " $roll",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isPresent) Color(0xFF2E7D32) else Color(0xFFC62828),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPresent) {
                    Icon(Icons.Default.Check, null, tint = Color.White)
                } else {
                    Text("A", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
fun AttendanceContent(
    padding: PaddingValues,
    state: Result<List<AttendanceEntity>>,
    isToday: Boolean,
    viewModel: AttendanceViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        when (state) {
            is Result.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is Result.Success -> {
                if (state.data.isEmpty()) {
                    Text(
                        stringResource(R.string.msg_no_students),
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (isToday) viewModel.markAllStatus(
                                        "Present",
                                        state.data
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(
                                        0xFF2E7D32
                                    )
                                )
                            ) {
                                Icon(Icons.Default.DoneAll, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_all_present), fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    if (isToday) viewModel.markAllStatus(
                                        "Absent",
                                        state.data
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Icon(Icons.Default.RemoveDone, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_all_absent), fontSize = 12.sp)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.data) { record ->
                                AttendanceStudentRow(
                                    name = record.studentName,
                                    roll = record.rollNo,
                                    status = record.status,
                                    enabled = isToday,
                                    onToggle = {
                                        viewModel.toggleStatus(
                                            record.studentId,
                                            state.data
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            is Result.Error -> {
                Text(
                    stringResource(R.string.label_error) + ": ${state.message}",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun AttendanceBottomSummary(
    state: Result<List<AttendanceEntity>>,
    isToday: Boolean,
    onSave: (List<AttendanceEntity>) -> Unit
) {
    if (state is Result.Success) {
        val records = state.data
        if (records.isNotEmpty()) {
            val total = records.size
            val present = records.count { it.status == "Present" }
            val absent = total - present

            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            stringResource(R.string.label_total_students),
                            total.toString(),
                            Color.Black
                        )
                        StatItem(
                            stringResource(R.string.label_total_present),
                            present.toString(),
                            Color(0xFF2E7D32)
                        )
                        StatItem(
                            stringResource(R.string.label_total_absent),
                            absent.toString(),
                            Color.Red
                        )
                    }

                    Button(
                        onClick = { onSave(records) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isToday
                    ) {
                        Text(
                            if (isToday) stringResource(R.string.btn_submit_attendance) else stringResource(
                                R.string.msg_old_attendance_locked
                            ),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}