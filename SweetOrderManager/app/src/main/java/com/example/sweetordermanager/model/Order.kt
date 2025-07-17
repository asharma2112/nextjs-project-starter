package com.example.sweetordermanager.model

data class Order(
    var id: String = "",
    val customerName: String = "",
    val date: String = "",
    val orderItems: List<OrderItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val delivered: Boolean = false
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", emptyList(), 0.0, false)
    
    // Helper function to calculate total price from order items
    fun calculateTotalPrice(): Double {
        return orderItems.sumOf { it.getTotalPrice() }
    }
}
