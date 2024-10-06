package com.example.bookreader.ui.theme.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bookreader.data.local.AppDatabase
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

    private val downloadHelper = DownloadHelper(app.applicationContext)

    fun addToDownload(book: LocalBook?) {
        val result = downloadHelper.startDownload(book!!, app)
        if (result)
            _download.postValue(Unit)
    }

    fun deleteFromDownload(book: LocalBook?) {
        val downloadDao =
            AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).downloadsDao()
        var filePath: String?
        viewModelScope.launch(Dispatchers.IO) {
            filePath = downloadDao.getDownloadIdById(book!!.id)
                ?.let { downloadHelper.getDownloadedFilePath(it) }
            Log.d("delete from downloads", "File path: $filePath")
            val result =
                book.let {
                    downloadHelper.removeFailedDownloadFromDatabase(
                        it,
                        filePath ?: "",
                        app
                    )
                }
            if (result)
                _download.postValue(Unit)
        }
    }

    suspend fun getFilePath(id: String): String? {
        return withContext(Dispatchers.IO) {
            val downloadDao =
                AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).downloadsDao()
            val downloadId = downloadDao.getDownloadIdById(id)
            if (downloadId != null) {
                downloadHelper.getDownloadedFilePath(downloadId)
            } else {
                ""
            }
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
