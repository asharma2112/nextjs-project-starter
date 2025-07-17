package com.example.sweetordermanager.util

import com.example.sweetordermanager.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirebaseHelper {
    private val db = FirebaseFirestore.getInstance()
    
    fun addOrder(
        order: Order,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(Constants.ORDERS_COLLECTION)
            .add(order)
            .addOnSuccessListener { documentReference ->
                // Update the order with the generated ID
                val updatedOrder = order.copy(id = documentReference.id)
                documentReference.set(updatedOrder)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { exception -> onFailure(exception) }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    
    fun fetchOrders(
        onSuccess: (List<Order>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(Constants.ORDERS_COLLECTION)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val orders = mutableListOf<Order>()
                for (document in documents) {
                    try {
                        val order = document.toObject(Order::class.java)
                        order.id = document.id
                        orders.add(order)
                    } catch (e: Exception) {
                        // Skip malformed documents
                        continue
                    }
                }
                onSuccess(orders)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    
    fun updateOrderStatus(
        orderId: String,
        delivered: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(Constants.ORDERS_COLLECTION)
            .document(orderId)
            .update("delivered", delivered)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
    
    fun fetchOrdersWithRealTimeListener(
        onSuccess: (List<Order>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(Constants.ORDERS_COLLECTION)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    onFailure(exception)
                    return@addSnapshotListener
                }
                
                val orders = mutableListOf<Order>()
                snapshots?.let { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        try {
                            val order = document.toObject(Order::class.java)
                            order?.let {
                                it.id = document.id
                                orders.add(it)
                            }
                        } catch (e: Exception) {
                            // Skip malformed documents
                            continue
                        }
                    }
                }
                onSuccess(orders)
            }
    }
}
