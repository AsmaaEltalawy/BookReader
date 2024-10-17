package com.example.bookreader.ui.theme.views.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.example.bookreader.R
import com.example.bookreader.baseClass.BaseActivity

class PrivacyPolicy : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_privacy_policy)

    }
}