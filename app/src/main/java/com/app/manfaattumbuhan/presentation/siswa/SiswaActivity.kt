package com.app.manfaattumbuhan.presentation.siswa

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.databinding.ActivitySiswaBinding

class SiswaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySiswaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySiswaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_siswa) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavSiswa.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.latihanFragment,
                R.id.pilihLevelFragment -> {
                    binding.bottomNavSiswa.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavSiswa.visibility = View.VISIBLE
                }
            }
        }
    }
}
