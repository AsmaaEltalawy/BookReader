package com.example.bookreader.data.remote


import com.example.bookreader.data.models.BooksResponse
import com.example.bookreader.data.models.DetailsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface BookService {
    @GET("recent")
    suspend fun getBooks(): BooksResponse
    @GET("book/{id}")
    suspend fun getBookById(@Path("id") id: String): DetailsResponse
    @GET("search/{query}")
    suspend fun searchBooks(@Path("query") query: String): BooksResponse

}