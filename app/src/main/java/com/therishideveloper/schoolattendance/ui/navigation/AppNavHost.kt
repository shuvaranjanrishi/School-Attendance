package com.therishideveloper.schoolattendance.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.therishideveloper.schoolattendance.ui.screens.*
import com.therishideveloper.schoolattendance.ui.viewmodels.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // এটি অবশ্যই থাকতে হবে

@Composable
fun AppNavHost(
    navController: NavHostController,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val attendanceViewModel: AttendanceViewModel = hiltViewModel()
    val studentViewModel: StudentViewModel = hiltViewModel()
    val reportViewModel: ReportViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val schoolViewModel: SchoolViewModel = hiltViewModel()
    val isLoggedIn by settingsViewModel.isLoggedInFlow.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn == true) Screen.Home.route else Screen.Auth.route,
        modifier = modifier
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = profileViewModel,
                onAuthSuccess = {}
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                homeViewModel = homeViewModel,
                onMenuClick = onMenuClick
            )
        }
        composable(
            route = Screen.Attendance.route,
            arguments = listOf(navArgument("classCode") { defaultValue = "" })
        ) { backStackEntry ->
            val classCode = backStackEntry.arguments?.getString("classCode") ?: ""
            if (classCode.isEmpty()) {
                AttendanceMainScreen(
                    viewModel = attendanceViewModel,
                    onClassClick = { selectedClass ->
                        navController.navigate(Screen.Attendance.createRoute(selectedClass))
                    },
                    onMenuClick = onMenuClick
                )
            } else {
                AttendanceTakeScreen(
                    viewModel = attendanceViewModel,
                    classCode = classCode,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.Students.route) {
            StudentListScreen(
                viewModel = studentViewModel,
                onMenuClick = onMenuClick,
                onStudentClick = { id ->
                    navController.navigate(Screen.StudentDetailScreen.createRoute(id))
                }
            )
        }
        composable(Screen.ReportTypeSelectionScreen.route) {
            ReportTypeSelectionScreen(
                onMenuClick = onMenuClick,
                onClassWiseClick = {
                    navController.navigate(Screen.ReportsSummeryClassWiseScreen.route)
                },
                onStudentWiseClick = {
                    navController.navigate(Screen.ReportsSummaryStudentWiseScreen.route)
                }
            )
        }
        composable(Screen.ReportsSummeryClassWiseScreen.route) {
            ReportSummeryClassWiseScreen(
                onMenuClick = onMenuClick,
                viewModel = reportViewModel,
                onClassClick = { classCode ->
                    val month = reportViewModel.selectedMonth.value
                    val year = reportViewModel.selectedYear.value
                    navController.navigate(
                        Screen.ClassWiseDetailsReportScreen.createRoute(classCode, month, year)
                    )
                }
            )
        }
        composable(Screen.ReportsSummaryStudentWiseScreen.route) {
            ReportSummaryStudentWiseScreen(
                viewModel = reportViewModel,
                onMenuClick = { navController.popBackStack() }, // ব্যাক বাটন হিসেবে
                onStudentClick = { id ->
                    navController.navigate(Screen.MonthlyReportDetailsCalendar.createRoute(id))
                }
            )
        }
        composable(
            route = Screen.ClassWiseDetailsReportScreen.route,
            arguments = listOf(
                navArgument("classCode") { type = NavType.StringType },
                navArgument("month") { type = NavType.StringType },
                navArgument("year") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val classCode = backStackEntry.arguments?.getString("classCode") ?: ""
            val month = backStackEntry.arguments?.getString("month") ?: ""
            val year = backStackEntry.arguments?.getString("year") ?: ""

            ReportClassWiseDetailsScreen(
                onBack = { navController.popBackStack() },
                viewModel = reportViewModel,
                classCode = classCode,
                month = month,
                year = year
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onMenuClick = onMenuClick,
                viewModel = settingsViewModel,
                onNavigateToSchoolProfile = { navController.navigate(Screen.SchoolProfile.route) },
                onNavigateToUserProfile = { navController.navigate(Screen.UserProfile.route) }
            )
        }

        composable(Screen.SchoolProfile.route) {
            var isEditMode by remember { mutableStateOf(false) }
            if (isEditMode) {
                EditSchoolScreen(
                    viewModel = schoolViewModel,
                    onBackClick = { isEditMode = false }
                )
            } else {
                SchoolProfileScreen(
                    viewModel = schoolViewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { isEditMode = true }
                )
            }
        }

        composable(Screen.UserProfile.route) {
            var isEditMode by remember { mutableStateOf(false) }
            if (isEditMode) {
                EditUserProfileScreen(
                    viewModel = profileViewModel,
                    onBackClick = { isEditMode = false }
                )
            } else {
                UserProfileScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { isEditMode = true }
                )
            }
        }

        composable(
            route = Screen.StudentDetailScreen.route,
            arguments = listOf(navArgument("studentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
            StudentDetailScreen(
                studentId = studentId,
                navController = navController,
                viewModel = studentViewModel
            )
        }
        composable(
            route = Screen.MonthlyReportDetailsCalendar.route,
            arguments = listOf(navArgument("studentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0

            ReportStudentWiseDetailsScreen(
                studentId = studentId,
                viewModel = reportViewModel, // ভিউমডেল পাস করে দিন
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.About.route) {
            AboutScreen()
        }
    }
}
