package com.washapp.data.model

data class Order(
    val orderId: String = "",
    val customerId: String = "",
    val serviceType: ServiceType = ServiceType.NON_COLORED,
    val loadSizeKg: Double = 0.0,
    val cost: Double = 0.0,
    val queuePosition: Int = 0,
    val assignedMachineId: String = "",
    val stage: ServiceStage = ServiceStage.QUEUED,
    val estimatedWashMinutes: Int = 0,
    val estimatedDryMinutes: Int = 0,
    val estimatedCompletionMinutes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ServiceType {
    COLORED,
    NON_COLORED
}

enum class ServiceStage {
    QUEUED,
    WASHING,
    DRYING,
    COMPLETED
}
