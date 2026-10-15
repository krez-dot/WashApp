package com.washapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.washapp.data.model.FoldingConfig
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

    /** Null until the admin first saves folding rates, which keeps the add-on hidden from customers. */
    suspend fun getFolding(): FoldingConfig? {
        val snapshot = pricing.document(FOLDING_DOC).get().await()
        return snapshot.toObject(FoldingConfig::class.java)
    }

    suspend fun saveFolding(config: FoldingConfig) {
        pricing.document(FOLDING_DOC).set(config).await()
    }

    suspend fun savePricing(config: PricingConfig) {
        pricing.document(config.serviceType.name).set(config).await()
    }

    private companion object {
        const val FOLDING_DOC = "FOLDING"
    }
}
