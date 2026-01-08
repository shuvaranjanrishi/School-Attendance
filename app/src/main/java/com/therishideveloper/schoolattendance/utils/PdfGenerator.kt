package com.therishideveloper.schoolattendance.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.data.local.SettingsManager
import com.therishideveloper.schoolattendance.data.local.entity.StudentEntity
import com.therishideveloper.schoolattendance.domain.repository.SchoolRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import androidx.core.graphics.toColorInt
import androidx.core.graphics.scale

@Singleton
class PdfGenerator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val schoolRepository: SchoolRepository,
    private val settingsManager: SettingsManager // Dynamic language source
) {

    private suspend fun getLabel(resId: Int): String {
        val savedLanguage = settingsManager.languageFlow.first()
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale(savedLanguage.code))
        return context.createConfigurationContext(config).getString(resId)
    }

    suspend fun downloadStudentPdf(student: StudentEntity): Result<File> {
        val school = schoolRepository.getSchool()
        val pdfDocument = PdfDocument()

        // PDF Page Configuration (A4 Size: 595x842)
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // --- 1. Header Section (Logo, School Name, Address) ---
        school?.logo?.let { bytes ->
            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val circularBitmap = getCircularBitmap(originalBitmap)
            canvas.drawBitmap(circularBitmap.scale(70, 70, false), 50f, 90f, null)
            circularBitmap.recycle()
        }

        // School Name with auto-font-size adjustment
        val schoolName = school?.name ?: getLabel(R.string.pdf_hint_school_name)
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        paint.textSize = if (paint.measureText(schoolName) > 280f) 22f else 28f
        canvas.drawText(schoolName, (pageWidth / 2).toFloat(), 55f, paint)

        // School Address
        paint.textSize = 14f
        paint.isFakeBoldText = false
        val schoolAddress = school?.address ?: getLabel(R.string.pdf_hint_school_address)
        canvas.drawText(schoolAddress, (pageWidth / 2).toFloat(), 80f, paint)

        // Profile Title
        paint.textSize = 20f
        paint.isFakeBoldText = true
        paint.color = Color.DKGRAY
        canvas.drawText(
            getLabel(R.string.pdf_student_profile),
            (pageWidth / 2).toFloat(),
            110f,
            paint
        )

        // --- 2. Profile Photo Box Section ---
        val boxWidth = 100f
        val boxHeight = 120f
        val boxLeft = 450f
        val boxTop = 70f

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = Color.BLACK
        paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f) // Dashed border
        canvas.drawRect(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight, paint)
        paint.pathEffect = null

        if (student.image != null) {
            val bitmap = BitmapFactory.decodeByteArray(student.image, 0, student.image.size)
            canvas.drawBitmap(
                bitmap.scale(boxWidth.toInt(), boxHeight.toInt(), false),
                boxLeft,
                boxTop,
                null
            )
        } else {
            paint.style = Paint.Style.FILL
            paint.textSize = 12f
            paint.isFakeBoldText = false
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                getLabel(R.string.pdf_profile_photo),
                boxLeft + (boxWidth / 2),
                boxTop + 65f,
                paint
            )
        }

        // --- 3. Divider Line ---
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.BLACK
        canvas.drawLine(50f, 200f, (pageWidth - 50).toFloat(), 200f, paint)

        // --- 4. Student Data Section (Localized) ---
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT
        var yPos = 240f
        val lineSpacing = 38f

        // Internal helper function for drawing labels and values
        suspend fun drawDataRow(
            labelRes: Int,
            value: String,
            x: Float,
            y: Float,
            isBold: Boolean = false
        ) {
            val label = getLabel(labelRes)
            paint.isFakeBoldText = false
            paint.textSize = 22f
            canvas.drawText(label, x, y, paint)
            if (isBold) paint.isFakeBoldText = true
            canvas.drawText(value, x + paint.measureText(label) + 10f, y, paint)
            paint.isFakeBoldText = false
        }

        // Basic Info
        drawDataRow(R.string.pdf_label_name, student.name, 60f, yPos, true)
        yPos += lineSpacing

        val rollValue = student.rollNo.localizeDigitsAndLabels()
        val classResId = ClassTypes.fromCode(student.className).stringRes
        val classValue = getLabel(classResId).replace("Class ", "")

        drawDataRow(R.string.pdf_label_roll, rollValue, 60f, yPos, true)
        drawDataRow(R.string.pdf_label_class, classValue, (pageWidth / 2f), yPos, true)
        yPos += lineSpacing

        // Identification & Personal Details
        val idLabelRes =
            if (student.idType == IdTypes.NID.code) R.string.pdf_label_nid else R.string.pdf_label_birth_reg
        drawDataRow(idLabelRes, student.nidOrBirthReg.localizeDigitsAndLabels(), 60f, yPos)
        yPos += lineSpacing

        val personalDetails = listOf(
            R.string.pdf_label_dob to student.dateOfBirth.localizeDigitsAndLabels(),
            R.string.pdf_label_age to student.age.localizeDigitsAndLabels(),
            R.string.pdf_label_father to student.fatherName.ifBlank { getLabel(R.string.not_given) },
            R.string.pdf_label_mother to student.motherName.ifBlank { getLabel(R.string.not_given) },
            R.string.pdf_label_mobile to student.phone.localizeDigitsAndLabels()
        )

        for ((res, valStr) in personalDetails) {
            drawDataRow(res, valStr, 60f, yPos)
            yPos += lineSpacing
        }

        // Gender & Religion
        drawDataRow(
            R.string.pdf_label_gender,
            getLabel(GenderTypes.fromCode(student.gender).stringRes),
            60f,
            yPos
        )
        drawDataRow(
            R.string.pdf_label_religion,
            getLabel(ReligionTypes.fromCode(student.religion).stringRes),
            (pageWidth / 2f),
            yPos
        )
        yPos += lineSpacing

        // Address & Admission
        drawDataRow(
            R.string.pdf_label_blood_group,
            getLabel(BloodGroupTypes.fromCode(student.bloodGroup).stringRes),
            60f,
            yPos
        )
        yPos += lineSpacing
        drawDataRow(
            R.string.pdf_label_address,
            student.address.ifBlank { getLabel(R.string.not_given) },
            60f,
            yPos
        )
        yPos += lineSpacing
        drawDataRow(
            R.string.pdf_label_country,
            getLabel(CountryTypes.fromCode(student.country).stringRes),
            60f,
            yPos
        )
        yPos += lineSpacing
        drawDataRow(
            R.string.pdf_label_admission_date,
            student.admissionDate?.localizeDigitsAndLabels() ?: "", 60f, yPos
        )

        // --- 5. Footer Section ---
        val footerTop = pageHeight - 60f
        paint.style = Paint.Style.FILL
        paint.color = "#F5F5F5".toColorInt()
        canvas.drawRect(0f, footerTop, pageWidth.toFloat(), pageHeight - 10f, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.BLACK
        paint.textSize = 12f
        canvas.drawText(
            getLabel(R.string.pdf_footer_brand_name),
            (pageWidth / 2).toFloat(),
            footerTop + 20f,
            paint
        )
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText(
            getLabel(R.string.pdf_footer_dev_label),
            (pageWidth / 2).toFloat(),
            footerTop + 30f,
            paint
        )
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText(
            getLabel(R.string.pdf_footer_rights),
            (pageWidth / 2).toFloat(),
            footerTop + 40f,
            paint
        )

        pdfDocument.finishPage(page)

        // --- 6. Storage & Saving ---
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val finalFolder = File(downloadDir, "School Attendance/Profiles")

        return try {
            if (!finalFolder.exists()) finalFolder.mkdirs()
            val sanitizedFileName = "Std_${student.rollNo}_${student.name.replace(" ", "_")}.pdf"
            val fileToSave = File(finalFolder, sanitizedFileName)

            pdfDocument.writeTo(FileOutputStream(fileToSave))

            showDownloadNotification(
                context,
                fileToSave,
                getLabel(R.string.pdf_download_success),
                getLabel(R.string.pdf_download_desc).format(student.name),
                "application/pdf"
            )
            Result.Success(fileToSave)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "PDF Export Failed")
        } finally {
            pdfDocument.close()
        }
    }

    /**
     * Internal helper to crop logo bitmaps into a circular shape.
     */
    private fun getCircularBitmap(src: Bitmap): Bitmap {
        val size = minOf(src.width, src.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply { isAntiAlias = true }
        canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), (size / 2).toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, Rect(0, 0, size, size), Rect(0, 0, size, size), paint)
        return output
    }
}