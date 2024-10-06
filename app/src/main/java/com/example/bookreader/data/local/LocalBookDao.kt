package com.example.bookreader.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bookreader.data.models.LocalBook

@Dao
interface LocalBookDao {
    @Insert
    suspend fun addBook(book: LocalBook)

    @Delete
    suspend fun deleteBook(book: LocalBook)

    @Update
    suspend fun updateBook(book: LocalBook)

    @Query("SELECT EXISTS(SELECT * FROM local_books WHERE id = :id)")
    fun isBookExists(id: String): Boolean

    @Query("SELECT * FROM local_books WHERE id = :id")
    fun getBookById(id: String): LocalBook?
}