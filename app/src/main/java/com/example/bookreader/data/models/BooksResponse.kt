package com.example.bookreader.data.models


data class BooksResponse(
    val books: List<LocalBook>,
    val status: String,
    val total: Int
)