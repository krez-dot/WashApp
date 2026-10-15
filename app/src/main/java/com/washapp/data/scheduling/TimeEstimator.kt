package com.washapp.data.scheduling

import com.washapp.data.model.Order

/**
 * Wash, dry and fold minutes are flat per load and set by the owner in Services; none of them
 * scale with weight.
 */
object TimeEstimator {
    fun estimate(
        order: Order,
        queuePosition: Int,
        averageStageMinutes: Int,
        washMinutes: Int,
        dryMinutes: Int,
        foldMinutes: Int
    ): Order {
        val orderFoldMinutes = if (order.folding) foldMinutes else 0
        val waitMinutes = queuePosition * averageStageMinutes
        return order.copy(
            estimatedWashMinutes = washMinutes,
            estimatedDryMinutes = dryMinutes,
            estimatedFoldMinutes = orderFoldMinutes,
            estimatedCompletionMinutes = waitMinutes + washMinutes + dryMinutes + orderFoldMinutes
        )
    }
}
