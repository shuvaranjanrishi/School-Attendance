package com.therishideveloper.schoolattendance.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.utils.ClassTypes
import com.therishideveloper.schoolattendance.utils.GenderTypes
@Composable
fun GenderDropdownFilter(
    genderOptions: List<GenderTypes>,
    selectedGenderCode: String?,
    onGenderSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentSelectedGender = genderOptions.find { it.code == selectedGenderCode }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(56.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text(
                text = currentSelectedGender?.let { stringResource(it.stringRes) }
                    ?: stringResource(R.string.all)
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.all)) },
                onClick = {
                    onGenderSelected(null)
                    expanded = false
                }
            )

            genderOptions.forEach { gender ->
                HorizontalDivider(thickness = 0.5.dp)
                DropdownMenuItem(
                    text = { Text(stringResource(gender.stringRes)) },
                    onClick = {
                        onGenderSelected(gender.code)
                        expanded = false
                    }
                )
            }
        }
    }
}

//@Composable
//fun GenderDropdownFilter(
//    selectedGender: String?,
//    onGenderSelected: (String?) -> Unit
//) {
//    var expanded by remember { mutableStateOf(false) }
//    val genderOptions = listOf("ছেলে", "মেয়ে")
//
//    Box {
//        OutlinedButton(
//            onClick = { expanded = true },
//            shape = RoundedCornerShape(12.dp),
//            modifier = Modifier.height(56.dp),
//            contentPadding = PaddingValues(horizontal = 12.dp)
//        ) {
//            Text(text = selectedGender ?: "লিঙ্গ")
//            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
//        }
//
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false }
//        ) {
//            DropdownMenuItem(
//                text = { Text("সব") },
//                onClick = {
//                    onGenderSelected(null)
//                    expanded = false
//                }
//            )
//            HorizontalDivider(thickness = 1.dp)
//            genderOptions.forEach { gender ->
//                DropdownMenuItem(
//                    text = { Text(gender) },
//                    onClick = {
//                        onGenderSelected(gender)
//                        expanded = false
//                    }
//                )
//                HorizontalDivider(thickness = 1.dp)
//            }
//        }
//    }
//}