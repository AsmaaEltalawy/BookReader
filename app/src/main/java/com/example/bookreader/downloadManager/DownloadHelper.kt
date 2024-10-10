package com.example.bookreader.downloadManager

import android.annotation.SuppressLint
import android.app.Application
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.bookreader.data.local.AppDatabase
import com.example.bookreader.data.models.Downloads
import com.example.bookreader.data.models.LocalBook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DownloadHelper(private val context: Context) {

    private lateinit var app : Application
    private val job = Job()
    private val downloadScope = CoroutineScope(Dispatchers.IO + job)

    private var downloadId: Long = 0
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // Method to start the download
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun startDownload(book: LocalBook, app: Application): Boolean {
        var isDone = false
        val bookUrl = book.download
        val bookTitle = book.title
        this.app = app

        val request = DownloadManager.Request(Uri.parse(bookUrl))
            .setTitle(bookTitle)
            .setDescription("Downloading...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$bookTitle.pdf")

        downloadId = downloadManager.enqueue(request)

        val filePath = getDownloadedFilePath(downloadId)

        // Create intent and pass the book object to the BroadcastReceiver
        val intent = Intent(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        intent.putExtra("book", book as Parcelable)

        // Register the BroadcastReceiver with the intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }

        downloadScope.launch {
            updateDatabaseWithFilePath(book, filePath ?: "", downloadId, app)
            isDone = true
        }
        return isDone
    }


    // BroadcastReceiver to handle completion
    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                val book = if (Build.VERSION.SDK_INT >= 33) {
                    intent.getParcelableExtra("book", LocalBook::class.java)
                } else {
                    @Suppress("DEPRECATION") intent.getParcelableExtra("book")
                }
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor: Cursor = downloadManager.query(query)

                val filePath = getDownloadedFilePath(downloadId)

                if (cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (statusIndex >= 0) {
                        val status = cursor.getInt(statusIndex)

                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                downloadScope.launch {
                                    if (book != null) {
                                        updateDatabaseWithFilePath(book, filePath ?: "", downloadId, app)
                                    }
                                }
                                Toast.makeText(context.applicationContext, "Download completed: $filePath", Toast.LENGTH_LONG).show()
                            }

                            DownloadManager.STATUS_FAILED -> {
                                val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                                val reason = if (reasonIndex >= 0) cursor.getInt(reasonIndex) else -1

                                downloadScope.launch {
                                    if (book != null) {
                                        removeFailedDownloadFromDatabase(book, filePath ?: "", app)
                                    }
                                }

                                val reasonText = getFailureReasonText(reason)
                                showFailureNotification(reasonText)
                            }
                        }
                    }
                }
                cursor.close()
            }
        }
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
            else -> "Download failed."
        }
    }

    private fun showFailureNotification(reasonText: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel if Android O or later
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



    // Update database with the file path once download completes
    private suspend fun updateDatabaseWithFilePath(book: LocalBook, filePath: String, downloadId: Long, app: Application) {
        val downloadDao =
            AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).downloadsDao()
        val localBookDao =
            AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).localBookDao()
        downloadScope.launch(Dispatchers.IO) {
            if(localBookDao.isBookExists(book.id))
                localBookDao.updateBook(book)
            else
                localBookDao.addBook(book)
            downloadDao.addBook(Downloads(book.id, filePath, downloadId))
        }
    }

    // Method to get the file path of a completed download
    fun getDownloadedFilePath(downloadId: Long): String? {
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
                                return resolveContentUriToFilePath(fileUri)
                            }
                        }
                    }
                }
            }
        }
        cursor.close()
        return null
    }

    private fun resolveContentUriToFilePath(uri: Uri): String? {
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

    fun removeFailedDownloadFromDatabase(book: LocalBook, filePath: String, app: Application) : Boolean {
        var done = false
        val downloadDao =
            AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).downloadsDao()
        val localBookDao =
            AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).localBookDao()
        downloadScope.launch(Dispatchers.IO) {
            if (localBookDao.isBookExists(book.id))
                localBookDao.updateBook(book)
            else
                localBookDao.deleteBook(book)
            if(File(filePath).exists()) {
                Log.d("delete from downloads", "File exists")
                deleteDownloadedFile(filePath,app)
            } else{
                Log.d("delete from downloads", "File : $filePath")
                Log.e("delete from downloads", "File does not exist")
            }
            //-1 means we don't care,
            // we don't need to search for the downloadID to delete or search for the book
            downloadDao.deleteBook(Downloads(book.id, filePath, -1))
            done= true
            Log.d("delete from downloads", "Book: $book")
        }
        return done
    }


    private fun deleteDownloadedFile(filePath: String,app: Application): Boolean {
        val file = File(filePath)
        Log.d("delete from downloads", "File: ${file.name} and its path is $filePath")
        return if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                Log.d("delete from downloads", "File deleted successfully.")
                downloadScope.launch {
                    withContext(Dispatchers.Main){
                        Toast.makeText(app.applicationContext, "Book deleted successfully.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                downloadScope.launch {
                    withContext(Dispatchers.Main){
                        Toast.makeText(app.applicationContext, "Failed to delete book.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            deleted
        } else {
            downloadScope.launch {
                withContext(Dispatchers.Main){
                    Toast.makeText(app.applicationContext, "File does not exist.", Toast.LENGTH_LONG).show()
                }
            }
            false
        }
    }

    // Method to query the download progress
    fun getDownloadProgress(downloadId: Long): Int {
        var totalBytes = 0
        var bytesDownloaded = 0
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor: Cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val statusIndexLoaded = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val statusIndexTotal = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            if (statusIndexLoaded != -1) {
                 bytesDownloaded = cursor.getInt(statusIndexLoaded)
            }
            if (statusIndexTotal != -1) {
                 totalBytes = cursor.getInt(statusIndexTotal)
            }
            if (totalBytes > 0) {
                return (bytesDownloaded * 100L / totalBytes).toInt()
            }
        }
        cursor.close()
        return -1
    }
}
