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
    /** The customer asked for priority; it only takes effect once the owner approves and prices it. */
    val priorityRequested: Boolean = false,
    /** Additional fee the owner sets when granting priority; included in `cost`. */
    val priorityPrice: Double = 0.0,
    val assignedMachineId: String = "",
    val stage: ServiceStage = ServiceStage.QUEUED,
    val estimatedWashMinutes: Int = 0,
    val estimatedDryMinutes: Int = 0,
    val folding: Boolean = false,
    /** Set by the owner when folding starts; the order's cost excludes it until then. */
    val foldingPrice: Double = 0.0,
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
