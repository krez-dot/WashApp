package com.washapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.washapp.data.model.Order
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val orders get() = firestore.collection(FirestorePaths.ORDERS)

    suspend fun submitOrder(order: Order): String {
        val docRef = orders.document()
        orders.document(docRef.id).set(order.copy(orderId = docRef.id)).await()
        return docRef.id
    }

    suspend fun getOrdersForCustomer(customerId: String): List<Order> {
        val snapshot = orders.whereEqualTo("customerId", customerId).get(Source.SERVER).await()
        return snapshot.toObjects(Order::class.java)
    }

    suspend fun getActiveQueue(): List<Order> {
        val snapshot = orders.whereEqualTo("stage", "QUEUED").get(Source.SERVER).await()
        return snapshot.toObjects(Order::class.java).sortedBy { it.queuePosition }
    }

    suspend fun updateStage(orderId: String, stage: String) {
        orders.document(orderId).update("stage", stage).await()
    }

    suspend fun swapQueuePositions(orderId: String, position: Int, otherOrderId: String, otherPosition: Int) {
        orders.document(orderId).update("queuePosition", otherPosition).await()
        orders.document(otherOrderId).update("queuePosition", position).await()
    }
}
