package com.example.bookreader.ui.theme.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bookreader.data.local.AppDatabase
import com.example.bookreader.data.models.DownloadState
import com.example.bookreader.data.models.LocalBook
import com.example.bookreader.downloadManager.DownloadHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadViewModel(val app: Application) : AndroidViewModel(app) {


    private val _download = MutableLiveData<Unit>()
    val download: LiveData<Unit> = _download
    private val _booksDownload = MutableLiveData<LocalBook?>()
    val booksDownload: LiveData<LocalBook?> = _booksDownload

    private val downloadHelper = DownloadHelper(app.applicationContext, app)

    fun addToDownload(book: LocalBook?) {
        viewModelScope.launch {
            val result = downloadHelper.startDownload(book!!)
            if (result) _download.postValue(Unit)
        }
    }

    fun deleteFromDownload(book: LocalBook?) {
        val loadingDao =
            AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).loadingDao()
        var filePath: String?

        viewModelScope.launch(Dispatchers.IO) {
            if (book != null) {
                Log.d("DetailsActivity", "Entered with Book: $book")
                val downloadId = loadingDao.getDownloadID(book.id)
                filePath = downloadId?.let { downloadHelper.getDownloadedFilePath(it) }
                Log.d("delete from downloads", "File path: $filePath")
                filePath?.let { downloadHelper.deleteDownloadedFile(it, app) }
                downloadHelper.removeDownloadFromDatabase(book, filePath ?: "", app)
                _download.postValue(Unit)
            }
        }
    }

    suspend fun getDownloadStatus(id: String): DownloadState {
        return downloadHelper.getDownloadStatus(id)
    }

    suspend fun getDownloadIdById(id: String): Long? {
        return withContext(Dispatchers.IO) {
            val loadingDao =
                AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).loadingDao()
            loadingDao.getDownloadID(id)
        }
    }


    suspend fun getFilePath(id: String): String {
        return withContext(Dispatchers.IO) {
            val downloadDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).downloadsDao()
            val filePath = downloadDao.getFilePathById(id)
            filePath ?: ""
        }
    }

    suspend fun getDownloadById(id: String): Int {
        return withContext(Dispatchers.IO) {
            val bookDao =
                AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).downloadsDao()
            val book: LocalBook? = bookDao.getDownloadById(id)
            Log.d("get from downloads", "Books: $id")
            _booksDownload.postValue(book)
            if (book != null) 1 else -1
        }
    }
}
