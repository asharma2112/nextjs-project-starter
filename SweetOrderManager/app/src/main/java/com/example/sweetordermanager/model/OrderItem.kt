package com.example.sweetordermanager.model

data class OrderItem(
    val productName: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0
) {
    // No-argument constructor for Firebase
    constructor() : this("", 0, 0.0)
    
    // Helper function to calculate total price for this item
    fun getTotalPrice(): Double {
        return quantity * unitPrice
    }
}
