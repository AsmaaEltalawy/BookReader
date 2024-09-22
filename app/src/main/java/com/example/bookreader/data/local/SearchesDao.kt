package com.example.bookreader.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookreader.data.models.RecentSearches

@Dao
interface SearchesDao {
    @Insert
    suspend fun addQuery(query: RecentSearches)

    @Query("SELECT * FROM recent_searches ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentSearches(limit: Int): List<RecentSearches>

}