package com.therishideveloper.schoolattendance.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.therishideveloper.schoolattendance.ui.components.*
import com.therishideveloper.schoolattendance.ui.navigation.AppNavHost
import com.therishideveloper.schoolattendance.ui.navigation.Screen
import com.therishideveloper.schoolattendance.ui.viewmodels.ProfileViewModel
import com.therishideveloper.schoolattendance.utils.AppActions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.therishideveloper.schoolattendance.utils.Result
import com.therishideveloper.schoolattendance.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showLogoutDialog by remember { mutableStateOf(false) }
    val userResult by profileViewModel.userState.collectAsState()
    val schoolData by profileViewModel.schoolState.collectAsState()
    val user = (userResult as? Result.Success)?.data
    val logoutSuccessMessage = stringResource(R.string.logout_success)

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(text = stringResource(R.string.logout_title), style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text(text = stringResource(R.string.logout_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        scope.launch {
                            drawerState.close()
                            delay(200)
                            profileViewModel.logout {
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                                Toast.makeText(context, logoutSuccessMessage, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute != "student_details_screen/{studentId}",
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                DrawerHeader(
                    schoolName = schoolData?.name ?: stringResource(R.string.hint_school_name_empty),
                    schoolAddress = schoolData?.address ?: stringResource(R.string.hint_address_empty),
                    userName = user?.name ?: stringResource(R.string.hint_teacher_name_empty),
                    userEmail = user?.email ?: ""
                )
                DrawerBody(
                    currentRoute = currentRoute,
                    onItemClick = { screen ->
                        scope.launch {
                            drawerState.close()
                            when (screen) {
                                Screen.ShareApp -> AppActions.shareApp(context)
                                Screen.MoreApps -> AppActions.openMoreApps(context)
                                Screen.RatingUs -> AppActions.rateApp(context)
                                Screen.Attendance -> {
                                    navController.navigate(Screen.Attendance.createRoute("")) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                else -> {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onLogoutClick = {
                        scope.launch {
                            drawerState.close()
                            showLogoutDialog = true
                        }
                    }
                )
            }
        }
    ) {
        AppNavHost(
            navController = navController,
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }
}
