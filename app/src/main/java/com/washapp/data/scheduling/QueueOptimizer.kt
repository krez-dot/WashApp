package com.washapp.data.scheduling

import com.washapp.data.model.Order

object QueueOptimizer {
    fun assignQueuePosition(currentQueue: List<Order>, incomingOrder: Order): Int {
        return currentQueue.size + 1
    }
}
