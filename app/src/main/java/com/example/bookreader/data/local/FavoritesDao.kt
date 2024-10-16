package com.example.bookreader.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.bookreader.data.models.Favorites
import com.example.bookreader.data.models.LocalBook


@Dao
interface FavoritesDao {
    @Query("SELECT * FROM local_books WHERE id IN (SELECT id FROM favorites)")
    suspend fun getAllFavourites(): List<LocalBook?>

    @Query("""
        SELECT * 
        FROM local_books
        JOIN favorites ON local_books.id = favorites.id
        WHERE favorites.id = :id
        """)
    suspend fun getFavById(id: String): LocalBook?

    @Query("""
        SELECT * 
        FROM local_books 
        JOIN favorites ON local_books.id = favorites.id
        WHERE local_books.authors LIKE '%' || :query || '%' 
           OR local_books.title LIKE '%' || :query || '%' 
           OR local_books.subtitle LIKE '%' || :query || '%' 
           OR local_books.description LIKE '%' || :query || '%' 
           OR local_books.publisher LIKE '%' || :query || '%'
        """)
    suspend fun getFavByQuery(query: String): List<LocalBook?>

    @Delete
    suspend fun deleteBookFromFav(vararg favBook: Favorites)

    @Insert
    suspend fun addBookFromFav(favBook: Favorites)
}