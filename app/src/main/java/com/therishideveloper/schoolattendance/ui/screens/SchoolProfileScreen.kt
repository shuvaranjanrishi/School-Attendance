package com.therishideveloper.schoolattendance.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.therishideveloper.schoolattendance.data.local.entity.SchoolEntity
import com.therishideveloper.schoolattendance.ui.viewmodels.SchoolViewModel
import com.therishideveloper.schoolattendance.utils.Result
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolProfileScreen(
    viewModel: SchoolViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val schoolResult by viewModel.schoolState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_school_profile), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                },
                colors = myTopBarColors()
            )
        }
    ) { padding ->
        when (val state = schoolResult) {
            is Result.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Result.Success -> {
                val school = state.data
                SchoolProfileContent(school, padding)
            }

            is Result.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "ত্রুটি: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun SchoolProfileContent(
    school: SchoolEntity?,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (school?.banner != null) {
                    val bitmap = BitmapFactory.decodeByteArray(school.banner, 0, school.banner.size)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "School Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(110.dp),
                shape = CircleShape,
                border = BorderStroke(4.dp, Color.White),
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (school?.logo != null) {
                        val bitmap = BitmapFactory.decodeByteArray(school.logo, 0, school.logo.size)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "School Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color.LightGray
                        )
                    }
                }
            }
        }

        // --- ২. স্কুলের নাম এবং ঠিকানা ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = school?.name?.ifEmpty { stringResource(R.string.hint_school_name_empty) }
                    ?: stringResource(R.string.hint_school_name_empty),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = school?.address?.ifEmpty { stringResource(R.string.hint_address_empty) }
                        ?: stringResource(R.string.hint_address_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 32.dp),
            thickness = 1.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )

        // --- ৩. ইনফরমেশন কার্ড সেকশন ---
        Spacer(modifier = Modifier.height(16.dp))

        ProfileInfoItem(
            label = stringResource(R.string.label_institution_type),
            value = stringResource(R.string.value_school_type),
            icon = Icons.Default.School
        )

        ProfileInfoItem(
            label =stringResource(R.string.label_app_status),
            value = stringResource(R.string.value_status_active),
            icon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileInfoItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}