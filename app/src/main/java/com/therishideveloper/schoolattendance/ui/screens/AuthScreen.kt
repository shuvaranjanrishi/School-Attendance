package com.therishideveloper.schoolattendance.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.data.local.entity.UserEntity
import com.therishideveloper.schoolattendance.ui.components.ModernInputField
import com.therishideveloper.schoolattendance.ui.components.showToast
import com.therishideveloper.schoolattendance.ui.viewmodels.ProfileViewModel
import com.therishideveloper.schoolattendance.utils.Result
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: ProfileViewModel,
    onAuthSuccess: () -> Unit
) {
    // --- State Variables ---
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var securityQuestion by remember { mutableStateOf("") }
    var securityAnswer by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    // --- Reset Logic States ---
    var showForgetDialog by remember { mutableStateOf(false) }
    var showResetSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var targetUser by remember { mutableStateOf<UserEntity?>(null) }

    val context = LocalContext.current
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    // --- Resources ---
    val emptyFieldsMsg = stringResource(R.string.error_empty_fields)
    val mismatchMsg = stringResource(R.string.error_password_mismatch)
    val invalidEmailMsg = stringResource(R.string.error_invalid_email)
    val shortPasswordMsg = stringResource(R.string.error_short_password)
    val emailExistsMsg = stringResource(R.string.error_email_exists)
    val invalidCredsMsg = stringResource(R.string.error_invalid_credentials)
    val errorEmailNotFoundMsg = stringResource(R.string.error_email_not_found)
    val errorInvalidAnswerMsg = stringResource(R.string.error_invalid_answer)
    val msgUpdateSuccess = stringResource(R.string.msg_update_success)

    // --- Auth Result Observer ---
    LaunchedEffect(Unit) {
        viewModel.authResult.collect { result ->
            when (result) {
                is Result.Success -> onAuthSuccess()
                is Result.Error -> {
                    val msg = when (result.message) {
                        "EMAIL_EXISTS" -> emailExistsMsg
                        "INVALID_CREDENTIALS" -> invalidCredsMsg
                        else -> result.message
                    }
                    showToast(context, msg)
                }
                else -> {}
            }
        }
    }

    // --- Step-by-Step Forget Password Dialog ---
    if (showForgetDialog) {
        var resetEmail by remember { mutableStateOf("") }
        var resetAnswer by remember { mutableStateOf("") }
        var fetchedQuestion by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showForgetDialog = false },
            title = { Text(stringResource(R.string.title_reset_password)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text(stringResource(R.string.label_email)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = fetchedQuestion == null,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    if (fetchedQuestion != null) {
                        Text(
                            text = "${stringResource(R.string.label_your_question)} $fetchedQuestion",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = resetAnswer,
                            onValueChange = { resetAnswer = it },
                            label = { Text(stringResource(R.string.label_security_answer)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val isEmailValid = resetEmail.trim().matches(emailPattern.toRegex())
                    if (!isEmailValid) {
                        showToast(context, invalidEmailMsg)
                        return@Button
                    }
                    viewModel.viewModelScope.launch {
                        if (fetchedQuestion == null) {
                            val user = viewModel.getUserByEmail(resetEmail)
                            if (user == null) {
                                showToast(context, errorEmailNotFoundMsg)
                            } else {
                                targetUser = user
                                fetchedQuestion = user.securityQuestion ?: "No Question Set"
                            }
                        } else {
                            if (resetAnswer.isBlank()) {
                                showToast(context, emptyFieldsMsg)
                            } else if (targetUser?.securityAnswer?.trim().equals(resetAnswer.trim(), ignoreCase = true)) {
                                showForgetDialog = false
                                showResetSheet = true
                            } else {
                                showToast(context, errorInvalidAnswerMsg)
                            }
                        }
                    }
                }) {
                    Text(if (fetchedQuestion == null) stringResource(R.string.btn_check) else stringResource(R.string.ok_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgetDialog = false }) { Text(stringResource(R.string.cancel_btn)) }
            }
        )
    }

    // --- Updated Bottom Sheet for Password Update ---
    if (showResetSheet && targetUser != null) {
        ModalBottomSheet(
            onDismissRequest = { showResetSheet = false },
            sheetState = sheetState
        ) {
            var newPass by remember { mutableStateOf("") }
            var confPass by remember { mutableStateOf("") }
            var newQues by remember { mutableStateOf(targetUser?.securityQuestion ?: "") }
            var newAns by remember { mutableStateOf(targetUser?.securityAnswer ?: "") }
            var isPassHidden by remember { mutableStateOf(true) }
            var isConfHidden by remember { mutableStateOf(true) }

            Column(modifier = Modifier.padding(24.dp).padding(bottom = 40.dp).verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.title_reset_sheet), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // New Password with Eye Button
                ModernInputField(
                    value = newPass, onValueChange = { newPass = it },
                    label = stringResource(R.string.label_new_password), icon = Icons.Default.Lock,
                    visualTransformation = if (isPassHidden) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        IconButton(onClick = { isPassHidden = !isPassHidden }) {
                            Icon(if (isPassHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Confirm New Password with Eye Button
                ModernInputField(
                    value = confPass, onValueChange = { confPass = it },
                    label = stringResource(R.string.label_confirm_password), icon = Icons.Default.Lock,
                    visualTransformation = if (isConfHidden) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        IconButton(onClick = { isConfHidden = !isConfHidden }) {
                            Icon(if (isConfHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.title_change_security),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                ModernInputField(value = newQues, onValueChange = { newQues = it }, label = stringResource(R.string.label_security_question), icon = Icons.Default.QuestionAnswer)
                Spacer(modifier = Modifier.height(12.dp))
                ModernInputField(value = newAns, onValueChange = { newAns = it }, label = stringResource(R.string.label_security_answer), icon = Icons.Default.CheckCircle)

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        when {
                            newPass.length < 6 -> showToast(context, shortPasswordMsg)
                            newPass != confPass -> showToast(context, mismatchMsg)
                            newQues.isBlank() || newAns.isBlank() -> showToast(context, emptyFieldsMsg)
                            else -> {
                                viewModel.insertOrUpdate(targetUser!!.copy(password = newPass, securityQuestion = newQues, securityAnswer = newAns))
                                showResetSheet = false
                                showToast(context, msgUpdateSuccess)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.btn_update_securely)) }
            }
        }
    }

    // --- Main UI ---
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = R.drawable.ic_about_school_art), contentDescription = null, modifier = Modifier.size(180.dp).padding(bottom = 16.dp))
        Text(text = if (isSignUp) stringResource(R.string.title_signup) else stringResource(R.string.title_login), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(24.dp))

        // Email Field (Autofill support added via KeyboardType)
        ModernInputField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.label_email),
            icon = Icons.Default.Email,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        ModernInputField(
            value = password, onValueChange = { password = it },
            label = stringResource(R.string.label_password), icon = Icons.Default.Lock,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, modifier = Modifier.size(20.dp))
                }
            },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        if (isSignUp) {
            Spacer(modifier = Modifier.height(16.dp))
            ModernInputField(
                value = confirmPassword, onValueChange = { confirmPassword = it },
                label = stringResource(R.string.label_confirm_password), icon = Icons.Default.Lock,
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, modifier = Modifier.size(20.dp))
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ModernInputField(value = securityQuestion, onValueChange = { securityQuestion = it }, label = stringResource(R.string.label_security_question), icon = Icons.Default.QuestionAnswer)
            Spacer(modifier = Modifier.height(16.dp))
            ModernInputField(value = securityAnswer, onValueChange = { securityAnswer = it }, label = stringResource(R.string.label_security_answer), icon = Icons.Default.CheckCircle)
        }

        if (!isSignUp) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { showForgetDialog = true }) {
                    Text(stringResource(R.string.btn_forget_password), fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val isEmailValid = email.trim().matches(emailPattern.toRegex())
                when {
                    email.isBlank() || password.isBlank() || (isSignUp && (confirmPassword.isBlank() || securityQuestion.isBlank() || securityAnswer.isBlank())) -> {
                        showToast(context, emptyFieldsMsg)
                    }
                    !isEmailValid -> showToast(context, invalidEmailMsg)
                    password.length < 6 -> showToast(context, shortPasswordMsg)
                    isSignUp && password != confirmPassword -> showToast(context, mismatchMsg)
                    else -> viewModel.performAuthWithSecurity(email, password, isSignUp, securityQuestion, securityAnswer)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (isSignUp) stringResource(R.string.btn_signup) else stringResource(R.string.btn_login), fontWeight = FontWeight.SemiBold)
        }

        TextButton(onClick = { isSignUp = !isSignUp; confirmPassword = "" }) {
            Text(if (isSignUp) stringResource(R.string.msg_has_account) else stringResource(R.string.msg_no_account))
        }
    }
}