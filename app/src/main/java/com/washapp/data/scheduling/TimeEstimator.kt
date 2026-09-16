package com.washapp.data.scheduling

import com.washapp.data.model.Order
import com.washapp.data.model.PricingConfig

object TimeEstimator {
    fun estimate(
        order: Order,
        pricing: PricingConfig,
        queuePosition: Int,
        averageStageMinutes: Int
    ): Order {
        val washMinutes = pricing.washMinutesPerKg * order.loadSizeKg.toInt()
        val dryMinutes = pricing.dryMinutesPerKg * order.loadSizeKg.toInt()
        val waitMinutes = queuePosition * averageStageMinutes
        return order.copy(
            estimatedWashMinutes = washMinutes,
            estimatedDryMinutes = dryMinutes,
            estimatedCompletionMinutes = waitMinutes + washMinutes + dryMinutes
        )
    }
}
