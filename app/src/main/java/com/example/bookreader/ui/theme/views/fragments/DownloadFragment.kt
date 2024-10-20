package com.example.bookreader.ui.theme.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.bookreader.R
import com.example.bookreader.adapter.BookOnClickListener
import com.example.bookreader.adapter.LibraryAdapter
import com.example.bookreader.databinding.FragmentDownloadBinding
import com.example.bookreader.ui.theme.viewmodels.DownloadViewModel
import com.example.bookreader.ui.theme.views.activities.DetailsActivity
import com.example.bookreader.ui.theme.views.fragments.HomeFragment.Companion.open
import com.google.android.material.appbar.AppBarLayout

class DownloadFragment : Fragment(), BookOnClickListener {

    private lateinit var binding: FragmentDownloadBinding
    private val downloadViewModel: DownloadViewModel by viewModels()
    private lateinit var downloadAdapter: LibraryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownloadBinding.inflate(inflater, container, false)
        val appBarLayout = activity?.findViewById<AppBarLayout>(R.id.appBarLayout)
        appBarLayout?.setExpanded(false, false)
        binding.downloadRecyclerView.isNestedScrollingEnabled = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the adapter and set it to the RecyclerView
        downloadAdapter = LibraryAdapter(this)
        binding.downloadRecyclerView.adapter = downloadAdapter

        // Observe downloaded books from the ViewModel
        downloadViewModel.booksDownload.observe(viewLifecycleOwner, Observer { books ->
            if (books.isNullOrEmpty()) {
                // Show empty state
                binding.emptyStateImageView.visibility = View.VISIBLE
                binding.emptyStateTextView.visibility = View.VISIBLE
                binding.downloadRecyclerView.visibility = View.GONE
                binding.downloadCountTextView.visibility = View.GONE
            } else {
                // Show RecyclerView with data
                binding.emptyStateImageView.visibility = View.GONE
                binding.emptyStateTextView.visibility = View.GONE
                binding.downloadRecyclerView.visibility = View.VISIBLE
                val count = books.size
                binding.downloadCountTextView.text = getString(R.string.downloadCounter, count) // Update text
                binding.downloadCountTextView.visibility = View.VISIBLE
                downloadAdapter.submitList(books)
            }
        })

        // Load downloaded books when the fragment is created
        loadDownloadedBooks()
    }

    private fun loadDownloadedBooks() {
        downloadViewModel.getAllDownloads() // Ensure this method is implemented in your ViewModel
    }

    override fun bookOnClick(position: Int) {
        open(requireContext(), DetailsActivity::class.java, position = position, type = 5)
    }
}
