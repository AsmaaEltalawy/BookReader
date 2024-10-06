package com.example.bookreader.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookreader.R
import com.example.bookreader.data.models.LocalBook
import com.example.bookreader.databinding.HorizontalBookBinding


class BookAdapter(
    private val bookOnClickListener: BookOnClickListener
) : ListAdapter<LocalBook, RecyclerView.ViewHolder>(
    BookDiffUtil()
) {
    class MyViewHolder(val binding: HorizontalBookBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {

            fun from(parent: ViewGroup): MyViewHolder {
                val inflator = LayoutInflater.from(parent.context)
                val binding = HorizontalBookBinding.inflate(inflator, parent, false)
                return MyViewHolder(binding)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            val book = getItem(position)
            holder.binding.root.setOnClickListener {
                bookOnClickListener.bookOnClick(position)
            }
            Glide.with(holder.binding.root.context)
                .clear(holder.binding.recentListCoverIV) // Clear any previous image before loading a new one
            Glide.with(holder.binding.root.context)
                .load(book.image)
                .placeholder(R.drawable.waiting) // Optional
                .error(R.drawable.error) // Optional
                .into(holder.binding.recentListCoverIV)
            holder.binding.book = book
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is MyViewHolder) {
            Glide.with(holder.binding.root.context).clear(holder.binding.recentListCoverIV)
        }
        super.onViewRecycled(holder)
    }
}

interface BookOnClickListener {
    fun bookOnClick(position: Int)
}

class BookDiffUtil : DiffUtil.ItemCallback<LocalBook>() {
    override fun areItemsTheSame(oldItem: LocalBook, newItem: LocalBook): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LocalBook, newItem: LocalBook): Boolean {
        return oldItem == newItem
    }

}