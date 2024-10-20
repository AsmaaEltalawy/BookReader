package com.example.bookreader.ui.theme.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.bookreader.R
import com.example.bookreader.adapter.BookOnClickListener
import com.example.bookreader.adapter.LibraryAdapter
import com.example.bookreader.databinding.FragmentFavoriteBinding
import com.example.bookreader.ui.theme.viewmodels.FavViewModel
import com.example.bookreader.ui.theme.views.activities.DetailsActivity
import com.example.bookreader.ui.theme.views.fragments.HomeFragment.Companion.open
import com.google.android.material.appbar.AppBarLayout

class FavoriteFragment : Fragment(), BookOnClickListener {

    private lateinit var binding: FragmentFavoriteBinding
    private val favViewModel: FavViewModel by viewModels()
    private lateinit var favoriteAdapter: LibraryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        val appBarLayout = activity?.findViewById<AppBarLayout>(R.id.appBarLayout)
        appBarLayout?.setExpanded(false, false)
        binding.favoriteRecyclerView.isNestedScrollingEnabled = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        favoriteAdapter = LibraryAdapter(this)
        binding.favoriteRecyclerView.adapter = favoriteAdapter

        // Observe the list of favorite books
        favViewModel.booksFav.observe(viewLifecycleOwner) { favorites ->
            if (favorites.isNullOrEmpty()) {
                // Show empty state
                binding.emptyStateImageView.visibility = View.VISIBLE
                binding.emptyStateTextView.visibility = View.VISIBLE
                binding.favoriteRecyclerView.visibility = View.GONE
                binding.favoriteCountTextView.visibility = View.GONE
            } else {
                // Show RecyclerView with data
                binding.emptyStateImageView.visibility = View.GONE
                binding.emptyStateTextView.visibility = View.GONE
                binding.favoriteRecyclerView.visibility = View.VISIBLE
                val count = favorites.size
                binding.favoriteCountTextView.text = getString(R.string.favoriteCounter, count)
                binding.favoriteCountTextView.visibility = View.VISIBLE
                favoriteAdapter.submitList(favorites)
            }
        }

        // Fetch favorite books when fragment is opened
        favViewModel.getAllFavorites()
    }

    // Handle click on a book
    override fun bookOnClick(position: Int) {
        open(requireContext(), DetailsActivity::class.java, position = position, type = 4)
    }
}
