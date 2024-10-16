package com.example.bookreader.ui.theme.views.activities

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.bookreader.R
import com.example.bookreader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val fragHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = fragHost.navController
        setupWithNavController(binding.BottomNavBar, navController)

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

        val navView = binding.navView
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.Darkmode -> {
                    // TO DO
                }

                R.id.language -> {
                    // TO DO
                }

                R.id.contact -> {
                    // TO DO
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
}

