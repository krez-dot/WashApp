package com.washapp.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.washapp.data.model.Order
import com.washapp.data.model.ServiceStage
import com.washapp.databinding.ItemOrderManagementBinding

class OrderManagementAdapter(
    private val onAdvanceStage: (Order) -> Unit
) : RecyclerView.Adapter<OrderManagementAdapter.OrderViewHolder>() {

    private val orders = mutableListOf<Order>()

    fun submitList(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
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
            binding.orderSummary.text =
                "#${order.queuePosition} · ${order.orderId.take(6)} · ${order.serviceType} · " +
                    "${order.loadSizeKg}kg · ${order.stage}"

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
