package com.therishideveloper.schoolattendance.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.util.Log
import androidx.core.content.FileProvider
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.data.local.SettingsManager
import com.therishideveloper.schoolattendance.data.local.entity.StudentEntity
import com.therishideveloper.schoolattendance.domain.repository.SchoolRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import java.util.Locale
import androidx.core.graphics.toColorInt
import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import java.text.SimpleDateFormat
import java.util.Date

@Singleton
class PdfGenerator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val schoolRepository: SchoolRepository,
    private val settingsManager: SettingsManager
) {

    suspend fun generateStudentProfilePdf(student: StudentEntity): Result<File> {
        val school = schoolRepository.getSchool()
        val (pdfDocument, page, canvas, paint, pageWidth, pageHeight) = PdfHelper.setupPdfPage(false)

        val schoolName = school?.name ?: getLabel(R.string.pdf_hint_school_name)
        val schoolAddress = school?.address ?: getLabel(R.string.pdf_hint_school_address)
        val pageTitle = getLabel(R.string.pdf_student_profile)
        val studentProfile = getLabel(R.string.pdf_profile_photo)
        PdfHelper.drawPortraitHeader(
            student,
            school,
            canvas,
            pageWidth,
            paint,
            schoolName,
            schoolAddress,
            pageTitle,
            studentProfile
        )

        var yPos = 240f
        val lineSpacing = 38f

        // বেসিক ইনফো
        PdfHelper.drawDataRow(
            canvas,
            paint,
            getLabel(R.string.pdf_label_name),
            student.name,
            60f,
            yPos,
            true
        )
        yPos += lineSpacing

        val rollValue = student.rollNo.localizeDigitsAndLabels()
        val classValue =
            getLabel(ClassTypes.fromCode(student.classCode).stringRes).replace("Class ", "")
        PdfHelper.drawDataRow(
            canvas,
            paint,
            getLabel(R.string.pdf_label_roll),
            rollValue,
            60f,
            yPos,
            true
        )
        PdfHelper.drawDataRow(
            canvas,
            paint,
            getLabel(R.string.pdf_label_class),
            classValue,
            (pageWidth / 2f),
            yPos,
            true
        )
        yPos += lineSpacing

        // আইডি ও পার্সোনাল ডিটেইলস
        val idLabelRes =
            if (student.idType == IdTypes.NID.code) R.string.pdf_label_nid else R.string.pdf_label_birth_reg
        PdfHelper.drawDataRow(
            canvas,
            paint,
            getLabel(idLabelRes),
            student.nidOrBirthReg.localizeDigitsAndLabels(),
            60f,
            yPos
        )
        yPos += lineSpacing

        val details = listOf(
            R.string.pdf_label_dob to student.dateOfBirth.localizeDigitsAndLabels(),
            R.string.pdf_label_age to student.age.localizeDigitsAndLabels(),
            R.string.pdf_label_father to student.fatherName.ifBlank { getLabel(R.string.not_given) },
            R.string.pdf_label_mother to student.motherName.ifBlank { getLabel(R.string.not_given) },
            R.string.pdf_label_mobile to student.phone.localizeDigitsAndLabels()
        )

        for ((res, value) in details) {
            PdfHelper.drawDataRow(canvas, paint, getLabel(res), value, 60f, yPos)
            yPos += lineSpacing
        }

        // জেন্ডার ও ধর্ম (একই লাইনে)
        PdfHelper.drawDataRow(
            canvas,
            paint,
            getLabel(R.string.pdf_label_gender),
            getLabel(GenderTypes.fromCode(student.genderCode).stringRes),
            60f,
            yPos
        )
        PdfHelper.drawDataRow(
            canvas,
            paint,
            getLabel(R.string.pdf_label_religion),
            getLabel(ReligionTypes.fromCode(student.religionCode).stringRes),
            (pageWidth / 2f),
            yPos
        )
        yPos += lineSpacing

        // অন্যান্য তথ্য
        PdfHelper.drawDataRow(
            canvas,
            paint,
            getLabel(R.string.pdf_label_blood_group),
            getLabel(BloodGroupTypes.fromCode(student.bloodGroup).stringRes),
            60f,
            yPos
        )
        yPos += lineSpacing
        PdfHelper.drawDataRow(
            canvas,
            paint,
            getLabel(R.string.pdf_label_address),
            student.address.ifBlank { getLabel(R.string.not_given) },
            60f,
            yPos
        )
        yPos += lineSpacing
        PdfHelper.drawDataRow(
            canvas,
            paint,
            getLabel(R.string.pdf_label_country),
            getLabel(CountryTypes.fromCode(student.country).stringRes),
            60f,
            yPos
        )
        yPos += lineSpacing
        PdfHelper.drawDataRow(
            canvas,
            paint,
            getLabel(R.string.pdf_label_admission_date),
            student.admissionDate.localizeDigitsAndLabels(),
            60f,
            yPos
        )

        //draw footer
        val devName = getLabel(R.string.dev_name)
        val devAuthorName = getLabel(R.string.dev_author_name)
        val devCopyRights = getLabel(R.string.dev_copy_rights)
        PdfHelper.drawProfileFooter(
            canvas,
            pageWidth,
            pageHeight,
            paint,
            devName,
            devAuthorName,
            devCopyRights
        )

        pdfDocument.finishPage(page)

        val fileName = "Std_${student.rollNo}_${student.name.replace(" ", "_")}.pdf"
        val title = getLabel(R.string.pdf_download_success)
        val desc = getLabel(R.string.pdf_download_desc).format(student.name)

        return PdfHelper.finalizePdf(
            context,
            pdfDocument,
            Constants.FOLDER_PROFILES,
            fileName,
            title,
            desc
        )
    }

    suspend fun generateMonthlyAttendanceReport(
        classCode: String,
        monthYear: String,
        attendanceData: List<AttendanceEntity>
    ): Result<File> {
        val school = schoolRepository.getSchool()
        val (pdfDocument, page, canvas, paint, pageWidth, pageHeight) = PdfHelper.setupPdfPage(true)

        //draw header
        val schoolName = school?.name ?: getLabel(R.string.pdf_hint_school_name)
        val schoolAddress = school?.address ?: getLabel(R.string.pdf_hint_school_address)
        val className = getLabel(ClassTypes.fromCode(classCode).stringRes).replace("Class ", "")
        val pageTitle = "${getLabel(R.string.label_report)}: $className ($monthYear)"
        PdfHelper.drawCommonHeader(
            canvas,
            school,
            pageWidth,
            paint,
            pageTitle,
            schoolName,
            schoolAddress
        )

        //table dimension and color
        val startX = 30f
        val startY = 160f
        val nameWidth = 140f
        val totalTableWidth = pageWidth - 60f
        val dayWidth = (totalTableWidth - nameWidth) / 31f
        val rowHeight = 25f
        val borderColor = "#CCCCCC".toColorInt()

        paint.style = Paint.Style.FILL
        paint.color = "#F5F5F5".toColorInt()
        canvas.drawRect(startX, startY, startX + totalTableWidth, startY + rowHeight, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = borderColor
        canvas.drawRect(startX, startY, startX + totalTableWidth, startY + rowHeight, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textSize = 10f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(getLabel(R.string.label_name), startX + 8, startY + 17, paint)

        paint.textAlign = Paint.Align.CENTER
        for (day in 1..31) {
            val xPos = startX + nameWidth + ((day - 1) * dayWidth) + (dayWidth / 2)
            val dividerX = startX + nameWidth + ((day - 1) * dayWidth)

            canvas.drawText(day.toString().localizeDigitsAndLabels(), xPos, startY + 17, paint)

            paint.style = Paint.Style.STROKE
            paint.color = borderColor
            canvas.drawLine(dividerX, startY, dividerX, startY + rowHeight, paint)
            paint.style = Paint.Style.FILL
            paint.color = Color.BLACK
        }

        var currentY = startY + rowHeight
        val groupedData = attendanceData.groupBy { it.studentName }

        groupedData.forEach { (name, records) ->
            if (currentY > pageHeight - 80f) return@forEach

            paint.style = Paint.Style.STROKE
            paint.color = borderColor
            canvas.drawRect(startX, currentY, startX + totalTableWidth, currentY + rowHeight, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.BLACK
            paint.isFakeBoldText = false
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(name, startX + 8, currentY + 17, paint)

            paint.textAlign = Paint.Align.CENTER
            for (day in 1..31) {
                val xPos = startX + nameWidth + ((day - 1) * dayWidth) + (dayWidth / 2)
                val dividerX = startX + nameWidth + ((day - 1) * dayWidth)
                val dayStr = String.format("%02d", day)
                val record = records.find { it.date.startsWith(dayStr) }

                val (status, color) = when (record?.status) {
                    "Present" -> "P" to "#2E7D32"
                    "Absent" -> "A" to "#D32F2F"
                    else -> "-" to "#9E9E9E"
                }

                // কলাম ডিভাইডার লাইন (একই borderColor)
                paint.style = Paint.Style.STROKE
                paint.color = borderColor
                canvas.drawLine(dividerX, currentY, dividerX, currentY + rowHeight, paint)

                // স্ট্যাটাস টেক্সট (P/A/-)
                paint.style = Paint.Style.FILL
                paint.color = color.toColorInt()
                canvas.drawText(status, xPos, currentY + 17, paint)
            }
            currentY += rowHeight
        }

        //draw footer
        val genDate = DateTimeUtils.getFileCreatedDate().localizeDigitsAndLabels()
        val devName = getLabel(R.string.dev_name)
        val devLabel = getLabel(R.string.label_developer)
        val genLabel = getLabel(R.string.generated_on)

        PdfHelper.drawCommonFooter(
            canvas = canvas,
            pageWidth = pageWidth,
            pageHeight = pageHeight,
            paint = paint,
            devName = devName,
            devLabel = devLabel,
            genLabel = genLabel,
            genDate = genDate
        )
        pdfDocument.finishPage(page)

        val fileName =
            "Report_${className.replace(" ", "_")}_${monthYear.replace(" ", "_")}.pdf"
        val title = getLabel(R.string.pdf_download_success)
        val desc = "$className - $monthYear"

        return PdfHelper.finalizePdf(
            context,
            pdfDocument,
            Constants.FOLDER_REPORTS,
            fileName,
            title,
            desc
        )
    }

    suspend fun generateStudentMonthlyDetailsReport(
        studentName: String,
        className: String,
        monthYear: String,
        rollNo: String,
        total: Int,
        present: Int,
        absent: Int,
        percent: Float,
        records: List<AttendanceEntity>,
        isSharing: Boolean = false
    ): Result<File> {
        val school = schoolRepository.getSchool()
        val (pdfDocument, page, canvas, paint, pageWidth, pageHeight) = PdfHelper.setupPdfPage(false)

        val title = "${getLabel(R.string.monthly_attendance_report)}: $monthYear"
        val schoolName = school?.name ?: getLabel(R.string.pdf_hint_school_name)
        val schoolAddress = school?.address ?: getLabel(R.string.pdf_hint_school_address)
        PdfHelper.drawCommonHeader(
            canvas,
            school,
            pageWidth,
            paint,
            title,
            schoolName,
            schoolAddress
        )

        canvas.drawRoundRect(40f, 165f, (pageWidth - 40f), 265f, 15f, 15f, paint)

        val cardTop = 165f
        val cardBottom = 265f
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(245, 245, 245)
        canvas.drawRoundRect(40f, cardTop, (pageWidth - 40).toFloat(), cardBottom, 15f, 15f, paint)

        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(
            (pageWidth / 2).toFloat(),
            cardTop + 20f,
            (pageWidth / 2).toFloat(),
            cardBottom - 20f,
            paint
        )

        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = true
        paint.textSize = 18f
        paint.color = "#1565C0".toColorInt() // ব্লু কালার
        canvas.drawText(studentName, 65f, cardTop + 35f, paint)

        paint.textSize = 13f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("${getLabel(R.string.label_class)}: $className", 65f, cardTop + 60f, paint)
        canvas.drawText(
            "${getLabel(R.string.label_roll)}: ${rollNo.localizeDigitsAndLabels()}",
            65f,
            cardTop + 80f,
            paint
        )

        val rightInfoStart = (pageWidth / 2).toFloat() + 25f
        paint.textSize = 12f
        paint.color = Color.BLACK
        canvas.drawText(
            "${getLabel(R.string.total_days)}: ${
                total.toString().localizeDigitsAndLabels()
            }", rightInfoStart, cardTop + 35f, paint
        )

        paint.color = "#2E7D32".toColorInt()
        canvas.drawText(
            "${getLabel(R.string.present)}: ${
                present.toString().localizeDigitsAndLabels()
            }", rightInfoStart, cardTop + 55f, paint
        )

        paint.color = "#D32F2F".toColorInt()
        canvas.drawText(
            "${getLabel(R.string.absent)}: ${
                absent.toString().localizeDigitsAndLabels()
            }", rightInfoStart, cardTop + 75f, paint
        )

        val centerX = (pageWidth - 100).toFloat()
        val centerY = (cardTop + cardBottom) / 2
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = Color.LTGRAY
        canvas.drawCircle(centerX, centerY, 30f, paint) // ব্যাকগ্রাউন্ড সার্কেল

        paint.color = if (percent >= 80) "#2E7D32".toColorInt() else "#FBC02D".toColorInt()
        val rectF = RectF(centerX - 30f, centerY - 30f, centerX + 30f, centerY + 30f)
        canvas.drawArc(rectF, -90f, (percent * 3.6).toFloat(), false, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 12f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "${percent.toInt()}%".localizeDigitsAndLabels(),
            centerX,
            centerY + 5f,
            paint
        )

        drawAttendanceLegend(canvas, paint, 300f)
        PdfHelper.drawCalendarGrid(canvas, paint, 65f, 380f, records)

        val genDate = DateTimeUtils.getFileCreatedDate().localizeDigitsAndLabels()
        val devName = getLabel(R.string.dev_name)
        val devLabel = getLabel(R.string.label_developer)
        val genLabel = getLabel(R.string.generated_on)

        PdfHelper.drawCommonFooter(
            canvas = canvas,
            pageWidth = pageWidth,
            pageHeight = pageHeight,
            paint = paint,
            devName = devName,
            devLabel = devLabel,
            genLabel = genLabel,
            genDate = genDate
        )

        pdfDocument.finishPage(page)

        val folderName = if (isSharing) "shared_reports" else Constants.FOLDER_REPORTS
        val fileName =
            "Report_${rollNo}_${studentName.replace(" ", "_")}_${monthYear.replace(" ", "_")}.pdf"
        val desc = getLabel(R.string.pdf_download_desc).format(studentName)

        return PdfHelper.finalizePdf(
            context = context,
            pdfDocument = pdfDocument,
            folderName = folderName,
            fileName = fileName,
            title = title,
            desc = desc,
            isSharing = isSharing
        )
    }

    fun sharePdfFile(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, "Share via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private suspend fun getLabel(resId: Int): String {
        val savedLanguage = settingsManager.languageFlow.first()
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale(savedLanguage.code))
        return context.createConfigurationContext(config).getString(resId)
    }

    private suspend fun drawAttendanceLegend(canvas: Canvas, paint: Paint, legendY: Float) {
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f

        // Present
        paint.color = "#2E7D32".toColorInt()
        canvas.drawCircle(60f, legendY - 5f, 5f, paint)
        paint.color = Color.GRAY
        canvas.drawText(getLabel(R.string.present), 75f, legendY, paint)

        // Absent
        paint.color = "#D32F2F".toColorInt()
        canvas.drawCircle(160f, legendY - 5f, 5f, paint)
        paint.color = Color.GRAY
        canvas.drawText(getLabel(R.string.absent), 175f, legendY, paint)

        // No Class
        paint.color = Color.LTGRAY
        canvas.drawCircle(260f, legendY - 5f, 5f, paint)
        paint.color = Color.GRAY
        canvas.drawText(getLabel(R.string.no_class), 275f, legendY, paint)
    }

}
