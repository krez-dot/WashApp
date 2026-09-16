package com.washapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.washapp.data.model.Machine
import kotlinx.coroutines.tasks.await

class MachineRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val machines get() = firestore.collection(FirestorePaths.MACHINES)

    suspend fun getAvailableMachines(): List<Machine> {
        val snapshot = machines.whereEqualTo("isAvailable", true).get().await()
        return snapshot.toObjects(Machine::class.java)
    }

    suspend fun assignMachine(machineId: String, orderId: String) {
        machines.document(machineId).update(
            mapOf(
                "isAvailable" to false,
                "currentOrderId" to orderId
            )
        ).await()
    }

    suspend fun releaseMachine(machineId: String) {
        machines.document(machineId).update(
            mapOf(
                "isAvailable" to true,
                "currentOrderId" to null
            )
        ).await()
    }
}
