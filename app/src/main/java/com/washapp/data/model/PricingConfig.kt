package com.washapp.data.model

/** Wash and dry times are flat per load, regardless of weight. */
data class PricingConfig(
    val serviceType: ServiceType = ServiceType.NON_COLORED,
    val pricePerKg: Double = 0.0,
    val washMinutes: Int = 30,
    val dryMinutes: Int = 45
)
