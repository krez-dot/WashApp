package com.washapp.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.washapp.data.model.Order
import com.washapp.data.model.ServiceStage
import com.washapp.databinding.ItemOrderManagementBinding
import com.washapp.util.bindStageChip
import com.washapp.util.serviceTypeLabel

class OrderManagementAdapter(
    private val onAdvanceStage: (Order) -> Unit
) : RecyclerView.Adapter<OrderManagementAdapter.OrderViewHolder>() {

    private val orders = mutableListOf<Order>()
    private var machineLabels: Map<String, String> = emptyMap()
    private var customerLabels: Map<String, String> = emptyMap()

    fun submitList(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }

    fun setMachineLabels(labels: Map<String, String>) {
        machineLabels = labels
        notifyDataSetChanged()
    }

    fun setCustomerLabels(labels: Map<String, String>) {
        customerLabels = labels
        notifyDataSetChanged()
    }

    fun updateItem(updated: Order) {
        val index = orders.indexOfFirst { it.orderId == updated.orderId }
        if (index == -1) return
        orders[index] = updated
        notifyItemChanged(index)
    }

    fun removeItem(orderId: String) {
        val index = orders.indexOfFirst { it.orderId == orderId }
        if (index == -1) return
        orders.removeAt(index)
        notifyItemRemoved(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): OrderViewHolder {
        val binding = ItemOrderManagementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(private val binding: ItemOrderManagementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            val context = binding.root.context
            binding.orderSummary.text = "Order ${order.orderId.take(6)}"
            binding.stageChip.bindStageChip(order.stage)
            binding.orderDetailsLabel.text = "Queue #${order.queuePosition} · " +
                "${context.serviceTypeLabel(order.serviceType)} · ${order.loadSizeKg} kg"

            if (order.assignedMachineId.isNotEmpty()) {
                binding.machineLabel.visibility = View.VISIBLE
                binding.machineLabel.text =
                    machineLabels[order.assignedMachineId] ?: order.assignedMachineId.take(6)
            } else {
                binding.machineLabel.visibility = View.GONE
            }

            binding.customerLabel.text = customerLabels[order.customerId] ?: order.customerId.take(8)

            val next = order.stage.next()
            if (next == null) {
                binding.advanceStageButton.visibility = View.GONE
            } else {
                binding.advanceStageButton.visibility = View.VISIBLE
                binding.advanceStageButton.text = when (next) {
                    ServiceStage.WASHING -> "Start Washing"
                    ServiceStage.DRYING -> "Start Drying"
                    ServiceStage.COMPLETED -> "Complete"
                    ServiceStage.QUEUED -> next.name
                }
                binding.advanceStageButton.setOnClickListener { onAdvanceStage(order) }
            }
        }
    }
}
