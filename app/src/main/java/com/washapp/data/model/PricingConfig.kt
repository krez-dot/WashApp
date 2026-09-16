package com.washapp.data.model

data class PricingConfig(
    val serviceType: ServiceType = ServiceType.NON_COLORED,
    val pricePerKg: Double = 0.0,
    val washMinutesPerKg: Int = 0,
    val dryMinutesPerKg: Int = 0
)
