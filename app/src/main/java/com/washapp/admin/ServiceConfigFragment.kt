package com.washapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
            save(
                ServiceType.NON_COLORED,
                binding.nonColoredPriceInput,
                binding.nonColoredWashInput,
                binding.nonColoredDryInput
            )
        }
        binding.saveColoredButton.setOnClickListener {
            save(
                ServiceType.COLORED,
                binding.coloredPriceInput,
                binding.coloredWashInput,
                binding.coloredDryInput
            )
        }

        loadPricing()
    }

    private fun loadPricing() {
        lifecycleScope.launch {
            try {
                val nonColored = pricingRepository.getPricing(ServiceType.NON_COLORED)
                binding.nonColoredPriceInput.setText(nonColored.pricePerKg.toString())
                binding.nonColoredWashInput.setText(nonColored.washMinutesPerKg.toString())
                binding.nonColoredDryInput.setText(nonColored.dryMinutesPerKg.toString())

                val colored = pricingRepository.getPricing(ServiceType.COLORED)
                binding.coloredPriceInput.setText(colored.pricePerKg.toString())
                binding.coloredWashInput.setText(colored.washMinutesPerKg.toString())
                binding.coloredDryInput.setText(colored.dryMinutesPerKg.toString())
            } catch (e: Exception) {
                // TODO: surface the fetch failure to the user (e.g. Snackbar)
            }
        }
    }

    private fun save(
        serviceType: ServiceType,
        priceInput: android.widget.EditText,
        washInput: android.widget.EditText,
        dryInput: android.widget.EditText
    ) {
        val price = priceInput.text.toString().toDoubleOrNull() ?: return
        val wash = washInput.text.toString().toIntOrNull() ?: return
        val dry = dryInput.text.toString().toIntOrNull() ?: return

        lifecycleScope.launch {
            try {
                pricingRepository.savePricing(
                    PricingConfig(
                        serviceType = serviceType,
                        pricePerKg = price,
                        washMinutesPerKg = wash,
                        dryMinutesPerKg = dry
                    )
                )
            } catch (e: Exception) {
                // TODO: surface the save failure to the user (e.g. Snackbar)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
