package com.washapp.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.washapp.data.model.Order
import com.washapp.data.model.ServiceType
import com.washapp.data.repository.AuthRepository
import com.washapp.data.repository.OrderRepository
import com.washapp.data.repository.PricingRepository
import com.washapp.data.scheduling.QueueOptimizer
import com.washapp.data.scheduling.TimeEstimator
import com.washapp.databinding.FragmentOrderSubmissionBinding
import kotlinx.coroutines.launch

class OrderSubmissionFragment : Fragment() {

    private var _binding: FragmentOrderSubmissionBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()
    private val orderRepository = OrderRepository()
    private val pricingRepository = PricingRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderSubmissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.submitOrderButton.setOnClickListener { submitOrder() }
    }

    private fun submitOrder() {
        val customerId = authRepository.currentUserId() ?: return
        val loadSize = binding.loadSizeInput.text.toString().toDoubleOrNull() ?: return
        val serviceType = if (binding.coloredCheckbox.isChecked) {
            ServiceType.COLORED
        } else {
            ServiceType.NON_COLORED
        }

        lifecycleScope.launch {
            try {
                val pricing = pricingRepository.getPricing(serviceType)
                val currentQueue = orderRepository.getActiveQueue()
                val queuePosition = QueueOptimizer.assignQueuePosition(currentQueue, Order())

                var order = Order(
                    customerId = customerId,
                    serviceType = serviceType,
                    loadSizeKg = loadSize,
                    cost = pricing.pricePerKg * loadSize,
                    queuePosition = queuePosition
                )
                order = TimeEstimator.estimate(order, pricing, queuePosition, averageStageMinutes = 15)
                orderRepository.submitOrder(order)
            } catch (e: Exception) {
                // TODO: surface the submission failure to the user (e.g. Snackbar)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
