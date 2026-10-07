package com.washapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.firebase.FirebaseApp

class WashApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        createOrderStatusChannel()
    }

    private fun createOrderStatusChannel() {
        val channel = NotificationChannel(
            ORDER_STATUS_CHANNEL_ID,
            getString(R.string.channel_order_status_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.channel_order_status_description)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val ORDER_STATUS_CHANNEL_ID = "order_status"
    }
}
