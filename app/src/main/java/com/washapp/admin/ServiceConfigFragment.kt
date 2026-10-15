package com.washapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.washapp.data.model.AddOnConfig
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
            save(ServiceType.NON_COLORED, binding.nonColoredPriceInput, binding.nonColoredWashInput, binding.nonColoredDryInput)
        }
        binding.saveColoredButton.setOnClickListener {
            save(ServiceType.COLORED, binding.coloredPriceInput, binding.coloredWashInput, binding.coloredDryInput)
        }

        binding.saveAddOnsButton.setOnClickListener { saveAddOns() }

        loadPricing()
    }

    private fun loadPricing() {
        lifecycleScope.launch {
            try {
                val nonColored = pricingRepository.getPricing(ServiceType.NON_COLORED)
                binding.nonColoredPriceInput.setText(nonColored.pricePerKg.toString())
                binding.nonColoredWashInput.setText(nonColored.washMinutes.toString())
                binding.nonColoredDryInput.setText(nonColored.dryMinutes.toString())

                val colored = pricingRepository.getPricing(ServiceType.COLORED)
                binding.coloredPriceInput.setText(colored.pricePerKg.toString())
                binding.coloredWashInput.setText(colored.washMinutes.toString())
                binding.coloredDryInput.setText(colored.dryMinutes.toString())

                val addOns = pricingRepository.getAddOns()
                binding.foldingPriceSettingInput.setText(addOns.foldingPrice.toString())
                binding.foldingMinutesSettingInput.setText(addOns.foldingMinutes.toString())
                binding.priorityFeeInput.setText(addOns.priorityFee.toString())
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't load pricing", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun save(
        serviceType: ServiceType,
        priceInput: EditText,
        washInput: EditText,
        dryInput: EditText
    ) {
        val price = priceInput.text.toString().toDoubleOrNull()
        if (price == null || price < 0) {
            Snackbar.make(binding.root, "Enter a valid price", Snackbar.LENGTH_SHORT).show()
            return
        }
        val washMinutes = washInput.text.toString().toIntOrNull()
        val dryMinutes = dryInput.text.toString().toIntOrNull()
        if (washMinutes == null || washMinutes <= 0 || dryMinutes == null || dryMinutes <= 0) {
            Snackbar.make(binding.root, "Enter wash and dry times in minutes", Snackbar.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                pricingRepository.savePricing(PricingConfig(
                        serviceType = serviceType,
                        pricePerKg = price,
                        washMinutes = washMinutes,
                        dryMinutes = dryMinutes
                    ))
                Snackbar.make(binding.root, "Saved", Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't save pricing", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun saveAddOns() {
        val foldingPrice = binding.foldingPriceSettingInput.text.toString().toDoubleOrNull()
        val foldingMinutes = binding.foldingMinutesSettingInput.text.toString().toIntOrNull()
        val priorityFee = binding.priorityFeeInput.text.toString().toDoubleOrNull()
        if (foldingPrice == null || foldingPrice < 0 || priorityFee == null || priorityFee < 0) {
            Snackbar.make(binding.root, "Enter valid prices", Snackbar.LENGTH_SHORT).show()
            return
        }
        if (foldingMinutes == null || foldingMinutes <= 0) {
            Snackbar.make(binding.root, "Enter the folding time in minutes", Snackbar.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                pricingRepository.saveAddOns(AddOnConfig(foldingPrice, foldingMinutes, priorityFee))
                Snackbar.make(binding.root, "Saved", Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't save add-ons", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
