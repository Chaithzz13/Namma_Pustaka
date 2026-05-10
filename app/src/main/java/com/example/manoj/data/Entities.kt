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
    val pages: Int = 100 // Default pages for leaderboard tracking
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val studentId: String,
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