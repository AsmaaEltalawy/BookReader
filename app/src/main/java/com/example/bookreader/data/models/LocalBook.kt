package com.example.bookreader.data.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity(tableName = "local_books")
@Parcelize
data class LocalBook(
    //it's book but the database version (?)
    @PrimaryKey val id: String,
    @ColumnInfo(defaultValue = "unknown") val authors: String = "unknown",
    @ColumnInfo(defaultValue = "unknown") val description: String = "unknown",
    @ColumnInfo(name = "download_url")val download: String = "download link", //download link
    @ColumnInfo(name = "img_url")val image: String = "img url",
    @ColumnInfo(defaultValue = "0") val pages: String = "0",
    @ColumnInfo(defaultValue = "unknown") val publisher: String = "unknown",
    @ColumnInfo(defaultValue = "unknown")val status: String = "unknown",
    @ColumnInfo(defaultValue = "unknown") val subtitle: String = "unknown",
    @ColumnInfo(defaultValue = "unknown") val title: String = "unknown",
    @ColumnInfo(defaultValue = "")val url: String = "", //book's url
    @ColumnInfo(defaultValue = "0000") val year: String = "0000",
) : Parcelable