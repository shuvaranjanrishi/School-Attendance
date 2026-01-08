package com.therishideveloper.schoolattendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(top = 48.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "School Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "মডেল হাই স্কুল",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        Text(
            text = "অ্যাডমিন ড্যাশবোর্ড",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}