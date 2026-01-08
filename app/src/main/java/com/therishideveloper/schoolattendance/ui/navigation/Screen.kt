package com.therishideveloper.schoolattendance.ui.navigation

import com.therishideveloper.schoolattendance.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val resId: Int, val icon: ImageVector) {
    object Auth : Screen("auth_screen", 0, Icons.Default.Dashboard)
    object Home : Screen("home", R.string.dashboard, Icons.Default.Dashboard)
    object Attendance : Screen(
        route = "attendance?className={className}",
        resId = R.string.take_attendance,
        icon = Icons.Default.CheckCircle
    ) {
        fun createRoute(className: String) = "attendance?className=$className"
    }
    object Students : Screen("students", R.string.student_list, Icons.Default.People)
    object Settings : Screen("settings", R.string.settings, Icons.Default.Settings)
    object UserProfile : Screen("user_profile", R.string.profile, Icons.Default.AccountBox)
    object SchoolProfile : Screen("school_profile", R.string.school_profile_settings, Icons.Default.School)
    object About : Screen("about", R.string.about, Icons.Default.Info)
    object ShareApp : Screen("share_app", R.string.share_app, Icons.Default.Share)
    object MoreApps : Screen("more_apps", R.string.more_apps, Icons.AutoMirrored.Filled.OpenInNew)
    object RatingUs : Screen("rating_us", R.string.rating_us, Icons.Default.StarRate)
    object StudentDetailScreen :
        Screen("student_details_screen/{studentId}", 0, Icons.Default.People) {
        fun createRoute(id: Int) = "student_details_screen/$id"
    }
}