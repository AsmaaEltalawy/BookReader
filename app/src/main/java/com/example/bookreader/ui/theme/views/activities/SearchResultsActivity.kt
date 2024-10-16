package com.example.bookreader.ui.theme.views.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookreader.R
import com.example.bookreader.adapter.BookAdapter
import com.example.bookreader.adapter.BookOnClickListener
import com.example.bookreader.baseClass.BaseActivity
import com.example.bookreader.data.models.SharedData
import com.example.bookreader.databinding.ActivitySearchResultsBinding
import com.example.bookreader.ui.theme.viewmodels.HomeViewModel
import com.example.bookreader.utils.NetworkUtils
import kotlinx.coroutines.launch

class SearchResultsActivity : BaseActivity(), BookOnClickListener {
    lateinit var binding: ActivitySearchResultsBinding
    private val apiViewModel: HomeViewModel by viewModels()
    private lateinit var bookAdapter: BookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_results)
        val query = intent.getStringExtra("query")

        binding.searchResultsError.visibility = android.view.View.GONE
        binding.searchResultsProgressBar.visibility = android.view.View.VISIBLE

        if (NetworkUtils.isNetworkAvailable(this)) {
            if (!query.isNullOrEmpty()) {
                val formattedQuery = formatQuery(query)
                apiViewModel.booksRecommend.observe(this) { booksResponse ->
                    if (booksResponse.books.isNotEmpty()) {
                        bookAdapter.submitList(booksResponse.books.toMutableList())
                        SharedData.searchResults = booksResponse.books.toMutableList()
                        binding.searchResultsRV.visibility = android.view.View.VISIBLE
                        binding.searchResultsError.visibility = android.view.View.GONE
                        binding.searchResultsProgressBar.visibility = android.view.View.GONE
                    } else {
                        showError()
                    }
                }
                lifecycleScope.launch {
                    try {
                        apiViewModel.getRecommendBooks(formattedQuery)
                    } catch (e: Exception) {
                        showError()
                    }
                }
                setupRecyclerView()
            } else {
                showError()
            }
        } else {
            showError()
        }
    }
    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(this)
        binding.searchResultsRV.layoutManager = GridLayoutManager(this, 2)
        binding.searchResultsRV.adapter = bookAdapter
    }

    private fun showError() {
        binding.searchResultsRV.visibility = android.view.View.GONE
        binding.searchResultsProgressBar.visibility = android.view.View.GONE
        binding.searchResultsError.visibility = android.view.View.VISIBLE
    }

    override fun bookOnClick(position: Int) {
        open(this, DetailsActivity::class.java, position, type = 3)
    }

    companion object {

        fun formatQuery(query: String): String {
            return query
                .replace("[^a-zA-Z]".toRegex(), "") // Remove non-alphabet characters
                .take(9)// Take the first 9 characters
        }

        fun open(
            context: Context,
            destination: Class<*>,
            position: Int,
            type: Int
        ) {
            val intent = Intent(context, destination)
            intent.putExtra("ITEM_POSITION", position)
            intent.putExtra("ITEM_TYPE", type)
            context.startActivity(intent)
        }
    }
}

