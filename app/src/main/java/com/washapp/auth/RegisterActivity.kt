package com.washapp.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.washapp.customer.CustomerActivity
import com.washapp.data.model.Role
import com.washapp.data.repository.AuthRepository
import com.washapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener { register() }
    }

    private fun register() {
        val name = binding.nameInput.text.toString()
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()

        lifecycleScope.launch {
            try {
                authRepository.register(name, email, password, Role.CUSTOMER)
                startActivity(Intent(this@RegisterActivity, CustomerActivity::class.java))
                finish()
            } catch (e: Exception) {
                // TODO: surface registration failure to the user (e.g. Snackbar)
            }
        }
    }
}
