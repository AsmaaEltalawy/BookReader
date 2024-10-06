package com.example.bookreader.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "recent_searches")
data class RecentSearches (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var query: String = ""
    )