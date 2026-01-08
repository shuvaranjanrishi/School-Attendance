package com.therishideveloper.schoolattendance.ui.screens

import StudentCard
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.therishideveloper.schoolattendance.data.local.entity.StudentEntity
import com.therishideveloper.schoolattendance.ui.components.GenderDropdownFilter
import com.therishideveloper.schoolattendance.ui.components.GenericFilterRow
import com.therishideveloper.schoolattendance.ui.components.GenericSearchBar
import com.therishideveloper.schoolattendance.ui.components.myTopBarColors
import com.therishideveloper.schoolattendance.ui.components.StudentFormDialog
import com.therishideveloper.schoolattendance.ui.viewmodels.StudentViewModel
import com.therishideveloper.schoolattendance.utils.Result
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.utils.ClassTypes
import com.therishideveloper.schoolattendance.utils.GenderTypes

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    viewModel: StudentViewModel,
    onMenuClick: () -> Unit,
    onStudentClick: (Int) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val studentList by viewModel.studentListState.collectAsState()
    var hasLoadedDataOnce by remember { mutableStateOf(false) }
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedGender by viewModel.selectedGender.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(studentList) {
        if (studentList is Result.Success) {
            hasLoadedDataOnce = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.student_list)) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.menu)
                        )
                    }
                },
                colors = myTopBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_new_student),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GenericSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = stringResource(R.string.search_hint),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))
                GenderDropdownFilter(
                    genderOptions = GenderTypes.getAll(),
                    selectedGenderCode = selectedGender,
                    onGenderSelected = { genderCode ->
                        viewModel.onGenderSelected(genderCode)
                    }
                )
            }
            GenericFilterRow(
                classes = ClassTypes.getAll(),
                selectedClass = selectedClass,
                onClassSelected = { viewModel.onClassSelected(it) }
            )

            if (studentList is Result.Success) {
                val count = (studentList as Result.Success<List<StudentEntity>>).data.size
                Text(
                    text = stringResource(R.string.total_count, count),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 0.dp,
                        bottom = 0.dp
                    )
                )
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    viewModel.refreshStudents()
                },
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (val state = studentList) {
                    is Result.Loading -> {
                        if (!hasLoadedDataOnce) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    is Result.Success -> {
                        val students = state.data
                        if (students.isEmpty()) {
                            EmptyStatePlaceholder(isSearching = searchQuery.isNotEmpty())
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                items(students) { student ->
                                    StudentCard(
                                        student = student,
                                        onClick = { onStudentClick(student.id) }
                                    )
                                }
                            }
                        }
                    }

                    is Result.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = state.message, color = Color.Red)
                        }
                    }
                }
            }
        }
        if (showAddDialog) {
            StudentFormDialog(
                viewModel,
                student = null,
                onDismiss = { showAddDialog = false },
                onConfirm = { newStudent ->
                    viewModel.addStudent(newStudent)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun EmptyStatePlaceholder(isSearching: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = if (isSearching) stringResource(R.string.no_result)
            else stringResource(R.string.empty_list_msg)
        )
    }
}