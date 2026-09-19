package com.washapp.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.washapp.data.model.Order
import com.washapp.databinding.ItemQueueOrderBinding

class QueueAdapter(
    private val onMoveUp: (Order, Order) -> Unit,
    private val onMoveDown: (Order, Order) -> Unit
) : RecyclerView.Adapter<QueueAdapter.QueueViewHolder>() {

    private val orders = mutableListOf<Order>()

    fun submitList(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }

    fun swapPositions(orderId: String, newPosition: Int, otherOrderId: String, otherNewPosition: Int) {
        val index = orders.indexOfFirst { it.orderId == orderId }
        val otherIndex = orders.indexOfFirst { it.orderId == otherOrderId }
        if (index == -1 || otherIndex == -1) return

        orders[index] = orders[index].copy(queuePosition = newPosition)
        orders[otherIndex] = orders[otherIndex].copy(queuePosition = otherNewPosition)
        orders.sortBy { it.queuePosition }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): QueueViewHolder {
        val binding = ItemQueueOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QueueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {
        holder.bind(orders[position], position)
    }

    override fun getItemCount(): Int = orders.size

    inner class QueueViewHolder(private val binding: ItemQueueOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order, position: Int) {
            binding.queueOrderSummary.text =
                "#${order.queuePosition} · ${order.orderId.take(6)} · ${order.serviceType} · ${order.loadSizeKg}kg"

            binding.moveUpButton.isEnabled = position > 0
            binding.moveUpButton.setOnClickListener {
                onMoveUp(order, orders[position - 1])
            }

            binding.moveDownButton.isEnabled = position < orders.size - 1
            binding.moveDownButton.setOnClickListener {
                onMoveDown(order, orders[position + 1])
            }
        }
    }
}
