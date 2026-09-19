package com.washapp.data.model

import com.google.firebase.firestore.PropertyName

data class Machine(
    val machineId: String = "",
    val label: String = "",
    @get:PropertyName("isAvailable")
    val isAvailable: Boolean = true,
    val currentOrderId: String? = null
)
