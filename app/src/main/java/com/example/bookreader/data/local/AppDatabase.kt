package com.example.bookreader.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bookreader.data.models.LocalBook
import com.example.bookreader.data.models.Downloads
import com.example.bookreader.data.models.Favorites
import com.example.bookreader.data.models.RecentSearches


@Database(entities = [LocalBook::class, Downloads::class, Favorites::class, RecentSearches::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localBookDao(): LocalBookDao
    abstract fun downloadsDao(): DownloadsDao
    abstract fun favoritesDao(): FavoritesDao
    abstract fun searchesDao(): SearchesDao

    object DatabaseBuilder{
        fun getInstance(context: Context): AppDatabase {
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "books_db"
            ).build()
            return db
        }
    }
}