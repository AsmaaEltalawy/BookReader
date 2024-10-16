package com.example.bookreader.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookreader.R
import com.example.bookreader.data.models.LocalBook
import com.example.bookreader.databinding.BookItemBinding

class LibraryAdapter(
    private val bookOnClickListener: BookOnClickListener
) : ListAdapter<LocalBook, LibraryAdapter.FavoriteViewHolder>(
    BookDiffUtil()
) {
    class FavoriteViewHolder(val binding: BookItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): FavoriteViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = BookItemBinding.inflate(inflater, parent, false)
                return FavoriteViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        return FavoriteViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val book = getItem(position)
        holder.binding.book = book

        holder.binding.root.setOnClickListener {
            bookOnClickListener.bookOnClick(position)
        }

        // Load the book cover image
        Glide.with(holder.binding.root.context)
            .load(book.image)
            .placeholder(R.drawable.waiting) // Optional
            .error(R.drawable.error) // Optional
            .into(holder.binding.bookImage) // Bind to your ImageView

        // Set book title (optional since it's already bound)
        holder.binding.bookTitle.text = book.title
    }

    override fun onViewRecycled(holder: FavoriteViewHolder) {
        Glide.with(holder.binding.root.context).clear(holder.binding.bookImage)
        super.onViewRecycled(holder)
    }
}


