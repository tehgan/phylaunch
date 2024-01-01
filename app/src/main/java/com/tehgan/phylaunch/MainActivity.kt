package com.tehgan.phylaunch

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tehgan.phylaunch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide NavBar; mostly unnecessary, but it's very prominent (and ugly) on KYF31
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

}