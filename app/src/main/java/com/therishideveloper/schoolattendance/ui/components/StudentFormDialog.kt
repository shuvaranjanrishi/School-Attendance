package com.therishideveloper.schoolattendance.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.therishideveloper.schoolattendance.data.local.entity.StudentEntity
import com.therishideveloper.schoolattendance.ui.viewmodels.StudentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.utils.BloodGroupTypes
import com.therishideveloper.schoolattendance.utils.ClassTypes
import com.therishideveloper.schoolattendance.utils.CountryTypes
import com.therishideveloper.schoolattendance.utils.GenderTypes
import com.therishideveloper.schoolattendance.utils.IdTypes
import com.therishideveloper.schoolattendance.utils.ReligionTypes
import com.therishideveloper.schoolattendance.utils.localizeDigitsAndLabels
import kotlinx.coroutines.CoroutineScope

class StudentFormState(student: StudentEntity?) {
    var name by mutableStateOf(student?.name ?: "")
    var roll by mutableStateOf(student?.rollNo ?: "")
    var sClass by mutableStateOf(
        student?.className ?: ClassTypes.PLAY.code
    )
    var sCountry by mutableStateOf(student?.country ?: CountryTypes.BANGLADESH.code)
    var fatherName by mutableStateOf(student?.fatherName ?: "")
    var motherName by mutableStateOf(student?.motherName ?: "")
    var address by mutableStateOf(student?.address ?: "")
    var selectedImage by mutableStateOf(student?.image)
    var gender by mutableStateOf(
        student?.gender ?: GenderTypes.MALE.code
    )
    var religion by mutableStateOf(
        student?.religion ?: ReligionTypes.ISLAM.code
    )
    var bloodGroup by mutableStateOf(
        student?.bloodGroup ?: BloodGroupTypes.UNKNOWN.code
    )
    var idType by mutableStateOf(
        when (student?.idType) {
            IdTypes.NID.code -> IdTypes.NID.code
            IdTypes.BIRTH.code -> IdTypes.BIRTH.code
            else -> IdTypes.BIRTH.code
        }
    )
    var nidOrBirthReg by mutableStateOf(student?.nidOrBirthReg ?: "")
    var dateOfBirth by mutableStateOf(student?.dateOfBirth ?: "")
    var age by mutableStateOf(student?.age ?: "")
    var admissionDate by mutableStateOf(
        student?.admissionDate ?: LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    )
    val countryCodes = listOf("🇧🇩 +880", "🇮🇳 +91")
    var selectedCountryCode by mutableStateOf(if (student?.phone?.startsWith("+91") == true) countryCodes[1] else countryCodes[0])
    var phone by mutableStateOf(student?.phone?.takeLast(10) ?: "")
    var classExpanded by mutableStateOf(false)
    var codeExpanded by mutableStateOf(false)
    var countryExpanded by mutableStateOf(false)
    var nameError by mutableStateOf(false)
    var rollError by mutableStateOf(false)
    var phoneError by mutableStateOf(false)
    var showDuplicateDialog by mutableStateOf(false)
    var duplicateName by mutableStateOf("")
    var duplicateGender by mutableStateOf("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormDialog(
    viewModel: StudentViewModel,
    student: StudentEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (StudentEntity) -> Unit
) {
    val context = LocalContext.current
    val isEditMode = student != null
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val state = rememberStudentFormState(student)
    val imagePicker = rememberAppImagePicker(context, 3f, 4f) { state.selectedImage = it }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .background(Color.Black.copy(0.5f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .heightIn(max = 550.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FormHeader(isEditMode, state.selectedImage) { imagePicker.launch("image/*") }
                    VerticalSpace(16)
                    FormField(
                        stringResource(R.string.label_student_name_star),
                        state.name,
                        { state.name = it; state.nameError = false },
                        state.nameError,
                        leadingIcon = Icons.Default.Person
                    )
                    FormField(
                        stringResource(R.string.label_roll_star),
                        state.roll,
                        {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) state.roll =
                                it; state.rollError = false
                        },
                        state.rollError,
                        KeyboardType.Number,
                        leadingIcon = Icons.Default.FormatListNumbered
                    )
                    OutlinedDateSection(
                        label = stringResource(R.string.label_birth_date_star),
                        selectedDate = state.dateOfBirth.localizeDigitsAndLabels(),
                        durationText = state.age.localizeDigitsAndLabels(),
                        durationLabel = stringResource(R.string.label_age),
                        onDateSelected = { date, age ->
                            state.dateOfBirth = date
                            state.age = age
                        }
                    )
                    IdentificationSection(state)
                    ContactSection(state)
                    FormField(
                        label = stringResource(R.string.label_father),
                        value = state.fatherName,
                        onValueChange = { state.fatherName = it },
                        leadingIcon = Icons.Default.Person
                    )
                    FormField(
                        label = stringResource(R.string.label_mother),
                        value = state.motherName,
                        onValueChange = { state.motherName = it },
                        leadingIcon = Icons.Default.Person
                    )
                    ClassSelectionSection(state)
                    GenderSection(state.gender) { state.gender = it }
                    ReligionSection(state.religion) { state.religion = it }
                    BloodGroupSection(state.bloodGroup) { state.bloodGroup = it }
                    AddressSection(state, scrollState, coroutineScope)
                    if (!isEditMode) {
                        FormField(
                            label = stringResource(R.string.label_admission_date),
                            value = state.admissionDate.localizeDigitsAndLabels(),
                            onValueChange = {},
                            isError = false,
                            readOnly = true,
                            leadingIcon = Icons.Default.CalendarToday
                        )
                        VerticalSpace(8)
                    }

                    // Final Actions (Save / Cancel)
                    FormActions(
                        state,
                        student,
                        viewModel,
                        isEditMode,
                        onDismiss,
                        onConfirm,
                        scrollState
                    )
                }
            }
        }
    }
}

