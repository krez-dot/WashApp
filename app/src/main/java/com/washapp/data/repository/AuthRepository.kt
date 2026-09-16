package com.washapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.washapp.data.model.Role
import com.washapp.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun register(name: String, email: String, password: String, role: Role): User {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid.orEmpty()
        val user = User(uid = uid, name = name, email = email, role = role)
        firestore.collection(FirestorePaths.USERS).document(uid).set(user).await()
        return user
    }

    suspend fun login(email: String, password: String): User {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid.orEmpty()
        val snapshot = firestore.collection(FirestorePaths.USERS).document(uid).get().await()
        return snapshot.toObject(User::class.java) ?: User(uid = uid, email = email)
    }

    suspend fun currentRole(): Role {
        val uid = auth.currentUser?.uid ?: return Role.CUSTOMER
        val snapshot = firestore.collection(FirestorePaths.USERS).document(uid).get().await()
        return snapshot.toObject(User::class.java)?.role ?: Role.CUSTOMER
    }

    fun logout() = auth.signOut()

    fun currentUserId(): String? = auth.currentUser?.uid
}
