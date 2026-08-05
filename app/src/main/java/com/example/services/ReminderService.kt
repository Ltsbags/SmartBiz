package com.example.services

import com.example.core.database.entity.ReminderEntity
import com.example.repositories.CustomerRepository
import com.example.repositories.InventoryRepository
import com.example.repositories.ReminderRepository
import com.example.repositories.SupplierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ReminderService(
    private val reminderRepository: ReminderRepository,
    private val inventoryRepository: InventoryRepository,
    private val customerRepository: CustomerRepository,
    private val supplierRepository: SupplierRepository,
    private val notificationEngine: NotificationEngine
) {

    val activeRemindersFlow: Flow<List<ReminderEntity>> = reminderRepository.activeRemindersFlow
    val allRemindersFlow: Flow<List<ReminderEntity>> = reminderRepository.allRemindersFlow

    suspend fun createCustomReminder(
        title: String,
        description: String,
        module: String = "CUSTOM",
        repeatType: String = "NONE",
        triggerTime: Long = System.currentTimeMillis()
    ): Long {
        val reminder = ReminderEntity(
            title = title,
            description = description,
            module = module,
            repeatType = repeatType,
            nextTrigger = triggerTime,
            isEnabled = true,
            createdDate = System.currentTimeMillis(),
            updatedDate = System.currentTimeMillis()
        )
        return reminderRepository.addReminder(reminder)
    }

    suspend fun evaluateAndGenerateAutoReminders() {
        // 1. Evaluate Low Stock
        val lowStockItems = try { inventoryRepository.lowStockItems.first() } catch (_: Exception) { emptyList() }
        if (lowStockItems.isNotEmpty()) {
            val title = "Low Stock Alert (${lowStockItems.size} Items)"
            val desc = "${lowStockItems.take(3).joinToString { it.name }} and others are running below reorder level."
            notificationEngine.publishEvent(
                NotificationEvent(
                    type = "INVENTORY",
                    title = title,
                    message = desc,
                    severity = "WARNING",
                    priority = "HIGH"
                )
            )
        }

        // 2. Check Customer Outstanding Balance
        val customersWithBalance = try { customerRepository.activeCustomers.first().filter { it.outstandingBalance > 0 } } catch (_: Exception) { emptyList() }
        if (customersWithBalance.isNotEmpty()) {
            val count = customersWithBalance.size
            val title = "Outstanding Receivables ($count Customers)"
            val desc = "Customers have outstanding balances pending collection."
            notificationEngine.publishEvent(
                NotificationEvent(
                    type = "CUSTOMERS",
                    title = title,
                    message = desc,
                    severity = "INFO",
                    priority = "MEDIUM"
                )
            )
        }

        // 3. Process Due Reminders
        val dueReminders = reminderRepository.getDueReminders()
        for (reminder in dueReminders) {
            notificationEngine.publishEvent(
                NotificationEvent(
                    type = when (reminder.module) {
                        "LOW_STOCK", "OUT_OF_STOCK" -> "INVENTORY"
                        "PAYMENT_DUE", "CUSTOMER_OUTSTANDING" -> "CUSTOMERS"
                        "SUPPLIER_PAYMENT" -> "SUPPLIERS"
                        "BACKUP_REMINDER", "BUSINESS_HEALTH" -> "SYSTEM"
                        else -> "CUSTOM"
                    },
                    title = reminder.title,
                    message = reminder.description,
                    severity = "INFO",
                    priority = "HIGH"
                )
            )

            // Calculate next trigger time based on repeatType
            if (reminder.repeatType != "NONE") {
                val nextTrigger = calculateNextTrigger(reminder.nextTrigger, reminder.repeatType)
                reminderRepository.updateReminder(
                    reminder.copy(
                        nextTrigger = nextTrigger,
                        updatedDate = System.currentTimeMillis()
                    )
                )
            } else {
                reminderRepository.toggleReminderStatus(reminder.id, false)
            }
        }
    }

    private fun calculateNextTrigger(currentTrigger: Long, repeatType: String): Long {
        val dayMs = 24 * 60 * 60 * 1000L
        return when (repeatType) {
            "DAILY" -> currentTrigger + dayMs
            "WEEKLY" -> currentTrigger + (7 * dayMs)
            "MONTHLY" -> currentTrigger + (30 * dayMs)
            "YEARLY" -> currentTrigger + (365 * dayMs)
            else -> currentTrigger + dayMs
        }
    }

    suspend fun toggleReminder(id: String, isEnabled: Boolean) {
        reminderRepository.toggleReminderStatus(id, isEnabled)
    }

    suspend fun deleteReminder(id: String) {
        reminderRepository.deleteReminder(id)
    }
}
