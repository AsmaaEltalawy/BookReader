package com.example.bookreader.ui.theme.views.fragments

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bookreader.R
import com.example.bookreader.data.models.User
import com.example.bookreader.databinding.FragmentEditProfileBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var currentUser: User
    private var isCurrentPasswordVisible: Boolean = false
    private var isNewPasswordVisible: Boolean = false
    private lateinit var appBarLayout: AppBarLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize View Binding
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        appBarLayout = requireActivity().findViewById(R.id.appBarLayout)
        auth = FirebaseAuth.getInstance()

        val backButton: ImageButton = binding.root.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Navigate back to ProfileFragment
            findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
        }


        // Fetch current user data
        fetchUserData()

        // Set click listener to save profile changes
        binding.saveButton.setOnClickListener {
            updateUserData()
        }

        binding.toggleCurrentPasswordVisibility.setOnClickListener {
            togglePasswordVisibility(binding.currentPassword, binding.toggleCurrentPasswordVisibility, isCurrentPasswordVisible)
            isCurrentPasswordVisible = !isCurrentPasswordVisible
        }

        binding.toggleNewPasswordVisibility.setOnClickListener {
            togglePasswordVisibility(binding.editPassword, binding.toggleNewPasswordVisibility, isNewPasswordVisible)
            isNewPasswordVisible = !isNewPasswordVisible
        }

        appBarLayout.setExpanded(false, false)

        return binding.root
    }

    private fun togglePasswordVisibility(passwordField: EditText, toggleButton: ImageButton, isPasswordVisible: Boolean) {
        if (isPasswordVisible) {
            // Hide the password
            passwordField.transformationMethod = PasswordTransformationMethod.getInstance()
            toggleButton.setImageResource(R.drawable.visibility_off)
        } else {
            // Show the password
            passwordField.transformationMethod = null
            toggleButton.setImageResource(R.drawable.visibility_on)
        }

        // Keep the cursor at the end of the text
        passwordField.setSelection(passwordField.text.length)
    }


    private fun fetchUserData() {
        val user = auth.currentUser

        if (user != null) {
            // Set the current email in the editEmail field
            binding.editEmail.setText(user.email)

            // Fetch other user details from Firestore
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        currentUser = document.toObject(User::class.java) ?: User()
                        binding.editUsername.setText(currentUser.name)
                    }
                }.addOnFailureListener { exception ->
                    Log.e("EditProfileFragment", "Error fetching user data", exception)
                    Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserData() {
        val user = auth.currentUser
        val updatedEmail = binding.editEmail.text.toString().trim()
        val updatedUsername = binding.editUsername.text.toString().trim()
        val updatedPassword = binding.editPassword.text.toString().trim()
        val currentPasswordInput = binding.currentPassword.text.toString().trim() // For re-authentication

        if (user != null) {
            // Check if the current password was provided
            if (currentPasswordInput.isNotEmpty()) {
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPasswordInput)

                // Re-authenticate the user
                user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // Update email if changed
                        if (updatedEmail != user.email) {
                            updateEmail(user, updatedEmail)
                        }

                        // Update password if provided
                        if (updatedPassword.isNotEmpty()) {
                            updatePassword(user, updatedPassword)
                        }

                        // Update the username in Firestore if changed
                        if (updatedUsername != currentUser.name) {
                            updateFirestoreUsername(user.uid, updatedUsername)
                        }
                    } else {
                        Log.e("EditProfileFragment", "Re-authentication failed", reauthTask.exception)
                        Toast.makeText(requireContext(), "Re-authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please enter your current password to update email or password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmail(user: FirebaseUser, updatedEmail: String) {
        user.updateEmail(updatedEmail).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateFirestoreEmail(user.uid, updatedEmail)
            } else {
                Log.e("EditProfileFragment", "Failed to update email", task.exception)
                Toast.makeText(requireContext(), "Failed to update email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePassword(user: FirebaseUser, updatedPassword: String) {
        user.updatePassword(updatedPassword).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
            } else {
                Log.e("EditProfileFragment", "Failed to update password", task.exception)
                Toast.makeText(requireContext(), "Failed to update password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFirestoreEmail(userId: String, updatedEmail: String) {
        firestore.collection("users").document(userId)
            .update("email", updatedEmail)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Email updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
            }
            .addOnFailureListener { exception ->
                Log.e("EditProfileFragment", "Error updating email in Firestore", exception)
                Toast.makeText(requireContext(), "Failed to update email in Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFirestoreUsername(userId: String, updatedUsername: String) {
        firestore.collection("users").document(userId)
            .update("name", updatedUsername)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
            }
            .addOnFailureListener { exception ->
                Log.e("EditProfileFragment", "Error updating username in Firestore", exception)
                Toast.makeText(requireContext(), "Failed to update username", Toast.LENGTH_SHORT).show()
            }
    }
}
