package com.therishideveloper.schoolattendance.data.local.backup

import android.content.Context
import android.os.Environment
import com.therishideveloper.schoolattendance.data.local.AppDatabase
import com.therishideveloper.schoolattendance.di.DatabaseModule.DB_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import com.therishideveloper.schoolattendance.utils.Result

@Singleton
class DatabaseBackupManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: AppDatabase
) {
    val backupFileName = DB_NAME+"_backup.db"

    private fun getPublicBackupFolder(): File {
        val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appFolder = File(downloadFolder, "School Attendance/Backup")
        if (!appFolder.exists()) appFolder.mkdirs()
        return appFolder
    }

    private fun checkpoint() {
        try {
            val db = database.openHelper.writableDatabase
            db.execSQL("PRAGMA wal_checkpoint(FULL);")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun backupDatabase(): Result<String> {
        return try {
            checkpoint()

            val dbFile = context.getDatabasePath(DB_NAME)
            val backupFile = File(getPublicBackupFolder(), backupFileName)

            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }
            Result.Success("Backup Saved: Downloads/School Attendance/Backup")
        } catch (e: Exception) {
            Result.Error("Backup failed: ${e.message}")
        }
    }

    fun restoreDatabase(): Result<String> {
        return try {
            database.close()

            val dbFile = context.getDatabasePath(DB_NAME)
            val backupFile = File(getPublicBackupFolder(), backupFileName)

            if (!backupFile.exists()) {
                return Result.Error("Backup file not found in Downloads!")
            }

            FileInputStream(backupFile).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }

            context.getDatabasePath("$DB_NAME-wal").delete()
            context.getDatabasePath("$DB_NAME-shm").delete()
            Result.Success("Restore successful! Please restart the app.")
        } catch (e: Exception) {
            Result.Error("Restore failed: ${e.message}")
        }
    }
}