package com.example.repositories

import com.example.core.database.dao.CustomerDao
import com.example.core.database.dao.CustomerLedgerDao
import com.example.core.database.entity.CustomerEntity
import com.example.core.database.entity.CustomerLedgerEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class CustomerRepository(
    private val customerDao: CustomerDao,
    private val customerLedgerDao: CustomerLedgerDao
) {
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val activeCustomers: Flow<List<CustomerEntity>> = customerDao.getActiveCustomers()
    val archivedCustomers: Flow<List<CustomerEntity>> = customerDao.getArchivedCustomers()
    val customerCount: Flow<Int> = customerDao.getCustomerCount()
    val activeCustomerCount: Flow<Int> = customerDao.getActiveCustomerCount()
    val totalOutstandingBalance: Flow<Double?> = customerDao.getTotalOutstandingBalance()

    fun getNewCustomersThisMonthCount(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return customerDao.getNewCustomersThisMonthCount(calendar.timeInMillis)
    }

    suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)

    suspend fun generateCustomerCode(): String {
        val nextNumber = (customerDao.getCustomerById(0)?.id ?: 0) + System.currentTimeMillis() % 10000
        return "CUST-${(1000 + (nextNumber % 8999)).toInt()}"
    }

    suspend fun insertCustomer(customer: CustomerEntity): Long {
        val code = if (customer.customerCode.isBlank()) generateCustomerCode() else customer.customerCode
        val createdCust = customer.copy(
            customerCode = code,
            outstandingBalance = if (customer.id == 0L) customer.openingBalance else customer.outstandingBalance
        )
        val newId = customerDao.insertCustomer(createdCust)

        // Seed initial opening balance ledger record if openingBalance > 0
        if (customer.id == 0L && customer.openingBalance > 0) {
            customerLedgerDao.insertLedger(
                CustomerLedgerEntity(
                    customerId = newId,
                    transactionType = "OPENING_BALANCE",
                    referenceNumber = "OB-$newId",
                    amount = customer.openingBalance,
                    balanceAfter = customer.openingBalance,
                    description = "Opening balance initial record",
                    transactionDate = System.currentTimeMillis()
                )
            )
        }
        return newId
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        val updatedCust = customer.copy(updatedDate = System.currentTimeMillis())
        customerDao.updateCustomer(updatedCust)
    }

    suspend fun toggleArchiveStatus(customer: CustomerEntity) {
        val updated = customer.copy(
            isArchived = !customer.isArchived,
            updatedDate = System.currentTimeMillis()
        )
        customerDao.updateCustomer(updated)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerLedgerDao.deleteLedgersForCustomer(customer.id)
        customerDao.deleteCustomer(customer)
    }

    fun getLedgerForCustomer(customerId: Long): Flow<List<CustomerLedgerEntity>> {
        return customerLedgerDao.getLedgersForCustomer(customerId)
    }

    suspend fun addLedgerEntry(entry: CustomerLedgerEntity): Long {
        return customerLedgerDao.insertLedger(entry)
    }
}
