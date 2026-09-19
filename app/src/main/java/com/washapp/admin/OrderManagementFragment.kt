package com.washapp.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.washapp.data.model.Order
import com.washapp.data.model.ServiceStage
import com.washapp.data.repository.AuthRepository
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
    private val authRepository = AuthRepository()
    private lateinit var adapter: OrderManagementAdapter
    private val machineLabels = mutableMapOf<String, String>()
    private val customerLabels = mutableMapOf<String, String>()
    private var allOrders: List<Order> = emptyList()

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

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = applyFilter(s?.toString().orEmpty())
        })

        loadOrders()
        loadMachineLabels()
    }

    private fun applyFilter(query: String) {
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) {
            allOrders
        } else {
            allOrders.filter { order ->
                order.orderId.contains(trimmed, ignoreCase = true) ||
                    (customerLabels[order.customerId] ?: "").contains(trimmed, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
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
                        replaceOrder(order.copy(stage = next))
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
        replaceOrder(order.copy(stage = next, assignedMachineId = machine.machineId))
    }

    private suspend fun completeOrder(order: Order, next: ServiceStage) {
        if (order.assignedMachineId.isNotEmpty()) {
            machineRepository.releaseMachine(order.assignedMachineId)
        }
        orderRepository.updateStage(order.orderId, next.name, order.queuePosition)
        allOrders = allOrders.filterNot { it.orderId == order.orderId }
        adapter.removeItem(order.orderId)
    }

    private fun replaceOrder(updated: Order) {
        allOrders = allOrders.map { if (it.orderId == updated.orderId) updated else it }
        adapter.updateItem(updated)
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            try {
                allOrders = orderRepository.getOrdersInProgress()
                adapter.submitList(allOrders)
                loadCustomerLabels()
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't load orders", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadCustomerLabels() {
        lifecycleScope.launch {
            try {
                val users = authRepository.getUsersByIds(allOrders.map { it.customerId })
                customerLabels.clear()
                users.forEach { (uid, user) -> customerLabels[uid] = "${user.name} · ${user.email}" }
                adapter.setCustomerLabels(customerLabels)
            } catch (e: Exception) {
                // Non-critical: falls back to showing a truncated customer ID.
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
