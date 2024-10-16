package com.example.bookreader.onDownloadCompleteListener

import android.app.Application
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bookreader.data.local.AppDatabase
import com.example.bookreader.data.models.Downloading
import com.example.bookreader.data.models.Downloads
import com.example.bookreader.data.models.LocalBook
import com.example.bookreader.data.models.SharedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class OnDownloadCompleteListener(val app: Application) : BroadcastReceiver() {
    private val job = Job()
    private val downloadScope = CoroutineScope(Dispatchers.IO + job)
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("OnDownloadCompleteListener", "onReceive called")
        val loadingDao = AppDatabase.DatabaseBuilder.getInstance(context).loadingDao()
        val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: return
        val bookId: String?
        runBlocking {  bookId = loadingDao.getIdByDownloadId(downloadId) }
        val book = SharedData.currentlyDownloadingBooks.firstOrNull { it.id == bookId }
        val filePath = getDownloadedFilePath(downloadId)
        if (filePath != null && bookId != null && downloadId != -1L) {
            if (book != null) {
                checkDownloadStatus(context, downloadId, book, filePath, app)
            }
        }
    }

    private fun checkDownloadStatus(context: Context, downloadId: Long, book: LocalBook, filePath: String, app: Application) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor: Cursor = downloadManager.query(query)

        downloadScope.launch {
            val loadingDao = AppDatabase.DatabaseBuilder.getInstance(context).loadingDao()
            loadingDao.deleteDownloadingBook(Downloading(book.id, -1L))
            Log.d("delete from downloads", "${loadingDao.getDownloadID(book.id)}")
        }

        if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (statusIndex >= 0) {
                when (val status = cursor.getInt(statusIndex)) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            addToDatabase(book, filePath, app)
                            Log.d("OnDownloadCompleteListener", "Book added to database")
                        }
                    }
                    DownloadManager.STATUS_FAILED -> {
                        val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                        val reason = if (reasonIndex >= 0) cursor.getInt(reasonIndex) else -1
                        val reasonText = getFailureReasonText(reason)
                        showFailureNotification(context, downloadId, reasonText)
                        Log.d("OnDownloadCompleteListener", "Download failed: $reasonText")
                    } else -> {
                        Log.d("OnDownloadCompleteListener", "Download status: $status")
                }
                }
            }
        }
        cursor.close()
    }

    private suspend fun addToDatabase(
        book: LocalBook,
        filePath: String,
        app: Application
    ) {
        Log.d("OnDownloadCompleteListener", "entered add to database method")
        val downloadDao =
            AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).downloadsDao()
        val localBookDao =
            AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).localBookDao()
        downloadScope.launch(Dispatchers.IO) {
            Log.d("OnDownloadCompleteListener","entered coroutine")
            Log.d("OnDownloadCompleteListener", "book is $book")
            if (!localBookDao.isBookExists(book.id)){
                localBookDao.addBook(book)
                Log.d("OnDownloadCompleteListener", "Book $book added to localbook database")
            }

            downloadDao.addBook(Downloads(book.id, filePath))
            Log.d("OnDownloadCompleteListener", "Book $book added to downloads database")
        }
    }

    // Method to get the file path of a completed download
    private fun getDownloadedFilePath(downloadId: Long): String? {
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
                                return resolveContentUriToFilePath(fileUri, app.applicationContext)
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

    private fun getFailureReasonText(reason: Int): String {
        return when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "Download cannot be resumed."
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Device not found."
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists."
            DownloadManager.ERROR_FILE_ERROR -> "File error."
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP error."
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient space."
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects."
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP code."
            DownloadManager.ERROR_UNKNOWN -> "Unknown error."
            else -> "Download failed with reason code: $reason"
        }
    }


    private fun showFailureNotification(context: Context, downloadId: Long, reasonText: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "DOWNLOAD_FAILED",
                "Download Failed",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for failed downloads"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create notification
        val notification = NotificationCompat.Builder(context, "DOWNLOAD_FAILED")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Download Failed")
            .setContentText(reasonText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Display notification
        notificationManager.notify(downloadId.toInt(), notification)
    }
    }
