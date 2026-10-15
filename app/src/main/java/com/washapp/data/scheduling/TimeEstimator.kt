package com.washapp.data.scheduling

import com.washapp.data.model.Order

/**
 * Wash and dry minutes are flat per load and set by the owner per service type. Folding uses a
 * fixed flat time since the owner prices it per order.
 */
object TimeEstimator {
    const val FOLD_MINUTES = 10

    fun estimate(
        order: Order,
        queuePosition: Int,
        averageStageMinutes: Int,
        washMinutes: Int,
        dryMinutes: Int
    ): Order {
        val foldMinutes = if (order.folding) FOLD_MINUTES else 0
        val waitMinutes = queuePosition * averageStageMinutes
        return order.copy(
            estimatedWashMinutes = washMinutes,
            estimatedDryMinutes = dryMinutes,
            estimatedFoldMinutes = foldMinutes,
            estimatedCompletionMinutes = waitMinutes + washMinutes + dryMinutes + foldMinutes
        )
    }
}
