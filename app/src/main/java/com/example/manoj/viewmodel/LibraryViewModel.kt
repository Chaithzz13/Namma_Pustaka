package com.example.manoj.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.manoj.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class) // Required for flatMapLatest
class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = LibraryDatabase.getDatabase(application).dao()

    // 1. ROLE & SESSION MANAGEMENT
    private val _isTeacherMode = MutableStateFlow(false)
    val isTeacherMode: StateFlow<Boolean> = _isTeacherMode

    private val _currentStudent = MutableStateFlow<StudentEntity?>(null)
    val currentStudent: StateFlow<StudentEntity?> = _currentStudent

    // 2. DATA STREAMS
    val allBooks: StateFlow<List<BookEntity>> = dao.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allStudents: StateFlow<List<StudentEntity>> = dao.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // --- ADDED: 3. LEADERBOARD LOGIC ---
    // This is what the LeaderboardScreen.kt was looking for!
    val leaderboard: StateFlow<List<Pair<String, Int>>> = allTransactions
        .map { transactions ->
            transactions.filter { it.returned }
                .groupBy { it.studentName }
                .map { (name, list) -> name to list.sumOf { it.pagesRead } }
                .sortedByDescending { it.second }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. AUTHENTICATION LOGIC
    fun loginAsTeacher() {
        _isTeacherMode.value = true
        _currentStudent.value = null
    }

    fun loginAsStudent(username: String, pass: String): Boolean {
        val student = allStudents.value.find { it.studentId == username && it.password == pass }
        return if (student != null) {
            _currentStudent.value = student
            _isTeacherMode.value = false
            true
        } else {
            false
        }
    }

    fun logout() {
        _isTeacherMode.value = false
        _currentStudent.value = null
    }

    // 5. LIBRARIAN ACTIONS (Issue/Return)
    suspend fun issueBookByLibrarian(bookCode: String, student: StudentEntity): Boolean {
        return withContext(Dispatchers.IO) {
            val book = dao.getBookByCode(bookCode.trim())
            if (book != null && !book.isIssued) {
                val fifteenDaysInMillis = 15L * 24 * 60 * 60 * 1000
                val dueDate = System.currentTimeMillis() + fifteenDaysInMillis

                dao.insertTransaction(TransactionEntity(
                    bookId = book.id,
                    studentId = student.id,
                    studentName = student.name,
                    bookTitle = book.title,
                    borrowDate = System.currentTimeMillis(),
                    dueDate = dueDate
                ))
                dao.updateBook(book.copy(isIssued = true))
                true
            } else false
        }
    }

    fun returnBookByLibrarian(bookCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val book = dao.getBookByCode(bookCode.trim())
            if (book != null && book.isIssued) {
                val transaction = dao.getActiveTransactionForBook(book.id)
                if (transaction != null) {
                    dao.updateTransaction(transaction.copy(
                        returnDate = System.currentTimeMillis(),
                        returned = true,
                        pagesRead = book.pages
                    ))
                    dao.updateBook(book.copy(isIssued = false))
                }
            }
        }
    }

    // 6. STUDENT-SPECIFIC STREAMS
    val myBorrowedBooks: Flow<List<TransactionEntity>> = _currentStudent.flatMapLatest { student ->
        if (student == null) flowOf(emptyList())
        else allTransactions.map { txList ->
            txList.filter { it.studentId == student.id && !it.returned }
        }
    }

    // 7. MANAGEMENT FUNCTIONS
    fun addBook(title: String, author: String, category: String, code: String, pages: Int = 100) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertBook(BookEntity(title = title, author = author, category = category, coverUrl = "", bookCode = code.trim(), pages = pages))
        }
    }

    fun registerStudent(name: String, studentId: String, className: String, pass: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertStudent(StudentEntity(name = name, studentId = studentId, className = className, password = pass))
        }
    }

    fun addReview(bookId: Long, rating: Int, comment: String) {
        val student = _currentStudent.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertReview(ReviewEntity(
                bookId = bookId,
                studentName = student.name,
                rating = rating,
                comment = comment
            ))
        }
    }

    fun getReviews(bookId: Long): Flow<List<ReviewEntity>> = dao.getReviewsForBook(bookId)
}