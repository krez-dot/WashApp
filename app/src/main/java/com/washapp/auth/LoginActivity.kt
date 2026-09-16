package com.washapp.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.washapp.admin.AdminActivity
import com.washapp.customer.CustomerActivity
import com.washapp.data.model.Role
import com.washapp.data.repository.AuthRepository
import com.washapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener { login() }
        binding.goToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()

        lifecycleScope.launch {
            try {
                val user = authRepository.login(email, password)
                val destination = if (user.role == Role.ADMINISTRATOR) {
                    AdminActivity::class.java
                } else {
                    CustomerActivity::class.java
                }
                startActivity(Intent(this@LoginActivity, destination))
                finish()
            } catch (e: Exception) {
                // TODO: surface authentication failure to the user (e.g. Snackbar)
            }
        }
    }
}
