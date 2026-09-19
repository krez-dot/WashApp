package com.washapp.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.washapp.data.model.Order
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val realtimeDb: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val orders get() = firestore.collection(FirestorePaths.ORDERS)
    private val orderStatus get() = realtimeDb.getReference("orderStatus")

    suspend fun submitOrder(order: Order): String {
        val docRef = orders.document()
        val finalOrder = order.copy(orderId = docRef.id)
        orders.document(docRef.id).set(finalOrder).await()
        pushStatus(docRef.id, finalOrder.stage.name, finalOrder.queuePosition)
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

    suspend fun getOrdersInProgress(): List<Order> {
        val snapshot = orders
            .whereIn("stage", listOf("QUEUED", "WASHING", "DRYING"))
            .get(Source.SERVER).await()
        return snapshot.toObjects(Order::class.java).sortedBy { it.queuePosition }
    }

    suspend fun updateStage(orderId: String, stage: String, queuePosition: Int) {
        orders.document(orderId).update("stage", stage).await()
        pushStatus(orderId, stage, queuePosition)
    }

    suspend fun swapQueuePositions(
        orderId: String, position: Int, stage: String,
        otherOrderId: String, otherPosition: Int, otherStage: String
    ) {
        orders.document(orderId).update("queuePosition", otherPosition).await()
        orders.document(otherOrderId).update("queuePosition", position).await()
        pushStatus(orderId, stage, otherPosition)
        pushStatus(otherOrderId, otherStage, position)
    }

    private suspend fun pushStatus(orderId: String, stage: String, queuePosition: Int) {
        orderStatus.child(orderId)
            .setValue(mapOf("stage" to stage, "queuePosition" to queuePosition))
            .await()
    }

    /**
     * Live-updates one order's stage/queuePosition via Realtime Database. Caller must remove
     * the returned listener (e.g. in onDestroyView) to avoid leaking it past the view's lifecycle.
     */
    fun observeOrderStatus(orderId: String, onUpdate: (stage: String, queuePosition: Int) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stage = snapshot.child("stage").getValue(String::class.java) ?: return
                val queuePosition = snapshot.child("queuePosition").getValue(Int::class.java) ?: return
                onUpdate(stage, queuePosition)
            }

            override fun onCancelled(error: DatabaseError) {
                // No-op: a denied/cancelled listener simply stops delivering live updates;
                // the screen still shows the last Firestore-fetched snapshot.
            }
        }
        orderStatus.child(orderId).addValueEventListener(listener)
        return listener
    }

    fun stopObserving(orderId: String, listener: ValueEventListener) {
        orderStatus.child(orderId).removeEventListener(listener)
    }
}
