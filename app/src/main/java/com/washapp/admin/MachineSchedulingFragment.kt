package com.washapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.washapp.R
import com.washapp.data.model.Machine
import com.washapp.data.repository.MachineRepository
import com.washapp.databinding.FragmentMachineSchedulingBinding
import kotlinx.coroutines.launch

class MachineSchedulingFragment : Fragment() {

    private var _binding: FragmentMachineSchedulingBinding? = null
    private val binding get() = _binding!!

    private val machineRepository = MachineRepository()
    private lateinit var adapter: MachineAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMachineSchedulingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MachineAdapter(
            onToggleAvailability = { machine -> toggleAvailability(machine) },
            onDelete = { machine -> confirmDelete(machine) }
        )
        binding.machineList.layoutManager = LinearLayoutManager(requireContext())
        binding.machineList.adapter = adapter

        binding.addMachineButton.setOnClickListener { addMachine() }

        loadMachines()
    }

    private fun addMachine() {
        val label = binding.newMachineLabelInput.text.toString().trim()
        if (label.isEmpty()) {
            Snackbar.make(binding.root, "Enter a machine label", Snackbar.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val machineId = machineRepository.addMachine(label)
                binding.newMachineLabelInput.text?.clear()
                adapter.appendItem(Machine(machineId = machineId, label = label, isAvailable = true))
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't add the machine", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun toggleAvailability(machine: Machine) {
        val updated = machine.copy(isAvailable = !machine.isAvailable)
        lifecycleScope.launch {
            try {
                machineRepository.setAvailability(machine.machineId, updated.isAvailable)
                // Update the list from the known write result rather than re-querying:
                // Firestore's write acknowledgment can arrive slightly before the local
                // query cache catches up, so an immediate re-fetch can read stale data.
                adapter.updateItem(updated)
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't update the machine", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmDelete(machine: Machine) {
        if (!machine.isAvailable) {
            Snackbar.make(binding.root, R.string.message_machine_in_use, Snackbar.LENGTH_LONG).show()
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_delete_machine)
            .setMessage(machine.label)
            .setPositiveButton(R.string.action_delete_machine) { _, _ -> deleteMachine(machine) }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun deleteMachine(machine: Machine) {
        lifecycleScope.launch {
            try {
                machineRepository.deleteMachine(machine.machineId)
                adapter.removeItem(machine.machineId)
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't delete the machine", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadMachines() {
        lifecycleScope.launch {
            try {
                adapter.submitList(machineRepository.getAllMachines())
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't load machines", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
