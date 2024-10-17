package com.example.bookreader.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bookreader.ui.theme.views.fragments.DownloadFragment
import com.example.bookreader.ui.theme.views.fragments.FavoriteFragment

class LibraryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment){

    private val fragmentList = listOf(
        FavoriteFragment(),
        DownloadFragment()
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position] as Fragment
}