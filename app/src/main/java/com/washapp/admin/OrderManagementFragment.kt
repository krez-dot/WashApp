package com.washapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.washapp.data.repository.OrderRepository
import com.washapp.databinding.FragmentOrderManagementBinding
import kotlinx.coroutines.launch

class OrderManagementFragment : Fragment() {

    private var _binding: FragmentOrderManagementBinding? = null
    private val binding get() = _binding!!

    private val orderRepository = OrderRepository()

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
        // TODO: replace with a RecyclerView bound to a Realtime Database listener, with
        // per-row controls to advance an order's stage (Washing -> Drying -> Completed).
        lifecycleScope.launch {
            try {
                val queue = orderRepository.getActiveQueue()
                binding.orderListSummary.text = queue.joinToString(separator = "\n") { order ->
                    "#${order.queuePosition} · ${order.orderId.take(6)} · ${order.stage}"
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
