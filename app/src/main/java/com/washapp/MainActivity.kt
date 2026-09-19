package com.washapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.washapp.admin.AdminActivity
import com.washapp.auth.LoginActivity
import com.washapp.customer.CustomerActivity
import com.washapp.data.model.Role
import com.washapp.data.repository.AuthRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        lifecycleScope.launch {
            val destination = try {
                if (authRepository.currentRole() == Role.ADMINISTRATOR) {
                    AdminActivity::class.java
                } else {
                    CustomerActivity::class.java
                }
            } catch (e: Exception) {
                // Firestore rules/network can fail this read; fall back to login rather
                // than crashing the app on an otherwise-valid signed-in session.
                LoginActivity::class.java
            }
            startActivity(Intent(this@MainActivity, destination))
            finish()
        }
    }
}
