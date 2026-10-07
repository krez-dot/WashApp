package com.washapp.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.ValueEventListener
import com.washapp.R
import com.washapp.WashApp
import com.washapp.customer.CustomerActivity
import com.washapp.data.model.ServiceStage
import com.washapp.data.repository.OrderRepository

/**
 * Raises a notification whenever one of the customer's orders changes service stage.
 *
 * Realtime Database replays the current value as soon as a listener attaches, so the first
 * callback per order is recorded as a baseline rather than announced — otherwise every order
 * would notify again on each app launch.
 */
class OrderStatusNotifier(
    private val context: Context,
    private val orderRepository: OrderRepository
) {
    private val listeners = mutableMapOf<String, ValueEventListener>()
    private val lastStage = mutableMapOf<String, String>()

    suspend fun start(customerId: String) {
        stop()
        val active = orderRepository.getOrdersForCustomer(customerId)
            .filter { it.stage != ServiceStage.COMPLETED }
        active.forEach { order ->
            val listener = orderRepository.observeOrderStatus(order.orderId) { stage, _ ->
                onStageUpdate(order.orderId, stage)
            }
            listeners[order.orderId] = listener
        }
    }

    fun stop() {
        listeners.forEach { (orderId, listener) -> orderRepository.stopObserving(orderId, listener) }
        listeners.clear()
        lastStage.clear()
    }

    private fun onStageUpdate(orderId: String, stage: String) {
        val previous = lastStage.put(orderId, stage)
        if (previous == null || previous == stage) return
        notifyStageChange(orderId, stage)
    }

    private fun notifyStageChange(orderId: String, stage: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val body = when (stage) {
            ServiceStage.WASHING.name -> context.getString(R.string.notification_stage_washing)
            ServiceStage.DRYING.name -> context.getString(R.string.notification_stage_drying)
            ServiceStage.COMPLETED.name -> context.getString(R.string.notification_stage_completed)
            else -> context.getString(R.string.notification_stage_queued)
        }

        val intent = Intent(context, CustomerActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, WashApp.ORDER_STATUS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title, orderId.take(6)))
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(orderId.hashCode(), notification)
    }
}
