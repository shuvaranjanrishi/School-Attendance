package com.therishideveloper.schoolattendance.utils

import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object CSVHelper {
    fun generateAttendanceCSV(
        className: String,
        monthYear: String,
        attendanceData: List<AttendanceEntity>
    ): String {
        val builder = StringBuilder()

        // ১. হেডার সারি (Header Row)
        builder.append("Class: $className, Report: $monthYear\n")
        builder.append("Student Name,")
        for (i in 1..31) {
            builder.append("$i,")
        }
        builder.append("\n")

        // ২. ডাটা সারি (Data Rows)
        val groupedData = attendanceData.groupBy { it.studentName }
        groupedData.forEach { (name, records) ->
            builder.append("$name,")
            for (day in 1..31) {
                val dayStr = String.format("%02d", day)
                val status = records.find { it.date.startsWith(dayStr) }?.status ?: "-"
                val shortStatus = if (status == "Present") "P" else if (status == "Absent") "A" else "-"
                builder.append("$shortStatus,")
            }
            builder.append("\n")
        }

        return builder.toString()
    }

    fun saveAndShareCSV(context: Context, fileName: String, content: String) {
        try {
            val file = File(context.cacheDir, "$fileName.csv")
            val out = FileOutputStream(file)
            out.write(content.toByteArray())
            out.close()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Attendance Report")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Report"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}