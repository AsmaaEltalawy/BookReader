package com.example.bookreader.ui.theme.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.bookreader.R
import com.example.bookreader.data.models.User
import com.example.bookreader.databinding.FragmentProfileBinding
import com.example.bookreader.ui.theme.viewmodels.DownloadViewModel
import com.example.bookreader.ui.theme.viewmodels.FavViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var downloadViewModel: DownloadViewModel
    private lateinit var favViewModel: FavViewModel
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: User
    private lateinit var appBarLayout: AppBarLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the binding layout
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        appBarLayout = requireActivity().findViewById(R.id.appBarLayout)
        auth = FirebaseAuth.getInstance()

        // Set up the edit profile button listener
        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }


        // Initialize ViewModels
        downloadViewModel = ViewModelProvider(this)[DownloadViewModel::class.java]
        favViewModel = ViewModelProvider(this)[FavViewModel::class.java]

        // Observe data for favorites and downloads
        observeFavorites()
        observeDownloads()

        // Fetch and display user data
        fetchUserData()

        Log.d("ProfileFragment", "ProfileFragment is created")

        appBarLayout.setExpanded(false, false)

        return binding.root
    }


    private fun observeFavorites() {
        // Call the method to get all favorites
        favViewModel.getAllFavorites()

        // Observe the LiveData containing the list of favorite books
        favViewModel.booksFav.observe(viewLifecycleOwner) { favoriteBooks ->
            // Update the count in the UI
            binding.favoritesNo.text = favoriteBooks?.size.toString()
        }
    }

    private fun observeDownloads() {
        // Call the method to get all downloaded books
        downloadViewModel.getAllDownloads()

        // Observe the LiveData containing the list of downloaded books
        downloadViewModel.booksDownload.observe(viewLifecycleOwner) { downloadedBooks ->
            // Update the count in the UI
            binding.downloadsNo.text = downloadedBooks.size.toString()
        }
    }

    private fun fetchUserData() {
        val user = auth.currentUser

        if (user != null) {
            binding.profileEmail.text = user.email ?: "N/A"
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        currentUser = document.toObject(User::class.java) ?: User()
                        binding.profileUsername.text = currentUser.name
                        binding.titleName.text = currentUser.name
                    }
                }
        } else {
            Log.e("ProfileFragment", "User not authenticated")
        }
        }

}
