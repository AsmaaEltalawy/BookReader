package com.example.bookreader.ui.theme.views.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
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
import com.example.bookreader.data.models.DetailsResponse
import com.example.bookreader.databinding.DetailsActivityBinding
import com.example.bookreader.ui.theme.viewmodels.DownloadViewModel
import com.example.bookreader.ui.theme.viewmodels.FavViewModel
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.graphics.Color


class DetailsActivity : ComponentActivity() {

    lateinit var binding: DetailsActivityBinding
    private val favViewModel: FavViewModel by viewModels()
    private val downloadViewModel: DownloadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.details_activity)
        transparentStatus()
        val detailsResponse = intent.getSerializableExtra("detailsResponse") as? DetailsResponse
        val position = intent.getIntExtra("ITEM_POSITION", 0)
        val id = detailsResponse?.id ?: "null"
        Log.d("DetailsActivity", "Received detailsResponse is at position: $position")
        lifecycleScope.launch {
            var isFavorite = false
            var isDownloaded = false
            val resultFav = getFavoriteStatus(id)
            val resultLoad = getDownloadStatus(id)
            when (resultFav) {
                1 -> isFavorite = true
                -1 -> isFavorite = false
            }
            when (resultLoad) {
                1 -> isDownloaded = true
                -1 -> isDownloaded = false

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
                    true -> {
                        binding.downloadButton.setBackgroundResource(R.drawable.downloaded)
                    }

                    false -> {
                        binding.downloadButton.setBackgroundResource(R.drawable.load)
                    }
                }
            }
        }

        if (detailsResponse != null) {
            binding.book = detailsResponse
            Glide.with(this@DetailsActivity)
                .load(binding.book?.image)
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
                .load(binding.book?.image)
                .placeholder(R.drawable.waiting)
                .error(R.drawable.error)
                .into(binding.cover)

        }

        binding.downloadButton.setOnClickListener {
            lifecycleScope.launch {
                val isDownloaded = getDownloadStatus(id)
                if (isDownloaded == 1) {
                    downloadViewModel.deleteFromDownload(binding.book)
                    it.setBackgroundResource(R.drawable.load)
                } else {
                    downloadViewModel.addToDownload(binding.book)
                    it.setBackgroundResource(R.drawable.downloaded)
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

        binding.readButton.setOnClickListener {

            lifecycleScope.launch {
                try {
                    val filePath = downloadViewModel.getFilePath(binding.book!!.id)
                    openPdfWithFallback(this@DetailsActivity, File(filePath ?: ""))
                } catch (e: Exception) {
                    showReDownloadDialog(this@DetailsActivity)
                }
            }
        }
    }



    @SuppressLint("MissingInflatedId")
    private fun showReDownloadDialog(context: Context) {
        val dialUI = LayoutInflater.from(context).inflate(R.layout.dial_ui, null)
        val okButton = dialUI.findViewById<Button>(R.id.ok_button)
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