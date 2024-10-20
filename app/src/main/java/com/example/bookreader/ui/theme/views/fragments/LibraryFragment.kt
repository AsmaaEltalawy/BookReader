package com.example.bookreader.ui.theme.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.example.bookreader.R
import com.example.bookreader.adapter.LibraryPagerAdapter
import com.example.bookreader.ui.theme.views.activities.MainActivity
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.bookreader.databinding.ActivityMainBinding


class LibraryFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var mainActivityBinding: ActivityMainBinding
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarParams: AppBarLayout.LayoutParams
    private var defaultScrollFlags: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        mainActivityBinding = (requireActivity() as MainActivity).binding
        viewPager = view.findViewById(R.id.viewpager)
        tabLayout = view.findViewById(R.id.tablayout)
        appBarLayout = mainActivityBinding.appBarLayout
        toolbar = mainActivityBinding.materialToolbar
        toolbarParams = toolbar.layoutParams as AppBarLayout.LayoutParams

        defaultScrollFlags = toolbarParams.scrollFlags

        val adapter = LibraryPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.favorites)
                1 -> getString(R.string.Downloads)
                else -> null
            }
        }.attach()

        return view
    }

    override fun onResume() {
        super.onResume()
        val appBarLayout = requireActivity().findViewById<AppBarLayout>(R.id.appBarLayout)
        appBarLayout.setExpanded(false, false)
       // appBarLayout.setBackgroundColor(resources.getColor(R.color.primary_color))
       // toolbar.setBackgroundColor(resources.getColor(R.color.primary_color))
    }

    override fun onPause() {
        super.onPause()
       // appBarLayout.setBackgroundColor(resources.getColor(R.color.theme))
       // toolbar.setBackgroundColor(resources.getColor(R.color.theme))
    }

}