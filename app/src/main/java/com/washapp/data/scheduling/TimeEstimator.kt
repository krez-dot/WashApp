package com.washapp.data.scheduling

import com.washapp.data.model.Order
import kotlin.math.roundToInt

/**
 * Stage durations are fixed rather than owner-configurable, so every estimate uses the same
 * per-kg rates. Adjust these constants to recalibrate the estimates.
 */
object TimeEstimator {
    const val WASH_MINUTES_PER_KG = 5
    const val DRY_MINUTES_PER_KG = 8
    const val FOLD_MINUTES_PER_KG = 3

    fun estimate(order: Order, queuePosition: Int, averageStageMinutes: Int): Order {
        val washMinutes = (order.loadSizeKg * WASH_MINUTES_PER_KG).roundToInt()
        val dryMinutes = (order.loadSizeKg * DRY_MINUTES_PER_KG).roundToInt()
        val foldMinutes = if (order.folding) (order.loadSizeKg * FOLD_MINUTES_PER_KG).roundToInt() else 0
        val waitMinutes = queuePosition * averageStageMinutes
        return order.copy(
            estimatedWashMinutes = washMinutes,
            estimatedDryMinutes = dryMinutes,
            estimatedFoldMinutes = foldMinutes,
            estimatedCompletionMinutes = waitMinutes + washMinutes + dryMinutes + foldMinutes
        )
    }
}
