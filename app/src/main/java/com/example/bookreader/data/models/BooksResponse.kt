package com.example.bookreader.data.models


data class BooksResponse(
    val books: List<DetailsResponse>,
    val status: String,
    val total: Int
)