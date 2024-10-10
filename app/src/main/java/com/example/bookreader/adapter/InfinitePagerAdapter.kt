package com.example.bookreader.adapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookreader.R
import com.example.bookreader.data.models.LocalBook
import com.example.bookreader.databinding.PagerBookBinding


class InfinitePagerAdapter(
    private var data: List<LocalBook>,
    private val pagerOnClickListener: PagerOnClickListener
) : ListAdapter<LocalBook, RecyclerView.ViewHolder>(ItemDiffCallback()) {
    var binding: PagerBookBinding? = null

    class MyViewHolder(val binding: PagerBookBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val binding =
                    PagerBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (data.isEmpty()) {
            Log.d("InfinitePagerAdapter", "Data is empty")
            return
        }
        val actualPosition = position % data.size
        if (holder is MyViewHolder) {
            binding = holder.binding
            holder.binding.book = data[actualPosition]
            holder.binding.infoButton.setOnClickListener {
                pagerOnClickListener.pagerOnClick(position, it, holder.binding)
            }

            holder.binding.root.setOnClickListener {
                pagerOnClickListener.pagerOnClick(position, it, holder.binding)
            }
            Glide.with(holder.binding.root.context)
                .clear(holder.binding.pagerIG) // Clear any previous image before loading a new one
            Glide.with(holder.binding.root.context)
                .load(data[actualPosition].image)
                .placeholder(R.drawable.waiting) // Optional
                .error(R.drawable.error) // Optional
                .into(holder.binding.pagerIG)
        }
    }


    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }
}

interface PagerOnClickListener {
    fun pagerOnClick(position: Int, view: View?, binding: PagerBookBinding? = null)
}

class ItemDiffCallback : DiffUtil.ItemCallback<LocalBook>() {
    override fun areItemsTheSame(oldItem: LocalBook, newItem: LocalBook): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LocalBook, newItem: LocalBook): Boolean {
        return oldItem == newItem
    }
}
