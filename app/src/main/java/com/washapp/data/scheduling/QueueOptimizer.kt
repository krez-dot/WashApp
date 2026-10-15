package com.washapp.data.scheduling

import com.washapp.data.model.Order

/**
 * Rule-based, priority-aware FIFO ordering. Priority (rush) orders are served before
 * standard ones; within each group orders keep the sequence they were submitted in.
 */
object QueueOptimizer {

    fun assignQueuePosition(currentQueue: List<Order>, incomingOrder: Order): Int {
        if (!incomingOrder.priority) return currentQueue.size + 1
        // A rush order goes behind any existing rush orders but ahead of standard ones.
        return currentQueue.count { it.priority } + 1
    }

    /** Priority first, then oldest submission first. */
    fun sortForQueue(orders: List<Order>): List<Order> =
        orders.sortedWith(compareByDescending<Order> { it.priority }.thenBy { it.createdAt })

    /**
     * Returns the queue in served order with positions renumbered from 1, so the stored
     * positions stay dense and consistent after a priority change.
     */
    fun renumber(orders: List<Order>): List<Order> =
        sortForQueue(orders).mapIndexed { index, order -> order.copy(queuePosition = index + 1) }
}
