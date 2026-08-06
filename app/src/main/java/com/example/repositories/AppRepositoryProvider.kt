package com.example.repositories

import android.content.Context
import com.example.core.database.DatabaseHelper
import com.example.core.services.SharedPreferencesService
import com.example.services.ActivityService
import com.example.services.AuditService
import com.example.services.AuthorizationService
import com.example.services.DeviceService
import com.example.services.HistoryService
import com.example.services.ImageService
import com.example.services.InsightsService
import com.example.services.PinManagementService
import com.example.services.SecurityEventService
import com.example.services.NotificationEngine
import com.example.services.NotificationPreferenceService
import com.example.services.ReminderService
import com.example.services.SchedulerService
import com.example.services.SessionHistoryService
import com.example.services.PolicyEngine
import com.example.services.PrivacyService
import com.example.services.TrustedDeviceService
import com.example.services.SessionPolicyService
import com.example.services.ComplianceService

class AppRepositoryProvider private constructor(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)
    private val prefsService = SharedPreferencesService.getInstance(context)

    val invoiceRepository: InvoiceRepository = InvoiceRepository(
        dbHelper.invoiceDao,
        dbHelper.inventoryDao,
        dbHelper.customerDao,
        dbHelper.customerLedgerDao
    )
    val inventoryRepository: InventoryRepository = InventoryRepository(dbHelper.inventoryDao)
    val customerRepository: CustomerRepository = CustomerRepository(dbHelper.customerDao, dbHelper.customerLedgerDao)
    val categoryRepository: CategoryRepository = CategoryRepository(dbHelper.categoryDao)
    val settingsRepository: SettingsRepository = SettingsRepository(prefsService)
    val supplierRepository: SupplierRepository = SupplierRepository(dbHelper.supplierDao, dbHelper.purchaseDao)
    val purchaseRepository: PurchaseRepository = PurchaseRepository(dbHelper.purchaseDao, dbHelper.inventoryDao, dbHelper.supplierDao)
    val expenseCategoryRepository: ExpenseCategoryRepository = ExpenseCategoryRepository(dbHelper.expenseCategoryDao)
    val expenseRepository: ExpenseRepository = ExpenseRepository(dbHelper.expenseDao, dbHelper.cashBookDao)
    val incomeRepository: IncomeRepository = IncomeRepository(dbHelper.incomeDao, dbHelper.cashBookDao)
    val cashBookRepository: CashBookRepository = CashBookRepository(dbHelper.cashBookDao)
    val reportsRepository: ReportsRepository = ReportsRepository(dbHelper.reportDao)
    val analyticsRepository: AnalyticsRepository = AnalyticsRepository(dbHelper.reportDao)
    val globalSearchRepository: GlobalSearchRepository = GlobalSearchRepository(dbHelper.globalSearchDao, prefsService)
    val authRepository: AuthRepository = AuthRepository(dbHelper.userDao, dbHelper.sessionDao, prefsService)
    val userRepository: UserRepository = UserRepository(dbHelper.userDao, prefsService)
    val deviceRepository: DeviceRepository = DeviceRepository(dbHelper.deviceDao)
    val loginHistoryRepository: LoginHistoryRepository = LoginHistoryRepository(dbHelper.loginHistoryDao)
    val rbacRepository: RbacRepository = RbacRepository(
        dbHelper.roleDao,
        dbHelper.permissionDao,
        dbHelper.rolePermissionDao,
        dbHelper.userRoleDao
    )

    val auditRepository: AuditRepository = AuditRepository(dbHelper.auditLogDao, prefsService)
    val historyRepository: HistoryRepository = HistoryRepository(dbHelper.entityHistoryDao)
    val activityRepository: ActivityRepository = ActivityRepository(auditRepository, historyRepository)

    val deviceService: DeviceService = DeviceService(context)
    val imageService: ImageService = ImageService(context)
    val pinManagementService: PinManagementService = PinManagementService()
    val sessionHistoryService: SessionHistoryService = SessionHistoryService(dbHelper.loginHistoryDao)
    val insightsService: InsightsService = InsightsService(dbHelper.reportDao)
    val authorizationService: AuthorizationService = AuthorizationService(rbacRepository, dbHelper.loginHistoryDao)

    val auditService: AuditService = AuditService(auditRepository, userRepository)
    val historyService: HistoryService = HistoryService(historyRepository, userRepository)
    val activityService: ActivityService = ActivityService(activityRepository)
    val securityEventService: SecurityEventService = SecurityEventService(auditService, auditRepository)

    val notificationRepository: NotificationRepository = NotificationRepository(dbHelper.notificationDao)
    val reminderRepository: ReminderRepository = ReminderRepository(dbHelper.reminderDao)
    val preferenceRepository: NotificationPreferenceRepository = NotificationPreferenceRepository(dbHelper.notificationPreferenceDao)
    val schedulerRepository: SchedulerRepository = SchedulerRepository(dbHelper.scheduledTaskDao)

    val securityPolicyRepository: SecurityPolicyRepository = SecurityPolicyRepository(dbHelper.securityPolicyDao)
    val privacyRepository: PrivacyRepository = PrivacyRepository(dbHelper.privacySettingsDao)
    val sessionPolicyRepository: SessionPolicyRepository = SessionPolicyRepository(dbHelper.sessionPolicyDao)
    val dataAccessPolicyRepository: DataAccessPolicyRepository = DataAccessPolicyRepository(dbHelper.dataAccessPolicyDao)
    val complianceRepository: ComplianceRepository = ComplianceRepository(dbHelper.compliancePolicyDao)
    val trustedDeviceRepository: TrustedDeviceRepository = TrustedDeviceRepository(dbHelper.deviceDao)

    val notificationPreferenceService: NotificationPreferenceService = NotificationPreferenceService(preferenceRepository)
    val notificationEngine: NotificationEngine = NotificationEngine(notificationRepository, notificationPreferenceService)
    val reminderService: ReminderService = ReminderService(
        reminderRepository,
        inventoryRepository,
        customerRepository,
        supplierRepository,
        notificationEngine
    )
    val schedulerService: SchedulerService = SchedulerService(schedulerRepository, notificationEngine)

    val privacyService: PrivacyService = PrivacyService(privacyRepository, auditService)
    val trustedDeviceService: TrustedDeviceService = TrustedDeviceService(trustedDeviceRepository, auditService)
    val sessionPolicyService: SessionPolicyService = SessionPolicyService(sessionPolicyRepository, auditService)
    val complianceService: ComplianceService = ComplianceService(complianceRepository, auditService)
    val policyEngine: PolicyEngine = PolicyEngine(
        securityPolicyRepository,
        privacyRepository,
        sessionPolicyRepository,
        dataAccessPolicyRepository,
        complianceRepository,
        trustedDeviceRepository,
        auditService
    )

    companion object {
        @Volatile
        private var INSTANCE: AppRepositoryProvider? = null

        fun initialize(context: Context): AppRepositoryProvider {
            return INSTANCE ?: synchronized(this) {
                val instance = AppRepositoryProvider(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        fun getInstance(): AppRepositoryProvider {
            return INSTANCE ?: throw IllegalStateException("AppRepositoryProvider must be initialized first")
        }
    }
}
