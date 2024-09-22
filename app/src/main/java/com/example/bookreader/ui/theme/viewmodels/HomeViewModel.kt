package com.example.bookreader.ui.theme.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bookreader.data.models.BooksResponse
import com.example.bookreader.data.models.DetailsResponse
import com.example.bookreader.data.models.LocalBook
import com.example.bookreader.data.remote.BookService
import com.example.bookreader.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val _booksRecent = MutableLiveData<BooksResponse>()
    val booksRecent: MutableLiveData<BooksResponse> = _booksRecent
    private val _booksRecommend = MutableLiveData<BooksResponse>()
    val booksRecommend: MutableLiveData<BooksResponse> = _booksRecommend


    fun getRecentBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                val retrofit = RetrofitClient.getInstance()
                val bookService = retrofit.create(BookService::class.java)
                val books = bookService.getBooks()
                val detailedBooks = mapBookToDetails(books)
                _booksRecent.postValue(detailedBooks)
                Log.e("HomeViewModelsDETAILS", "Books: $detailedBooks")
            }
        }
    }

    fun getRecommendBooks(query: String = "python") {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val retrofit = RetrofitClient.getInstance()
                val bookService = retrofit.create(BookService::class.java)
                val books = bookService.searchBooks(query)
                val detailedBooks = mapBookToDetails(books)
                _booksRecommend.postValue(detailedBooks)
                Log.d("HomeViewModels", "Books: $detailedBooks")
            }
        }
    }

    companion object{
         suspend fun mapBookToDetails(books: BooksResponse): BooksResponse {
            return withContext(Dispatchers.IO) {
                val deferredResults = books.books.map { book ->
                    async {
                        try {
                            val retrofit = RetrofitClient.getInstance()
                            val apiService = retrofit.create(BookService::class.java)
                            apiService.getBookById(book.id)
                        } catch (e: Exception) {
                            Log.e("HomeViewModels", "Error fetching book details", e)
                            null
                        }
                    }
                }
                val detailsList = deferredResults.awaitAll().filterNotNull()
                val detailedBooks = BooksResponse(status = "OK", books = detailsList , total = detailsList.size)
                detailedBooks
            }
        }

         suspend fun mapLocalToBook(book: LocalBook?): DetailsResponse? {
            if (book == null) {
                return null
            }
            return withContext(Dispatchers.IO) {
                val detailsResponse = DetailsResponse(
                    id = book.id,
                    authors = book.authors,
                    description = book.description,
                    download = book.download,
                    image = book.image,
                    pages = book.pages,
                    publisher = book.publisher,
                    status = book.status,
                    subtitle = book.subtitle,
                    title = book.title,
                    url = book.url,
                    year = book.year,
                    isFavorite = book.isFavorite,
                    isDownloaded = book.isDownloaded
                )
                detailsResponse
            }
        }

         suspend fun mapBookToLocal(book: DetailsResponse?): LocalBook? {
             if (book == null) {
                 return null
             }
            return withContext(Dispatchers.IO) {
                val localBook = LocalBook(
                    id = book.id,
                    authors = book.authors,
                    description = book.description,
                    download = book.download,
                    image = book.image,
                    pages = book.pages,
                    publisher = book.publisher,
                    status = book.status,
                    subtitle = book.subtitle,
                    title = book.title,
                    url = book.url,
                    year = book.year,
                    isFavorite = book.isFavorite,
                    isDownloaded = book.isDownloaded
                )
                localBook
            }
        }

    }

}