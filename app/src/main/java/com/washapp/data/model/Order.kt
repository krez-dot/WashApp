package com.washapp.data.model

data class Order(
    val orderId: String = "",
    val customerId: String = "",
    val serviceType: ServiceType = ServiceType.NON_COLORED,
    val loadSizeKg: Double = 0.0,
    val cost: Double = 0.0,
    val queuePosition: Int = 0,
    /**
     * Rush orders agreed with the shop administrator. Deliberately not named `isPriority`:
     * Firestore strips the `is` prefix from Kotlin boolean getters and would store it as
     * `priority` anyway, which previously caused a field-name mismatch on Machine.
     */
    val priority: Boolean = false,
    val assignedMachineId: String = "",
    val stage: ServiceStage = ServiceStage.QUEUED,
    val estimatedWashMinutes: Int = 0,
    val estimatedDryMinutes: Int = 0,
    val folding: Boolean = false,
    val estimatedFoldMinutes: Int = 0,
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
    FOLDING,
    COMPLETED;

    /** Folding is an optional add-on, so only orders that bought it pass through FOLDING. */
    fun next(withFolding: Boolean): ServiceStage? = when (this) {
        QUEUED -> WASHING
        WASHING -> DRYING
        DRYING -> if (withFolding) FOLDING else COMPLETED
        FOLDING -> COMPLETED
        COMPLETED -> null
    }
}
