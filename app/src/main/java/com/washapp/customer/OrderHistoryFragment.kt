package com.washapp.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.washapp.R
import com.washapp.data.model.ServiceStage
import com.washapp.data.repository.AuthRepository
import com.washapp.data.repository.OrderRepository
import com.washapp.databinding.FragmentOrderTrackingBinding
import kotlinx.coroutines.launch

class OrderHistoryFragment : Fragment() {

    private var _binding: FragmentOrderTrackingBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()
    private val orderRepository = OrderRepository()
    private lateinit var adapter: OrderTrackingAdapter

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

        binding.emptyState.text = getString(R.string.placeholder_no_history)
        adapter = OrderTrackingAdapter(onCancel = {})
        binding.ordersList.layoutManager = LinearLayoutManager(requireContext())
        binding.ordersList.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        val customerId = authRepository.currentUserId() ?: return
        lifecycleScope.launch {
            try {
                val completed = orderRepository.getOrdersForCustomer(customerId)
                    .filter { it.stage == ServiceStage.COMPLETED }
                    .sortedByDescending { it.createdAt }
                adapter.submitList(completed)
                binding.emptyState.visibility = if (completed.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't load order history", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
