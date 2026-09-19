package com.washapp.customer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.washapp.auth.LoginActivity
import com.washapp.data.repository.AuthRepository
import com.washapp.databinding.ActivityCustomerBinding
import com.washapp.util.applyPoppinsRecursively
import com.washapp.util.showAccountDialog

class CustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.customerNavHost.id) as NavHostFragment
        binding.customerBottomNav.setupWithNavController(navHostFragment.navController)
        applyPoppinsRecursively(binding.customerBottomNav)

        binding.logoutButton.setOnClickListener {
            authRepository.logout()
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        }
        binding.accountButton.setOnClickListener {
            showAccountDialog(this, binding.root, lifecycleScope, authRepository)
        }
    }
}
