package com.example.repositories

import com.example.core.database.dao.InventoryDao
import com.example.core.database.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val inventoryDao: InventoryDao) {
    val allItems: Flow<List<InventoryItemEntity>> = inventoryDao.getAllItems()
    val archivedItems: Flow<List<InventoryItemEntity>> = inventoryDao.getArchivedItems()
    val lowStockItems: Flow<List<InventoryItemEntity>> = inventoryDao.getLowStockItems()
    val outOfStockItems: Flow<List<InventoryItemEntity>> = inventoryDao.getOutOfStockItems()
    val totalInventoryValue: Flow<Double?> = inventoryDao.getTotalInventoryValue()
    val itemCount: Flow<Int> = inventoryDao.getItemCount()
    val outOfStockCount: Flow<Int> = inventoryDao.getOutOfStockCount()

    suspend fun getItemById(id: Long): InventoryItemEntity? = inventoryDao.getItemById(id)

    suspend fun insertItem(item: InventoryItemEntity): Long = inventoryDao.insertItem(item)

    suspend fun updateItem(item: InventoryItemEntity) = inventoryDao.updateItem(item)

    suspend fun deleteItem(item: InventoryItemEntity) = inventoryDao.deleteItem(item)

    suspend fun setArchived(id: Long, isArchived: Boolean) = inventoryDao.updateArchiveStatus(id, isArchived)

    suspend fun updateStock(id: Long, newQuantity: Int) = inventoryDao.updateStock(id, newQuantity)

    suspend fun duplicateItem(item: InventoryItemEntity): Long {
        val newItem = item.copy(
            id = 0,
            name = "${item.name} (Copy)",
            sku = if (item.sku.isNotEmpty()) "${item.sku}-COPY" else "",
            createdDate = System.currentTimeMillis(),
            updatedDate = System.currentTimeMillis()
        )
        return inventoryDao.insertItem(newItem)
    }
}
