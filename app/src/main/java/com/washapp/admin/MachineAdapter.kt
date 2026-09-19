package com.washapp.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.washapp.data.model.Machine
import com.washapp.databinding.ItemMachineBinding

class MachineAdapter(
    private val onToggleAvailability: (Machine) -> Unit,
    private val onDelete: (Machine) -> Unit
) : RecyclerView.Adapter<MachineAdapter.MachineViewHolder>() {

    private val machines = mutableListOf<Machine>()

    fun submitList(newMachines: List<Machine>) {
        machines.clear()
        machines.addAll(newMachines)
        notifyDataSetChanged()
    }

    fun updateItem(updated: Machine) {
        val index = machines.indexOfFirst { it.machineId == updated.machineId }
        if (index == -1) return
        machines[index] = updated
        notifyItemChanged(index)
    }

    fun appendItem(machine: Machine) {
        machines.add(machine)
        notifyItemInserted(machines.size - 1)
    }

    fun removeItem(machineId: String) {
        val index = machines.indexOfFirst { it.machineId == machineId }
        if (index == -1) return
        machines.removeAt(index)
        notifyItemRemoved(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): MachineViewHolder {
        val binding = ItemMachineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MachineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MachineViewHolder, position: Int) {
        holder.bind(machines[position])
    }

    override fun getItemCount(): Int = machines.size

    inner class MachineViewHolder(private val binding: ItemMachineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(machine: Machine) {
            val status = if (machine.isAvailable) "Available" else "In use"
            binding.machineLabel.text = "${machine.label} · $status"
            binding.toggleAvailabilityButton.setOnClickListener { onToggleAvailability(machine) }
            binding.deleteMachineButton.setOnClickListener { onDelete(machine) }
        }
    }
}
