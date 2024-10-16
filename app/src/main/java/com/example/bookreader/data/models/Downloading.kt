package com.example.bookreader.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloading_books")
data class Downloading (
    @PrimaryKey val id: String,
    @ColumnInfo val downloadId: Long
    )
