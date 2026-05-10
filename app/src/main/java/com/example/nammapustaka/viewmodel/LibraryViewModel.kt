package com.example.nammapustaka.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammapustaka.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = LibraryDatabase.getDatabase(application).dao()

    val allBooks: StateFlow<List<BookEntity>> = dao.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allStudents: StateFlow<List<StudentEntity>> = dao.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allTransactions: StateFlow<List<TransactionEntity>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBook(title: String, author: String, category: String, code: String) {
        viewModelScope.launch {
            dao.insertBook(BookEntity(
                title = title, 
                author = author, 
                category = category, 
                coverUrl = "", 
                bookCode = code
            ))
        }
    }

    fun addStudent(name: String, studentId: String, className: String) {
        viewModelScope.launch {
            dao.insertStudent(StudentEntity(name = name, studentId = studentId, className = className))
        }
    }

    fun processScannedCode(code: String, studentId: Long, studentName: String) {
        viewModelScope.launch {
            val book = dao.getBookByCode(code) ?: return@launch
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
            } else {
                // Return
                val transaction = dao.getActiveTransactionForBook(book.id)
                if (transaction != null) {
                    dao.updateTransaction(transaction.copy(
                        returnDate = System.currentTimeMillis(), 
                        returned = true
                    ))
                    dao.updateBook(book.copy(isIssued = false))
                }
            }
        }
    }
}