package com.example.bookreader.ui.theme.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bookreader.data.local.AppDatabase
import com.example.bookreader.data.local.MySharedPreference
import com.example.bookreader.data.models.BooksResponse
import com.example.bookreader.data.models.LocalBook
import com.example.bookreader.data.remote.BookService
import com.example.bookreader.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(val app: Application) : AndroidViewModel(app) {
    private val _booksRecent = MutableLiveData<BooksResponse>()
    val booksRecent: MutableLiveData<BooksResponse> = _booksRecent
    private val _booksRecommend = MutableLiveData<BooksResponse>()
    val booksRecommend: MutableLiveData<BooksResponse> = _booksRecommend
    private val _lastRead = MutableLiveData<LocalBook?>()
    val lastRead: MutableLiveData<LocalBook?> = _lastRead



    fun getRecentBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                val retrofit = RetrofitClient.getInstance()
                val bookService = retrofit.create(BookService::class.java)
                val books = bookService.getBooks()
                val detailedBooks = mapBookToDetails(books)
                _booksRecent.postValue(detailedBooks)
                //  Log.e("HomeViewModelsDETAILS", "Books: $detailedBooks")
            }
        }
    }

    fun getRecommendBooks(query: String = "python") {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val retrofit = RetrofitClient.getInstance()
                val bookService = retrofit.create(BookService::class.java)
                val books = bookService.searchBooks(query)
                val detailedBooks = mapBookToDetails(books) // ....
                _booksRecommend.postValue(detailedBooks)
                // Log.d("HomeViewModels", "Books: $detailedBooks")
            }
        }
    }


    fun getLastRead() {
        val prefs = MySharedPreference(app)
        val id = prefs.getId()
        viewModelScope.launch {
            if (id.isNullOrEmpty()) {
                _lastRead.postValue(null)
            } else {
                var book = fromDB(id)
                if (book != null) {
                    _lastRead.postValue(book)
                }else{
                     book = fromAPI(id)
                    _lastRead.postValue(book)
                }
            }
        }
    }

    private suspend fun fromDB(id: String): LocalBook? {
        return withContext(Dispatchers.IO) {
            val db = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext)
            val bookDao = db.localBookDao()
            val book = bookDao.getBookById(id)
            book
        }
    }

    private suspend fun fromAPI(id: String): LocalBook? {
        return withContext(Dispatchers.IO) {
            val retrofit = RetrofitClient.getInstance()
            val bookService = retrofit.create(BookService::class.java)
            try {
                val book = bookService.getBookById(id)
                book
            } catch (e: Exception) {
                null
            }
        }
    }


    companion object {
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
                val detailedBooks =
                    BooksResponse(status = "OK", books = detailsList, total = detailsList.size)
                detailedBooks
            }
        }

    }

}