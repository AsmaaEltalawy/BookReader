package com.example.bookreader.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = LocalBook::class,
            parentColumns = ["id"], // Column in LocalBook
            childColumns = ["id"], // Column in Downloads
        )
    ]
)
data class Favorites(
    @PrimaryKey val id: String
)