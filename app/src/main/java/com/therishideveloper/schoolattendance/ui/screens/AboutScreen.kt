package com.therishideveloper.schoolattendance.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.utils.AppActions

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val appVersion = remember { AppActions.getAppVersionName(context) }
    val greyColor = Color.Gray

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "${stringResource(R.string.label_version)} $appVersion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.about_app_desc),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
            Image(
                painter = painterResource(id = R.drawable.ic_about_school_art),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(24.dp)
                    .weight(1f),
                contentScale = ContentScale.Fit
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.pdf_footer_brand_name),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.pdf_footer_dev_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = greyColor
                    )
                    Text(
                        text = stringResource(R.string.pdf_footer_rights),
                        style = MaterialTheme.typography.labelLarge,
                        color = greyColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { AppActions.contactUs(context) }) {
                    Text(stringResource(R.string.contact_us))
                }
                TextButton(onClick = { AppActions.openPrivacyPolicy(context) }) {
                    Text(stringResource(R.string.privacy_policy))
                }
            }
        }
    }
}