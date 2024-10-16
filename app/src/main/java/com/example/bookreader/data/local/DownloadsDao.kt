package com.example.bookreader.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.bookreader.data.models.Downloads
import com.example.bookreader.data.models.LocalBook


@Dao
interface DownloadsDao {
    @Query("SELECT * FROM local_books WHERE id IN (SELECT id FROM downloads)")
    suspend fun getAll(): List<LocalBook?>

    @Query("""
        SELECT * 
        FROM local_books
        JOIN downloads ON local_books.id = downloads.id
        WHERE downloads.id = :id
        """)
    suspend fun getDownloadById(id: String): LocalBook?

    @Query("""
        SELECT * 
        FROM local_books 
        JOIN downloads ON local_books.id = downloads.id
        WHERE local_books.authors LIKE '%' || :query || '%' 
           OR local_books.title LIKE '%' || :query || '%' 
           OR local_books.subtitle LIKE '%' || :query || '%' 
           OR local_books.description LIKE '%' || :query || '%' 
           OR local_books.publisher LIKE '%' || :query || '%'
        """)
    suspend fun getDownloadsByQuery(query: String): List<LocalBook?>

    @Query("SELECT filePath FROM downloads WHERE id = :id")
    fun getFilePathById(id: String): String?

    @Delete
    suspend fun deleteBook(vararg downloadedBook: Downloads)

    @Insert
    suspend fun addBook(downloadedBook: Downloads)
}