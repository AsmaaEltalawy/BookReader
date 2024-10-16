package com.example.bookreader.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bookreader.data.models.Downloading
import com.example.bookreader.data.models.Downloads
import com.example.bookreader.data.models.Favorites
import com.example.bookreader.data.models.LocalBook
import com.example.bookreader.data.models.RecentSearches

/*
import androidx.room.AutoMigration
import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec
 */


@Database(
    entities = [LocalBook::class,
        Downloads::class,
        Downloading::class,
        Favorites::class,
        RecentSearches::class],
    version = 1,
    /*
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = AppDatabase.MyAutoMigration::class)
    ],
     */
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localBookDao(): LocalBookDao
    abstract fun downloadsDao(): DownloadsDao
    abstract fun favoritesDao(): FavoritesDao
    abstract fun searchesDao(): SearchesDao
    abstract fun loadingDao(): LoadingDao

    object DatabaseBuilder {
        fun getInstance(context: Context): AppDatabase {
            val db = Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "books_db"
            )
                .build()
            return db
        }
    }
    /*
        @DeleteColumn.Entries(
            DeleteColumn(tableName = "local_books", columnName = "isFavorite"),
            DeleteColumn(tableName = "local_books", columnName = "isDownloaded")
        )
        class MyAutoMigration : AutoMigrationSpec
    */
}