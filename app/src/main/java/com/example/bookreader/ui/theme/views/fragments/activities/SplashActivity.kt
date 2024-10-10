package com.example.bookreader.ui.theme.views.fragments.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookreader.R
import com.example.bookreader.databinding.ActivityRegisterBinding
import com.example.bookreader.databinding.ActivitySplashBinding
import com.example.bookreader.ui.theme.mvi.splashmvi.SplashIntent
import com.example.bookreader.ui.theme.mvi.splashmvi.SplashState
import com.example.bookreader.ui.theme.viewmodels.SplashViewModel
import com.example.bookreader.ui.theme.views.activities.MainActivity
import com.example.bookreader.utils.Constant
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        lifecycleScope.launch {
            delay(2500)
            handelState()
            checkUserSigned()
        }
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        binding.splashLogo.startAnimation(rotateAnimation)
    }

    private fun checkUserSigned() {
        val sharedPreferences = getSharedPreferences(Constant.REGISTER_INFO, Context.MODE_PRIVATE)
        val isLogin = sharedPreferences.getBoolean(Constant.USER_IS_REGISTERED, false)
        if (isLogin) {
            lifecycleScope.launch {
                viewModel.channel.send(SplashIntent.CheckUserIsSigned)
            }
        } else {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun handelState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is SplashState.Error -> handelError(state.message)
                    SplashState.Idle -> {}
                    is SplashState.UserIsSigned -> handelUserSigned(state.isFound)
                }
            }
        }
    }

    private fun handelUserSigned(found: Boolean) {
        if (found) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun handelError(message: String) {
        Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG)
            .show()
    }
}