package com.washapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.washapp.databinding.FragmentQueueManagementBinding

class QueueManagementFragment : Fragment() {

    private var _binding: FragmentQueueManagementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQueueManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    // TODO: wire up manual queue priority overrides via OrderRepository once the
    // drag-to-reorder RecyclerView UI for administrator queue control is designed.

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
