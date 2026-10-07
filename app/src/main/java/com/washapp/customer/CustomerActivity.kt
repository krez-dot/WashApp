package com.washapp.customer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.washapp.auth.LoginActivity
import com.washapp.data.repository.AuthRepository
import com.washapp.data.repository.OrderRepository
import com.washapp.databinding.ActivityCustomerBinding
import com.washapp.notifications.OrderStatusNotifier
import com.washapp.util.applyPoppinsRecursively
import com.washapp.util.showAccountDialog
import kotlinx.coroutines.launch

class CustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerBinding
    private val authRepository = AuthRepository()
    private val orderRepository = OrderRepository()
    private lateinit var statusNotifier: OrderStatusNotifier

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

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

        statusNotifier = OrderStatusNotifier(this, orderRepository)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        startStatusNotifier()
    }

    /**
     * Re-attaches status listeners so an order submitted during this session starts
     * raising alerts without waiting for the activity to be recreated.
     */
    fun refreshStatusNotifier() = startStatusNotifier()

    private fun startStatusNotifier() {
        val customerId = authRepository.currentUserId() ?: return
        lifecycleScope.launch {
            try {
                statusNotifier.start(customerId)
            } catch (e: Exception) {
                // Non-critical: the tracking screen still shows live status without alerts.
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        statusNotifier.stop()
    }
}
