package com.example.bookreader.ui.theme.views.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.bookreader.R
import com.example.bookreader.databinding.ActivityMainBinding
import com.example.bookreader.ui.theme.views.fragments.HomeFragment
import com.example.bookreader.ui.theme.views.fragments.LibraryFragment
import com.google.android.material.appbar.AppBarLayout

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarLayout: AppBarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        appBarLayout = findViewById(R.id.appBarLayout)
        appBarLayout.setExpanded(true, false)

        val fragHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = fragHost.navController
        NavigationUI.setupWithNavController(binding.BottomNavBar, navController)

        val toolbar = binding.materialToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = null

        val drawerLayout = binding.drawerLayout

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set up navigation item selection
        val navView = binding.navView
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.Darkmode -> {
                    // Handle dark mode
                }
                R.id.language -> {
                    // Handle language selection
                }
                R.id.contact -> {
                    // Handle contact
                }
                R.id.logout -> {
                    logout()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        } 
    }

    private fun logout() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}
