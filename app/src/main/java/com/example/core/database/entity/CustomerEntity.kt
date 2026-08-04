package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerCode: String = "",
    val name: String,
    val company: String = "",
    val phone: String,
    val alternateNumber: String = "",
    val email: String = "",
    val gstNumber: String = "",
    val panNumber: String = "",
    val billingAddress: String = "",
    val shippingAddress: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "India",
    val pincode: String = "",
    val customerType: String = "Retail", // Retail, Wholesale, Distributor, Corporate, Other
    val openingBalance: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val creditLimit: Double = 0.0,
    val paymentTermsDays: Int = 30,
    val notes: String = "",
    val tags: String = "",
    val isArchived: Boolean = false,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)
