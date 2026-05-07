package com.app.manfaattumbuhan.presentation.guru

import android.os.Bundle
import android.view.View
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

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.profilGuruFragment,
                R.id.kelolaSoalFragment,
                R.id.kelolaMateriFragment -> {
                    binding.bottomNavGuru.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavGuru.visibility = View.VISIBLE
                }
            }
        }
    }
}
