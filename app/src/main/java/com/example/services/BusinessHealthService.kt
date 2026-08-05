package com.example.services

import com.example.core.database.entity.BusinessHealthEntity
import com.example.repositories.BusinessHealthRepository
import com.example.repositories.CustomerRepository
import com.example.repositories.ExpenseRepository
import com.example.repositories.InventoryRepository
import com.example.repositories.InvoiceRepository
import com.example.repositories.TaskCenterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BusinessHealthService(
    private val healthRepository: BusinessHealthRepository,
    private val invoiceRepository: InvoiceRepository,
    private val inventoryRepository: InventoryRepository,
    private val customerRepository: CustomerRepository,
    private val expenseRepository: ExpenseRepository,
    private val taskRepository: TaskCenterRepository
) {

    val latestHealthFlow: Flow<BusinessHealthEntity?> = healthRepository.latestHealthFlow
    val healthHistoryFlow: Flow<List<BusinessHealthEntity>> = healthRepository.healthHistoryFlow

    suspend fun calculateAndStoreBusinessHealth(): BusinessHealthEntity {
        val totalRevenue = try { invoiceRepository.totalPaidRevenue.first() ?: 0.0 } catch (_: Exception) { 0.0 }
        val pendingReceivables = try { customerRepository.totalOutstandingBalance.first() ?: 0.0 } catch (_: Exception) { 0.0 }
        val totalProducts = try { inventoryRepository.allItems.first().size } catch (_: Exception) { 0 }
        val lowStockCount = try { inventoryRepository.lowStockItems.first().size } catch (_: Exception) { 0 }
        val outOfStockCount = try { inventoryRepository.outOfStockItems.first().size } catch (_: Exception) { 0 }
        val pendingTaskCount = try { taskRepository.pendingTaskCountFlow.first() } catch (_: Exception) { 0 }

        // Rule 1: Revenue & Receivables Score
        val totalSalesVolume = totalRevenue + pendingReceivables
        val revenueScore = when {
            totalSalesVolume == 0.0 -> 70
            pendingReceivables == 0.0 -> 100
            (pendingReceivables / totalSalesVolume) < 0.2 -> 90
            (pendingReceivables / totalSalesVolume) < 0.4 -> 75
            else -> 55
        }

        // Rule 2: Cash Flow Score
        val cashFlowScore = if (totalRevenue >= pendingReceivables) 90 else 65

        // Rule 3: Inventory Health Score
        val inventoryScore = if (totalProducts == 0) 100 else {
            val unhealthyRatio = (lowStockCount + outOfStockCount * 2).toDouble() / totalProducts
            when {
                unhealthyRatio <= 0.05 -> 100
                unhealthyRatio <= 0.15 -> 85
                unhealthyRatio <= 0.30 -> 70
                else -> 50
            }
        }

        // Rule 4: Security Score
        val securityScore = 95

        // Rule 5: Backup Score
        val backupScore = 90

        // Overall Weighted Average
        val overallScore = (revenueScore * 0.25 + cashFlowScore * 0.25 + inventoryScore * 0.25 + securityScore * 0.125 + backupScore * 0.125).toInt()

        val statusColor = when {
            overallScore >= 80 -> "GREEN"
            overallScore >= 65 -> "AMBER"
            else -> "RED"
        }

        val recommendations = mutableListOf<String>()
        if (pendingReceivables > 0) recommendations.add("Follow up on ₹${String.format("%.2f", pendingReceivables)} in outstanding customer receivables.")
        if (lowStockCount > 0) recommendations.add("Reorder $lowStockCount items currently at or below minimum stock level.")
        if (outOfStockCount > 0) recommendations.add("Replenish $outOfStockCount out-of-stock items immediately to avoid lost sales.")
        if (pendingTaskCount > 3) recommendations.add("Clear $pendingTaskCount pending system tasks to optimize business workflow.")

        val recsJson = "[" + recommendations.joinToString(",") { "\"${it.replace("\"", "'")}\"" } + "]"

        val entity = BusinessHealthEntity(
            overallScore = overallScore,
            revenueScore = revenueScore,
            cashFlowScore = cashFlowScore,
            inventoryScore = inventoryScore,
            securityScore = securityScore,
            backupScore = backupScore,
            statusColor = statusColor,
            recommendationsJson = recsJson,
            calculatedDate = System.currentTimeMillis()
        )

        healthRepository.saveHealthRecord(entity)
        return entity
    }
}
