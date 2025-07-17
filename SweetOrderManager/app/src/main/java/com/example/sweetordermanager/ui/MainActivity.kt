package com.example.sweetordermanager.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sweetordermanager.databinding.ActivityMainBinding
import com.example.sweetordermanager.model.Order
import com.example.sweetordermanager.ui.adapters.OrdersAdapter
import com.example.sweetordermanager.util.Constants
import com.example.sweetordermanager.util.FirebaseHelper
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var ordersAdapter: OrdersAdapter
    private val firebaseHelper = FirebaseHelper()
    private var allOrders = mutableListOf<Order>()
    private var filteredOrders = mutableListOf<Order>()
    
    private val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFilters()
        setupListeners()
        loadOrders()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter(filteredOrders) { order ->
            confirmDelivery(order)
        }
        binding.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ordersAdapter
        }
    }

    private fun setupFilters() {
        // Setup product filter spinner
        val productAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Constants.PRODUCT_NAMES
        )
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productFilterSpinner.adapter = productAdapter
    }

    private fun setupListeners() {
        binding.addOrderBtn.setOnClickListener {
            startActivity(Intent(this, AddOrderActivity::class.java))
        }

        binding.dateFilterText.setOnClickListener {
            showDatePicker()
        }

        binding.clearFiltersBtn.setOnClickListener {
            clearFilters()
        }

        binding.productFilterSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                applyFilters()
            }
        })
    }

    private fun loadOrders() {
        firebaseHelper.fetchOrdersWithRealTimeListener(
            onSuccess = { orders ->
                allOrders.clear()
                allOrders.addAll(orders)
                applyFilters()
                updateEmptyState()
            },
            onFailure = { exception ->
                Toast.makeText(
                    this,
                    "Error loading orders: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun applyFilters() {
        filteredOrders.clear()
        
        val selectedProduct = if (binding.productFilterSpinner.selectedItemPosition > 0) {
            binding.productFilterSpinner.selectedItem.toString()
        } else null
        
        val selectedDate = binding.dateFilterText.text.toString()
            .takeIf { it.isNotEmpty() && it != "Select Date" }

        filteredOrders.addAll(allOrders.filter { order ->
            val matchesProduct = selectedProduct == null || 
                order.orderItems.any { it.productName == selectedProduct }
            
            val matchesDate = selectedDate == null || 
                order.date == selectedDate
            
            matchesProduct && matchesDate
        })
        
        ordersAdapter.notifyDataSetChanged()
        updateSummary()
    }

    private fun clearFilters() {
        binding.productFilterSpinner.setSelection(0)
        binding.dateFilterText.text = "Select Date"
        applyFilters()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                binding.dateFilterText.text = dateFormat.format(selectedDate)
                applyFilters()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun confirmDelivery(order: Order) {
        firebaseHelper.updateOrderStatus(
            order.id,
            true,
            onSuccess = {
                Toast.makeText(this, "Delivery confirmed", Toast.LENGTH_SHORT).show()
                loadOrders()
            },
            onFailure = { exception ->
                Toast.makeText(
                    this,
                    "Failed to confirm delivery: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun updateSummary() {
        val grandTotal = filteredOrders.sumOf { it.totalPrice }
        binding.grandTotalText.text = "Grand Total: ₹${"%.2f".format(grandTotal)}"

        // Calculate product-wise sales
        val productSales = mutableMapOf<String, Double>()
        filteredOrders.forEach { order ->
            order.orderItems.forEach { item ->
                val current = productSales.getOrDefault(item.productName, 0.0)
                productSales[item.productName] = current + item.getTotalPrice()
            }
        }

        val summaryText = if (productSales.isEmpty()) {
            "No sales data"
        } else {
            productSales.entries.joinToString("\n") { (product, total) ->
                "$product: ₹${"%.2f".format(total)}"
            }
        }
        binding.productSummaryText.text = summaryText
    }

    private fun updateEmptyState() {
        if (filteredOrders.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.ordersRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.ordersRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }
}
