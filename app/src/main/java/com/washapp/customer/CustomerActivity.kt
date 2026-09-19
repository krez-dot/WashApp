package com.washapp.customer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.washapp.databinding.ActivityCustomerBinding
import com.washapp.util.applyPoppinsRecursively

class CustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.customerNavHost.id) as NavHostFragment
        binding.customerBottomNav.setupWithNavController(navHostFragment.navController)
        applyPoppinsRecursively(binding.customerBottomNav)
    }
}
