package com.washapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.washapp.data.model.Order
import com.washapp.data.model.ServiceStage
import com.washapp.data.repository.OrderRepository
import com.washapp.databinding.FragmentOrderManagementBinding
import kotlinx.coroutines.launch

class OrderManagementFragment : Fragment() {

    private var _binding: FragmentOrderManagementBinding? = null
    private val binding get() = _binding!!

    private val orderRepository = OrderRepository()
    private lateinit var adapter: OrderManagementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrderManagementAdapter(onAdvanceStage = { order -> advanceStage(order) })
        binding.orderList.layoutManager = LinearLayoutManager(requireContext())
        binding.orderList.adapter = adapter

        loadOrders()
    }

    private fun advanceStage(order: Order) {
        val next = order.stage.next() ?: return

        lifecycleScope.launch {
            try {
                orderRepository.updateStage(order.orderId, next.name)
                // Update the list from the known write result rather than re-querying:
                // Firestore's write acknowledgment can arrive slightly before the local
                // query cache catches up, so an immediate re-fetch can read stale data.
                if (next == ServiceStage.COMPLETED) {
                    adapter.removeItem(order.orderId)
                } else {
                    adapter.updateItem(order.copy(stage = next))
                }
            } catch (e: Exception) {
                // TODO: surface the stage-update failure to the user (e.g. Snackbar)
            }
        }
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            try {
                adapter.submitList(orderRepository.getOrdersInProgress())
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
