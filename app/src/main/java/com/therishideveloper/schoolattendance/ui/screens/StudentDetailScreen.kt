package com.therishideveloper.schoolattendance.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.Toast
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.therishideveloper.schoolattendance.data.local.entity.StudentEntity
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.ui.components.StudentFormDialog
import com.therishideveloper.schoolattendance.ui.viewmodels.StudentViewModel
import com.therishideveloper.schoolattendance.utils.Result
import androidx.core.net.toUri
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.ui.components.LoadingOverlay
import com.therishideveloper.schoolattendance.ui.components.showToast
import com.therishideveloper.schoolattendance.ui.event.UiEvent
import com.therishideveloper.schoolattendance.utils.BloodGroupTypes
import com.therishideveloper.schoolattendance.utils.ClassTypes
import com.therishideveloper.schoolattendance.utils.CountryTypes
import com.therishideveloper.schoolattendance.utils.GenderTypes
import com.therishideveloper.schoolattendance.utils.IdTypes
import com.therishideveloper.schoolattendance.utils.ReligionTypes
import com.therishideveloper.schoolattendance.utils.localizeDigitsAndLabels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    studentId: Int,
    navController: NavController,
    viewModel: StudentViewModel
) {
    val studentResult by remember(studentId) {
        viewModel.getStudentById(studentId)
    }.collectAsState()

    // Dialog States
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCallConfirm by remember { mutableStateOf(false) }
    var showDownloadConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { event ->
            when(event) {
                is UiEvent.ShowToastRes -> showToast(context,context.getString(event.resId))
                is UiEvent.ShowToastStr -> showToast(context,event.message)
                else -> {}
            }
        }
    }

    when (val state = studentResult) {
        is Result.Loading -> LoadingScreen()
        is Result.Error -> ErrorScreen(state.message)
        is Result.Success -> {
            val student = state.data
            if (student == null) {
                EmptyStateScreen()
            } else {
                Scaffold(
                    topBar = {
                        DetailTopBar(
                            onBack = { navController.popBackStack() },
                            onEdit = { showEditDialog = true },
                            onDownload = { showDownloadConfirm = true }
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Profile Header Section
                        StudentProfileHeader(student)

                        // Information Card Section
                        StudentInfoCard(student, onCallClick = { showCallConfirm = true })

                        // Delete Action Section
                        DeleteProfileButton(onDeleteClick = { showDeleteConfirm = true })
                    }
                }

                // Dialogs Handling
                DetailDialogs(
                    showCall = showCallConfirm,
                    showDelete = showDeleteConfirm,
                    showEdit = showEditDialog,
                    showDownload = showDownloadConfirm,
                    student = student,
                    viewModel = viewModel,
                    onDismissCall = { showCallConfirm = false },
                    onDismissDelete = { showDeleteConfirm = false },
                    onDismissEdit = { showEditDialog = false },
                    onDismissDownload = { showDownloadConfirm = false },
                    navController = navController
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDownload: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(R.string.profile_details_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        },
        actions = {
            IconButton(onClick = onDownload) {
                Icon(Icons.Default.Download, contentDescription = "Download PDF")
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
        },
        colors = myTopBarColors()
    )
}

@Composable
fun StudentProfileHeader(student: StudentEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (student.image != null) {
                    val bitmap = BitmapFactory.decodeByteArray(student.image, 0, student.image.size)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier.size(70.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val classType = ClassTypes.fromCode(student.className)
            Text(
                text = student.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(

                text = stringResource(
                    R.string.details_label_class,
                    stringResource(classType.stringRes).replace("Class ", "")
                ),
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StudentInfoCard(student: StudentEntity, onCallClick: () -> Unit) {
    val idTypeObj = IdTypes.fromCode(student.idType)

    val idLabel = when (idTypeObj) {
        IdTypes.NID -> stringResource(R.string.label_nid)
        IdTypes.BIRTH -> stringResource(R.string.label_birth_reg)
        else -> stringResource(R.string.label_id_default)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                0.4f
            )
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            InfoRow(
                Icons.Default.Badge,
                stringResource(R.string.label_roll),
                student.rollNo.localizeDigitsAndLabels()
            )
            DetailDivider()
            InfoRow(
                Icons.Default.CalendarToday,
                stringResource(R.string.label_dob),
                student.dateOfBirth.localizeDigitsAndLabels()
            )
            DetailDivider()
            InfoRow(
                Icons.Default.Info,
                stringResource(R.string.label_age),
                student.age.localizeDigitsAndLabels()
            )
            DetailDivider()
            InfoRow(
                Icons.Default.RecentActors,
                idLabel,
                student.nidOrBirthReg.localizeDigitsAndLabels()
            )
            DetailDivider()
            InfoRow(
                Icons.Default.FamilyRestroom,
                stringResource(R.string.label_father),
                student.fatherName
            )
            DetailDivider()
            InfoRow(
                Icons.Default.FamilyRestroom,
                stringResource(R.string.label_mother),
                student.motherName
            )
            DetailDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    InfoRow(
                        Icons.Default.Phone,
                        stringResource(R.string.label_phone),
                        student.phone.localizeDigitsAndLabels()
                    )
                }
                if (student.phone.isNotBlank()) {
                    IconButton(
                        onClick = onCallClick,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Call,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            DetailDivider()
            val genderType = GenderTypes.fromCode(student.gender)
            InfoRow(
                Icons.Default.Wc,
                label = stringResource(R.string.label_gender),
                value = stringResource(genderType.stringRes)
            )
            DetailDivider()
            val religionType = ReligionTypes.fromCode(student.religion)
            InfoRow(
                Icons.Default.Church,
                label = stringResource(R.string.label_religion),
                value = stringResource(religionType.stringRes)
            )
            DetailDivider()
            val bloodGroupType = BloodGroupTypes.fromCode(student.bloodGroup)
            InfoRow(
                Icons.Default.Bloodtype,
                label = stringResource(R.string.label_blood_group),
                value = stringResource(bloodGroupType.stringRes),
                iconColor = Color.Red
            )
            DetailDivider()
            InfoRow(
                Icons.Default.Home,
                stringResource(R.string.label_address),
                student.address
            )
            DetailDivider()
            val countryObj = CountryTypes.fromCode(student.country)
            InfoRow(
                icon = Icons.Default.Public,
                label = stringResource(R.string.label_country),
                value = "${countryObj.flag}  ${stringResource(countryObj.stringRes)}"
            )
            DetailDivider()
            InfoRow(
                Icons.Default.CalendarMonth,
                stringResource(R.string.label_admission_date),
                student.admissionDate.localizeDigitsAndLabels()
            )
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Text(
                text = value.ifBlank { stringResource(R.string.not_given) },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DetailDialogs(
    showCall: Boolean,
    showDelete: Boolean,
    showEdit: Boolean,
    showDownload: Boolean,
    student: StudentEntity,
    viewModel: StudentViewModel,
    onDismissCall: () -> Unit,
    onDismissDelete: () -> Unit,
    onDismissEdit: () -> Unit,
    onDismissDownload: () -> Unit,
    navController: NavController
) {
    val isPdfGenerating by viewModel.isPdfGenerating.collectAsState()
    val context = LocalContext.current
    if (showDownload) {
        AlertDialog(
            onDismissRequest = onDismissDownload,
            title = { Text(stringResource(R.string.download_confirm_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.download_confirm_msg, student.name))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.file_location_msg),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onDismissDownload()
                    viewModel.generatePdf(student)
                }) {
                    Text(stringResource(R.string.download_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDownload) {
                    Text(stringResource(R.string.cancel_btn))
                }
            }
        )
    }

    LoadingOverlay(
        isLoading = isPdfGenerating,
        message = stringResource(id = R.string.pdf_creating_msg) // পিডিএফ তৈরির মেসেজ
    )

    if (showCall) {
        AlertDialog(
            onDismissRequest = onDismissCall,
            title = { Text(stringResource(R.string.call_title)) },
            text = { Text(stringResource(R.string.call_msg, student.name)) },
            confirmButton = {
                Button(onClick = {
                    onDismissCall()
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data =
                            "tel:${student.phone}".toUri()
                    }
                    context.startActivity(intent)
                }) { Text(stringResource(R.string.call_btn)) }
            },
            dismissButton = { TextButton(onClick = onDismissCall) { Text(stringResource(R.string.cancel_btn)) } }
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.delete_title)) },
            text = { Text(stringResource(R.string.delete_msg)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissDelete()
                        viewModel.deleteStudent(student)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text(stringResource(R.string.delete_confirm_btn), color = Color.White) }
            },
            dismissButton = { TextButton(onClick = onDismissDelete) { Text(stringResource(R.string.cancel_btn)) } }
        )
    }

    if (showEdit) {
        StudentFormDialog(
            viewModel = viewModel,
            student = student,
            onDismiss = onDismissEdit,
            onConfirm = { updatedStudent ->
                viewModel.updateStudent(updatedStudent)
                onDismissEdit()
            }
        )
    }
}

@Composable
fun DetailDivider() {
    HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
}

@Composable
fun DeleteProfileButton(onDeleteClick: () -> Unit) {
    Spacer(modifier = Modifier.height(30.dp))
    TextButton(
        onClick = onDeleteClick,
        modifier = Modifier
            .padding(bottom = 30.dp)
            .fillMaxWidth()
    ) {
        Icon(Icons.Default.DeleteOutline, null, tint = Color.Red)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.delete_profile_btn), color = Color.Red)
    }
}

// Loading, Error, Empty states for cleaner look
@Composable
fun LoadingScreen() =
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

@Composable
fun ErrorScreen(msg: String) = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(
        text = msg.ifBlank { stringResource(R.string.error_msg) },
        color = Color.Red
    )
}

@Composable
fun EmptyStateScreen() = Box(
    Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) { Text(stringResource(R.string.empty_student_msg)) }