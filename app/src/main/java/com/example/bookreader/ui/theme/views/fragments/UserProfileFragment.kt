package com.example.bookreader.ui.theme.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bookreader.R
import com.example.bookreader.databinding.FragmentUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.bookreader.data.models.User
import com.google.android.material.appbar.AppBarLayout

class UserProfileFragment : Fragment() {

    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val userId = arguments?.getString("userId")
        if (userId != null) {
            fetchUserData(userId)
        } else {
            // Handle missing userId case
            Log.e("UserProfileFragment", "No userId provided")
        }

    }

    private fun fetchUserData(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") // Get the username
                    val email = document.getString("email") // Get the email

                    // Update the UI with the user's name and email
                    binding.titleName.text = name
                    binding.profileUsername.text = name
                    binding.profileEmail.text = email
                } else {
                    Log.e("UserProfileFragment", "No such user found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserProfileFragment", "Error fetching user data: ", e)
            }
    }
    override fun onResume() {
        super.onResume()
        val appBarLayout = requireActivity().findViewById<AppBarLayout>(R.id.appBarLayout)
        appBarLayout.setExpanded(false, false)
    }
}
