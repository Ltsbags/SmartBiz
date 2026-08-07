package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE isArchived = 1 ORDER BY name ASC")
    fun getArchivedItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: Long): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE stockQuantity <= minStockThreshold AND isArchived = 0")
    fun getLowStockItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE stockQuantity <= 0 AND isArchived = 0")
    fun getOutOfStockItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT COUNT(*) FROM inventory_items WHERE isArchived = 0")
    fun getItemCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM inventory_items WHERE stockQuantity <= 0 AND isArchived = 0")
    fun getOutOfStockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM inventory_items WHERE stockQuantity <= minStockThreshold AND isArchived = 0")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT SUM(stockQuantity * unitPrice) FROM inventory_items WHERE isArchived = 0")
    fun getTotalInventoryValue(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)

    @Query("UPDATE inventory_items SET isArchived = :isArchived, updatedDate = :updatedDate WHERE id = :id")
    suspend fun updateArchiveStatus(id: Long, isArchived: Boolean, updatedDate: Long = System.currentTimeMillis())

    @Query("UPDATE inventory_items SET stockQuantity = :newStock, updatedDate = :updatedDate WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int, updatedDate: Long = System.currentTimeMillis())

    @Query("UPDATE inventory_items SET stockQuantity = stockQuantity + :quantity, updatedDate = :updatedDate WHERE id = :id")
    suspend fun increaseStock(id: Long, quantity: Int, updatedDate: Long = System.currentTimeMillis())

    @Query("UPDATE inventory_items SET stockQuantity = CASE WHEN stockQuantity - :quantity < 0 THEN 0 ELSE stockQuantity - :quantity END, updatedDate = :updatedDate WHERE id = :id")
    suspend fun decreaseStock(id: Long, quantity: Int, updatedDate: Long = System.currentTimeMillis())

    @Query("UPDATE inventory_items SET purchasePrice = :price, costPrice = :price, updatedDate = :updatedDate WHERE id = :id")
    suspend fun updatePurchasePrice(id: Long, price: Double, updatedDate: Long = System.currentTimeMillis())

    @Query("DELETE FROM inventory_items")
    suspend fun deleteAll()
}
