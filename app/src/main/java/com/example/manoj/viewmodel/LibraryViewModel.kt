package com.example.manoj.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.manoj.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = LibraryDatabase.getDatabase(application).dao()

    val allBooks: StateFlow<List<BookEntity>> = dao.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        
    val allStudents: StateFlow<List<StudentEntity>> = dao.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        
    val allTransactions: StateFlow<List<TransactionEntity>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val leaderboard: StateFlow<List<Pair<String, Int>>> = allTransactions
        .map { transactions ->
            transactions.filter { it.returned } // Only count returned/read books
                .groupBy { it.studentName }
                .map { (name, list) -> name to list.sumOf { it.pagesRead } }
                .sortedByDescending { it.second }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBook(title: String, author: String, category: String, code: String, pages: Int = 100) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertBook(BookEntity(
                title = title, 
                author = author, 
                category = category, 
                coverUrl = "", 
                bookCode = code.trim(),
                pages = pages
            ))
        }
    }

    fun addStudent(name: String, studentId: String, className: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertStudent(StudentEntity(name = name, studentId = studentId, className = className))
        }
    }

    fun addReview(bookId: Long, studentName: String, rating: Int, comment: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertReview(ReviewEntity(bookId = bookId, studentName = studentName, rating = rating, comment = comment))
        }
    }
    
    fun getReviews(bookId: Long): Flow<List<ReviewEntity>> = dao.getReviewsForBook(bookId)

    suspend fun processScannedCode(code: String, studentId: Long, studentName: String): Boolean {
        return withContext(Dispatchers.IO) {
            val cleanCode = code.trim()
            val book = dao.getBookByCode(cleanCode)
            if (book == null) {
                Log.e("LibraryVM", "Book not found: $cleanCode")
                return@withContext false
            }

            if (!book.isIssued) {
                // Borrow
                dao.insertTransaction(TransactionEntity(
                    bookId = book.id,
                    studentId = studentId,
                    studentName = studentName,
                    bookTitle = book.title,
                    borrowDate = System.currentTimeMillis()
                ))
                dao.updateBook(book.copy(isIssued = true))
                Log.d("LibraryVM", "Borrowed: ${book.title}")
            } else {
                // Return
                val transaction = dao.getActiveTransactionForBook(book.id)
                if (transaction != null) {
                    dao.updateTransaction(transaction.copy(
                        returnDate = System.currentTimeMillis(), 
                        returned = true,
                        pagesRead = book.pages
                    ))
                    dao.updateBook(book.copy(isIssued = false))
                    Log.d("LibraryVM", "Returned: ${book.title}")
                } else {
                    Log.e("LibraryVM", "No active transaction for book: ${book.title}")
                }
            }
            true
        }
    }
}