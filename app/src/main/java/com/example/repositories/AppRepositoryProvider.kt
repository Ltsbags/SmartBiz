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
import com.example.services.communication.CommunicationEngineService

class AppRepositoryProvider private constructor(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)
    private val prefsService = SharedPreferencesService.getInstance(context)

    val communicationRepository: CommunicationRepository = CommunicationRepository(dbHelper.communicationDao)
    
    val executionQueue: com.example.services.workflow.ExecutionQueue = com.example.services.workflow.ExecutionQueue()
    val ruleEngine: com.example.services.workflow.RuleEngine = com.example.services.workflow.RuleEngine()
    val approvalEngine: com.example.services.workflow.ApprovalEngine = com.example.services.workflow.ApprovalEngine(dbHelper.workflowDao)
    val actionEngine: com.example.services.workflow.ActionEngine = com.example.services.workflow.ActionEngine(dbHelper.workflowDao, approvalEngine)
    val workflowEngine: com.example.services.workflow.WorkflowEngine = com.example.services.workflow.WorkflowEngine(dbHelper.workflowDao, ruleEngine, actionEngine, executionQueue)
    val workflowSchedulerService: com.example.services.workflow.WorkflowSchedulerService = com.example.services.workflow.WorkflowSchedulerService(workflowEngine)
    val aiWorkflowAssistant: com.example.services.workflow.AiWorkflowAssistant = com.example.services.workflow.AiWorkflowAssistant()
    val workflowRepository: WorkflowRepository = WorkflowRepository(dbHelper.workflowDao, workflowEngine, ruleEngine, approvalEngine, aiWorkflowAssistant)
    val templateRepository: TemplateRepository = TemplateRepository(dbHelper.communicationDao)
    val deliveryRepository: DeliveryRepository = DeliveryRepository(dbHelper.communicationDao)

    val paymentRepository: PaymentRepository = PaymentRepository(dbHelper)

    val pluginPermissionService: com.example.services.plugin.PluginPermissionService = com.example.services.plugin.PluginPermissionService(dbHelper.pluginDao)
    val pluginLicenseService: com.example.services.plugin.PluginLicenseService = com.example.services.plugin.PluginLicenseService(dbHelper.pluginDao)
    val pluginManagerService: com.example.services.plugin.PluginManagerService = com.example.services.plugin.PluginManagerService(dbHelper.pluginDao, pluginPermissionService, pluginLicenseService)
    val pluginRepository: PluginRepository = PluginRepository(dbHelper.pluginDao, pluginManagerService, pluginPermissionService, pluginLicenseService)

    val publicApiRepository: PublicApiRepository = PublicApiRepository()
    val publicApiGateway: com.example.publicapi.gateway.PublicApiGateway = com.example.publicapi.gateway.PublicApiGateway(
        publicApiRepository = publicApiRepository,
        invoiceDao = dbHelper.invoiceDao,
        customerDao = dbHelper.customerDao,
        inventoryDao = dbHelper.inventoryDao
    )

    val templateEngineService: com.example.services.communication.TemplateEngineService = com.example.services.communication.TemplateEngineService()
    val deliveryTrackingService: com.example.services.communication.DeliveryTrackingService = com.example.services.communication.DeliveryTrackingService(communicationRepository)
    val communicationRetryService: com.example.services.communication.CommunicationRetryService = com.example.services.communication.CommunicationRetryService(communicationRepository, deliveryTrackingService)
    val attachmentService: com.example.services.communication.AttachmentService = com.example.services.communication.AttachmentService(context)
    val webhookFoundationService: com.example.services.communication.WebhookFoundationService = com.example.services.communication.WebhookFoundationService(deliveryTrackingService)

    val communicationEngineService: CommunicationEngineService = CommunicationEngineService(
        repository = communicationRepository,
        templateEngine = templateEngineService,
        deliveryTracking = deliveryTrackingService,
        retryService = communicationRetryService,
        webhookService = webhookFoundationService
    )

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

    val realtimeService: com.example.services.RealtimeService = com.example.services.RealtimeService(
        dbHelper.realtimeEventDao,
        dbHelper.realtimeSessionDao
    )
    val presenceService: com.example.services.PresenceService = com.example.services.PresenceService(
        dbHelper.presenceDao
    )
    val connectionService: com.example.services.ConnectionService = com.example.services.ConnectionService(
        context,
        realtimeService
    )

    val realtimeRepository: RealtimeRepository = RealtimeRepository(realtimeService)
    val presenceRepository: PresenceRepository = PresenceRepository(presenceService)

    val aggregationService: com.example.services.bi.AggregationService = com.example.services.bi.AggregationService(
        dbHelper.reportDao,
        dbHelper.aggregatedMetricsDao,
        dbHelper.branchMetricsDao
    )
    val kpiEngineService: com.example.services.bi.KPIEngineService = com.example.services.bi.KPIEngineService(
        dbHelper.kpiDefinitionDao
    )
    val forecastingService: com.example.services.bi.ForecastingService = com.example.services.bi.ForecastingService(
        dbHelper.forecastingSnapshotDao
    )
    val biService: com.example.services.bi.BIService = com.example.services.bi.BIService(
        reportDao = dbHelper.reportDao,
        aggregatedMetricsDao = dbHelper.aggregatedMetricsDao,
        branchMetricsDao = dbHelper.branchMetricsDao,
        kpiDefinitionDao = dbHelper.kpiDefinitionDao,
        reportDefinitionDao = dbHelper.reportDefinitionDao,
        savedReportSnapshotDao = dbHelper.savedReportSnapshotDao,
        forecastingSnapshotDao = dbHelper.forecastingSnapshotDao,
        aggregationService = aggregationService,
        kpiEngineService = kpiEngineService,
        forecastingService = forecastingService
    )
    val reportingRepository: ReportingRepository = ReportingRepository(biService)

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
