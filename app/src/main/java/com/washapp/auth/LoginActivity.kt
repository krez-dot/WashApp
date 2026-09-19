package com.washapp.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.washapp.R
import com.washapp.admin.AdminActivity
import com.washapp.customer.CustomerActivity
import com.washapp.data.model.Role
import com.washapp.data.repository.AuthRepository
import com.washapp.databinding.ActivityLoginBinding
import com.washapp.databinding.DialogResetPasswordBinding
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
        binding.forgotPassword.setOnClickListener { showResetPasswordDialog() }
    }

    private fun showResetPasswordDialog() {
        val dialogBinding = DialogResetPasswordBinding.inflate(layoutInflater)
        dialogBinding.resetEmailInput.setText(binding.emailInput.text)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.title_reset_password)
            .setMessage(R.string.message_reset_password)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_send) { _, _ ->
                val email = dialogBinding.resetEmailInput.text.toString()
                lifecycleScope.launch {
                    try {
                        authRepository.sendPasswordResetEmail(email)
                        Snackbar.make(binding.root, R.string.message_reset_email_sent, Snackbar.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Snackbar.make(binding.root, e.message ?: "Couldn't send reset email", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
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
                Snackbar.make(binding.root, e.message ?: "Login failed", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
