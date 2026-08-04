package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    val category: String = "General",
    val brand: String = "",
    val description: String = "",
    val unit: String = "pcs",
    val purchasePrice: Double = 0.0,
    val costPrice: Double = 0.0,
    val unitPrice: Double = 0.0, // Selling Price
    val gstPercentage: Double = 0.0,
    val openingStock: Int = 0,
    val stockQuantity: Int = 0, // Current Stock
    val minStockThreshold: Int = 5,
    val maxStock: Int = 100,
    val location: String = "",
    val imagePath: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val isArchived: Boolean = false
)
