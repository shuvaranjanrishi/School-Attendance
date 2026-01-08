package com.therishideveloper.schoolattendance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.utils.ClassTypes

@Composable
fun GenericFilterRow(
    classes: List<ClassTypes>,
    selectedClass: String?,
    onClassSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedClass == null,
                onClick = { onClassSelected(null) },
                label = { Text(stringResource(R.string.all)) },
                shape = RoundedCornerShape(10.dp),
                leadingIcon = if (selectedClass == null) {
                    { SelectedIcon() }
                } else null
            )
        }
        items(classes) { classType ->
            FilterChip(
                selected = selectedClass == classType.code,
                onClick = { onClassSelected(classType.code) },
                label = {
                    Text(stringResource(classType.stringRes))
                },
                shape = RoundedCornerShape(10.dp),
                leadingIcon = if (selectedClass == classType.code) {
                    { SelectedIcon() }
                } else null
            )
        }
    }
}

@Composable
fun SelectedIcon() {
    Icon(
        imageVector = Icons.Default.Done,
        contentDescription = null,
        modifier = Modifier.size(FilterChipDefaults.IconSize),
        tint = MaterialTheme.colorScheme.primary
    )
}
