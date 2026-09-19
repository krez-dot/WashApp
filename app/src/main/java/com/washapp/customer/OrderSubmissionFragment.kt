package com.washapp.customer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.washapp.R
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

        binding.loadSizeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = updatePricePreview()
        })
        binding.coloredCheckbox.setOnCheckedChangeListener { _, _ -> updatePricePreview() }
    }

    private fun updatePricePreview() {
        val loadSize = binding.loadSizeInput.text.toString().toDoubleOrNull()
        if (loadSize == null || loadSize <= 0) {
            binding.pricePreviewLabel.visibility = View.GONE
            return
        }
        val serviceType = currentServiceType()
        lifecycleScope.launch {
            try {
                val pricing = pricingRepository.getPricing(serviceType)
                binding.pricePreviewLabel.text =
                    getString(R.string.format_estimated_cost, pricing.pricePerKg * loadSize)
                binding.pricePreviewLabel.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.pricePreviewLabel.visibility = View.GONE
            }
        }
    }

    private fun currentServiceType(): ServiceType =
        if (binding.coloredCheckbox.isChecked) ServiceType.COLORED else ServiceType.NON_COLORED

    private fun submitOrder() {
        val customerId = authRepository.currentUserId() ?: return
        val loadSize = binding.loadSizeInput.text.toString().toDoubleOrNull()
        if (loadSize == null || loadSize <= 0) {
            Snackbar.make(binding.root, "Enter a load size greater than 0", Snackbar.LENGTH_SHORT).show()
            return
        }
        val serviceType = currentServiceType()

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
                binding.loadSizeInput.text?.clear()
                binding.coloredCheckbox.isChecked = false
                binding.pricePreviewLabel.visibility = View.GONE
                showConfirmation(order)
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Order submission failed", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showConfirmation(order: Order) {
        if (_binding == null) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_order_confirmed)
            .setMessage(
                getString(
                    R.string.message_order_confirmed,
                    order.queuePosition,
                    order.estimatedCompletionMinutes,
                    order.cost
                )
            )
            .setPositiveButton(R.string.action_ok, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
