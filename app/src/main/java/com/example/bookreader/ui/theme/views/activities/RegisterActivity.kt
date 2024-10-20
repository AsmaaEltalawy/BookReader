package com.example.bookreader.ui.theme.views.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.bookreader.R
import com.example.bookreader.baseClass.BaseActivity
import com.example.bookreader.databinding.ActivityRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        val fragHost =
            supportFragmentManager.findFragmentById(R.id.register_containerView) as NavHostFragment
        navController = fragHost.navController
    }
}