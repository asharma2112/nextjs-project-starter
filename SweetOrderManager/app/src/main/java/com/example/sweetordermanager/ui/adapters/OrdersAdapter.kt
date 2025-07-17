package com.example.sweetordermanager.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sweetordermanager.databinding.ItemOrderBinding
import com.example.sweetordermanager.model.Order
import com.example.sweetordermanager.util.Constants

class OrdersAdapter(
    private val orders: List<Order>,
    private val onConfirmDelivery: (Order) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(private val binding: ItemOrderBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(order: Order) {
            binding.apply {
                customerNameText.text = order.customerName
                dateText.text = order.date
                totalPriceText.text = "Total: ₹${"%.2f".format(order.totalPrice)}"
                
                // Set delivery status
                deliveredStatus.text = if (order.delivered) "Delivered" else "Pending"
                deliveredStatus.setBackgroundColor(
                    if (order.delivered) 
                        root.context.getColor(android.R.color.holo_green_dark) 
                    else 
                        root.context.getColor(android.R.color.holo_orange_dark)
                )
                
                // Show order items
                val itemsText = order.orderItems.joinToString(", ") { 
                    "${it.productName} (${it.quantity})" 
                }
                orderItemsText.text = "Items: $itemsText"
                
                // Handle confirm delivery button
                confirmDeliveryBtn.apply {
                    visibility = if (order.delivered) android.view.View.GONE else android.view.View.VISIBLE
                    setOnClickListener {
                        onConfirmDelivery(order)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size
}
