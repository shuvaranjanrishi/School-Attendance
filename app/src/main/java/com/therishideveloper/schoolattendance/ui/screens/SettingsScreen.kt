package com.therishideveloper.schoolattendance.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.therishideveloper.schoolattendance.data.local.AppLanguage
import com.therishideveloper.schoolattendance.data.local.ThemeMode
import com.therishideveloper.schoolattendance.ui.components.SelectionBottomSheet
import com.therishideveloper.schoolattendance.ui.components.SettingsItem
import com.therishideveloper.schoolattendance.ui.components.SettingsSection
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.ui.viewmodels.SettingsViewModel
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.ui.components.LoadingOverlay
import com.therishideveloper.schoolattendance.ui.components.ModernInputField
import com.therishideveloper.schoolattendance.ui.components.showToast
import com.therishideveloper.schoolattendance.ui.event.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit, viewModel: SettingsViewModel,
    onNavigateToSchoolProfile: () -> Unit,
    onNavigateToUserProfile: () -> Unit
) {

    val currentTheme by viewModel.themeMode.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()

    // States for Sheets and Dialogs
    var showPasswordSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var activeDialog by remember { mutableStateOf<SettingsDialogType?>(null) }

    val languages = AppLanguage.entries
    val context = LocalContext.current

    // SharedFlow Collector for Toast/Dialog Events
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { event ->
            when (event) {
                is UiEvent.ShowRestartDialog -> showRestartDialog = true
                is UiEvent.ShowToastRes -> showToast(context, context.getString(event.resId))
                is UiEvent.ShowToastStr -> showToast(context, event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu))
                    }
                },
                colors = myTopBarColors()
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // --- Appearance Section ---
                SettingsSection(title = stringResource(R.string.appearance)) {
                    SettingsItem(
                        title = stringResource(R.string.theme_settings),
                        subtitle = stringResource(R.string.change_theme_and_color_scheme),
                        icon = Icons.Default.Palette,
                        onClick = { showThemeSheet = true }
                    )

                    SettingsItem(
                        title = stringResource(R.string.language),
                        subtitle = stringResource(R.string.select_bangla_or_english_or_hindi_language),
                        icon = Icons.Default.Language,
                        onClick = { showLanguageSheet = true }
                    )

                    SettingsItem(
                        title = stringResource(R.string.profile_settings),
                        subtitle = stringResource(R.string.update_your_personal_profile),
                        icon = Icons.Default.Person,
                        onClick = { onNavigateToUserProfile() }
                    )

                    SettingsItem(
                        title = stringResource(R.string.school_profile_settings),
                        subtitle = stringResource(R.string.change_school_name_logo_and_address),
                        icon = Icons.Default.School,
                        onClick = {
                            onNavigateToSchoolProfile()
                        }
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
// --- Security Section ---
                SettingsSection(title = stringResource(R.string.security)) {
                    SettingsItem(
                        title = stringResource(R.string.change_password),
                        subtitle = stringResource(R.string.change_password_desc),
                        icon = Icons.Default.Lock,
                        onClick = { showPasswordSheet = true }
                    )
                }
                HorizontalDivider(
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // --- Download Section ---
                SettingsSection(title = stringResource(R.string.section_file_download)) {
                    SettingsItem(
                        title = stringResource(R.string.download_excel_file),
                        subtitle = stringResource(R.string.export_student_list_desc),
                        icon = Icons.Default.FileDownload,
                        onClick = { activeDialog = SettingsDialogType.EXCEL }
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // --- Backup & Restore Section ---
                SettingsSection(title = stringResource(R.string.section_data_backup_and_restore)) {
                    SettingsItem(
                        title = stringResource(R.string.backup_database),
                        subtitle = stringResource(R.string.save_a_copy_of_your_data),
                        icon = Icons.Default.CloudUpload,
                        onClick = { activeDialog = SettingsDialogType.BACKUP }
                    )

                    SettingsItem(
                        title = stringResource(R.string.restore_database),
                        subtitle = stringResource(R.string.restore_data_from_previous_backup),
                        icon = Icons.Default.SettingsBackupRestore,
                        onClick = { activeDialog = SettingsDialogType.RESTORE }
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // --- Advanced Section ---
                SettingsSection(title = stringResource(R.string.section_advanced_settings)) {
                    SettingsItem(
                        title = stringResource(R.string.delete_all_data),
                        subtitle = stringResource(R.string.delete_data_desc),
                        icon = Icons.Default.DeleteForever,
                        onClick = { activeDialog = SettingsDialogType.DELETE_ALL }
                    )
                }
            }

            // --- Sheets & Overlays ---
            if (showThemeSheet) {
                ThemeSheet(currentTheme, viewModel) { showThemeSheet = false }
            }

            if (showLanguageSheet) {
                LanguageSheet(currentLang, languages, viewModel) { showLanguageSheet = false }
            }

            if (showPasswordSheet) {
                PasswordChangeSheet(
                    onDismiss = { showPasswordSheet = false },
                    viewModel = viewModel
                )
            }

            activeDialog?.let { type ->
                SettingsConfirmDialog(
                    type = type,
                    onConfirm = {
                        when (type) {
                            SettingsDialogType.EXCEL -> viewModel.exportToExcel()
                            SettingsDialogType.BACKUP -> viewModel.performBackup()
                            SettingsDialogType.RESTORE -> viewModel.performRestore()
                            SettingsDialogType.DELETE_ALL -> viewModel.clearAllStudents()
                        }
                        activeDialog = null
                    },
                    onDismiss = { activeDialog = null }
                )
            }

            // রিস্টার্ট ডায়ালগ
            if (showRestartDialog) {
                RestartAppDialog(context)
            }

            LoadingOverlay(
                isLoading = isExporting,
                message = stringResource(R.string.exporting_excel)
            )
        }
    }
}

enum class SettingsDialogType { EXCEL, BACKUP, RESTORE, DELETE_ALL }

@Composable
fun SettingsConfirmDialog(type: SettingsDialogType, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val title = when (type) {
        SettingsDialogType.EXCEL -> R.string.download_excel_file
        SettingsDialogType.BACKUP -> R.string.backup_info_title
        SettingsDialogType.RESTORE -> R.string.confirm_restore_title
        SettingsDialogType.DELETE_ALL -> R.string.delete_confirm_title
    }
    val message = when (type) {
        SettingsDialogType.EXCEL -> R.string.export_confirm_msg
        SettingsDialogType.BACKUP -> R.string.backup_info_msg
        SettingsDialogType.RESTORE -> R.string.confirm_restore_msg
        SettingsDialogType.DELETE_ALL -> R.string.delete_confirm_msg
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title)) },
        text = { Text(stringResource(message)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (type == SettingsDialogType.DELETE_ALL) ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                ) else ButtonDefaults.buttonColors()
            ) {
                Text(stringResource(if (type == SettingsDialogType.DELETE_ALL) R.string.delete_btn else if (type == SettingsDialogType.BACKUP) R.string.backup_now else R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun ThemeSheet(currentTheme: ThemeMode, viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    SelectionBottomSheet(
        title = "থিম নির্বাচন করুন",
        items = ThemeMode.entries,
        selectedItem = currentTheme,
        itemLabel = { mode ->
            when (mode) {
                ThemeMode.LIGHT -> "লাইট মোড"
                ThemeMode.DARK -> "ডার্ক মোড"
                ThemeMode.SYSTEM -> "সিস্টেম ডিফল্ট"
            }
        },
        itemDescription = { mode ->
            when (mode) {
                ThemeMode.LIGHT -> "উজ্জ্বল ইন্টারফেস"
                ThemeMode.DARK -> "চোখের আরামের ইন্টারফেস"
                ThemeMode.SYSTEM -> "ফোনের সেটিং অনুযায়ী লাইট/ডার্ক"
            }
        },
        itemColor = { mode ->
            when (mode) {
                ThemeMode.LIGHT -> Color(0xFF6750A4)
                ThemeMode.DARK -> Color(0xFFD0BCFF)
                ThemeMode.SYSTEM -> Color(0xFF6750A4) // simplified for preview
            }
        },
        onDismiss = onDismiss,
        onItemSelected = { viewModel.setTheme(it); onDismiss() }
    )
}

@Composable
fun LanguageSheet(
    currentLang: AppLanguage,
    languages: List<AppLanguage>,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    SelectionBottomSheet(
        title = stringResource(R.string.select_language),
        items = languages,
        selectedItem = currentLang,
        itemLabel = { lang ->
            "${lang.flag}  " + when (lang) {
                AppLanguage.BENGALI -> stringResource(R.string.bangla)
                AppLanguage.ENGLISH -> stringResource(R.string.english)
                AppLanguage.HINDI -> stringResource(R.string.hindi)
            }
        },
        itemDescription = { lang ->
            when (lang) {
                AppLanguage.BENGALI -> stringResource(R.string.item_desc_bangla)
                AppLanguage.ENGLISH -> stringResource(R.string.item_desc_english)
                AppLanguage.HINDI -> stringResource(R.string.item_desc_hindi)
            }
        },
        itemColor = { lang ->
            when (lang) {
                AppLanguage.BENGALI -> Color(0xFF006A4E)
                AppLanguage.ENGLISH -> Color(0xFF3C3B6E)
                AppLanguage.HINDI -> Color(0xFFFF9933)
            }
        },
        onDismiss = onDismiss,
        onItemSelected = { viewModel.setLanguage(it); onDismiss() }
    )
}

@Composable
fun RestartAppDialog(context: android.content.Context) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(R.string.restore_success_title)) },
        text = { Text(stringResource(R.string.restore_success_msg)) },
        confirmButton = {
            Button(onClick = { (context as? Activity)?.finishAffinity() }) {
                Text(stringResource(R.string.exit_app_btn))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordChangeSheet(onDismiss: () -> Unit, viewModel: SettingsViewModel) {
    // English Comment: Local states for input fields
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // English Comment: Listen for success event to close the sheet
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { event ->
            if (event is UiEvent.ShowToastRes && event.resId == R.string.password_update_success) {
                onDismiss() // Close only on success
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
                .imePadding(), // English Comment: Prevents keyboard from covering input fields
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.change_password),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Current Password field
            ModernInputField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = stringResource(R.string.current_password),
                icon = Icons.Default.Lock,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // New Password field
            ModernInputField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = stringResource(R.string.new_password),
                icon = Icons.Default.Lock,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password field with visibility toggle
            ModernInputField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = stringResource(R.string.confirm_new_password),
                icon = Icons.Default.Lock,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // English Comment: Basic validation before calling ViewModel
                    if (newPassword.isEmpty() || currentPassword.isEmpty()) {
                        showToast(context, "সবগুলো ফিল্ড পূরণ করুন")
                    } else if (newPassword == confirmPassword) {
                        viewModel.updatePassword(currentPassword, newPassword)
                        // Note: We don't call onDismiss() here anymore!
                    } else {
                        showToast(context, context.getString(R.string.password_mismatch))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.update_password), fontWeight = FontWeight.Bold)
            }
        }
    }
}