package com.washapp.customer

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.washapp.R
import com.washapp.data.model.Order
import com.washapp.data.model.ServiceStage
import com.washapp.data.model.ServiceType
import com.washapp.databinding.ItemOrderTrackingBinding
import com.washapp.util.bindStageChip
import com.washapp.util.orderServiceLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                "${context.orderServiceLabel(order)} · ${order.loadSizeKg} kg"
            binding.priorityBadge.visibility =
                if (order.priority) android.view.View.VISIBLE else android.view.View.GONE
            if (order.stage == ServiceStage.COMPLETED) {
                // A finished order has no queue or wait left, so show when it was placed instead.
                val placed = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
                    .format(Date(order.createdAt))
                binding.queuePositionLabel.text = context.getString(R.string.format_placed_on, placed)
                binding.estimatedCompletionLabel.visibility = android.view.View.GONE
                binding.stageProgress.visibility = android.view.View.GONE
            } else {
                binding.queuePositionLabel.text = "Queue position: ${order.queuePosition}"
                binding.estimatedCompletionLabel.text =
                    "Estimated completion: ${order.estimatedCompletionMinutes} min"
                binding.estimatedCompletionLabel.visibility = android.view.View.VISIBLE
                bindProgress(order)
            }
            binding.costLabel.text = context.getString(R.string.format_currency, order.cost)

            if (order.stage == ServiceStage.QUEUED) {
                binding.cancelOrderButton.visibility = android.view.View.VISIBLE
                binding.cancelOrderButton.setOnClickListener { onCancel(order) }
            } else {
                binding.cancelOrderButton.visibility = android.view.View.GONE
                binding.cancelOrderButton.setOnClickListener(null)
            }
        }

        /** Fills the bar in equal steps across the stages this order will actually pass through. */
        private fun bindProgress(order: Order) {
            val steps = if (order.folding) 5 else 4
            val position = when (order.stage) {
                ServiceStage.QUEUED -> 1
                ServiceStage.WASHING -> 2
                ServiceStage.DRYING -> 3
                ServiceStage.FOLDING -> 4
                ServiceStage.COMPLETED -> steps
            }
            val context = binding.root.context
            val colorRes = when (order.stage) {
                ServiceStage.QUEUED -> R.color.washapp_stage_queued
                ServiceStage.WASHING -> R.color.washapp_stage_washing
                ServiceStage.DRYING -> R.color.washapp_stage_drying
                ServiceStage.FOLDING -> R.color.washapp_stage_folding
                ServiceStage.COMPLETED -> R.color.washapp_stage_completed
            }
            binding.stageProgress.visibility = android.view.View.VISIBLE
            binding.stageProgress.setIndicatorColor(context.getColor(colorRes))
            binding.stageProgress.setProgressCompat(position * 100 / steps, false)
        }
    }
}
