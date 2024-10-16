package com.example.bookreader.baseClass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.bookreader.data.local.MySharedPreference
import java.util.Locale

abstract class BaseActivity : AppCompatActivity() {
    private lateinit var mySharedPreference: MySharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mySharedPreference = MySharedPreference(this)

        // Set theme based on preferences
        setAppTheme()
        setAppLocale()
    }

    private fun setAppTheme() {
        val isDarkMode = mySharedPreference.getTheme()
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun setAppLocale() {
        val locale = Locale(mySharedPreference.getLocale())
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onResume() {
        super.onResume()

        // Check for theme changes
        val isDarkModeCurrentlyActive = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        val isDarkModeChanged = mySharedPreference.getTheme() != isDarkModeCurrentlyActive

        // Check for locale changes
        val currentLocale = Locale.getDefault().language
        val localeChanged = currentLocale != mySharedPreference.getLocale()

        // Only recreate if there's an actual change
        if (isDarkModeChanged || localeChanged) {
            recreate()
        }
    }

}
