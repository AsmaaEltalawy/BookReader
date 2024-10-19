package com.example.bookreader.ui.theme.views.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.bookreader.R
import com.example.bookreader.baseClass.BaseActivity
import com.example.bookreader.data.local.MySharedPreference
import com.example.bookreader.databinding.ActivityMainBinding
import com.example.bookreader.utils.Constant
import java.util.Locale

class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var mySharedPreference: MySharedPreference
    private lateinit var sharedPreference:SharedPreferences

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        mySharedPreference = MySharedPreference(this)
        sharedPreference = getSharedPreferences(Constant.REGISTER_INFO, Context.MODE_PRIVATE)
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
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView = binding.navigationview
        // Find the SwitchCompat from the navigation drawer
        val switchView = navView.menu.findItem(R.id.dark)?.actionView as? SwitchCompat

        // Set initial switch state based on saved preferences
        switchView?.isChecked = mySharedPreference.getTheme()

        // Handle switch toggle directly
        switchView?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            saveThemePreference(isChecked)
        }
        binding.navigationview.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.language -> {
                    val currentLocale = resources.configuration.locales[0].language
                    if (currentLocale == "ar") {
                        setLocale("en") // Switch to English
                    } else {
                        setLocale("ar") // Switch to Arabic
                    }
                    true
                }

                R.id.PrivacyPolicy -> {
                    val intent = Intent(this, PrivacyPolicy::class.java)
                    startActivity(intent)
                    true
                }

                R.id.contact -> {
                    contactUs()
                    true
                }
                R.id.logout -> {
                    logout()
                    true
                }

                else -> false
            }
        }
    }
/*
    private fun setThemeBasedOnPreferences() {
        if (mySharedPreference.getTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
*/

    private fun logout() {
        sharedPreference.edit().apply(){
            putBoolean(Constant.USER_IS_REGISTERED, false)
        }.commit()
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveThemePreference(isDarkMode: Boolean) {
        mySharedPreference.saveTheme(isDarkMode)
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        mySharedPreference.saveLocale(languageCode)

        // Restart activity to apply changes
        val refreshIntent = Intent(this, MainActivity::class.java)
        startActivity(refreshIntent)
        finish()
    }

    private fun contactUs() {
        val phoneNumber = "01280925493"
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }
}


