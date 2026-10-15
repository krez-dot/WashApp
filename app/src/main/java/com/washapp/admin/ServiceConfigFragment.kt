package com.washapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.washapp.data.model.PricingConfig
import com.washapp.data.model.ServiceType
import com.washapp.data.repository.PricingRepository
import com.washapp.databinding.FragmentServiceConfigBinding
import kotlinx.coroutines.launch

class ServiceConfigFragment : Fragment() {

    private var _binding: FragmentServiceConfigBinding? = null
    private val binding get() = _binding!!

    private val pricingRepository = PricingRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveNonColoredButton.setOnClickListener {
            save(ServiceType.NON_COLORED, binding.nonColoredPriceInput)
        }
        binding.saveColoredButton.setOnClickListener {
            save(ServiceType.COLORED, binding.coloredPriceInput)
        }

        loadPricing()
    }

    private fun loadPricing() {
        lifecycleScope.launch {
            try {
                val nonColored = pricingRepository.getPricing(ServiceType.NON_COLORED)
                binding.nonColoredPriceInput.setText(nonColored.pricePerKg.toString())

                val colored = pricingRepository.getPricing(ServiceType.COLORED)
                binding.coloredPriceInput.setText(colored.pricePerKg.toString())
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't load pricing", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun save(serviceType: ServiceType, priceInput: EditText) {
        val price = priceInput.text.toString().toDoubleOrNull()
        if (price == null || price < 0) {
            Snackbar.make(binding.root, "Enter a valid price", Snackbar.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                pricingRepository.savePricing(PricingConfig(serviceType = serviceType, pricePerKg = price))
                Snackbar.make(binding.root, "Saved", Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't save pricing", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
