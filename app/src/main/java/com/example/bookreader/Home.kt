package com.example.bookreader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.bookreader.databinding.ActivityHomeBinding
import com.example.bookreader.ui.theme.views.activities.MainActivity

class Home : ComponentActivity() {
    lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {

     super.onCreate(savedInstanceState)
binding= DataBindingUtil.setContentView(this,R.layout.activity_home)
        binding.enterBT.setOnClickListener{
        val  intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        }
    }
