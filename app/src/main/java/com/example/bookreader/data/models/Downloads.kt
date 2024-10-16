package com.example.bookreader.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "downloads",
    foreignKeys = [
        ForeignKey(
            entity= LocalBook::class,
            parentColumns = ["id"], // Column in LocalBook
            childColumns = ["id"], // Column in Downloads
        )
    ]
)

data class Downloads(
    @PrimaryKey val id: String,
    @ColumnInfo(defaultValue = "") val filePath: String,
)