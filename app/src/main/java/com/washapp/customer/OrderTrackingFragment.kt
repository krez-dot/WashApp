package com.washapp.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ValueEventListener
import com.washapp.R
import com.washapp.data.model.Order
import com.washapp.data.model.ServiceStage
import com.washapp.data.repository.AuthRepository
import com.washapp.data.repository.OrderRepository
import com.washapp.databinding.FragmentOrderTrackingBinding
import kotlinx.coroutines.launch

class OrderTrackingFragment : Fragment() {

    private var _binding: FragmentOrderTrackingBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()
    private val orderRepository = OrderRepository()
    private lateinit var adapter: OrderTrackingAdapter

    // Firestore holds the static order fields (cost, load size, ...); Realtime Database
    // pushes live stage/queuePosition updates on top of this map without a manual refresh.
    private val liveOrders = mutableMapOf<String, Order>()
    private val statusListeners = mutableMapOf<String, ValueEventListener>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrderTrackingAdapter(onCancel = { order -> confirmCancel(order) })
        binding.ordersList.layoutManager = LinearLayoutManager(requireContext())
        binding.ordersList.adapter = adapter

        loadOrders()
    }

    private fun confirmCancel(order: Order) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_cancel_order)
            .setMessage(R.string.message_cancel_order)
            .setPositiveButton(R.string.action_cancel_order) { _, _ -> cancelOrder(order) }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun cancelOrder(order: Order) {
        lifecycleScope.launch {
            try {
                orderRepository.cancelOrder(order.orderId)
                statusListeners.remove(order.orderId)?.let {
                    orderRepository.stopObserving(order.orderId, it)
                }
                liveOrders.remove(order.orderId)
                if (_binding != null) {
                    adapter.removeItem(order.orderId)
                    binding.emptyState.visibility = if (liveOrders.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't cancel the order", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadOrders() {
        val customerId = authRepository.currentUserId() ?: return
        lifecycleScope.launch {
            try {
                val orders = orderRepository.getOrdersForCustomer(customerId)
                liveOrders.clear()
                orders.forEach { liveOrders[it.orderId] = it }
                renderOrders()

                orders.forEach { order ->
                    val listener = orderRepository.observeOrderStatus(order.orderId) { stage, queuePosition ->
                        onStatusUpdate(order.orderId, stage, queuePosition)
                    }
                    statusListeners[order.orderId] = listener
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't load your orders", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun onStatusUpdate(orderId: String, stage: String, queuePosition: Int) {
        if (_binding == null) return
        val stageEnum = try {
            ServiceStage.valueOf(stage)
        } catch (e: IllegalArgumentException) {
            return
        }
        val current = liveOrders[orderId] ?: return
        val updated = current.copy(stage = stageEnum, queuePosition = queuePosition)
        liveOrders[orderId] = updated
        adapter.updateItem(updated)
    }

    private fun renderOrders() {
        if (_binding == null) return
        val orders = liveOrders.values.toList()
        adapter.submitList(orders)
        binding.emptyState.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        statusListeners.forEach { (orderId, listener) -> orderRepository.stopObserving(orderId, listener) }
        statusListeners.clear()
        liveOrders.clear()
        _binding = null
    }
}