@Composable
fun rememberStudentFormState(student: StudentEntity?) =
    remember(student) { StudentFormState(student) }

@Composable
fun IdentificationSection(state: StudentFormState) {
    val idOptions = IdTypes.getAll().filter { it != IdTypes.NONE }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
    ) {
        Text(
            text = stringResource(R.string.label_identification),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            idOptions.forEach { type ->
                val isSelected = state.idType == type.code

                FilterChip(
                    selected = isSelected,
                    onClick = { state.idType = type.code },
                    label = {
                        Text(
                            text = stringResource(type.stringRes),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }
        }
        val currentIdTypeObj = IdTypes.fromCode(state.idType)
        val idNumberLabel = if (currentIdTypeObj != IdTypes.NONE) {
            "${stringResource(currentIdTypeObj.stringRes)} ${stringResource(R.string.label_number)}"
        } else {
            stringResource(R.string.label_id_number)
        }

        OutlinedTextField(
            value = state.nidOrBirthReg,
            onValueChange = { if (it.length <= 20) state.nidOrBirthReg = it },
            label = { Text(idNumberLabel) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}

@Composable
fun FormActions(
    state: StudentFormState,
    student: StudentEntity?,
    viewModel: StudentViewModel,
    isEditMode: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (StudentEntity) -> Unit,
    scrollState: ScrollState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    VerticalSpace(24)

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_btn)) }
        HorizontalSpace(8)

        Button(
            onClick = {
                val code = state.selectedCountryCode.split(" ")[1]
                val isNameValid = state.name.isNotBlank()
                val isRollValid = state.roll.isNotBlank()
                val isPhoneValid = state.phone.length == 10

                if (isNameValid && isRollValid && isPhoneValid) {
                    coroutineScope.launch {
                        // Check for duplicate roll in the same class
                        val existingStudent = viewModel.getStudentByRoll(state.roll, state.sClass)

                        if (existingStudent != null && existingStudent.id != (student?.id ?: 0)) {
                            state.duplicateName = existingStudent.name
                            state.duplicateGender = existingStudent.gender
                            state.showDuplicateDialog = true
                        } else {
                            // Saving student data
                            onConfirm(
                                StudentEntity(
                                    id = student?.id ?: 0,
                                    name = state.name,
                                    rollNo = state.roll,
                                    dateOfBirth = state.dateOfBirth,
                                    age = state.age,
                                    nidOrBirthReg = state.nidOrBirthReg,
                                    idType = state.idType,
                                    className = state.sClass,
                                    fatherName = state.fatherName,
                                    motherName = state.motherName,
                                    phone = "$code${state.phone}",
                                    gender = state.gender,
                                    religion = state.religion,
                                    bloodGroup = state.bloodGroup,
                                    address = state.address,
                                    country = state.sCountry,
                                    admissionDate = student?.admissionDate ?: state.admissionDate,
                                    image = state.selectedImage
                                )
                            )
                            showToast(
                                context,
                                msg = if (isEditMode) {
                                    context.getString(R.string.student_updated)
                                } else {
                                    context.getString(R.string.student_admitted)
                                }
                            )
                        }
                    }
                } else {
                    // Updating error states
                    state.nameError = !isNameValid
                    state.rollError = !isRollValid
                    state.phoneError = !isPhoneValid

                    val errorMsg = when {
                        !isNameValid -> context.getString(R.string.give_student_name)
                        !isRollValid -> context.getString(R.string.give_student_roll)
                        else -> context.getString(R.string.give_correct_phone_no)
                    }
                    showToast(context, errorMsg)

                    coroutineScope.launch {
                        scrollState.animateScrollTo(0)
                    }
                }
            }) {
            Text(
                if (isEditMode) {
                    stringResource(R.string.update_btn)
                } else {
                    stringResource(R.string.confirm_admission_btn)
                }
            )
        }
    }

    // Duplicate Roll Alert Dialog
    if (state.showDuplicateDialog) {
        DuplicateRollDialog(state)
    }
}

@Composable
fun DuplicateRollDialog(state: StudentFormState) {
    AlertDialog(
        onDismissRequest = { state.showDuplicateDialog = false },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.title_duplicate_roll))
            }
        },
        text = {
            // English: Get appropriate student identity based on gender code
            val identityRes = when (state.duplicateGender) {
                GenderTypes.FEMALE.code -> R.string.student_female
                else -> R.string.student_male
            }
            val identity = stringResource(identityRes)

            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.msg_duplicate_prefix))
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        append(state.duplicateName)
                    }
                    append(stringResource(R.string.msg_duplicate_middle))
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        append(identity)
                    }
                    append(stringResource(R.string.msg_duplicate_suffix))
                }
            )
        },
        confirmButton = {
            Button(onClick = { state.showDuplicateDialog = false }) {
                Text(stringResource(R.string.ok_btn))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSection(state: StudentFormState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country Code Dropdown Section
            ExposedDropdownMenuBox(
                expanded = state.codeExpanded,
                onExpandedChange = { state.codeExpanded = !state.codeExpanded },
                modifier = Modifier.weight(0.4f)
            ) {
                OutlinedTextField(
                    // Splitting to show only the code (e.g., +880)
                    value = state.selectedCountryCode.split(" ")[1],
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_code)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.codeExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable) // Fixed anchor type
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = state.codeExpanded,
                    onDismissRequest = { state.codeExpanded = false }
                ) {
                    state.countryCodes.forEach { code ->
                        DropdownMenuItem(
                            text = { Text(code) },
                            onClick = {
                                state.selectedCountryCode = code
                                state.codeExpanded = false
                            }
                        )
                    }
                }
            }

            HorizontalSpace(8)

            FormField(
                label = stringResource(R.string.label_phone_star),
                value = state.phone,
                onValueChange = {
                    if (it.length <= 10) state.phone = it
                    state.phoneError = false
                },
                isError = state.phoneError,
                keyboardType = KeyboardType.Phone,
                modifier = Modifier.weight(0.6f),
                placeholder = stringResource(R.string.hint_phone_number_length),
                leadingIcon = Icons.Default.Phone
            )
        }
    }
}

