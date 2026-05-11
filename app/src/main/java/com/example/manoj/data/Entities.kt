package com.example.manoj.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val category: String, // Story, Science, History, etc.
    val coverUrl: String,
    val isIssued: Boolean = false,
    val bookCode: String,
    val pages: Int = 100
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val studentId: String, // This will act as the Username
    val password: String = "1234", // Default password for rural setup
    val className: String
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val studentId: Long,
    val studentName: String,
    val bookTitle: String,
    val borrowDate: Long,
    val dueDate: Long, // New field: Stores the timestamp for the 15-day limit
    val returnDate: Long? = null,
    val returned: Boolean = false,
    val pagesRead: Int = 0
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val studentName: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)