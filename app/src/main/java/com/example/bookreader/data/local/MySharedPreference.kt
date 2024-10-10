package com.example.bookreader.data.local

import android.content.Context

class MySharedPreference (context: Context) {
    private val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

    fun saveData(id: String) {
        with(prefs.edit()) {
            putString("id", id)
            apply()
        }}

    fun getId(): String? {
        return prefs.getString("id", null)
    }

}