package com.example.cti_cart.data.model

data class RFQ(
    val id: String = "",
    val userId: String = "",

    val partName: String = "",
    val quantity: String = "",
    val machine: String = "",
    val requiredBy: String = "",
    val fileUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    val interestedSuppliers: List<String> = emptyList(),

    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Open"
)