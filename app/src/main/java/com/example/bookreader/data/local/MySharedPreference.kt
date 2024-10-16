package com.example.bookreader.data.local

import android.content.Context

class MySharedPreference (context: Context) {
    private val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    private val themePrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    private val localPrefs = context.getSharedPreferences("local_prefs", Context.MODE_PRIVATE)

    fun saveData(id: String) {
        with(prefs.edit()) {
            putString("id", id)
            apply()
        }}

    fun getId(): String? {
        return prefs.getString("id", null)
    }

    fun saveTheme(isDarkMode: Boolean) {
        themePrefs.edit().putBoolean("isDarkMode", isDarkMode).apply()
    }

    fun getTheme(): Boolean {
        return themePrefs.getBoolean("isDarkMode", false)
    }

    fun saveLocale(languageCode: String) {
        localPrefs.edit().putString("languageCode", languageCode).apply()
    }

    fun getLocale(): String {
        return if (localPrefs.getString("languageCode", "en") == null){
            "en"
        } else
            localPrefs.getString("languageCode", "en")!!
    }

}