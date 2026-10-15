package com.washapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.washapp.data.model.AddOnConfig
import com.washapp.data.model.PricingConfig
import com.washapp.data.model.ServiceType
import kotlinx.coroutines.tasks.await

class PricingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val pricing get() = firestore.collection(FirestorePaths.PRICING)

    suspend fun getPricing(serviceType: ServiceType): PricingConfig {
        val snapshot = pricing.document(serviceType.name).get().await()
        return snapshot.toObject(PricingConfig::class.java)
            ?: PricingConfig(serviceType = serviceType)
    }

    suspend fun savePricing(config: PricingConfig) {
        pricing.document(config.serviceType.name).set(config).await()
    }

    suspend fun getAddOns(): AddOnConfig {
        val snapshot = pricing.document(ADD_ONS_DOC).get().await()
        return snapshot.toObject(AddOnConfig::class.java) ?: AddOnConfig()
    }

    suspend fun saveAddOns(config: AddOnConfig) {
        pricing.document(ADD_ONS_DOC).set(config).await()
    }

    private companion object {
        const val ADD_ONS_DOC = "ADD_ONS"
    }
}
