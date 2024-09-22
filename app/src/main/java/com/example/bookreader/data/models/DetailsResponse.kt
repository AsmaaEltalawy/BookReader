package com.example.bookreader.data.models

import java.io.Serializable

//aka BOOK
data class DetailsResponse(
    val authors: String = "unknown",
    var description: String = "unknown",
    val download: String = "",
    val id: String = "unknown",
    var image: String = "",
    var pages: String = "unknown",
    var publisher: String = "unknown",
    val status: String = "unknown",
    val subtitle: String = "unknown",
    var title: String = "unknown",
    var url: String = "",
    var year: String = "unknown",
    var isFavorite: Boolean = false,
    var isDownloaded: Boolean = false
) : Serializable