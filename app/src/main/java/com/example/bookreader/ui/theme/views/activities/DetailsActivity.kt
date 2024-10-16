package com.example.bookreader.ui.theme.views.activities


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import com.example.bookreader.data.local.MySharedPreference
import com.example.bookreader.data.models.Comment
import com.example.bookreader.data.models.LocalBook
import com.example.bookreader.data.models.SharedData
import com.example.bookreader.databinding.ActivityDetailsBinding
import com.example.bookreader.databinding.DialogCommentBinding
import com.example.bookreader.ui.theme.viewmodels.DownloadViewModel
import com.example.bookreader.ui.theme.viewmodels.FavViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class DetailsActivity : ComponentActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private val favViewModel: FavViewModel by viewModels()
    private val downloadViewModel: DownloadViewModel by viewModels()
    private lateinit var loadingDialog: AlertDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var commentsAdapter: CommentsAdapter
    private val commentsList = mutableListOf<Comment>()
    private lateinit var comment : String
    private lateinit var bookId : String
    private val timestamp = System.currentTimeMillis().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_details)
        transparentStatus()

        loadingDialog = createLoadingDialog()
        firebaseAuth = FirebaseAuth.getInstance()


        val position = intent.getIntExtra("ITEM_POSITION", 0)
        val type = intent.getIntExtra("ITEM_TYPE", 0)

        val book = when (type) {
            3 -> SharedData.searchResults[position]
            2 -> SharedData.RecommendedBooks[position]
            1 -> SharedData.RecentBooks[position]
            0 -> SharedData.lastReadBook
            else -> null
        }

        if (book != null) {
            binding.book = book
            setupBookCover(book)
            fetchComments(book.id)  // Fetch comments when the book is set
        }

         bookId = book?.id ?: ""
        Log.d("DetailsActivity", "Received book is at position: $position")

        lifecycleScope.launch {
            val isFavorite = getFavoriteStatus(bookId) == 1
            val isDownloaded = getDownloadStatus(bookId) == 1

            withContext(Dispatchers.Main) {
                binding.favButton.setBackgroundResource(if (isFavorite) R.drawable.hearted else R.drawable.heart)
                binding.downloadButton.setBackgroundResource(if (isDownloaded) R.drawable.downloaded else R.drawable.load)
            }
        }

        if (book != null) {
            binding.book = book
            setupBookCover(book)
        }

        binding.downloadButton.setOnClickListener {
            handleDownloadButtonClick(book)
        }

        binding.favButton.setOnClickListener {
            handleFavoriteButtonClick(book)
        }

        val prefs = MySharedPreference(this)
        binding.readButton.setOnClickListener {
            handleReadButtonClick(book, prefs)
        }

        binding.addCommentBt.setOnClickListener {
            if (book != null) {
                addCommentDialog(book.id)
            }
        }
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
        Glide.with(this)
            .load(book.image)
            .placeholder(R.drawable.waiting)
            .error(R.drawable.error)
            .into(binding.cover)

        Glide.with(this)
            .load(book.image)
            .placeholder(R.drawable.waiting)
            .error(R.drawable.error)
            .transform(BlurTransformation(25, 3))
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    binding.blurView.background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun handleDownloadButtonClick(book: LocalBook?) {
        lifecycleScope.launch {
            val isDownloaded = getDownloadStatus(book?.id ?: "") == 1
            if (isDownloaded) {
                downloadViewModel.deleteFromDownload(book)
                binding.downloadButton.setBackgroundResource(R.drawable.load)
            } else {
                try {
                    downloadViewModel.addToDownload(book)
                    binding.downloadButton.setBackgroundResource(R.drawable.downloaded)
                } catch (e: Exception) {
                    showReDownloadDialog(this@DetailsActivity, e.message.toString())
                }
            }
        }
    }

    private fun handleFavoriteButtonClick(book: LocalBook?) {
        lifecycleScope.launch {
            val isFavorite = getFavoriteStatus(book?.id ?: "") == 1
            if (isFavorite) {
                favViewModel.deleteFromFav(book)
                binding.favButton.setBackgroundResource(R.drawable.heart)
            } else {
                favViewModel.addToFav(book)
                binding.favButton.setBackgroundResource(R.drawable.hearting)
                delay(70)
                binding.favButton.setBackgroundResource(R.drawable.hearted)
            }
        }
    }

    private fun handleReadButtonClick(book: LocalBook?, prefs: MySharedPreference) {
        lifecycleScope.launch {
            try {
                val filePath = downloadViewModel.getFilePath(book?.id ?: "")
                openPdfWithFallback(this@DetailsActivity, File(filePath ?: ""))
                prefs.saveData(book?.id ?: "")
                Log.d("lastRead", "Last read book saved: ${prefs.getId()}")
            } catch (e: Exception) {
                showReDownloadDialog(this@DetailsActivity, e.message.toString())
            }
        }
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

        // Fetch the username from FireStore

        firebaseAuth.currentUser?.let { user ->
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("name") ?: "Unknown User"
                    hashMap["username"] = username

                    // Add the comment to the FireStore for the specific book
                    val commentsRef = FirebaseFirestore.getInstance().collection("books")
                        .document(bookId).collection("comments").document(timestamp)

                    commentsRef.set(hashMap).addOnSuccessListener{
                        Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show()
                        hideLoadingDialog()
                        fetchComments(bookId) // Fetch comments to display
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to add comment: ${e.message}", Toast.LENGTH_SHORT).show()
                        hideLoadingDialog()
                    }
                }
            }
        }
    }

    private fun fetchComments(bookId: String) {
        val commentsRef = FirebaseFirestore.getInstance().collection("books").document(bookId).collection("comments")
        commentsRef.orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener { documents ->
            commentsList.clear()
            for (document in documents) {
                val comment = document.toObject(Comment::class.java)
                commentsList.add(comment)
            }
            updateRecyclerView()
        }.addOnFailureListener { e ->
            Log.e("DetailsActivity", "Error fetching comments: ${e.message}")
        }
    }

    private fun updateRecyclerView() {
        commentsAdapter = CommentsAdapter(commentsList, bookId, this)
        binding.commentsRecyclerView.adapter = commentsAdapter
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
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

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
                Toast.makeText(context, "No PDF viewer or browser found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getFavoriteStatus(id: String): Int = coroutineScope {
        val deferredResult = async(Dispatchers.IO) {
            favViewModel.getFavById(id)
        }
        deferredResult.await()
    }

    private suspend fun getDownloadStatus(id: String): Int = coroutineScope {
        val deferredResult = async(Dispatchers.IO) {
            downloadViewModel.getDownloadById(id)
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
}