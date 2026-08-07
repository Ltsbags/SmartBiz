package com.example.core.database

import android.content.Context
import com.example.core.database.dao.CashBookDao
import com.example.core.database.dao.CategoryDao
import com.example.core.database.dao.CustomerDao
import com.example.core.database.dao.CustomerLedgerDao
import com.example.core.database.dao.DeviceDao
import com.example.core.database.dao.ExpenseCategoryDao
import com.example.core.database.dao.ExpenseDao
import com.example.core.database.dao.GlobalSearchDao
import com.example.core.database.dao.IncomeDao
import com.example.core.database.dao.InventoryDao
import com.example.core.database.dao.InvoiceDao
import com.example.core.database.dao.LoginHistoryDao
import com.example.core.database.dao.PurchaseDao
import com.example.core.database.dao.ReportDao
import com.example.core.database.dao.SessionDao
import com.example.core.database.dao.SupplierDao
import com.example.core.database.dao.UserDao

import com.example.core.database.dao.PermissionDao
import com.example.core.database.dao.RoleDao
import com.example.core.database.dao.RolePermissionDao
import com.example.core.database.dao.UserRoleDao
import com.example.core.database.dao.AuditLogDao
import com.example.core.database.dao.EntityHistoryDao
import com.example.core.database.dao.SecurityEventDao
import com.example.core.database.dao.SecurityPolicyDao
import com.example.core.database.dao.RealtimeSessionDao
import com.example.core.database.dao.PresenceDao
import com.example.core.database.dao.RealtimeEventDao
import com.example.core.database.dao.NotificationDao
import com.example.core.database.dao.ReminderDao
import com.example.core.database.dao.NotificationPreferenceDao
import com.example.core.database.dao.ScheduledTaskDao
import com.example.core.database.dao.PrivacySettingsDao
import com.example.core.database.dao.SessionPolicyDao
import com.example.core.database.dao.DataAccessPolicyDao
import com.example.core.database.dao.CommunicationDao
import com.example.core.database.dao.PaymentDao
import com.example.core.database.dao.PaymentRequestDao
import com.example.core.database.dao.PaymentGatewayLogDao
import com.example.core.database.dao.RefundDao
import com.example.core.database.dao.ReportDefinitionDao
import com.example.core.database.dao.SavedReportSnapshotDao
import com.example.core.database.dao.KpiDefinitionDao
import com.example.core.database.dao.AggregatedMetricsDao
import com.example.core.database.dao.BranchMetricsDao
import com.example.core.database.dao.ForecastingSnapshotDao
import com.example.core.database.dao.CompliancePolicyDao
import com.example.core.database.dao.GlobalizationDao
import com.example.core.database.dao.WorkflowDao
import com.example.core.database.dao.PluginDao
import com.example.core.database.dao.ScalabilityDao

class DatabaseHelper(context: Context) {
    private val db = AppDatabase.getDatabase(context)

    val scalabilityDao: ScalabilityDao get() = db.scalabilityDao()
    val workflowDao: WorkflowDao get() = db.workflowDao()
    val globalizationDao: GlobalizationDao get() = db.globalizationDao()
    val pluginDao: PluginDao get() = db.pluginDao()

    val reportDefinitionDao: ReportDefinitionDao get() = db.reportDefinitionDao()
    val savedReportSnapshotDao: SavedReportSnapshotDao get() = db.savedReportSnapshotDao()
    val kpiDefinitionDao: KpiDefinitionDao get() = db.kpiDefinitionDao()
    val aggregatedMetricsDao: AggregatedMetricsDao get() = db.aggregatedMetricsDao()
    val branchMetricsDao: BranchMetricsDao get() = db.branchMetricsDao()
    val forecastingSnapshotDao: ForecastingSnapshotDao get() = db.forecastingSnapshotDao()
    val paymentDao: PaymentDao get() = db.paymentDao()
    val paymentRequestDao: PaymentRequestDao get() = db.paymentRequestDao()
    val paymentGatewayLogDao: PaymentGatewayLogDao get() = db.paymentGatewayLogDao()
    val refundDao: RefundDao get() = db.refundDao()
    val communicationDao: CommunicationDao get() = db.communicationDao()
    val invoiceDao: InvoiceDao get() = db.invoiceDao()
    val inventoryDao: InventoryDao get() = db.inventoryDao()
    val customerDao: CustomerDao get() = db.customerDao()
    val customerLedgerDao: CustomerLedgerDao get() = db.customerLedgerDao()
    val categoryDao: CategoryDao get() = db.categoryDao()
    val supplierDao: SupplierDao get() = db.supplierDao()
    val purchaseDao: PurchaseDao get() = db.purchaseDao()
    val expenseCategoryDao: ExpenseCategoryDao get() = db.expenseCategoryDao()
    val expenseDao: ExpenseDao get() = db.expenseDao()
    val incomeDao: IncomeDao get() = db.incomeDao()
    val cashBookDao: CashBookDao get() = db.cashBookDao()
    val reportDao: ReportDao get() = db.reportDao()
    val globalSearchDao: GlobalSearchDao get() = db.globalSearchDao()
    val userDao: UserDao get() = db.userDao()
    val sessionDao: SessionDao get() = db.sessionDao()
    val deviceDao: DeviceDao get() = db.deviceDao()
    val loginHistoryDao: LoginHistoryDao get() = db.loginHistoryDao()
    val roleDao: RoleDao get() = db.roleDao()
    val permissionDao: PermissionDao get() = db.permissionDao()
    val rolePermissionDao: RolePermissionDao get() = db.rolePermissionDao()
    val userRoleDao: UserRoleDao get() = db.userRoleDao()
    val auditLogDao: AuditLogDao get() = db.auditLogDao()
    val entityHistoryDao: EntityHistoryDao get() = db.entityHistoryDao()
    val securityEventDao: SecurityEventDao get() = db.securityEventDao()
    val securityPolicyDao: SecurityPolicyDao get() = db.securityPolicyDao()
    val notificationDao: NotificationDao get() = db.notificationDao()
    val reminderDao: ReminderDao get() = db.reminderDao()
    val notificationPreferenceDao: NotificationPreferenceDao get() = db.notificationPreferenceDao()
    val scheduledTaskDao: ScheduledTaskDao get() = db.scheduledTaskDao()
    val privacySettingsDao: PrivacySettingsDao get() = db.privacySettingsDao()
    val sessionPolicyDao: SessionPolicyDao get() = db.sessionPolicyDao()
    val dataAccessPolicyDao: DataAccessPolicyDao get() = db.dataAccessPolicyDao()
    val compliancePolicyDao: CompliancePolicyDao get() = db.compliancePolicyDao()
    val realtimeSessionDao: RealtimeSessionDao get() = db.realtimeSessionDao()
    val presenceDao: PresenceDao get() = db.presenceDao()
    val realtimeEventDao: RealtimeEventDao get() = db.realtimeEventDao()

    companion object {
        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseHelper(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
