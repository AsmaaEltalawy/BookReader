package com.example.bookreader.ui.theme.views.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookreader.R
import com.example.bookreader.adapter.BookAdapter
import com.example.bookreader.adapter.BookOnClickListener
import com.example.bookreader.data.models.DetailsResponse
import com.example.bookreader.data.models.RecentSearches
import com.example.bookreader.databinding.SearchResultsActivityBinding
import com.example.bookreader.ui.theme.viewmodels.HomeViewModel
import kotlinx.coroutines.launch

class SearchResultsActivity : ComponentActivity(), BookOnClickListener {
    lateinit var binding: SearchResultsActivityBinding
    private val apiViewModel: HomeViewModel by viewModels()
    private lateinit var bookAdapter: BookAdapter
    private lateinit var results : List<DetailsResponse>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.search_results_activity)
        val queryOBJ = intent.getSerializableExtra("query") as? RecentSearches
        val id = queryOBJ?.id
        val query = queryOBJ?.query
        Log.d("serchresultsquery", "Received query: $query")

        if (query != null && query != "") {
            val formattedQuery = formatQuery(query)
            apiViewModel.getRecommendBooks(formattedQuery)
        }
        bookAdapter = BookAdapter(this)
        binding.searchResultsRV.layoutManager = GridLayoutManager(this, 2)
        binding.searchResultsRV.adapter = bookAdapter
        apiViewModel.booksRecommend.observe(this) {
            val recommendedBooks = it.books.toMutableList()
            lifecycleScope.launch {
                bookAdapter.submitList(recommendedBooks)
                results = recommendedBooks
            }
        }
    }

    override fun bookOnClick(position: Int) {
        val book = results[position]
        open(this, DetailsActivity::class.java, book)
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
            book: DetailsResponse? = null,
            position: Int = 0
        ) {
            val intent = Intent(context, destination)
            val bundle = Bundle()
            bundle.putSerializable("detailsResponse", book)
            bundle.let { intent.putExtras(it) }
            intent.putExtra("ITEM_POSITION", position)
            context.startActivity(intent)
        }
    }
}

