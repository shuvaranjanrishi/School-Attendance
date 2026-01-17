package com.therishideveloper.schoolattendance.ui.components

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.utils.DateTimeUtils
import com.therishideveloper.schoolattendance.utils.DateTimeUtils.calculateDuration
import com.therishideveloper.schoolattendance.utils.DateTimeUtils.formatDateToString
import com.yalantis.ucrop.UCrop
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommonDatePickerDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (String, String) -> Unit
) {
    if (showDialog) {
        val currentLocale = LocalConfiguration.current.locales[0]
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )

        CompositionLocalProvider(LocalConfiguration provides LocalConfiguration.current.apply {
            setLocale(currentLocale)
        }) {
            DatePickerDialog(
                onDismissRequest = onDismiss,
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            onDateSelected(formatDateToString(date), calculateDuration(date))
                        }
                        onDismiss()
                    }) { Text(stringResource(R.string.btn_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDatePicker(
    showDialog: Boolean,
    initialDate: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    if (showDialog) {
        val currentLocale = LocalConfiguration.current.locales[0]

        val currentMillis = remember(initialDate) { DateTimeUtils.dateToMillis(initialDate) }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // ভবিষ্যৎ তারিখ বন্ধ রাখার লজিক
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )

        CompositionLocalProvider(
            LocalConfiguration provides LocalConfiguration.current.apply {
                setLocale(currentLocale)
            }
        ) {
            DatePickerDialog(
                onDismissRequest = onDismiss,
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(DateTimeUtils.millisToDate(millis))
                        }
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.btn_ok), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun OutlinedDateSection(
    label: String,
    selectedDate: String,
    durationText: String,
    durationLabel: String,
    onDateSelected: (String, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    CommonDatePickerDialog(showDialog, { showDialog = false }, onDateSelected)

    Column {
        OutlinedDateInputField(
            value = selectedDate,
            label = label,
            icon = Icons.Default.CalendarToday,
            onClick = { showDialog = true }
        )

        OutlinedTextField(
            value = durationText,
            onValueChange = {},
            label = { Text(durationLabel) },
            readOnly = true,
            enabled = false,
            leadingIcon = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            shape = RoundedCornerShape(4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun OutlinedDateInputField(value: String, label: String, icon: ImageVector, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable { onClick() }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = false,
            leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.primary
            )
        )
        Box(modifier = Modifier.matchParentSize().background(Color.Transparent))
    }
}

@Composable
fun ModernInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    showUnderline: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onClick: (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 14.sp) },
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
        trailingIcon = trailingIcon,
        modifier = modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        visualTransformation = visualTransformation,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.2f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
            focusedIndicatorColor = if (showUnderline) MaterialTheme.colorScheme.primary else Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = MaterialTheme.colorScheme.error,
        ),
        shape = RoundedCornerShape(16.dp),
        isError = isError,
        singleLine = true,
        readOnly = readOnly,
        enabled = if (onClick != null) false else enabled,
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun ModernDateSection(
    label: String,
    selectedDate: String,
    onDateSelected: (String, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    CommonDatePickerDialog(showDialog, { showDialog = false }, onDateSelected)

    ModernInputField(
        value = selectedDate,
        onValueChange = {},
        label = label,
        icon = Icons.Default.CalendarToday,
        readOnly = true,
        showUnderline = false,
        onClick = { showDialog = true }
    )
}

@Composable
fun rememberAppImagePicker(
    context: Context,
    aspectX: Float = 1f,
    aspectY: Float = 1f,
    onImageSelected: (ByteArray) -> Unit
): ManagedActivityResultLauncher<String, Uri?> {
    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            UCrop.getOutput(result.data!!)?.let { uri ->
                val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
                val out = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                onImageSelected(out.toByteArray())
            }
        }
    }

    return rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val destUri = Uri.fromFile(File(context.cacheDir, "crop_${System.currentTimeMillis()}.jpg"))
            val intent = UCrop.of(it, destUri).withAspectRatio(aspectX, aspectY).getIntent(context)
            cropLauncher.launch(intent)
        }
    }
}
