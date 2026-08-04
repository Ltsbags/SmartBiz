package com.example.repositories

import com.example.core.database.dao.PurchaseDao
import com.example.core.database.dao.SupplierDao
import com.example.core.database.entity.PurchaseWithItems
import com.example.core.database.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

class SupplierRepository(
    private val supplierDao: SupplierDao,
    private val purchaseDao: PurchaseDao
) {
    val activeSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllActiveSuppliers()
    val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()
    val supplierCount: Flow<Int> = supplierDao.getSupplierCount()

    suspend fun getSupplierById(id: Long): SupplierEntity? {
        return supplierDao.getSupplierById(id)
    }

    suspend fun saveSupplier(supplier: SupplierEntity): Long {
        return if (supplier.id > 0) {
            supplierDao.updateSupplier(supplier.copy(updatedDate = System.currentTimeMillis()))
            supplier.id
        } else {
            val code = if (supplier.supplierCode.isBlank()) {
                val count = supplierDao.getRawSupplierCount() + 1
                "SUP-${1000 + count}"
            } else {
                supplier.supplierCode
            }
            supplierDao.insertSupplier(supplier.copy(supplierCode = code))
        }
    }

    suspend fun deleteSupplier(supplier: SupplierEntity) {
        supplierDao.deleteSupplier(supplier)
    }

    suspend fun setArchiveStatus(supplierId: Long, isArchived: Boolean) {
        supplierDao.setArchivedStatus(supplierId, isArchived)
    }

    fun getPurchasesBySupplier(supplierId: Long): Flow<List<PurchaseWithItems>> {
        return purchaseDao.getPurchasesBySupplierWithItems(supplierId)
    }
}
