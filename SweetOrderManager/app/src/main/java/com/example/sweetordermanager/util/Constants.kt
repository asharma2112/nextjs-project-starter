package com.example.sweetordermanager.util

object Constants {
    // Product Prices
    val PRODUCT_PRICES = mapOf(
        "Bundi" to 400.0,
        "Khaja" to 400.0,
        "Mohanthad" to 450.0,
        "Champakali Ganthiya" to 250.0,
        "Mathadi" to 350.0
    )
    
    // Product names list for spinner
    val PRODUCT_NAMES = listOf(
        "Select Product",
        "Bundi",
        "Khaja", 
        "Mohanthad",
        "Champakali Ganthiya",
        "Mathadi"
    )
    
    // Firestore Collection Name
    const val ORDERS_COLLECTION = "orders"
    
    // Date format
    const val DATE_FORMAT = "dd/MM/yyyy"
}
