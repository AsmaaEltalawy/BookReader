package com.example.bookreader.ui.theme.views.fragments.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookreader.R
import com.example.bookreader.adapter.BookAdapter
import com.example.bookreader.adapter.BookOnClickListener
import com.example.bookreader.data.models.SharedData
import com.example.bookreader.databinding.ActivitySearchResultsBinding
import com.example.bookreader.ui.theme.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchResultsActivity : ComponentActivity(), BookOnClickListener {
    lateinit var binding: ActivitySearchResultsBinding
    private val apiViewModel: HomeViewModel by viewModels()
    private lateinit var bookAdapter: BookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_results)
        val query = intent.getStringExtra("query")

        if (query != null && query != "") {
            val formattedQuery = formatQuery(query)
            apiViewModel.getRecommendBooks(formattedQuery)
        }

        binding.searchResultsProgressBar.visibility = android.view.View.VISIBLE

        bookAdapter = BookAdapter(this)
        binding.searchResultsRV.layoutManager = GridLayoutManager(this, 2)
        binding.searchResultsRV.adapter = bookAdapter
        apiViewModel.booksRecommend.observe(this) {
            SharedData.searchResults = it.books.toMutableList()
            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    binding.searchResultsProgressBar.visibility = android.view.View.GONE
                }
                bookAdapter.submitList(SharedData.searchResults)
            }
        }
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
            position: Int ,
            type: Int
        ) {
            val intent = Intent(context, destination)
            intent.putExtra("ITEM_POSITION", position)
            intent.putExtra("ITEM_TYPE", type)
            context.startActivity(intent)
        }
    }
}

