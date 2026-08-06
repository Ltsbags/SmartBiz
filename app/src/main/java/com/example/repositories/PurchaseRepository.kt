package com.example.repositories

import com.example.core.database.dao.InventoryDao
import com.example.core.database.dao.PurchaseDao
import com.example.core.database.dao.SupplierDao
import com.example.core.database.entity.PurchaseEntity
import com.example.core.database.entity.PurchaseItemEntity
import com.example.core.database.entity.PurchaseWithItems
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PurchaseRepository(
    private val purchaseDao: PurchaseDao,
    private val inventoryDao: InventoryDao,
    private val supplierDao: SupplierDao
) {
    val allPurchasesWithItems: Flow<List<PurchaseWithItems>> = purchaseDao.getAllPurchasesWithItems()
    val totalPurchasesAmount: Flow<Double?> = purchaseDao.getTotalPurchasesAmount()

    suspend fun getPurchaseById(id: Long): PurchaseWithItems? {
        return purchaseDao.getPurchaseWithItemsById(id)
    }

    suspend fun savePurchase(
        purchase: PurchaseEntity,
        items: List<PurchaseItemEntity>
    ): Long {
        val existing = if (purchase.id > 0) purchaseDao.getPurchaseWithItemsById(purchase.id) else null
        val now = System.currentTimeMillis()

        val finalPurchaseNumber = if (purchase.purchaseNumber.isBlank()) {
            generatePurchaseNumber()
        } else {
            purchase.purchaseNumber
        }

        val updatedPurchase = purchase.copy(
            purchaseNumber = finalPurchaseNumber,
            itemsCount = items.size,
            updatedDate = now
        )

        val purchaseId = if (updatedPurchase.id > 0) {
            purchaseDao.updatePurchase(updatedPurchase)
            purchaseDao.deletePurchaseItemsByPurchaseId(updatedPurchase.id)
            updatedPurchase.id
        } else {
            purchaseDao.insertPurchase(updatedPurchase)
        }

        val itemsWithId = items.map { it.copy(purchaseId = purchaseId) }
        purchaseDao.insertPurchaseItems(itemsWithId)

        val prevStatus = existing?.purchase?.status ?: "DRAFT"
        val newStatus = updatedPurchase.status

        // AUTOMATIC STOCK INWARD INTEGRATION
        if (prevStatus != "RECEIVED" && newStatus == "RECEIVED") {
            // Inward stock into inventory
            itemsWithId.forEach { item ->
                inventoryDao.increaseStock(item.productId, item.quantity.toInt(), now)
                if (item.purchasePrice > 0) {
                    inventoryDao.updatePurchasePrice(item.productId, item.purchasePrice, now)
                }
            }
            // Update supplier balance if balanceAmount > 0
            if (updatedPurchase.supplierId > 0 && updatedPurchase.balanceAmount > 0) {
                supplierDao.updateOutstandingBalance(updatedPurchase.supplierId, updatedPurchase.balanceAmount, now)
            }
        } else if (prevStatus == "RECEIVED" && newStatus == "CANCELLED") {
            // Reverse stock inward
            itemsWithId.forEach { item ->
                inventoryDao.decreaseStock(item.productId, item.quantity.toInt(), now)
            }
            // Reverse supplier balance
            if (updatedPurchase.supplierId > 0 && updatedPurchase.balanceAmount > 0) {
                supplierDao.updateOutstandingBalance(updatedPurchase.supplierId, -updatedPurchase.balanceAmount, now)
            }
        }

        return purchaseId
    }

    suspend fun updatePurchaseStatus(purchaseId: Long, newStatus: String) {
        val pWithItems = purchaseDao.getPurchaseWithItemsById(purchaseId) ?: return
        val current = pWithItems.purchase
        if (current.status == newStatus) return

        val updated = current.copy(status = newStatus, updatedDate = System.currentTimeMillis())
        savePurchase(updated, pWithItems.items)
    }

    suspend fun deletePurchase(purchase: PurchaseEntity) {
        val pWithItems = purchaseDao.getPurchaseWithItemsById(purchase.id)
        if (pWithItems != null && pWithItems.purchase.status == "RECEIVED") {
            // Reverse stock if deleted while received
            pWithItems.items.forEach { item ->
                inventoryDao.decreaseStock(item.productId, item.quantity.toInt())
            }
            if (pWithItems.purchase.supplierId > 0 && pWithItems.purchase.balanceAmount > 0) {
                supplierDao.updateOutstandingBalance(pWithItems.purchase.supplierId, -pWithItems.purchase.balanceAmount)
            }
        }
        purchaseDao.deletePurchaseItemsByPurchaseId(purchase.id)
        purchaseDao.deletePurchase(purchase)
    }

    private suspend fun generatePurchaseNumber(): String {
        val count = purchaseDao.getPurchaseCount() + 1
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        return "PO-$year-${String.format("%03d", count)}"
    }
}
