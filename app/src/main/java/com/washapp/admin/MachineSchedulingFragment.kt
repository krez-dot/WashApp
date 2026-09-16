package com.washapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.washapp.databinding.FragmentMachineSchedulingBinding

class MachineSchedulingFragment : Fragment() {

    private var _binding: FragmentMachineSchedulingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMachineSchedulingBinding.inflate(inflater, container, false)
        return binding.root
    }

    // TODO: wire up MachineRepository + MachineScheduler here to show per-machine
    // assignment status and let the administrator manage machine count/availability.

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
