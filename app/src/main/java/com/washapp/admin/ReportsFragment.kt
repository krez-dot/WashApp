package com.washapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.washapp.R
import com.washapp.data.model.ServiceStage
import com.washapp.data.model.ServiceType
import com.washapp.data.repository.OrderRepository
import com.washapp.databinding.FragmentReportsBinding
import kotlinx.coroutines.launch

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val orderRepository = OrderRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadReport()
    }

    private fun loadReport() {
        lifecycleScope.launch {
            try {
                val orders = orderRepository.getAllOrders()
                val completed = orders.count { it.stage == ServiceStage.COMPLETED }
                val inProgress = orders.size - completed
                val totalRevenue = orders.sumOf { it.cost }
                val colored = orders.count { it.serviceType == ServiceType.COLORED }
                val nonColored = orders.size - colored

                binding.totalRevenueLabel.text = getString(R.string.format_currency, totalRevenue)
                binding.totalOrdersLabel.text = orders.size.toString()
                binding.completedOrdersLabel.text = "$completed completed"
                binding.inProgressOrdersLabel.text = "$inProgress in progress"
                binding.coloredSplitLabel.text =
                    getString(R.string.format_colored_split, colored, nonColored)
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Couldn't load report", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
