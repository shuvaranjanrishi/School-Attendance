package com.therishideveloper.schoolattendance.data.local.export

import android.content.Context
import android.os.Environment
import com.therishideveloper.schoolattendance.data.local.entity.StudentEntity
import com.therishideveloper.schoolattendance.utils.Result // আপনার কাস্টম Result ক্লাস
import com.therishideveloper.schoolattendance.utils.showDownloadNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

@Singleton
class ExcelExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun exportStudentsToExcel(students: List<StudentEntity>): Result<String> {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Students List")

            val constructorParams =
                StudentEntity::class.primaryConstructor?.parameters ?: emptyList()
            val propertiesMap = StudentEntity::class.memberProperties.associateBy { it.name }

            val activeFields = constructorParams
                .mapNotNull { param -> propertiesMap[param.name] }
                .filter { it.name != "image" && it.name != "id" }

            val headerRow = sheet.createRow(0)
            activeFields.forEachIndexed { index, prop ->
                val formattedName = prop.name
                    .replace(Regex("([a-z])([A-Z])"), "$1 $2")
                    .replaceFirstChar { it.uppercase() }
                headerRow.createCell(index).setCellValue(formattedName)
            }

            students.forEachIndexed { rowIndex, student ->
                val row = sheet.createRow(rowIndex + 1)
                activeFields.forEachIndexed { colIndex, prop ->
                    val value = prop.get(student)
                    row.createCell(colIndex).setCellValue(value?.toString() ?: "")
                }
            }

            activeFields.indices.forEach { index ->
                sheet.setColumnWidth(index, 20 * 256)
            }

            val exportFolder = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "School Attendance/Exports"
            )
            if (!exportFolder.exists()) exportFolder.mkdirs()

            val fileName = "Student_List_${System.currentTimeMillis()}.xlsx"
            val file = File(exportFolder, fileName)

            FileOutputStream(file).use { output ->
                workbook.write(output)
            }
            workbook.close()

            showDownloadNotification(
                context = context,
                file = file,
                title = "Excel Download Complete",
                description = "All students exported in original database order.",
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )

            Result.Success("Excel Exported Successfully")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Export Error")
        }
    }
}