package com.washapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.washapp.data.model.Machine
import kotlinx.coroutines.tasks.await

class MachineRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val machines get() = firestore.collection(FirestorePaths.MACHINES)

    suspend fun getAvailableMachines(): List<Machine> {
        val snapshot = machines.whereEqualTo("isAvailable", true).get(Source.SERVER).await()
        return snapshot.toObjects(Machine::class.java)
    }

    suspend fun getAllMachines(): List<Machine> {
        val snapshot = machines.get(Source.SERVER).await()
        return snapshot.toObjects(Machine::class.java)
    }

    suspend fun addMachine(label: String): String {
        val docRef = machines.document()
        machines.document(docRef.id).set(
            Machine(machineId = docRef.id, label = label, isAvailable = true)
        ).await()
        return docRef.id
    }

    suspend fun setAvailability(machineId: String, isAvailable: Boolean) {
        machines.document(machineId).update("isAvailable", isAvailable).await()
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

    suspend fun deleteMachine(machineId: String) {
        machines.document(machineId).delete().await()
    }
}
