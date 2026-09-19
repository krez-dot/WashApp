package com.washapp.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.washapp.auth.LoginActivity
import com.washapp.data.repository.AuthRepository
import com.washapp.databinding.ActivityAdminBinding
import com.washapp.util.applyPoppinsRecursively

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.adminNavHost.id) as NavHostFragment
        binding.adminBottomNav.setupWithNavController(navHostFragment.navController)
        applyPoppinsRecursively(binding.adminBottomNav)

        binding.logoutButton.setOnClickListener {
            authRepository.logout()
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        }
    }
}
