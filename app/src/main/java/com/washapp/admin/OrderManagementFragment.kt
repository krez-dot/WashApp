package com.washapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.washapp.data.model.Order
import com.washapp.data.model.ServiceStage
import com.washapp.data.repository.MachineRepository
import com.washapp.data.repository.OrderRepository
import com.washapp.data.scheduling.MachineScheduler
import com.washapp.databinding.FragmentOrderManagementBinding
import kotlinx.coroutines.launch

class OrderManagementFragment : Fragment() {

    private var _binding: FragmentOrderManagementBinding? = null
    private val binding get() = _binding!!

    private val orderRepository = OrderRepository()
    private val machineRepository = MachineRepository()
    private lateinit var adapter: OrderManagementAdapter
    private val machineLabels = mutableMapOf<String, String>()

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
        loadMachineLabels()
    }

    private fun advanceStage(order: Order) {
        val next = order.stage.next() ?: return

        lifecycleScope.launch {
            try {
                when (next) {
                    ServiceStage.WASHING -> startWashing(order, next)
                    ServiceStage.COMPLETED -> completeOrder(order, next)
                    else -> {
                        orderRepository.updateStage(order.orderId, next.name, order.queuePosition)
                        adapter.updateItem(order.copy(stage = next))
                    }
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't update the order", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun startWashing(order: Order, next: ServiceStage) {
        val machine = MachineScheduler.findAvailableMachine(machineRepository.getAvailableMachines())
        if (machine == null) {
            Snackbar.make(binding.root, "No machines available", Snackbar.LENGTH_LONG).show()
            return
        }
        machineRepository.assignMachine(machine.machineId, order.orderId)
        orderRepository.updateStage(order.orderId, next.name, order.queuePosition, machine.machineId)
        // Update the list from the known write result rather than re-querying:
        // Firestore's write acknowledgment can arrive slightly before the local
        // query cache catches up, so an immediate re-fetch can read stale data.
        machineLabels[machine.machineId] = machine.label
        adapter.setMachineLabels(machineLabels)
        adapter.updateItem(order.copy(stage = next, assignedMachineId = machine.machineId))
    }

    private suspend fun completeOrder(order: Order, next: ServiceStage) {
        if (order.assignedMachineId.isNotEmpty()) {
            machineRepository.releaseMachine(order.assignedMachineId)
        }
        orderRepository.updateStage(order.orderId, next.name, order.queuePosition)
        adapter.removeItem(order.orderId)
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            try {
                adapter.submitList(orderRepository.getOrdersInProgress())
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't load orders", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadMachineLabels() {
        lifecycleScope.launch {
            try {
                val machines = machineRepository.getAllMachines()
                machineLabels.clear()
                machines.forEach { machineLabels[it.machineId] = it.label }
                adapter.setMachineLabels(machineLabels)
            } catch (e: Exception) {
                // Non-critical: falls back to showing a truncated machine ID.
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
