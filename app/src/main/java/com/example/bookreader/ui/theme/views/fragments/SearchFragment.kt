package com.example.bookreader.ui.theme.views.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookreader.R
import com.example.bookreader.adapter.RecentSearchAdapter
import com.example.bookreader.adapter.SearchOnClickListener
import com.example.bookreader.ui.theme.views.fragments.activities.SearchResultsActivity
import com.example.bookreader.data.models.RecentSearches
import com.example.bookreader.databinding.FragmentSearchBinding
import com.example.bookreader.ui.theme.viewmodels.SearchViewModel
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.launch


class SearchFragment : Fragment(), SearchOnClickListener {
    lateinit var binding: FragmentSearchBinding
    private lateinit var data: List<RecentSearches>
    private lateinit var recentSearchAdapter: RecentSearchAdapter
    private val searchViewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        Log.e("SearchFragment", "Binding: $binding")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recentSearchAdapter = RecentSearchAdapter(this)
        binding.recentSearchesRV.layoutManager = object : LinearLayoutManager(requireContext()) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        binding.recentSearchesRV.adapter = recentSearchAdapter

        binding.searchBT.setOnClickListener {
            lifecycleScope.launch {
                val query = binding.searchView.query.toString()
                searchViewModel.addQuery(query)
                open(requireContext(), SearchResultsActivity::class.java, query)
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    lifecycleScope.launch {
                        searchViewModel.addQuery(query)
                        open(requireContext(), SearchResultsActivity::class.java, query)
                    }
                }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val appBarLayout = requireActivity().findViewById<AppBarLayout>(R.id.appBarLayout)
        appBarLayout.setExpanded(true, true)
        searchViewModel.getRecentSearches(8)
        searchViewModel.recentSearches.observe(viewLifecycleOwner) {
            data = it
            if (data.size < 8) recentSearchAdapter.submitList(data)
            else recentSearchAdapter.submitList(data.take(8))
            binding.recentSearchesRV.scrollToPosition(0)
        }
    }
    override fun searchOnClick(position: Int, id: Int) {
        binding.searchView.setQuery(data[position].query, false)
    }

    companion object {
        fun open(
            context: Context,
            destination: Class<*>,
            query: String,
            id: Int = 0
        ) {
            val intent = Intent(context, destination)
            intent.putExtra("query", query)
            intent.putExtra("id", id)
            context.startActivity(intent)
        }
    }
}

