package com.therishideveloper.schoolattendance.utils

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity

object PDFHelper {
    fun generatePDF(context: Context, className: String, monthYear: String, attendanceData: List<AttendanceEntity>) {
        val webView = WebView(context)

        // ১. এইচটিএমএল (HTML) দিয়ে টেবিল তৈরি (পিডিএফের কন্টেন্ট)
        val htmlContent = StringBuilder()
        htmlContent.append("""
            <html>
            <head>
                <style>
                    table { width: 100%; border-collapse: collapse; }
                    th, td { border: 1px solid black; padding: 5px; text-align: center; font-size: 10px; }
                    h2 { text-align: center; }
                </style>
            </head>
            <body>
                <h2>Attendance Report: $className ($monthYear)</h2>
                <table>
                    <tr>
                        <th>Student Name</th>
                        ${(1..31).joinToString("") { "<th>$it</th>" }}
                    </tr>
        """.trimIndent())

        val groupedData = attendanceData.groupBy { it.studentName }
        groupedData.forEach { (name, records) ->
            htmlContent.append("<tr><td>$name</td>")
            for (day in 1..31) {
                val dayStr = String.format("%02d", day)
                val status = records.find { it.date.startsWith(dayStr) }?.status ?: "-"
                val displayStatus = if (status == "Present") "P" else if (status == "Absent") "A" else "-"
                htmlContent.append("<td>$displayStatus</td>")
            }
            htmlContent.append("</tr>")
        }

        htmlContent.append("</table></body></html>")

        // ২. ওয়েবভিউতে লোড করে প্রিন্টারে পাঠানো
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "Attendance_Report_$className"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)

                printManager.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder().build()
                )
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent.toString(), "text/html", "utf-8", null)
    }
}