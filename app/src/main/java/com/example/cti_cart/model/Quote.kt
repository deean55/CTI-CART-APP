package com.example.cti_cart.data.model

data class Quote(
    val id: String = "",
    val rfqId: String = "",
    val buyerId: String = "",
    val supplierId: String = "",
    val supplierName: String = "",
    val partName: String = "",
    val quantity: Double = 0.0,
    val unitPrice: Double = 0.0,
    val totalAmount: Double = 0.0,
    val deliveryTime: String = "",
    val validity: String = "",
    val paymentTerms: String = "",
    val remarks: String = "",
    val status: String = "Pending",
    val createdAt: Long = 0L
)
