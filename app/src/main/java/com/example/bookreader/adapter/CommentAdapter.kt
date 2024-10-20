package com.example.bookreader.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bookreader.data.models.Comment
import com.example.bookreader.databinding.CommentItemBinding
import com.example.bookreader.ui.theme.views.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentsAdapter(
    private val comments: MutableList<Comment>,
    private val bookId: String,
    private val context: Context
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(val binding: CommentItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.binding.name.text = comment.username
        holder.binding.comment.text = comment.comment
        holder.binding.date.text = formatDate(comment.timestamp)

        // Get the current user's UID
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Only allow deletion if the current user is the author of the comment
        if (comment.uid == currentUserUid) {
            holder.binding.root.setOnLongClickListener {
                showDeleteConfirmationDialog(position, comment)
                true
            }
        } else {
            // Disable the delete option for non-authors
            holder.binding.root.setOnLongClickListener {
                Toast.makeText(context, "You can only delete your own comments", Toast.LENGTH_SHORT).show()
                true
            }
        }
        holder.binding.profileImg.setOnClickListener {

            if (comment.uid == currentUserUid) {
                // Navigate to ProfileFragment
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra("navigateTo", "ProfileFragment")
                }
                context.startActivity(intent)
            } else {
                // Navigate to UserProfileFragment
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra("navigateTo", "UserProfileFragment")
                    putExtra("userId", comment.uid)  // Pass the user's ID for the UserProfileFragment
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = comments.size

    private fun formatDate(timestamp: String): String {
        val date = Date(timestamp.toLong())
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }

    // Show a confirmation dialog
    private fun showDeleteConfirmationDialog(position: Int, comment: Comment) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Comment")
        builder.setMessage("Are you sure you want to delete this comment?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            deleteCommentFromFirebase(comment, position)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    // Delete the comment from Firebase and the app
    private fun deleteCommentFromFirebase(comment: Comment, position: Int) {
        val firestore = FirebaseFirestore.getInstance()
        val commentRef = firestore.collection("books").document(bookId)
            .collection("comments").document(comment.id)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Attempt to delete the comment
                commentRef.delete().await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show()
                    // Remove the comment from the list and update the UI
                    comments.removeAt(position)
                    notifyItemRemoved(position)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Display a message and log any errors
                    Toast.makeText(context, "Failed to delete comment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.e("CommentsAdapter", "Error deleting comment: ${e.message}")
            }
        }
    }
}