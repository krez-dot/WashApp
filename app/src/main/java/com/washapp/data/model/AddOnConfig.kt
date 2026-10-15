package com.washapp.data.model

/** Owner-set add-on settings: a flat folding price and time, and a flat priority fee, per order. */
data class AddOnConfig(
    val foldingPrice: Double = 0.0,
    val foldingMinutes: Int = 10,
    val priorityFee: Double = 0.0
)
