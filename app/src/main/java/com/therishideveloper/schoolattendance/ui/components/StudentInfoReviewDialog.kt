package com.therishideveloper.schoolattendance.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.ui.screens.StudentFormState
import com.therishideveloper.schoolattendance.utils.ClassTypes
import com.therishideveloper.schoolattendance.utils.CountryTypes
import com.therishideveloper.schoolattendance.utils.GenderTypes
import com.therishideveloper.schoolattendance.utils.IdTypes
import com.therishideveloper.schoolattendance.utils.ReligionTypes
import com.therishideveloper.schoolattendance.utils.localizeDigitsAndLabels

@Composable
fun StudentReviewDialog(
    isEditMode: Boolean,
    state: StudentFormState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {}, // Custom buttons in content
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.92f),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.review_info_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                VerticalSpace(16)

                // English Comment: Square Profile Image
                Surface(
                    modifier = Modifier.size(140.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    if (state.selectedImage != null) {
                        AsyncImage(
                            model = state.selectedImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(30.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                VerticalSpace(20)

                val className = stringResource(ClassTypes.fromCode(state.sClass).stringRes)
                val gender = stringResource(GenderTypes.fromCode(state.gender).stringRes)
                val religion = stringResource(ReligionTypes.fromCode(state.religion).stringRes)
                val country = stringResource(CountryTypes.fromCode(state.sCountry).stringRes)
                val idLabel =
                    if (state.idType == IdTypes.NID.code) R.string.label_nid else R.string.label_birth_reg

                // English Comment: Label and Value alignment
                val details = listOf(
                    stringResource(R.string.label_name) to state.name,
                    stringResource(R.string.label_roll) to state.roll,
                    stringResource(R.string.label_class) to className,
                    stringResource(R.string.label_dob) to state.dateOfBirth,
                    stringResource(R.string.label_age) to state.age,
                    stringResource(idLabel) to state.nidOrBirthReg,
                    stringResource(R.string.label_phone) to state.phone,
                    stringResource(R.string.label_father) to state.fatherName,
                    stringResource(R.string.label_mother) to state.motherName,
                    stringResource(R.string.label_gender) to gender,
                    stringResource(R.string.label_religion) to religion,
                    stringResource(R.string.label_address) to state.address,
                    stringResource(R.string.label_country) to country,
                    stringResource(R.string.label_admission_date) to state.admissionDate
                )

                details.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "$label :",
                            modifier = Modifier.weight(0.4f),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = value.ifBlank { "---" }.localizeDigitsAndLabels(),
                            modifier = Modifier.weight(0.6f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                VerticalSpace(24)

                // English Comment: Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f)) {
//                        Text(stringResource(R.string.btn_confirm))
                        Text(
                            if (isEditMode) {
                                stringResource(R.string.btn_update)
                            } else {
                                stringResource(R.string.btn_confirm_admission)
                            }
                        )
                    }
                }
            }
        }
    )
}