package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supplierCode: String,
    val supplierName: String,
    val businessName: String = "",
    val phone: String,
    val alternateNumber: String = "",
    val email: String = "",
    val gstNumber: String = "",
    val panNumber: String = "",
    val billingAddress: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "United States",
    val pincode: String = "",
    val openingBalance: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val paymentTerms: String = "Net 30",
    val notes: String = "",
    val status: String = "ACTIVE", // ACTIVE, INACTIVE
    val isArchived: Boolean = false,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)
