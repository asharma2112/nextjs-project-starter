package com.example.sweetordermanager.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sweetordermanager.databinding.ActivityAddOrderBinding
import com.example.sweetordermanager.databinding.ItemOrderItemBinding
import com.example.sweetordermanager.model.Order
import com.example.sweetordermanager.model.OrderItem
import com.example.sweetordermanager.util.Constants
import com.example.sweetordermanager.util.FirebaseHelper
import java.text.SimpleDateFormat
import java.util.*

class AddOrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddOrderBinding
    private val firebaseHelper = FirebaseHelper()
    private val orderItems = mutableListOf<OrderItem>()
    private val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
    private val itemViews = mutableListOf<ItemOrderItemBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        addOrderItem() // Add first item by default
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        binding.dateEditText.setOnClickListener {
            showDatePicker()
        }

        binding.addItemBtn.setOnClickListener {
            addOrderItem()
        }

        binding.saveOrderBtn.setOnClickListener {
            saveOrder()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                binding.dateEditText.setText(dateFormat.format(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun addOrderItem() {
        val itemBinding = ItemOrderItemBinding.inflate(
            LayoutInflater.from(this),
            binding.orderItemsContainer,
            false
        )

        // Setup product spinner
        val productAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Constants.PRODUCT_NAMES
        )
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        itemBinding.productSpinner.adapter = productAdapter

        // Setup listeners
        itemBinding.productSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position > 0) {
                    val productName = Constants.PRODUCT_NAMES[position]
                    val unitPrice = Constants.PRODUCT_PRICES[productName] ?: 0.0
                    itemBinding.unitPriceEditText.setText(unitPrice.toString())
                    updateItemTotal(itemBinding)
                } else {
                    itemBinding.unitPriceEditText.setText("")
                    itemBinding.itemTotalText.text = "Total: ₹0"
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                itemBinding.unitPriceEditText.setText("")
                itemBinding.itemTotalText.text = "Total: ₹0"
            }
        })

        itemBinding.quantityEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateItemTotal(itemBinding)
            }
        })

        itemBinding.removeBtn.setOnClickListener {
            removeOrderItem(itemBinding)
        }

        binding.orderItemsContainer.addView(itemBinding.root)
        itemViews.add(itemBinding)
    }

    private fun removeOrderItem(itemBinding: ItemOrderItemBinding) {
        val index = itemViews.indexOf(itemBinding)
        if (index != -1) {
            binding.orderItemsContainer.removeView(itemBinding.root)
            itemViews.removeAt(index)
            updateTotalPrice()
        }
    }

    private fun updateItemTotal(itemBinding: ItemOrderItemBinding) {
        val quantityText = itemBinding.quantityEditText.text.toString()
        val unitPriceText = itemBinding.unitPriceEditText.text.toString()

        if (quantityText.isNotEmpty() && unitPriceText.isNotEmpty()) {
            try {
                val quantity = quantityText.toInt()
                val unitPrice = unitPriceText.toDouble()
                val total = quantity * unitPrice
                itemBinding.itemTotalText.text = "Total: ₹${"%.2f".format(total)}"
                updateTotalPrice()
            } catch (e: NumberFormatException) {
                itemBinding.itemTotalText.text = "Total: ₹0"
            }
        } else {
            itemBinding.itemTotalText.text = "Total: ₹0"
        }
    }

    private fun updateTotalPrice() {
        var total = 0.0
        for (itemBinding in itemViews) {
            val quantityText = itemBinding.quantityEditText.text.toString()
            val unitPriceText = itemBinding.unitPriceEditText.text.toString()
            
            if (quantityText.isNotEmpty() && unitPriceText.isNotEmpty()) {
                try {
                    val quantity = quantityText.toInt()
                    val unitPrice = unitPriceText.toDouble()
                    total += quantity * unitPrice
                } catch (e: NumberFormatException) {
                    // Skip invalid values
                }
            }
        }
        binding.totalPriceText.text = "Total: ₹${"%.2f".format(total)}"
    }

    private fun saveOrder() {
        val customerName = binding.customerNameEditText.text.toString().trim()
        val date = binding.dateEditText.text.toString().trim()

        // Validate inputs
        if (customerName.isEmpty()) {
            binding.customerNameLayout.error = "Please enter customer name"
            return
        }

        if (date.isEmpty()) {
            binding.dateLayout.error = "Please select order date"
            return
        }

        if (itemViews.isEmpty()) {
            Toast.makeText(this, "Please add at least one item", Toast.LENGTH_SHORT).show()
            return
        }

        // Collect order items
        val orderItems = mutableListOf<OrderItem>()
        for (itemBinding in itemViews) {
            val productPosition = itemBinding.productSpinner.selectedItemPosition
            if (productPosition <= 0) {
                Toast.makeText(this, "Please select a product for all items", Toast.LENGTH_SHORT).show()
                return
            }

            val productName = Constants.PRODUCT_NAMES[productPosition]
            val quantityText = itemBinding.quantityEditText.text.toString()
            val unitPriceText = itemBinding.unitPriceEditText.text.toString()

            if (quantityText.isEmpty()) {
                Toast.makeText(this, "Please enter quantity for all items", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                val quantity = quantityText.toInt()
                val unitPrice = unitPriceText.toDouble()
                
                if (quantity <= 0) {
                    Toast.makeText(this, "Please enter valid quantity", Toast.LENGTH_SHORT).show()
                    return
                }

                orderItems.add(OrderItem(productName, quantity, unitPrice))
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Calculate total price
        val totalPrice = orderItems.sumOf { it.getTotalPrice() }

        // Create order
        val order = Order(
            customerName = customerName,
            date = date,
            orderItems = orderItems,
            totalPrice = totalPrice,
            delivered = false
        )

        // Save to Firebase
        firebaseHelper.addOrder(
            order,
            onSuccess = {
                Toast.makeText(this, "Order saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            },
            onFailure = { exception ->
                Toast.makeText(
                    this,
                    "Failed to save order: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}
