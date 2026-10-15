package com.washapp.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.washapp.data.model.Order
import com.washapp.data.model.ServiceStage
import com.washapp.data.scheduling.QueueOptimizer
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

    suspend fun getOrder(orderId: String): Order? =
        orders.document(orderId).get(Source.SERVER).await().toObject(Order::class.java)

    /**
     * Moves to FOLDING, frees the machine, and records the owner's folding price and time:
     * the price joins the order total and the time replaces the default fold estimate.
     */
    suspend fun startFolding(order: Order, foldingPrice: Double, foldMinutes: Int): Order {
        val updated = order.copy(
            stage = ServiceStage.FOLDING,
            assignedMachineId = "",
            foldingPrice = foldingPrice,
            cost = order.cost - order.foldingPrice + foldingPrice,
            estimatedFoldMinutes = foldMinutes,
            estimatedCompletionMinutes =
                order.estimatedCompletionMinutes - order.estimatedFoldMinutes + foldMinutes
        )
        orders.document(order.orderId).update(
            mapOf(
                "stage" to updated.stage.name,
                "assignedMachineId" to "",
                "foldingPrice" to updated.foldingPrice,
                "cost" to updated.cost,
                "estimatedFoldMinutes" to updated.estimatedFoldMinutes,
                "estimatedCompletionMinutes" to updated.estimatedCompletionMinutes
            )
        ).await()
        pushStatus(order.orderId, updated.stage.name, order.queuePosition)
        return updated
    }

    suspend fun getAllOrders(): List<Order> {
        val snapshot = orders.get(Source.SERVER).await()
        return snapshot.toObjects(Order::class.java)
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

    suspend fun updateStage(
        orderId: String, stage: String, queuePosition: Int,
        assignedMachineId: String? = null
    ) {
        val updates = mutableMapOf<String, Any>("stage" to stage)
        if (assignedMachineId != null) {
            updates["assignedMachineId"] = assignedMachineId
        }
        orders.document(orderId).update(updates).await()
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

    /**
     * Flips an order's rush flag and renumbers the whole active queue so positions stay
     * dense and priority-first. Granting priority also records the owner's additional fee in the
     * order total; removing it takes the fee back out. Returns the reordered queue.
     */
    suspend fun setPriority(orderId: String, priority: Boolean, fee: Double = 0.0): List<Order> {
        val target = getActiveQueue().first { it.orderId == orderId }
        val newFee = if (priority) fee else 0.0
        val newCost = target.cost - target.priorityPrice + newFee
        orders.document(orderId).update(
            mapOf("priority" to priority, "priorityPrice" to newFee, "cost" to newCost)
        ).await()

        val updated = getActiveQueue().map {
            if (it.orderId == orderId) it.copy(priority = priority, priorityPrice = newFee, cost = newCost) else it
        }
        val renumbered = QueueOptimizer.renumber(updated)

        val batch = firestore.batch()
        renumbered.forEach { batch.update(orders.document(it.orderId), "queuePosition", it.queuePosition) }
        batch.commit().await()

        renumbered.forEach { pushStatus(it.orderId, it.stage.name, it.queuePosition) }
        return renumbered
    }

    suspend fun cancelOrder(orderId: String) {
        orders.document(orderId).delete().await()
        orderStatus.child(orderId).removeValue().await()
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
