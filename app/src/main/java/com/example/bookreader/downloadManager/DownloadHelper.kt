package com.example.bookreader.downloadManager

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.bookreader.data.local.AppDatabase
import com.example.bookreader.data.models.DownloadState
import com.example.bookreader.data.models.Downloading
import com.example.bookreader.data.models.Downloads
import com.example.bookreader.data.models.LocalBook
import com.example.bookreader.data.models.SharedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

class DownloadHelper(private val context: Context, val app: Application) {
    private val job = Job()
    private val downloadScope = CoroutineScope(Dispatchers.IO + job)
    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun startDownload(book: LocalBook): Boolean {
        val bookUrl = book.download
        val bookTitle = book.title

        val request = DownloadManager.Request(Uri.parse(bookUrl))
            .setTitle(bookTitle)
            .setDescription("Downloading...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$bookTitle.pdf")

        val downloadId = downloadManager.enqueue(request)

        downloadScope.launch(Dispatchers.IO) {
            addToCurrentlyDownloading(book.id, downloadId, app)
        }

        SharedData.currentlyDownloadingBooks.add(book)
        return true
    }



    private suspend fun isDownloading(bookId: String): Boolean {
        val loadingDao = AppDatabase.DatabaseBuilder.getInstance(context).loadingDao()
        val downloadId = loadingDao.getDownloadID(bookId)
        Log.d("isDownloading", "Download ID: $downloadId")
        return downloadId != null
    }

    private suspend fun addToCurrentlyDownloading(
        bookId: String,
        downloadId: Long,
        app: Application
    ) {
        val loadingDao =
            AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).loadingDao()
        loadingDao.addDownloadingBook(Downloading(id = bookId, downloadId = downloadId))
    }


    suspend fun getDownloadStatus(bookId: String): DownloadState = withContext(Dispatchers.IO) {
        val downloadDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).downloadsDao()
        val book = downloadDao.getDownloadById(bookId)

        if (book != null) {
            return@withContext DownloadState.DOWNLOADED
        }

        if (isDownloading(bookId)){
            return@withContext DownloadState.DOWNLOADING
        }

        return@withContext DownloadState.NOT_DOWNLOADED
    }

    fun getDownloadedFilePath(downloadId: Long): String? {
        val downloadManager =
            app.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor: Cursor = downloadManager.query(query)

        if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (statusIndex >= 0) {
                val status = cursor.getInt(statusIndex)
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    if (uriIndex >= 0) {
                        val uriString = cursor.getString(uriIndex)
                        if (uriString != null) {
                            val fileUri = Uri.parse(uriString)
                            if (fileUri.scheme == "file") {
                                // Return the actual file path for file scheme URIs
                                return fileUri.path
                            } else if (fileUri.scheme == "content") {
                                // Handle content URIs (Scoped Storage on Android 10+)
                                return resolveContentUriToFilePath(fileUri, context)
                            }
                        }
                    }
                }
            }
        }
        cursor.close()
        return null
    }

    private fun resolveContentUriToFilePath(uri: Uri, context: Context): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                filePath = it.getString(columnIndex)
            }
        }
        return filePath
    }


    //DELETE

    fun removeDownloadFromDatabase(
        book: LocalBook,
        filePath: String,
        app: Application
    ): Boolean {
        val downloadDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).downloadsDao()
        val localBookDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).localBookDao()
        val favDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).favoritesDao()

        return runBlocking {
            var fileDeleted = false

            launch(Dispatchers.IO) {
                downloadDao.deleteBook(Downloads(book.id, filePath))
                if (favDao.getFavById(id = book.id) == null) {
                    localBookDao.deleteBook(book)
                }
                if (File(filePath).exists()) {
                    Log.d("delete from downloads", "File exists: $filePath")
                    fileDeleted = deleteDownloadedFile(filePath, app)
                } else {
                    Log.e("delete from downloads", "File does not exist: $filePath")
                }
                Log.d("delete from downloads", "Book deleted from database: $book")
                if(localBookDao.isBookExists(book.id))
                    Log.d("delete from downloads", "Book exists in database: $book")
                else {
                    Log.d("delete from downloads", "Book does not exist in database: $book")
                }

            }.join()  // Ensure the coroutine finishes execution

            return@runBlocking fileDeleted  // Return whether file deletion was successful
        }
    }


    fun deleteDownloadedFile(filePath: String, app: Application): Boolean {
        val file = File(filePath)
        Log.d("delete from downloads", "File: ${file.name} and its path is $filePath")
        return if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                Log.d("delete from downloads", "File deleted successfully.")
                downloadScope.launch {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            app.applicationContext,
                            "Book deleted successfully.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                downloadScope.launch {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            app.applicationContext,
                            "Failed to delete book.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            deleted
        } else {
            downloadScope.launch {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        app.applicationContext,
                        "File does not exist.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            false
        }
    }
}