package com.therishideveloper.schoolattendance.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.data.local.entity.UserEntity
import com.therishideveloper.schoolattendance.ui.components.ModernInputField
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.ui.components.rememberAppImagePicker
import com.therishideveloper.schoolattendance.ui.components.showToast
import com.therishideveloper.schoolattendance.ui.viewmodels.ProfileViewModel
import com.therishideveloper.schoolattendance.utils.Result
import com.therishideveloper.schoolattendance.utils.localizeDigitsAndLabels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.therishideveloper.schoolattendance.ui.components.ModernDateSection

class UserFormState(user: UserEntity?) {
    var name by mutableStateOf(user?.name ?: "")
    var designation by mutableStateOf(user?.designation ?: "")
    var qualification by mutableStateOf(user?.qualification ?: "")
    var teacherId by mutableStateOf(user?.teacherId ?: "")
    var joiningDate by mutableStateOf(user?.joiningDate ?: "")
    var jobDuration by mutableStateOf(user?.jobDuration ?: "")
    var assignedClasses by mutableStateOf(user?.assignedClasses ?: "")
    var subjectExpert by mutableStateOf(user?.subjectExpert ?: "")
    var phone by mutableStateOf(user?.phone ?: "")
    var email by mutableStateOf(user?.email ?: "")
    var password by mutableStateOf(user?.password ?: "")
    var securityQuestion by mutableStateOf(user?.securityQuestion ?: "")
    var securityAnswer by mutableStateOf(user?.securityAnswer ?: "")
    var imageBytes by mutableStateOf(user?.image)
    var nameError by mutableStateOf(false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val userResult by viewModel.userState.collectAsState()
    val errorNameRequired = stringResource(R.string.error_name_required)

    when (userResult) {
        is Result.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is Result.Success -> {
            val user = (userResult as Result.Success<UserEntity?>).data
            val state = remember(user) { UserFormState(user) }
            val imagePicker = rememberAppImagePicker(context, 1f, 1f) { state.imageBytes = it }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.edit_information)) },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(0.4f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                shadowElevation = 8.dp
                            ) {
                                state.imageBytes?.let { bytes ->
                                    val bitmap = remember(bytes) {
                                        BitmapFactory.decodeByteArray(
                                            bytes,
                                            0,
                                            bytes.size
                                        )
                                    }
                                    Image(
                                        bitmap.asImageBitmap(),
                                        null,
                                        Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } ?: Icon(
                                    Icons.Default.Person,
                                    null,
                                    modifier = Modifier.padding(25.dp),
                                    tint = Color.Gray
                                )
                            }
                            SmallFloatingActionButton(
                                onClick = { imagePicker.launch("image/*") },
                                containerColor = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    null,
                                    Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.label_personal_info),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        ModernInputField(
                            state.name,
                            { state.name = it; state.nameError = false },
                            stringResource(R.string.label_full_name),
                            Icons.Default.Person,
                            isError = state.nameError
                        )
                        ModernInputField(
                            state.designation,
                            { state.designation = it },
                            stringResource(R.string.label_designation),
                            Icons.Default.Work
                        )
                        ModernInputField(
                            state.qualification,
                            { state.qualification = it },
                            stringResource(R.string.label_qualification),
                            Icons.Default.HistoryEdu
                        )

                        ModernInputField(
                            state.phone,
                            onValueChange = {
                                if (it.length <= 11 && it.all { char -> char.isDigit() }) state.phone =
                                    it
                            },
                            stringResource(R.string.label_phone),
                            Icons.Default.Phone,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        ModernInputField(
                            state.email,
                            { state.email = it },
                            stringResource(R.string.label_email),
                            Icons.Default.Email,
                            readOnly = true,
                            enabled = false
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Text(
                            stringResource(R.string.label_academic_info),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ModernInputField(
                                    value = state.teacherId,
                                    onValueChange = { state.teacherId = it },
                                    label = stringResource(R.string.label_teacher_id),
                                    icon = Icons.Default.Badge,
                                    modifier = Modifier.weight(1f)
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { }) {
                                    ModernDateSection(
                                        label = stringResource(R.string.label_joining_date),
                                        selectedDate = state.joiningDate.localizeDigitsAndLabels(),
                                        onDateSelected = { date, age ->
                                            state.joiningDate = date
                                            state.jobDuration = age
                                        }
                                    )
                                }
                            }
                        }

                        ModernInputField(
                            value = state.jobDuration.localizeDigitsAndLabels(),
                            onValueChange = { },
                            label = stringResource(R.string.label_job_duration),
                            Icons.Default.Info,
                            readOnly = true,
                            enabled = false
                        )
                        ModernInputField(
                            state.subjectExpert,
                            { state.subjectExpert = it },
                            stringResource(R.string.label_subject),
                            Icons.AutoMirrored.Filled.MenuBook
                        )
                        ModernInputField(
                            state.assignedClasses,
                            { state.assignedClasses = it },
                            stringResource(R.string.label_assigned_classes),
                            Icons.Default.Groups
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (state.name.isNotBlank()) {
                                    val updatedUser = UserEntity(
                                        1,
                                        state.name,
                                        state.designation,
                                        state.qualification,
                                        state.teacherId,
                                        state.joiningDate,
                                        state.jobDuration,
                                        state.assignedClasses,
                                        state.subjectExpert,
                                        state.phone,
                                        state.email,
                                        state.password,
                                        state.securityQuestion,
                                        state.securityAnswer,
                                        state.imageBytes
                                    )
                                    viewModel.insertOrUpdate(updatedUser)
                                    onBackClick()
                                } else {
                                    state.nameError = true
                                    showToast(
                                        context,
                                        errorNameRequired
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_save_info))
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }

        else -> {}
    }
}
