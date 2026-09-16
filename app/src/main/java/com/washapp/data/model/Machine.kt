package com.washapp.data.model

data class Machine(
    val machineId: String = "",
    val label: String = "",
    val isAvailable: Boolean = true,
    val currentOrderId: String? = null
)
