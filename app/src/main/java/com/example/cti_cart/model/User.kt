package com.example.cti_cart.model

data class User(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val companyName: String = "",
    val address: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val role: String = "customer"
)