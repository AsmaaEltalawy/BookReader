package com.example.bookreader.ui.theme.views.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.bookreader.R
import com.example.bookreader.adapter.BookAdapter
import com.example.bookreader.adapter.BookOnClickListener
import com.example.bookreader.adapter.InfinitePagerAdapter
import com.example.bookreader.adapter.PagerOnClickListener
import com.example.bookreader.data.models.LocalBook
import com.example.bookreader.data.models.SharedData
import com.example.bookreader.databinding.FragmentHomeBinding
import com.example.bookreader.databinding.PagerBookBinding
import com.example.bookreader.ui.theme.viewmodels.HomeViewModel
import com.example.bookreader.ui.theme.views.fragments.activities.DetailsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs


class HomeFragment : Fragment(), PagerOnClickListener, BookOnClickListener {

    private var isAutoScrollActive = true
    lateinit var handler: Handler
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewPager2: ViewPager2
    private val homeViewModel: HomeViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //https://hips.hearstapps.com/hmg-prod/images/wisteria-in-bloom-royalty-free-image-1653423554.jpg
        //getString(R.string.lorem_ipsum)
        binding.recentBook = LocalBook(
            id = "",
            image = "",
            title = "",
            description = ""
        )

        viewPager2 = binding.viewpager2
        handler = Handler(Looper.myLooper()!!)
        viewPager2.offscreenPageLimit = 3
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = true
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        setUpTransform()

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 2000)
            }
        })

        binding.recommendProgressBar.visibility = View.VISIBLE
        homeViewModel.getRecommendBooks()
        var pagerAdapter: InfinitePagerAdapter
        homeViewModel.booksRecommend.observe(viewLifecycleOwner) {
            SharedData.RecommendedBooks = it.books.toMutableList()
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    binding.recommendProgressBar.visibility = View.GONE
                }
                pagerAdapter = InfinitePagerAdapter(it.books, this@HomeFragment)
                viewPager2.adapter = pagerAdapter
            }
        }
        binding.recentProgressBar.visibility = View.VISIBLE





        homeViewModel.getRecentBooks()
        val recentAdapter = BookAdapter(this@HomeFragment)
        binding.recentListRV.adapter = recentAdapter
        binding.recentListRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        homeViewModel.booksRecent.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    binding.recentProgressBar.visibility = View.GONE
                }
            }
            SharedData.RecentBooks = it.books.toMutableList()
            recentAdapter.submitList(it.books)
        }

        homeViewModel.lastRead.observe(viewLifecycleOwner) {
            if (it != null) {
                SharedData.lastReadBook = it
                binding.recentBook = it
                binding.recentReadBookCard.visibility = View.VISIBLE
                binding.recentReadBookCardERROR.visibility = View.INVISIBLE
                Glide.with(requireContext())
                    .load(binding.recentBook?.image)
                    .placeholder(R.drawable.waiting)
                    .error(R.drawable.error)
                    .into(binding.recentCoverIV)
                binding.recentReadBookCard.setOnClickListener {
                    cardOnClick()
                }
            } else {
                binding.recentReadBookCard.visibility = View.INVISIBLE
                binding.recentReadBookCardERROR.visibility = View.VISIBLE
            }
        }
    }


    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 2000)
        homeViewModel.getLastRead()
    }

    private val runnable = Runnable { viewPager2.currentItem += 1 }

    private fun toggleAutoScroll() {
        isAutoScrollActive = !isAutoScrollActive
        if (isAutoScrollActive) {
            handler.postDelayed(runnable, 2000)
        } else {
            handler.removeCallbacks(runnable)
        }
    }

    private fun setUpTransform() {
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        viewPager2.setPageTransformer(transformer)
    }

    private fun cardOnClick() {
        open(requireContext(), DetailsActivity::class.java, 0, type = 0)
    }

    override fun pagerOnClick(position: Int, view: View?, binding: PagerBookBinding?) {
        val actualPosition = position % SharedData.RecommendedBooks.size
        val recyclerView = viewPager2.getChildAt(0) as? RecyclerView

        if (recyclerView != null) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(viewPager2.currentItem)
            if (viewHolder != null && viewHolder is InfinitePagerAdapter.MyViewHolder) {
                if (view != null) {
                    when (view.id) {
                        R.id.infoButton -> {
                            if (viewHolder.binding.pagerTV.visibility == View.VISIBLE) viewHolder.binding.pagerTV.visibility =
                                View.GONE
                            open(
                                requireContext(),
                                DetailsActivity::class.java,
                                position = actualPosition,
                                type = 2
                            )
                        }

                        else -> {
                            val textView = viewHolder.binding.pagerTV
                            textView.visibility =
                                if (textView.visibility == View.GONE) View.VISIBLE else View.GONE
                            toggleAutoScroll()
                        }
                    }
                }
            } else {
                Log.e("There", "ViewHolder is null or not an instance of MyViewHolder")
            }
        } else {
            Log.e("There", "RecyclerView is null")
        }
    }

    override fun bookOnClick(position: Int) {
        open(requireContext(), DetailsActivity::class.java, position = position, type = 1)
    }

    companion object {
        // 0 for last read ,  1 for recent, 2 for recommended, 3 for search results
        fun open(
            context: Context,
            destination: Class<*>,
            position: Int,
            type: Int
        ) {
            val intent = Intent(context, destination)
            intent.putExtra("ITEM_POSITION", position)
            intent.putExtra("ITEM_TYPE", type)
            context.startActivity(intent)
        }
    }
}
