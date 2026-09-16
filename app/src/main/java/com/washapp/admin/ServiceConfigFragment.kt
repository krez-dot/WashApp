package com.washapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.washapp.databinding.FragmentServiceConfigBinding

class ServiceConfigFragment : Fragment() {

    private var _binding: FragmentServiceConfigBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    // TODO: wire up PricingRepository CRUD here for service types, cost per service,
    // and stage duration configuration (feeds directly into TimeEstimator's inputs).

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
