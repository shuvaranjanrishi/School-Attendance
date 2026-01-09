package com.therishideveloper.schoolattendance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.therishideveloper.schoolattendance.ui.navigation.Screen
import com.therishideveloper.schoolattendance.R

@Composable
fun DrawerBody(
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
    onLogoutClick: () -> Unit
) {
    val mainItems = listOf(
        Screen.Home,
        Screen.Attendance,
        Screen.Students,
        Screen.Reports
    )

    val otherItems = listOf(
        Screen.Settings,
        Screen.About,
        Screen.ShareApp,
        Screen.MoreApps,
        Screen.RatingUs,
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            mainItems.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(stringResource(item.resId)) },
                    selected = currentRoute == item.route,
                    onClick = { onItemClick(item) },
                    icon = { Icon(item.icon, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            otherItems.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(stringResource(item.resId)) },
                    selected = currentRoute == item.route,
                    onClick = { onItemClick(item) },
                    icon = { Icon(item.icon, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.logout)) },
            selected = false,
            onClick = onLogoutClick,
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedTextColor = MaterialTheme.colorScheme.error,
                unselectedIconColor = MaterialTheme.colorScheme.error
            )
        )
    }
}