@Composable
fun FormHeader(
    isEditMode: Boolean,
    selectedImage: ByteArray?,
    onImageClick: () -> Unit
) {
    // Dialog Title
    Text(
        text = if (isEditMode) {
            stringResource(R.string.edit_information)
        } else {
            stringResource(R.string.admit_new_student)
        },
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )

    VerticalSpace(16)

    // Profile Image Picker UI
    Box(
        Modifier
            .size(85.dp)
            .clip(CircleShape)
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary,
                CircleShape
            )
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable { onImageClick() },
        contentAlignment = Alignment.Center
    ) {
        if (selectedImage != null) {
            // Convert ByteArray to ImageBitmap for display
            val bitmap = remember(selectedImage) {
                BitmapFactory.decodeByteArray(selectedImage, 0, selectedImage.size)
            }
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Profile Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Default icon when no image is selected
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Add Photo",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassSelectionSection(state: StudentFormState) {
    val classNames = ClassTypes.getAll()

    VerticalSpace(4)

    ExposedDropdownMenuBox(
        expanded = state.classExpanded,
        onExpandedChange = { state.classExpanded = !state.classExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        // বর্তমানে সিলেক্ট করা কোড থেকে নাম বের করা
        val selectedClassName = stringResource(ClassTypes.fromCode(state.sClass).stringRes)

        OutlinedTextField(
            value = selectedClassName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_class)) }, // "শ্রেণী"
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.classExpanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Class, null, tint = MaterialTheme.colorScheme.primary)
            },
            singleLine = true,
        )

        ExposedDropdownMenu(
            expanded = state.classExpanded,
            onDismissRequest = { state.classExpanded = false }
        ) {
            classNames.forEach { type ->
                DropdownMenuItem(
                    text = { Text(stringResource(type.stringRes)) },
                    onClick = {
                        state.sClass = type.code // কোড সেভ হচ্ছে (যেমন: "C1")
                        state.classExpanded = false
                    }
                )
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun VerticalSpace(height: Int) {
    Spacer(modifier = Modifier.height(height.dp))
}

@Composable
fun HorizontalSpace(width: Int) {
    Spacer(modifier = Modifier.width(width.dp))
}

fun showToast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    placeholder: String = "",
    leadingIcon: ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,
        readOnly = readOnly,
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, color = Color.Gray) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 0.dp, end = 0.dp, top = 2.dp, bottom = 2.dp),
        singleLine = true
    )
}

