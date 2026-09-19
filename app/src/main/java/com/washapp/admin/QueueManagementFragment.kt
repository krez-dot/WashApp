package com.washapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.washapp.data.model.Order
import com.washapp.data.repository.OrderRepository
import com.washapp.databinding.FragmentQueueManagementBinding
import kotlinx.coroutines.launch

class QueueManagementFragment : Fragment() {

    private var _binding: FragmentQueueManagementBinding? = null
    private val binding get() = _binding!!

    private val orderRepository = OrderRepository()
    private lateinit var adapter: QueueAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQueueManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = QueueAdapter(
            onMoveUp = { order, aboveOrder -> swapPositions(order, aboveOrder) },
            onMoveDown = { order, belowOrder -> swapPositions(order, belowOrder) }
        )
        binding.queueList.layoutManager = LinearLayoutManager(requireContext())
        binding.queueList.adapter = adapter

        loadQueue()
    }

    private fun swapPositions(order: Order, otherOrder: Order) {
        lifecycleScope.launch {
            try {
                orderRepository.swapQueuePositions(
                    order.orderId, order.queuePosition,
                    otherOrder.orderId, otherOrder.queuePosition
                )
                // Update the list from the known write result rather than re-querying:
                // Firestore's write acknowledgment can arrive slightly before the local
                // query cache catches up, so an immediate re-fetch can read stale data.
                adapter.swapPositions(
                    order.orderId, otherOrder.queuePosition,
                    otherOrder.orderId, order.queuePosition
                )
            } catch (e: Exception) {
                // TODO: surface the reorder failure to the user (e.g. Snackbar)
            }
        }
    }

    private fun loadQueue() {
        lifecycleScope.launch {
            try {
                adapter.submitList(orderRepository.getActiveQueue())
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
