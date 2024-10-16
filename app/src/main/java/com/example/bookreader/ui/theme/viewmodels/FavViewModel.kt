package com.example.bookreader.ui.theme.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bookreader.data.local.AppDatabase
import com.example.bookreader.data.models.Favorites
import com.example.bookreader.data.models.LocalBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavViewModel(val app: Application) : AndroidViewModel(app) {

    private val _addFav = MutableLiveData<Unit>()
    val addFav: MutableLiveData<Unit> = _addFav
    private val _deleteFav = MutableLiveData<Unit>()
    val deleteFav: MutableLiveData<Unit> = _deleteFav
    private val _booksFav = MutableLiveData<List<LocalBook>?>()
    val booksFav: LiveData<List<LocalBook>?> = _booksFav
    private val _singleBookFav = MutableLiveData<LocalBook?>()
    val singleBookFav: MutableLiveData<LocalBook?> = _singleBookFav

    fun getAllFavorites() {
        val bookDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).favoritesDao()
        viewModelScope.launch(Dispatchers.IO) {
            val favorites = bookDao.getAllFavourites().filterNotNull()
            _booksFav.postValue(favorites)
        }
    }


    suspend fun addToFav(book: LocalBook?) {
        val bookDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).favoritesDao()
        val localBookDao =
            AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).localBookDao()
        viewModelScope.launch(Dispatchers.IO) {
            if (book == null) {
                Log.d("add to favourites", "Book is null")
                return@launch
            } else {
                if(localBookDao.isBookExists(book.id))
                    localBookDao.updateBook(book)
                else
                    localBookDao.addBook(book)
                _addFav.postValue(bookDao.addBookFromFav(Favorites(book.id)))
                Log.d("add to favourites", "Books: $book")
            }
        }
    }

    suspend fun deleteFromFav(book: LocalBook?) {
        val bookDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).favoritesDao()
        val localBookDao =
            AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).localBookDao()
        viewModelScope.launch(Dispatchers.IO) {
            if (book == null) {
                Log.d("delete from favourites", "Book is null")
                return@launch
            } else {
                if(localBookDao.isBookExists(book.id))
                    localBookDao.updateBook(book)
                else
                    localBookDao.deleteBook(book)
                _deleteFav.postValue(bookDao.deleteBookFromFav(Favorites(book.id)))
                Log.d("delete from favourites", "Books: $book")
            }
        }
    }

    suspend fun getFavById(id: String): Int {
        return withContext(Dispatchers.IO) {
            val bookDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).favoritesDao()
            val book: LocalBook? = bookDao.getFavById(id)
            Log.d("get from favourites", "Books: $id")
            _singleBookFav.postValue(book)
            if (book != null) 1 else -1
        }
    }
}