package com.therishideveloper.schoolattendance.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.os.LocaleListCompat
import androidx.navigation.compose.rememberNavController
import com.therishideveloper.schoolattendance.data.local.ThemeMode
import com.therishideveloper.schoolattendance.ui.navigation.AppNavHost
import com.therishideveloper.schoolattendance.ui.screens.MainScreen
import com.therishideveloper.schoolattendance.ui.theme.SchoolAttendanceTheme
import com.therishideveloper.schoolattendance.ui.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoggedInFlow.value == null
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val currentLang by viewModel.currentLanguage.collectAsState()
            val isLoggedIn by viewModel.isLoggedInFlow.collectAsState()

            val config = LocalConfiguration.current
            val locale = Locale(currentLang.code)
            config.setLocale(locale)

            CompositionLocalProvider(LocalConfiguration provides config) {
                key(currentLang.code) {
                    SchoolAttendanceTheme(
                        darkTheme = when (themeMode) {
                            ThemeMode.LIGHT -> false
                            ThemeMode.DARK -> true
                            ThemeMode.SYSTEM -> isSystemInDarkTheme()
                        }
                    ) {
                        if (isLoggedIn != null) {
                            if (isLoggedIn == true) {
                                MainScreen()
                            } else {
                                val navController = rememberNavController()
                                AppNavHost(
                                    navController = navController,
                                    onMenuClick = {}
                                )
                            }
                        }
                    }
                }
            }

            LaunchedEffect(currentLang) {
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(currentLang.code)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }
        }
    }
}
