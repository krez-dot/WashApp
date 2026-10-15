package com.washapp.data.scheduling

import com.washapp.data.model.FoldingConfig
import com.washapp.data.model.Order
import com.washapp.data.model.PricingConfig

object TimeEstimator {
    fun estimate(
        order: Order,
        pricing: PricingConfig,
        queuePosition: Int,
        averageStageMinutes: Int,
        folding: FoldingConfig? = null
    ): Order {
        val washMinutes = pricing.washMinutesPerKg * order.loadSizeKg.toInt()
        val dryMinutes = pricing.dryMinutesPerKg * order.loadSizeKg.toInt()
        val foldMinutes = if (order.folding && folding != null) {
            folding.foldMinutesPerKg * order.loadSizeKg.toInt()
        } else {
            0
        }
        val waitMinutes = queuePosition * averageStageMinutes
        return order.copy(
            estimatedWashMinutes = washMinutes,
            estimatedDryMinutes = dryMinutes,
            estimatedFoldMinutes = foldMinutes,
            estimatedCompletionMinutes = waitMinutes + washMinutes + dryMinutes + foldMinutes
        )
    }
}
