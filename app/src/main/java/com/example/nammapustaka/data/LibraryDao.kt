package com.example.nammapustaka.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("SELECT * FROM books WHERE bookCode = :code LIMIT 1")
    suspend fun getBookByCode(code: String): BookEntity?

    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Insert
    suspend fun insertStudent(student: StudentEntity)

    @Query("SELECT * FROM transactions ORDER BY borrowDate DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE bookId = :bookId AND returned = 0 LIMIT 1")
    suspend fun getActiveTransactionForBook(bookId: Long): TransactionEntity?

    @Query("SELECT * FROM reviews WHERE bookId = :bookId")
    fun getReviewsForBook(bookId: Long): Flow<List<ReviewEntity>>

    @Insert
    suspend fun insertReview(review: ReviewEntity)
}