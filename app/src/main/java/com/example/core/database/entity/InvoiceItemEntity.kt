package com.example.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("invoiceId"), Index("productId")]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "invoiceId")
    val invoiceId: Long = 0,
    val productId: Long = 0,
    val productName: String,
    val sku: String = "",
    val quantity: Double = 1.0,
    val unit: String = "pcs",
    val sellingPrice: Double = 0.0,
    val discount: Double = 0.0,
    val gstPercentage: Double = 0.0,
    val taxAmount: Double = 0.0,
    val lineTotal: Double = 0.0
)
