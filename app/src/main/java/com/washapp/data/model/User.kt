package com.washapp.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: Role = Role.CUSTOMER
)

enum class Role {
    CUSTOMER,
    ADMINISTRATOR
}
