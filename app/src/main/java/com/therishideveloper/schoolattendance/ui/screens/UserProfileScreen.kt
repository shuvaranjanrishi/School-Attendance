package com.therishideveloper.schoolattendance.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.data.local.entity.UserEntity
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.ui.viewmodels.ProfileViewModel
import com.therishideveloper.schoolattendance.utils.Result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val userResult by viewModel.userState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, "Edit", tint = Color.White)
                    }
                },
                colors = myTopBarColors()
            )
        }
    ) { padding ->
        when (val state = userResult) {
            is Result.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Result.Success -> {
                UserProfileContent(state.data, padding)
            }

            is Result.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}")
                }
            }
        }
    }
}

@Composable
fun UserProfileContent(user: UserEntity?, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        // --- Header Section ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(MaterialTheme.colorScheme.primary.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(150.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(5.dp, Color.White),
                shadowElevation = 8.dp,
            ) {
                user?.image?.let { bytes ->
                    val bitmap =
                        remember(bytes) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                    Image(bitmap.asImageBitmap(), null, contentScale = ContentScale.Crop)
                } ?: Icon(
                    Icons.Default.Person,
                    null,
                    modifier = Modifier.size(150.dp),
                    tint = Color.LightGray
                )
            }
        }

        // --- Name & Designation ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                user?.name ?: "Teacher Name",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(user?.designation ?: "Designation", color = MaterialTheme.colorScheme.primary)
        }

        // --- Statistics Summary (Dummy Data) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatItem(stringResource(R.string.stats_total_classes), "145", Modifier.weight(1f))
            StatItem(stringResource(R.string.stats_attendance_done), "132", Modifier.weight(1f))
            StatItem(stringResource(R.string.stats_avg_present), "88%", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Detail Info Items ---
        ProfileInfoItem(
            stringResource(R.string.label_teacher_id),
            user?.teacherId ?: "---",
            Icons.Default.Badge
        )
        ProfileInfoItem(
            stringResource(R.string.label_qualification),
            user?.qualification ?: "---",
            Icons.Default.HistoryEdu
        )
        ProfileInfoItem(
            stringResource(R.string.label_subject),
            user?.subjectExpert ?: "---",
            Icons.Default.Book
        )
        ProfileInfoItem(
            stringResource(R.string.label_assigned_classes),
            user?.assignedClasses ?: "---",
            Icons.Default.Groups
        )
        ProfileInfoItem(
            stringResource(R.string.label_joining_date),
            user?.joiningDate ?: "---",
            Icons.Default.DateRange
        )
        ProfileInfoItem(
            stringResource(R.string.label_job_duration),
            user?.jobDuration ?: "---",
            Icons.Default.Info
        )
        ProfileInfoItem(
            stringResource(R.string.label_phone),
            user?.phone ?: "---",
            Icons.Default.Phone
        )
        ProfileInfoItem(
            stringResource(R.string.label_email),
            user?.email ?: "---",
            Icons.Default.Email
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(label, fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}