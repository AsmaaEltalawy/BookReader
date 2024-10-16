package com.example.bookreader.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.bookreader.data.models.Downloading


@Dao
interface LoadingDao {
    @Query("SELECT * FROM downloading_books")
    suspend fun getAllDownloadingBooks(): List<Downloading>

    @Insert
    suspend fun addDownloadingBook(loading: Downloading)

    @Delete
    suspend fun deleteDownloadingBook(loading: Downloading)

    @Query("SELECT downloadId FROM downloading_books WHERE id = :id")
    suspend fun getDownloadID(id: String): Long?

    @Query("SELECT id FROM downloading_books WHERE downloadId = :downloadId")
    suspend fun getIdByDownloadId(downloadId: Long): String?
}