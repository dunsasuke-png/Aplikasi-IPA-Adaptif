package com.app.manfaattumbuhan.presentation.guru

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.databinding.ActivityGuruBinding

class GuruActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuruBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuruBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_guru) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavGuru.setupWithNavController(navController)
    }
}
