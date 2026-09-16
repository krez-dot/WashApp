package com.washapp.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.washapp.data.repository.AuthRepository
import com.washapp.data.repository.OrderRepository
import com.washapp.databinding.FragmentOrderTrackingBinding
import kotlinx.coroutines.launch

class OrderTrackingFragment : Fragment() {

    private var _binding: FragmentOrderTrackingBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()
    private val orderRepository = OrderRepository()

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
        // TODO: replace this one-shot Firestore read with a Realtime Database listener
        // so the stage column updates live without a manual refresh.
        loadOrders()
    }

    private fun loadOrders() {
        val customerId = authRepository.currentUserId() ?: return
        lifecycleScope.launch {
            try {
                val orders = orderRepository.getOrdersForCustomer(customerId)
                binding.ordersSummary.text = orders.joinToString(separator = "\n\n") { order ->
                    "Order ${order.orderId.take(6)} — ${order.stage}\n" +
                        "Queue position: ${order.queuePosition}\n" +
                        "Estimated completion: ${order.estimatedCompletionMinutes} min\n" +
                        "Cost: ₱${order.cost}"
                }
            } catch (e: Exception) {
                // TODO: surface the fetch failure to the user (e.g. Snackbar)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
