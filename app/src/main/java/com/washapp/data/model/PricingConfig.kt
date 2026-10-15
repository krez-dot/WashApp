package com.washapp.data.model

data class PricingConfig(
    val serviceType: ServiceType = ServiceType.NON_COLORED,
    val pricePerKg: Double = 0.0
)
