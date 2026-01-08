package com.therishideveloper.schoolattendance.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import com.therishideveloper.schoolattendance.R

object AppActions {

    fun shareApp(context: Context) {
        val message = context.getString(R.string.share_app_message) + "\n" +
                "https://play.google.com/store/apps/details?id=${context.packageName}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(
            Intent.createChooser(
                intent,
                context.getString(R.string.share_chooser_title)
            )
        )
    }

    fun openMoreApps(context: Context) {
        val developerName = "The Rishi Developer"
        val marketUri = Uri.parse("market://search?q=pub:$developerName")
        val webSearchUrl = "https://play.google.com/store/search?q=pub:$developerName&c=apps"

        launchIntent(context, marketUri, webSearchUrl)
    }

    fun rateApp(context: Context) {
        val marketUri = Uri.parse("market://details?id=${context.packageName}")
        val webUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"

        launchIntent(context, marketUri, webUrl)
    }

    private fun launchIntent(context: Context, marketUri: Uri, webUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            setPackage("com.android.vending")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
        }
    }

    fun contactUs(context: Context) {
        val email = context.getString(R.string.dev_email)
        val subject = "School Attendance App Query"
        val body = "\n\n--- App Version: 1.0.0 ---"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "ইমেইল পাঠান..."))
        } catch (e: Exception) {
            Toast.makeText(context, "ইমেইল অ্যাপ খুঁজে পাওয়া যায়নি!", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPrivacyPolicy(context: Context) {
        val privacyUrl = "https://your-google-doc-link-here.com"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl))
        context.startActivity(intent)
    }

    fun getAppVersionName(context: Context): String? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}