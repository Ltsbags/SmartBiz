package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_items")
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val purchaseId: Long = 0,
    val productId: Long,
    val productName: String,
    val sku: String = "",
    val quantity: Double = 1.0,
    val unit: String = "pcs",
    val purchasePrice: Double = 0.0,
    val taxPercentage: Double = 0.0,
    val lineTotal: Double = 0.0
)
