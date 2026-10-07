package com.washapp.customer

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.washapp.data.model.Order
import com.washapp.data.model.ServiceStage
import com.washapp.data.model.ServiceType
import com.washapp.databinding.ItemOrderTrackingBinding
import com.washapp.util.bindStageChip
import com.washapp.util.serviceTypeLabel

class OrderTrackingAdapter(
    private val onCancel: (Order) -> Unit
) : RecyclerView.Adapter<OrderTrackingAdapter.OrderViewHolder>() {

    private val orders = mutableListOf<Order>()

    fun submitList(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders.sortedBy { it.orderId })
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

    fun isEmpty(): Boolean = orders.isEmpty()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): OrderViewHolder {
        val binding = ItemOrderTrackingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(private val binding: ItemOrderTrackingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            val context = binding.root.context
            binding.orderIdLabel.text = "Order ${order.orderId.take(6)}"
            binding.stageChip.bindStageChip(order.stage)
            binding.orderDetailsLabel.text =
                "${context.serviceTypeLabel(order.serviceType)} · ${order.loadSizeKg} kg"
            binding.queuePositionLabel.text = "Queue position: ${order.queuePosition}"
            binding.estimatedCompletionLabel.text =
                "Estimated completion: ${order.estimatedCompletionMinutes} min"
            binding.costLabel.text = "₱${order.cost}"

            if (order.stage == ServiceStage.QUEUED) {
                binding.cancelOrderButton.visibility = android.view.View.VISIBLE
                binding.cancelOrderButton.setOnClickListener { onCancel(order) }
            } else {
                binding.cancelOrderButton.visibility = android.view.View.GONE
                binding.cancelOrderButton.setOnClickListener(null)
            }
        }
    }
}
