package com.washapp.customer

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.washapp.data.model.Order
import com.washapp.data.model.ServiceStage
import com.washapp.databinding.ItemOrderTrackingBinding

class OrderTrackingAdapter : RecyclerView.Adapter<OrderTrackingAdapter.OrderViewHolder>() {

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
            binding.stageChip.text = order.stage.name
            binding.queuePositionLabel.text = "Queue position: ${order.queuePosition}"
            binding.estimatedCompletionLabel.text =
                "Estimated completion: ${order.estimatedCompletionMinutes} min"
            binding.costLabel.text = "₱${order.cost}"

            val colorRes = when (order.stage) {
                ServiceStage.QUEUED -> com.washapp.R.color.washapp_stage_queued
                ServiceStage.WASHING -> com.washapp.R.color.washapp_stage_washing
                ServiceStage.DRYING -> com.washapp.R.color.washapp_stage_drying
                ServiceStage.COMPLETED -> com.washapp.R.color.washapp_stage_completed
            }
            binding.stageChip.backgroundTintList =
                ColorStateList.valueOf(context.getColor(colorRes))
        }
    }
}
