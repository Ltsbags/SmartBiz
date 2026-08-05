package com.example.services

import com.example.core.database.entity.TaskCenterEntity
import com.example.repositories.CustomerRepository
import com.example.repositories.ExpenseRepository
import com.example.repositories.InventoryRepository
import com.example.repositories.InvoiceRepository
import com.example.repositories.PurchaseRepository
import com.example.repositories.SupplierRepository
import com.example.repositories.TaskCenterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TaskEngineService(
    private val taskRepository: TaskCenterRepository,
    private val inventoryRepository: InventoryRepository,
    private val customerRepository: CustomerRepository,
    private val supplierRepository: SupplierRepository,
    private val invoiceRepository: InvoiceRepository,
    private val purchaseRepository: PurchaseRepository,
    private val expenseRepository: ExpenseRepository
) {

    val pendingTasksFlow: Flow<List<TaskCenterEntity>> = taskRepository.pendingTasksFlow
    val pendingTaskCountFlow: Flow<Int> = taskRepository.pendingTaskCountFlow
    val allTasksFlow: Flow<List<TaskCenterEntity>> = taskRepository.allTasksFlow

    suspend fun evaluateAndGenerateTasks() {
        // 1. Low Stock Task
        try {
            val lowStock = inventoryRepository.lowStockItems.first()
            if (lowStock.isNotEmpty()) {
                val existing = taskRepository.getPendingTaskByType("LOW_STOCK")
                if (existing == null) {
                    taskRepository.addTask(
                        TaskCenterEntity(
                            title = "Low Stock Reorder Alert",
                            description = "${lowStock.size} items are below minimum reorder thresholds. Reorder now.",
                            taskType = "LOW_STOCK",
                            severity = "HIGH",
                            priority = "HIGH",
                            actionUrl = "inventory"
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // 2. Outstanding Collection Task
        try {
            val outstanding = customerRepository.totalOutstandingBalance.first() ?: 0.0
            if (outstanding > 0) {
                val existing = taskRepository.getPendingTaskByType("OUTSTANDING_COLLECTION")
                if (existing == null) {
                    taskRepository.addTask(
                        TaskCenterEntity(
                            title = "Collect Outstanding Receivables",
                            description = "Total pending collection balance is ₹${String.format("%.2f", outstanding)}. Follow up with customers.",
                            taskType = "OUTSTANDING_COLLECTION",
                            severity = "MEDIUM",
                            priority = "NORMAL",
                            actionUrl = "customers"
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // 3. Database & Encrypted Backup Task
        try {
            val existing = taskRepository.getPendingTaskByType("BACKUP_REQUIRED")
            if (existing == null) {
                taskRepository.addTask(
                    TaskCenterEntity(
                        title = "Routine Encrypted Backup Required",
                        description = "Perform a local encrypted database backup to prevent data loss.",
                        taskType = "BACKUP_REQUIRED",
                        severity = "MEDIUM",
                        priority = "HIGH",
                        actionUrl = "settings/backup"
                    )
                )
            }
        } catch (_: Exception) {}

        // 4. Database Optimization Check
        try {
            val existing = taskRepository.getPendingTaskByType("DB_OPTIMIZATION")
            if (existing == null) {
                taskRepository.addTask(
                    TaskCenterEntity(
                        title = "Database Index Optimization",
                        description = "Run maintenance optimize command to maintain top query speeds.",
                        taskType = "DB_OPTIMIZATION",
                        severity = "LOW",
                        priority = "LOW",
                        actionUrl = "settings/database"
                    )
                )
            }
        } catch (_: Exception) {}
    }

    suspend fun completeTask(taskId: String) {
        taskRepository.completeTask(taskId)
    }

    suspend fun deleteTask(taskId: String) {
        taskRepository.deleteTask(taskId)
    }

    suspend fun clearCompleted() {
        taskRepository.clearCompletedTasks()
    }
}