@Composable
fun GenderSection(selectedGenderCode: String, onGenderSelected: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = stringResource(R.string.label_gender),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GenderTypes.getAll().forEach { type ->
                val isSelected = selectedGenderCode == type.code

                FilterChip(
                    selected = isSelected,
                    onClick = { onGenderSelected(type.code) },
                    label = {
                        Text(
                            text = stringResource(type.stringRes), // অটোমেটিক ভাষা পরিবর্তন হবে
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
fun ReligionSection(
    selectedReligionCode: String,
    onReligionSelected: (String) -> Unit
) {

    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.label_religion), // "ধর্ম:" স্ট্রিং রিসোর্স থেকে
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        VerticalSpace(2)

        ReligionTypes.getAll().chunked(3).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { religionItem ->
                    val isSelected = selectedReligionCode == religionItem.code

                    FilterChip(
                        selected = isSelected,
                        onClick = { onReligionSelected(religionItem.code) }, // ডাটাবেস কোড পাস হচ্ছে
                        label = {
                            Text(
                                text = stringResource(religionItem.stringRes), // ভাষা অনুযায়ী টেক্সট
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size < 3) {
                    repeat(3 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun BloodGroupSection(selectedCode: String, onGroupSelected: (String) -> Unit) {
    val groups = BloodGroupTypes.getAll()

    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.label_blood_group),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        VerticalSpace(4)

        groups.chunked(3).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { group ->
                    FilterChip(
                        selected = selectedCode == group.code,
                        onClick = { onGroupSelected(group.code) },
                        label = {
                            Text(
                                text = stringResource(group.stringRes),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        leadingIcon = if (selectedCode == group.code) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSection(
    state: StudentFormState,
    scrollState: ScrollState,
    coroutineScope: CoroutineScope
) {
    val countries = CountryTypes.getAll()

    OutlinedTextField(
        value = state.address,
        onValueChange = { state.address = it },
        label = { Text(stringResource(R.string.label_address)) },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusEvent {
                if (it.isFocused) {
                    coroutineScope.launch {
                        delay(400)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                }
            },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Address Icon",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        maxLines = 2,
    )

    VerticalSpace(4)

    ExposedDropdownMenuBox(
        expanded = state.countryExpanded,
        onExpandedChange = { state.countryExpanded = !state.countryExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        val selectedCountry = CountryTypes.fromCode(state.sCountry)
        val selectedCountryName = stringResource(selectedCountry.stringRes)

        OutlinedTextField(
            value = "${selectedCountry.flag}  $selectedCountryName",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_country)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.countryExpanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Public Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            singleLine = true,
        )
        ExposedDropdownMenu(
            expanded = state.countryExpanded,
            onDismissRequest = { state.countryExpanded = false }
        ) {
            countries.forEach { item ->
                DropdownMenuItem(
                    text = { Text("${item.flag}  ${stringResource(item.stringRes)}") },
                    onClick = {
                        state.sCountry = item.code
                        state.countryExpanded = false
                    },
                )
                HorizontalDivider(Modifier.fillMaxWidth())
            }
        }
    }
}
