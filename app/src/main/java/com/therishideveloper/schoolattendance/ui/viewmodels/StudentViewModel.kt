package com.therishideveloper.schoolattendance.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.data.local.entity.StudentEntity
import com.therishideveloper.schoolattendance.domain.repository.StudentRepository
import com.therishideveloper.schoolattendance.ui.event.UiEvent
import com.therishideveloper.schoolattendance.utils.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.therishideveloper.schoolattendance.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val repository: StudentRepository,
    private val pdfGenerator: PdfGenerator
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    // ১. ক্লাস ফিল্টারের জন্য নতুন স্টেট
    private val _selectedClass = MutableStateFlow<String?>(null) // null মানে সব ক্লাস
    val selectedClass = _selectedClass.asStateFlow()

    private val _selectedGender = MutableStateFlow<String?>(null)
    val selectedGender = _selectedGender.asStateFlow()

    private val _toastEvent = MutableSharedFlow<UiEvent>()
    val toastEvent = _toastEvent.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val studentListState = combine(
        _searchQuery,
        _selectedClass,
        _selectedGender,
        refreshTrigger
    ) { query, classFilter, gender, _ ->
        Triple(query, classFilter, gender)
    }
        .debounce(300L)
        .flatMapLatest { (query, classFilter, gender) ->
            repository.searchAndFilterStudents(
                query = "%$query%",
                classFilter = classFilter,
                genderFilter = gender
            )
        }
        .map { Result.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Result.Loading
        )

    fun onGenderSelected(gender: String?) {
        _selectedGender.value = gender
    }

    fun onClassSelected(className: String?) {
        _selectedClass.value = className
    }

    fun refreshStudents() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            delay(1000)
            refreshTrigger.value = System.currentTimeMillis()
            _isRefreshing.value = false
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun addStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.addStudent(student)
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun getStudentById(studentId: Int): StateFlow<Result<StudentEntity?>> {
        return repository.getStudentById(studentId)
            .map<StudentEntity?, Result<StudentEntity?>> { student ->
                if (student != null) Result.Success(student)
                else Result.Error("Error: Student Info Not Found")
            }
            .onStart {
                emit(Result.Loading)
//                delay(2000)
            }
            .catch { e ->
                emit(Result.Error(e.message ?: "Unknown Error"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Result.Loading
            )
    }

    suspend fun getStudentByRoll(roll: String, className: String): StudentEntity? {
        return repository.getStudentByRollAndClass(roll, className)
    }

    private val _isPdfGenerating = MutableStateFlow(false)
    val isPdfGenerating = _isPdfGenerating.asStateFlow()

    fun generatePdf(student: StudentEntity) {
        viewModelScope.launch {
            _isPdfGenerating.value = true
            val result = withContext(Dispatchers.IO) {
                pdfGenerator.downloadStudentPdf(student)
            }
            _isPdfGenerating.value = false

            when (result) {
                is Result.Success -> {
                    _toastEvent.emit(UiEvent.ShowToastRes(R.string.pdf_save_success))
                }

                is Result.Error -> {
                    _toastEvent.emit(UiEvent.ShowToastStr("Error: ${result.message}"))
                }

                else -> Unit
            }
        }
    }
}