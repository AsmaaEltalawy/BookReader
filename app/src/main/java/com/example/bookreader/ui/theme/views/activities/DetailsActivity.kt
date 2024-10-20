package com.example.bookreader.ui.theme.views.activities

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.bookreader.R
import com.example.bookreader.adapter.CommentsAdapter
import com.example.bookreader.baseClass.BaseActivity
import com.example.bookreader.data.local.MySharedPreference
import com.example.bookreader.data.models.Comment
import com.example.bookreader.data.models.DownloadState
import com.example.bookreader.data.models.LocalBook
import com.example.bookreader.data.models.SharedData
import com.example.bookreader.databinding.ActivityDetailsBinding
import com.example.bookreader.databinding.DialogCommentBinding
import com.example.bookreader.ui.theme.viewmodels.DownloadViewModel
import com.example.bookreader.ui.theme.viewmodels.FavViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File


class DetailsActivity : BaseActivity() {

    lateinit var binding: ActivityDetailsBinding
    private val favViewModel: FavViewModel by viewModels()
    private val downloadViewModel: DownloadViewModel by viewModels()
    private lateinit var loadingDialog: AlertDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var commentsAdapter: CommentsAdapter
    private val commentsList = mutableListOf<Comment>()
    private lateinit var comment: String
    private val timestamp = System.currentTimeMillis().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_details)
        transparentStatus()
        loadingDialog = createLoadingDialog()
        firebaseAuth = FirebaseAuth.getInstance()
        val position = intent.getIntExtra("ITEM_POSITION", 0)
        val type = intent.getIntExtra("ITEM_TYPE", 0)
        val book: LocalBook?
        book = try {
            when (type) {
                5 -> SharedData.DownloadedList[position]
                4 -> SharedData.FavoritedList[position]
                3 -> SharedData.searchResults[position]
                2 -> SharedData.RecommendedBooks[position]
                1 -> SharedData.RecentBooks[position]
                0 -> SharedData.lastReadBook
                else -> null
            }
        } catch (e: Exception){
            null
        }

        val id = book?.id ?: ""
        Log.d("DetailsActivity", "Received book is at position: $position")
        var isDownloaded: DownloadState
        lifecycleScope.launch {
            var isFavorite = false
            val resultFav = getFavoriteStatus(id)
            isDownloaded = downloadViewModel.getDownloadStatus(id)
            when (resultFav) {
                1 -> isFavorite = true
                -1 -> isFavorite = false
            }

            withContext(Dispatchers.Main) {
                when (isFavorite) {
                    true -> {
                        binding.favButton.setBackgroundResource(R.drawable.hearted)
                    }

                    else -> {
                        binding.favButton.setBackgroundResource(R.drawable.heart)
                    }
                }

                when (isDownloaded) {
                    DownloadState.DOWNLOADED -> {
                        binding.downloadButton.setBackgroundResource(R.drawable.downloaded)
                    }

                    DownloadState.DOWNLOADING -> {
                        val downloadId = downloadViewModel.getDownloadIdById(id)
                        if (downloadId != null) {
                            binding.downloadButton.setBackgroundResource(R.drawable.waiting)
                            if (book != null) {
                                trackDownloadProgress(
                                    downloadId,
                                    downloadViewModel,
                                    book.id,
                                    binding.downloadProgressBar,
                                    binding.downloadButton
                                )
                            }
                        }
                    }

                    DownloadState.NOT_DOWNLOADED -> {
                        binding.downloadButton.setBackgroundResource(R.drawable.load)
                    }
                }
            }
        }

        if (book != null) {
            binding.book = book
            setupBookCover(book)
            fetchComments(book.id)  // Fetch comments when the book is set
        }

        downloadViewModel.download.observe(this) {
            lifecycleScope.launch {
                isDownloaded = downloadViewModel.getDownloadStatus(id)
                val downloadId = downloadViewModel.getDownloadIdById(id)
                Log.d("DetailsActivity", "Download ID: $downloadId")
                if (isDownloaded == DownloadState.DOWNLOADED) {
                    binding.downloadButton.setBackgroundResource(R.drawable.downloaded)
                }
                if (isDownloaded == DownloadState.NOT_DOWNLOADED) {
                    binding.downloadButton.setBackgroundResource(R.drawable.load)
                    binding.downloadProgressBar.visibility = View.GONE
                }
                if (downloadId != null && book != null) {
                    trackDownloadProgress(
                        downloadId,
                        downloadViewModel,
                        book.id,
                        binding.downloadProgressBar,
                        binding.downloadButton
                    )
                }
            }
        }

        binding.downloadButton.setOnClickListener {
            runBlocking { isDownloaded = downloadViewModel.getDownloadStatus(id) }
            lifecycleScope.launch {
                when (isDownloaded) {
                    DownloadState.DOWNLOADED -> {
                        try {
                            Log.d("DetailsActivity", "clicked to delete")
                            downloadViewModel.deleteFromDownload(binding.book)
                        } catch (e: Exception) {
                            showReDownloadDialog(this@DetailsActivity, e.message.toString())

                        }

                        lifecycleScope.launch {
                            isDownloaded = downloadViewModel.getDownloadStatus(id)
                            if (isDownloaded == DownloadState.NOT_DOWNLOADED) {
                                binding.downloadButton.setBackgroundResource(R.drawable.load)
                            }
                        }
                    }

                    DownloadState.DOWNLOADING -> {
                        Log.d("DetailsActivity", "Downloading")
                        Toast.makeText(this@DetailsActivity, "Downloading", Toast.LENGTH_SHORT)
                            .show()
                    }

                    DownloadState.NOT_DOWNLOADED -> {
                        try {
                            Log.d("DetailsActivity", "clicked to download")
                            downloadViewModel.addToDownload(binding.book)
                        } catch (e: Exception) {
                            showReDownloadDialog(this@DetailsActivity, e.message.toString())
                        }
                    }
                }
            }
        }

        binding.favButton.setOnClickListener {
            lifecycleScope.launch {
                val isFavorite = getFavoriteStatus(id)
                if (isFavorite == 1) {
                    favViewModel.deleteFromFav(binding.book)
                    it.setBackgroundResource(R.drawable.heart)
                } else {
                    favViewModel.addToFav(binding.book)
                    withContext(Dispatchers.Main) {
                        it.setBackgroundResource(R.drawable.hearting)
                    }
                    delay(70)
                    it.setBackgroundResource(R.drawable.hearted)
                }
            }
        }
        val prefs = MySharedPreference(this)
        binding.readButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val filePath = downloadViewModel.getFilePath(binding.book!!.id)
                    openPdfWithFallback(this@DetailsActivity, File(filePath ?: ""))
                    if (book != null) {
                        prefs.saveData(book.id)
                        Log.d("lastRead", "Last read book saved: ${prefs.getId()}")
                    }
                } catch (e: Exception) {
                    showReDownloadDialog(this@DetailsActivity, e.message.toString())
                }
            }
        }

        binding.addCommentBt.setOnClickListener {
            if (book != null) {
                addCommentDialog(book.id)
            }
        }
    }


    private fun showReDownloadDialog(context: Context, message: String) {
        val dialUI = LayoutInflater.from(context).inflate(R.layout.dial_ui, null)
        val okButton = dialUI.findViewById<Button>(R.id.ok_button)
        val messageTextView = dialUI.findViewById<TextView>(R.id.dial_description)
        messageTextView.text = message
        val dialog = AlertDialog.Builder(context)
            .setView(dialUI)
            .create()

        okButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun openPdfWithFallback(context: Context, file: File) {
        try {
            val uri =
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            // Try opening with a PDF viewer
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Open PDF")
            context.startActivity(chooser)

        } catch (e: ActivityNotFoundException) {
            // If no PDF viewer is installed, open it in a browser
            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.fromFile(file), "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            }

            try {
                context.startActivity(browserIntent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(context, "No PDF viewer or browser found", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private suspend fun getFavoriteStatus(id: String): Int = coroutineScope {
        val deferredResult = async(Dispatchers.IO) {
            favViewModel.getFavById(id)
        }
        deferredResult.await()
    }

    private fun transparentStatus() {
        val w = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun trackDownloadProgress(
        downloadId: Long,
        downloadViewModel: DownloadViewModel,
        bookID: String,
        progressBar: ProgressBar,
        downloadButton: ImageButton
    ) {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var status: Int = DownloadManager.STATUS_FAILED
        var state: DownloadState
        // Use a Handler to repeatedly check download progress every second
        val handler = Handler(Looper.getMainLooper())
        downloadButton.setBackgroundResource(R.drawable.waiting)
        handler.post(object : Runnable {
            override fun run() {
                runBlocking { state = downloadViewModel.getDownloadStatus(bookID) }
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                // Get the status of the download
                if (cursor != null && cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (statusIndex >= 0) {
                        status = cursor.getInt(statusIndex)
                        if (status == DownloadManager.STATUS_RUNNING) {
                            // Get progress
                            val bytesDownloadedIndex =
                                cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            val bytesTotalIndex =
                                cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            if (bytesDownloadedIndex >= 0 && bytesTotalIndex >= 0) {
                                val downloadedBytes = cursor.getInt(bytesDownloadedIndex)
                                val totalBytes = cursor.getInt(bytesTotalIndex)
                                if (totalBytes > 0) {
                                    val progress = (downloadedBytes * 100L) / totalBytes
                                    progressBar.visibility = View.VISIBLE
                                    progressBar.progress = progress.toInt()
                                }
                            }
                        } else if (state == DownloadState.DOWNLOADED) {
                            // Download completed, hide the progress bar
                            progressBar.visibility = View.GONE
                            downloadButton.setBackgroundResource(R.drawable.downloaded)
                            downloadButton.isEnabled = true
                            handler.removeCallbacksAndMessages(null)
                            //if DownloadManager.STATUS_FAILED or any other case
                        } else if (state != DownloadState.DOWNLOADING) {
                            progressBar.visibility = View.GONE
                            downloadButton.setBackgroundResource(R.drawable.load)
                            downloadButton.isEnabled = true
                            handler.removeCallbacksAndMessages(null)
                        }
                    }
                    cursor.close()

                    if (status == DownloadManager.STATUS_RUNNING) {
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        })
    }

    private fun createLoadingDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_loading, null)
        builder.setView(dialogView)
        builder.setCancelable(false)

        return builder.create()
    }

    private fun showLoadingDialog() {
        if (!loadingDialog.isShowing) {
            loadingDialog.show()
        }
    }

    private fun hideLoadingDialog() {
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }


    private fun setupBookCover(book: LocalBook) {
        Glide.with(this@DetailsActivity)
            .load(book.image)
            .placeholder(R.drawable.waiting)
            .error(R.drawable.error)
            .transform(
                BlurTransformation(
                    25,
                    3
                )
            )
            .into((object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    binding.blurView.background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            }))

        Glide.with(this@DetailsActivity)
            .load(book.image)
            .placeholder(R.drawable.waiting)
            .error(R.drawable.error)
            .into(binding.cover)
    }

    private fun addCommentDialog(bookId: String) {
        val commentAddBinding = DialogCommentBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        val alertDialog = builder.create()
        alertDialog.show()

        commentAddBinding.backBT.setOnClickListener { alertDialog.dismiss() }
        commentAddBinding.submitBT.setOnClickListener {
            // Correctly retrieve the comment text
            comment = commentAddBinding.comment.editText?.text.toString().trim()

            if (comment.isEmpty()) {
                Toast.makeText(this, "Enter comment", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
                addComment(bookId)
            }
        }
    }

    private fun addComment(bookId: String) {
        showLoadingDialog()
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = timestamp
        hashMap["timestamp"] = timestamp
        hashMap["comment"] = comment
        hashMap["uid"] = firebaseAuth.currentUser?.uid ?: ""

        firebaseAuth.currentUser?.let { user ->
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("name") ?: "Unknown User"
                        hashMap["username"] = username

                        // Add the comment to FireStore
                        val commentsRef = FirebaseFirestore.getInstance().collection("books")
                            .document(bookId).collection("comments").document(timestamp)

                        commentsRef.set(hashMap).addOnSuccessListener {
                            Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show()
                            hideLoadingDialog()
                            // Directly add the new comment to the list
                            val newComment = Comment(
                                id = timestamp,
                                comment = comment,
                                uid = firebaseAuth.currentUser?.uid ?: "",
                                username = username,
                                timestamp = timestamp
                            )
                            // Add the new comment to the list and notify the adapter
                            commentsList.add(0, newComment) // Add at the top for newest first
                            commentsAdapter.notifyItemInserted(0)
                            binding.commentsRecyclerView.scrollToPosition(0) // Scroll to the top
                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Failed to add comment: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            hideLoadingDialog()
                        }
                    }
                }
        }
    }

    private fun fetchComments(bookId: String) {
        val commentsRef = FirebaseFirestore.getInstance().collection("books").document(bookId)
            .collection("comments")
        commentsRef.orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { documents ->
                commentsList.clear()
                for (document in documents) {
                    val comment = document.toObject(Comment::class.java)
                    commentsList.add(comment)
                }
                updateRecyclerView(bookId)
            }.addOnFailureListener { e ->
                Log.e("DetailsActivity", "Error fetching comments: ${e.message}")
            }
    }

    private fun updateRecyclerView(bookId: String) {
        commentsAdapter = CommentsAdapter(commentsList, bookId, this)
        binding.commentsRecyclerView.adapter = commentsAdapter
    }
}