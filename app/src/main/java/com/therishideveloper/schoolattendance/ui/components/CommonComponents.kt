package com.therishideveloper.schoolattendance.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therishideveloper.schoolattendance.utils.localizeDigitsAndLabels

@Composable
fun CircularProgress(
    percentage: Float,
    size: Int = 80,
    strokeWidth: Int = 8
) {
    val progressColor = when {
        percentage < 30f -> Color(0xFFD32F2F)
        percentage < 60f -> Color(0xFFFBC02D)
        else -> Color(0xFF2E7D32)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size.dp)
    ) {
        CircularProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxSize(),
            color = progressColor,
            strokeWidth = strokeWidth.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Text(
            text = "${percentage.toInt()}%".localizeDigitsAndLabels(),
            fontSize = (size / 5).sp,
            fontWeight = FontWeight.ExtraBold,
            color = progressColor
        )
    }
}