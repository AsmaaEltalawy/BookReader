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
import com.example.bookreader.data.models.DetailsResponse
import com.example.bookreader.databinding.HomeFragmentBinding
import com.example.bookreader.databinding.PagerBookBinding
import com.example.bookreader.ui.theme.viewmodels.HomeViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import com.example.bookreader.ui.theme.views.activities.DetailsActivity


class HomeFragment : Fragment(), PagerOnClickListener, BookOnClickListener {
    private var isAutoScrollActive = true
    lateinit var handler: Handler
    private lateinit var binding: HomeFragmentBinding
    private lateinit var viewPager2: ViewPager2
    private val testData = mutableListOf<DetailsResponse>()
    private lateinit var recentBooks: List<DetailsResponse>
    private lateinit var recommendedBooks: MutableList<DetailsResponse>
    private var lastPosition: Int = 0
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        testData.add(
            DetailsResponse(
                image = "https://hips.hearstapps.com/hmg-prod/images/wisteria-in-bloom-royalty-free-image-1653423554.jpg",
                title = "five",
                description = getString(R.string.lorem_ipsum)
            )
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

        homeViewModel.getRecommendBooks()
        var pagerAdapter: InfinitePagerAdapter
        homeViewModel.booksRecommend.observe(viewLifecycleOwner) {
            recommendedBooks = it.books.toMutableList()
            lifecycleScope.launch {
                pagerAdapter = InfinitePagerAdapter(recommendedBooks, this@HomeFragment)
                viewPager2.adapter = pagerAdapter
            }
        }

        homeViewModel.getRecentBooks()
        val recentAdapter: BookAdapter = BookAdapter(this@HomeFragment)
        binding.recentListRV.adapter = recentAdapter
        binding.recentListRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        homeViewModel.booksRecent.observe(viewLifecycleOwner) {
            recentBooks = it.books
            recentAdapter.submitList(it.books)
        }

        binding.recentBook = testData[0]
        Glide.with(requireContext())
            .load(binding.recentBook?.image)
            .placeholder(R.drawable.waiting)
            .error(R.drawable.error)
            .into(binding.recentCoverIV)
        binding.recentReadBookCard.setOnClickListener {
            cardOnClick(0)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 2000)
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

    private fun cardOnClick(position: Int) {
        open(requireContext(), DetailsActivity::class.java, testData[position])
    }

    override fun pagerOnClick(position: Int, view: View?, binding: PagerBookBinding?) {
        val actualPosition = position % recommendedBooks.size
        val recyclerView = viewPager2.getChildAt(0) as? RecyclerView

        if (recyclerView != null) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(viewPager2.currentItem)
            if (viewHolder != null && viewHolder is InfinitePagerAdapter.MyViewHolder) {
                if (view != null) {
                    when (view.id) {
                        R.id.infoButton -> {
                            val book = binding?.book
                            lastPosition = actualPosition
                            if (viewHolder.binding.pagerTV.visibility == View.VISIBLE) viewHolder.binding.pagerTV.visibility =
                                View.GONE
                            open(
                                requireContext(),
                                DetailsActivity::class.java,
                                book,
                                position = lastPosition
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
        val book = recentBooks[position]
        open(requireContext(), DetailsActivity::class.java, book)
    }

    companion object {
        fun open(
            context: Context,
            destination: Class<*>,
            book: DetailsResponse? = null,
            position: Int = 0
        ) {
            val intent = Intent(context, destination)
            val bundle = Bundle()
            bundle.putSerializable("detailsResponse", book)
            bundle.let { intent.putExtras(it) }
            intent.putExtra("ITEM_POSITION", position)
            context.startActivity(intent)
        }
    }
}
