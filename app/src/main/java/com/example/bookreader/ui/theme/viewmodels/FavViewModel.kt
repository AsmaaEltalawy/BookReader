package com.example.bookreader.ui.theme.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bookreader.data.local.AppDatabase
import com.example.bookreader.data.models.DetailsResponse
import com.example.bookreader.data.models.Favorites
import com.example.bookreader.ui.theme.viewmodels.HomeViewModel.Companion.mapBookToLocal
import com.example.bookreader.ui.theme.viewmodels.HomeViewModel.Companion.mapLocalToBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavViewModel(val app: Application) : AndroidViewModel(app) {

    private val _addFav = MutableLiveData<Unit>()
    val addFav: MutableLiveData<Unit> = _addFav
    private val _deleteFav = MutableLiveData<Unit>()
    val deleteFav: MutableLiveData<Unit> = _deleteFav
    private val _booksFav = MutableLiveData<DetailsResponse?>()
    val booksFav: MutableLiveData<DetailsResponse?> = _booksFav


    suspend fun addToFav(book: DetailsResponse?) {
        val bookDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).favoritesDao()
        val localBookDao =
            AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).localBookDao()
        viewModelScope.launch(Dispatchers.IO) {
            val localBook = mapBookToLocal(book)
            if (localBook == null) {
                Log.d("add to favourites", "Book is null")
                return@launch
            } else {
                if(localBookDao.isBookExists(localBook.id))
                    localBookDao.updateBook(localBook)
                else
                    localBookDao.addBook(localBook)
                _addFav.postValue(bookDao.addBookFromFav(Favorites(localBook.id)))
                Log.d("add to favourites", "Books: $localBook")
            }
        }
    }

    suspend fun deleteFromFav(book: DetailsResponse?) {
        val bookDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).favoritesDao()
        val localBookDao =
            AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).localBookDao()
        viewModelScope.launch(Dispatchers.IO) {
            val localBook = mapBookToLocal(book)
            if (localBook == null) {
                Log.d("delete from favourites", "Book is null")
                return@launch
            } else {
                if(localBookDao.isBookExists(localBook.id))
                    localBookDao.updateBook(localBook)
                else
                    localBookDao.deleteBook(localBook)
                _deleteFav.postValue(bookDao.deleteBookFromFav(Favorites(localBook.id)))
                Log.d("delete from favourites", "Books: $localBook")
            }
        }
    }

    suspend fun getFavById(id: String): Int {
        return withContext(Dispatchers.IO) {
            val bookDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).favoritesDao()
            val book: DetailsResponse? = mapLocalToBook(bookDao.getFavById(id))
            Log.d("get from favourites", "Books: $id")
            _booksFav.postValue(book)
            if (book != null) 1 else -1
        }
    }
}