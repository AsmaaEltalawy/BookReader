package com.example.bookreader.adapter
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookreader.data.models.RecentSearches
import com.example.bookreader.databinding.RecentSearchItemBinding

class RecentSearchAdapter(
    private val searchOnClickListener: SearchOnClickListener
) : ListAdapter<RecentSearches, RecyclerView.ViewHolder>(
    SearchDiffUtil()
) {
    class MyViewHolder(val binding: RecentSearchItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {

            fun from(parent: ViewGroup): MyViewHolder {
                val inflator = LayoutInflater.from(parent.context)
                val binding = RecentSearchItemBinding.inflate(inflator, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            val search = getItem(position)
            holder.binding.searchItemTV.text = search.query

            holder.itemView.setOnClickListener {
                val adapterPosition = holder.adapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    searchOnClickListener.searchOnClick(adapterPosition, search.id)
                }
            }

            Log.e("RecentSearchAdapter bound the word: ", "Search: $search")
        }
    }

}

interface SearchOnClickListener {
    fun searchOnClick(position: Int,id: Int)
}

class SearchDiffUtil : DiffUtil.ItemCallback<RecentSearches>() {
    override fun areItemsTheSame(oldItem: RecentSearches, newItem: RecentSearches): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RecentSearches, newItem: RecentSearches): Boolean {
        return oldItem == newItem
    }

}