package com.therishideveloper.schoolattendance.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.data.local.entity.SchoolEntity
import com.therishideveloper.schoolattendance.ui.components.ModernInputField
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.ui.components.rememberAppImagePicker
import com.therishideveloper.schoolattendance.ui.viewmodels.SchoolViewModel
import com.therishideveloper.schoolattendance.utils.Result

class SchoolFormState(school: SchoolEntity?) {
    var name by mutableStateOf(school?.name ?: "")
    var address by mutableStateOf(school?.address ?: "")
    var logoBytes by mutableStateOf(school?.logo)
    var bannerBytes by mutableStateOf(school?.banner)

    var nameError by mutableStateOf(false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSchoolScreen(
    viewModel: SchoolViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val schoolResult by viewModel.schoolState.collectAsState()

    when (schoolResult) {
        is Result.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is Result.Success -> {
            val school = (schoolResult as Result.Success<SchoolEntity?>).data
            val state = remember(school) { SchoolFormState(school) }

            val logoPicker = rememberAppImagePicker(context, 1f, 1f) { state.logoBytes = it }
            val bannerPicker = rememberAppImagePicker(context, 16f, 9f) { state.bannerBytes = it }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.title_edit_school)) },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { bannerPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            state.bannerBytes?.let { bytes ->
                                val bitmap = remember(bytes) {
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                }
                                Image(
                                    bitmap.asImageBitmap(),
                                    null,
                                    Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } ?: Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, null, Modifier.size(40.dp))
                                Text(
                                    text = "ব্যানার যোগ করুন",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(4.dp, Color.White, CircleShape)
                                .clickable { logoPicker.launch("image/*") },
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                state.logoBytes?.let { bytes ->
                                    val bitmap = remember(bytes) {
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    }
                                    Image(
                                        bitmap.asImageBitmap(),
                                        null,
                                        Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } ?: Icon(Icons.Default.AddAPhoto, null, Modifier.size(30.dp))
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        ModernInputField(
                            value = state.name,
                            onValueChange = { state.name = it; state.nameError = false },
                            label = stringResource(R.string.label_school_name),
                            icon = Icons.Default.School,
                            isError = state.nameError
                        )

                        ModernInputField(
                            value = state.address,
                            onValueChange = { state.address = it },
                            label = stringResource(R.string.label_address),
                            icon = Icons.Default.LocationOn
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (state.name.isBlank()) {
                                    state.nameError = true
                                } else {
                                    val updatedSchool = SchoolEntity(
                                        id = school?.id ?: 1,
                                        name = state.name,
                                        address = state.address,
                                        logo = state.logoBytes,
                                        banner = state.bannerBytes
                                    )
                                    viewModel.insertOrUpdate(updatedSchool)
                                    onBackClick()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.btn_save_info),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        is Result.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error: ${(schoolResult as Result.Error).message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
