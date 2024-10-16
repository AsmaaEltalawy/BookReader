package com.example.bookreader.baseClass

import android.app.DownloadManager
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.bookreader.bookreaderapp.Application
import com.example.bookreader.data.local.MySharedPreference
import com.example.bookreader.onDownloadCompleteListener.OnDownloadCompleteListener
import java.util.Locale

abstract class BaseActivity : AppCompatActivity() {
    private lateinit var mySharedPreference: MySharedPreference
    private lateinit var receiver : OnDownloadCompleteListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mySharedPreference = MySharedPreference(this)
        receiver = OnDownloadCompleteListener(app = this.applicationContext as Application)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                RECEIVER_EXPORTED
            )
            Log.d("BaseActivity", "Receiver registered")
        } else {
            registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
            Log.d("BaseActivity", "Receiver registered")
        }
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

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
        Log.d("BaseActivity", "Receiver unregistered")
    }

}
