package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.constants.AppConstants
import com.example.core.database.dao.*
import com.example.core.database.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        InventoryItemEntity::class,
        CustomerEntity::class,
        CustomerLedgerEntity::class,
        CategoryEntity::class,
        SupplierEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        ExpenseCategoryEntity::class,
        ExpenseEntity::class,
        IncomeEntity::class,
        CashBookEntryEntity::class,
        UserEntity::class,
        SessionEntity::class,
        DeviceEntity::class,
        LoginHistoryEntity::class,
        RoleEntity::class,
        PermissionEntity::class,
        RolePermissionCrossRef::class,
        UserRoleCrossRef::class,
        AuditLogEntity::class,
        EntityHistoryEntity::class,
        SecurityEventEntity::class,
        SecurityPolicyEntity::class,
        NotificationEntity::class,
        ReminderEntity::class,
        NotificationPreferenceEntity::class,
        ScheduledTaskEntity::class,
        PrivacySettingsEntity::class,
        SessionPolicyEntity::class,
        DataAccessPolicyEntity::class,
        CompliancePolicyEntity::class,
        RealtimeSessionEntity::class,
        PresenceEntity::class,
        RealtimeEventEntity::class,
        CommunicationMessageEntity::class,
        CommunicationTemplateEntity::class,
        CommunicationAutomationRuleEntity::class,
        CommunicationLogEntity::class,
        PaymentEntity::class,
        PaymentRequestEntity::class,
        PaymentGatewayLogEntity::class,
        RefundEntity::class,
        ReportDefinitionEntity::class,
        SavedReportSnapshotEntity::class,
        KpiDefinitionEntity::class,
        AggregatedDailyMetricsEntity::class,
        BranchMetricsEntity::class,
        ForecastingSnapshotEntity::class,
        PluginEntity::class,
        PluginSettingsEntity::class,
        PluginPermissionEntity::class,
        PluginRegistryEntity::class,
        WorkflowEntity::class,
        WorkflowExecutionEntity::class,
        RuleEntity::class,
        ApprovalRequestEntity::class,
        AutomationHistoryEntity::class,
        LocaleEntity::class,
        TranslationEntity::class,
        CurrencySettingsEntity::class,
        TaxProfileEntity::class,
        CacheMetricsEntity::class,
        QueueJobEntity::class,
        SystemHealthMetricEntity::class,
        PerformanceBenchmarkEntity::class,
        DashboardLayoutEntity::class,
        DashboardWidgetEntity::class,
        TaskCenterEntity::class,
        BusinessHealthEntity::class
    ],
    version = AppConstants.DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scalabilityDao(): ScalabilityDao
    abstract fun workflowDao(): WorkflowDao
    abstract fun globalizationDao(): GlobalizationDao
    abstract fun pluginDao(): PluginDao
    abstract fun reportDefinitionDao(): ReportDefinitionDao
    abstract fun savedReportSnapshotDao(): SavedReportSnapshotDao
    abstract fun kpiDefinitionDao(): KpiDefinitionDao
    abstract fun aggregatedMetricsDao(): AggregatedMetricsDao
    abstract fun branchMetricsDao(): BranchMetricsDao
    abstract fun forecastingSnapshotDao(): ForecastingSnapshotDao
    abstract fun paymentDao(): PaymentDao
    abstract fun paymentRequestDao(): PaymentRequestDao
    abstract fun paymentGatewayLogDao(): PaymentGatewayLogDao
    abstract fun refundDao(): RefundDao
    abstract fun communicationDao(): CommunicationDao
    abstract fun realtimeSessionDao(): RealtimeSessionDao
    abstract fun presenceDao(): PresenceDao
    abstract fun realtimeEventDao(): RealtimeEventDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerLedgerDao(): CustomerLedgerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun cashBookDao(): CashBookDao
    abstract fun reportDao(): ReportDao
    abstract fun globalSearchDao(): GlobalSearchDao
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun deviceDao(): DeviceDao
    abstract fun loginHistoryDao(): LoginHistoryDao
    abstract fun roleDao(): RoleDao
    abstract fun permissionDao(): PermissionDao
    abstract fun rolePermissionDao(): RolePermissionDao
    abstract fun userRoleDao(): UserRoleDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun entityHistoryDao(): EntityHistoryDao
    abstract fun securityEventDao(): SecurityEventDao
    abstract fun securityPolicyDao(): SecurityPolicyDao
    abstract fun notificationDao(): NotificationDao
    abstract fun reminderDao(): ReminderDao
    abstract fun notificationPreferenceDao(): NotificationPreferenceDao
    abstract fun scheduledTaskDao(): ScheduledTaskDao
    abstract fun privacySettingsDao(): PrivacySettingsDao
    abstract fun sessionPolicyDao(): SessionPolicyDao
    abstract fun dataAccessPolicyDao(): DataAccessPolicyDao
    abstract fun compliancePolicyDao(): CompliancePolicyDao
    abstract fun dashboardLayoutDao(): DashboardLayoutDao
    abstract fun dashboardWidgetDao(): DashboardWidgetDao
    abstract fun taskCenterDao(): TaskCenterDao
    abstract fun businessHealthDao(): BusinessHealthDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `users` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `fullName` TEXT NOT NULL,
                        `businessName` TEXT NOT NULL,
                        `mobileNumber` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `roleId` TEXT NOT NULL,
                        `pinHash` TEXT NOT NULL,
                        `profileImage` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `lastLogin` INTEGER NOT NULL,
                        `createdDate` INTEGER NOT NULL,
                        `updatedDate` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_mobileNumber` ON `users` (`mobileNumber`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_userId` ON `users` (`userId`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sessions` (
                        `sessionId` TEXT NOT NULL PRIMARY KEY,
                        `userId` TEXT NOT NULL,
                        `loginTime` INTEGER NOT NULL,
                        `expiryTime` INTEGER NOT NULL,
                        `deviceId` TEXT NOT NULL,
                        `deviceName` TEXT NOT NULL,
                        `appVersion` TEXT NOT NULL,
                        `sessionStatus` TEXT NOT NULL,
                        `authToken` TEXT NOT NULL,
                        `refreshToken` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sessions_userId` ON `sessions` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sessions_sessionStatus` ON `sessions` (`sessionStatus`)")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `users` ADD COLUMN `displayName` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `users` ADD COLUMN `designation` TEXT NOT NULL DEFAULT 'Business Owner'")
                db.execSQL("ALTER TABLE `users` ADD COLUMN `alternateNumber` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `users` ADD COLUMN `dob` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `users` ADD COLUMN `gender` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `users` ADD COLUMN `languagePreference` TEXT NOT NULL DEFAULT 'en'")
                db.execSQL("ALTER TABLE `users` ADD COLUMN `timeZone` TEXT NOT NULL DEFAULT 'UTC'")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `devices` (
                        `deviceId` TEXT NOT NULL PRIMARY KEY,
                        `userId` TEXT NOT NULL,
                        `deviceName` TEXT NOT NULL,
                        `androidVersion` TEXT NOT NULL,
                        `appVersion` TEXT NOT NULL,
                        `deviceIdentifier` TEXT NOT NULL,
                        `lastLoginTime` INTEGER NOT NULL,
                        `isTrusted` INTEGER NOT NULL,
                        `registeredDate` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_devices_userId` ON `devices` (`userId`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `login_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `action` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `details` TEXT NOT NULL,
                        `deviceId` TEXT NOT NULL,
                        `deviceName` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_login_history_userId` ON `login_history` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_login_history_timestamp` ON `login_history` (`timestamp`)")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `roles`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `roles` (
                        `roleId` TEXT NOT NULL PRIMARY KEY,
                        `roleName` TEXT NOT NULL,
                        `roleCode` TEXT NOT NULL,
                        `description` TEXT NOT NULL DEFAULT '',
                        `isSystemRole` INTEGER NOT NULL DEFAULT 1,
                        `isCustomRole` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL DEFAULT 0,
                        `updatedAt` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE IF EXISTS `permissions`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `permissions` (
                        `permissionId` TEXT NOT NULL PRIMARY KEY,
                        `permissionCode` TEXT NOT NULL,
                        `permissionName` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `description` TEXT NOT NULL DEFAULT '',
                        `isSensitive` INTEGER NOT NULL DEFAULT 0,
                        `requiresPinConfirmation` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_permissions_permissionCode` ON `permissions` (`permissionCode`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_permissions_category` ON `permissions` (`category`)")

                db.execSQL("DROP TABLE IF EXISTS `role_permission_cross_ref`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `role_permission_cross_ref` (
                        `roleId` TEXT NOT NULL,
                        `permissionId` TEXT NOT NULL,
                        PRIMARY KEY(`roleId`, `permissionId`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_role_permission_cross_ref_roleId` ON `role_permission_cross_ref` (`roleId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_role_permission_cross_ref_permissionId` ON `role_permission_cross_ref` (`permissionId`)")

                db.execSQL("DROP TABLE IF EXISTS `user_role_cross_ref`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_role_cross_ref` (
                        `userId` TEXT NOT NULL,
                        `roleId` TEXT NOT NULL,
                        `assignedAt` INTEGER NOT NULL DEFAULT 0,
                        `assignedBy` TEXT NOT NULL DEFAULT 'SYSTEM',
                        PRIMARY KEY(`userId`, `roleId`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_role_cross_ref_userId` ON `user_role_cross_ref` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_role_cross_ref_roleId` ON `user_role_cross_ref` (`roleId`)")
            }
        }

        private fun ensureAllTablesExist(db: SupportSQLiteDatabase) {
            fun addColumnIfNotExists(tableName: String, columnName: String, columnDef: String) {
                try {
                    val cursor = db.query("PRAGMA table_info(`$tableName`)")
                    var exists = false
                    val nameIdx = cursor.getColumnIndex("name")
                    while (cursor.moveToNext()) {
                        if (nameIdx != -1 && cursor.getString(nameIdx) == columnName) {
                            exists = true
                            break
                        }
                    }
                    cursor.close()
                    if (!exists) {
                        db.execSQL("ALTER TABLE `$tableName` ADD COLUMN `$columnName` $columnDef")
                    }
                } catch (_: Exception) {}
            }

            addColumnIfNotExists("devices", "platform", "TEXT NOT NULL DEFAULT 'Android'")
            addColumnIfNotExists("devices", "lastActiveTime", "INTEGER NOT NULL DEFAULT 0")
            addColumnIfNotExists("devices", "isCurrentDevice", "INTEGER NOT NULL DEFAULT 0")
            addColumnIfNotExists("devices", "trustLevel", "TEXT NOT NULL DEFAULT 'HIGH'")

            db.execSQL("DROP TABLE IF EXISTS `audit_logs`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `audit_logs` (
                    `auditId` TEXT NOT NULL PRIMARY KEY,
                    `businessId` TEXT NOT NULL,
                    `branchId` TEXT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `userName` TEXT NOT NULL,
                    `sessionId` TEXT NOT NULL,
                    `module` TEXT NOT NULL,
                    `entityName` TEXT NOT NULL,
                    `entityId` TEXT NOT NULL,
                    `action` TEXT NOT NULL,
                    `severity` TEXT NOT NULL,
                    `oldValueJson` TEXT,
                    `newValueJson` TEXT,
                    `description` TEXT NOT NULL,
                    `ipAddress` TEXT NOT NULL,
                    `deviceName` TEXT NOT NULL,
                    `appVersion` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_auditId` ON `audit_logs` (`auditId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_userId` ON `audit_logs` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_module` ON `audit_logs` (`module`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_timestamp` ON `audit_logs` (`timestamp`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_severity` ON `audit_logs` (`severity`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_entityName` ON `audit_logs` (`entityName`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_entityId` ON `audit_logs` (`entityId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_businessId` ON `audit_logs` (`businessId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_branchId` ON `audit_logs` (`branchId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_action` ON `audit_logs` (`action`)")

            db.execSQL("DROP TABLE IF EXISTS `entity_history`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `entity_history` (
                    `historyId` TEXT NOT NULL PRIMARY KEY,
                    `entityName` TEXT NOT NULL,
                    `entityId` TEXT NOT NULL,
                    `action` TEXT NOT NULL,
                    `oldValueJson` TEXT,
                    `newValueJson` TEXT,
                    `modifiedFieldsJson` TEXT,
                    `userId` TEXT NOT NULL,
                    `userName` TEXT NOT NULL,
                    `businessId` TEXT NOT NULL,
                    `branchId` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_entity_history_historyId` ON `entity_history` (`historyId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_entity_history_entityName` ON `entity_history` (`entityName`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_entity_history_entityId` ON `entity_history` (`entityId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_entity_history_userId` ON `entity_history` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_entity_history_timestamp` ON `entity_history` (`timestamp`)")

            db.execSQL("DROP TABLE IF EXISTS `security_events`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `security_events` (
                    `eventId` TEXT NOT NULL PRIMARY KEY,
                    `eventType` TEXT NOT NULL,
                    `severity` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `module` TEXT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `userName` TEXT NOT NULL,
                    `deviceName` TEXT NOT NULL,
                    `ipAddress` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_eventId` ON `security_events` (`eventId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_eventType` ON `security_events` (`eventType`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_severity` ON `security_events` (`severity`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_module` ON `security_events` (`module`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_userId` ON `security_events` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_timestamp` ON `security_events` (`timestamp`)")

            db.execSQL("DROP TABLE IF EXISTS `security_policies`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `security_policies` (
                    `policyKey` TEXT NOT NULL PRIMARY KEY,
                    `policyName` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `isEnabled` INTEGER NOT NULL,
                    `rulesJson` TEXT NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL("DROP TABLE IF EXISTS `notifications`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `notifications` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `businessId` TEXT NOT NULL,
                    `branchId` TEXT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `message` TEXT NOT NULL,
                    `severity` TEXT NOT NULL,
                    `priority` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `isPinned` INTEGER NOT NULL,
                    `isArchived` INTEGER NOT NULL,
                    `createdDate` INTEGER NOT NULL,
                    `scheduledDate` INTEGER,
                    `deliveredDate` INTEGER,
                    `readDate` INTEGER,
                    `payloadJson` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_id` ON `notifications` (`id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_businessId` ON `notifications` (`businessId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_type` ON `notifications` (`type`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_severity` ON `notifications` (`severity`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_priority` ON `notifications` (`priority`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_status` ON `notifications` (`status`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_createdDate` ON `notifications` (`createdDate`)")

            db.execSQL("DROP TABLE IF EXISTS `reminders`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `reminders` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `title` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `module` TEXT NOT NULL,
                    `referenceId` TEXT,
                    `repeatType` TEXT NOT NULL,
                    `nextTrigger` INTEGER NOT NULL,
                    `isEnabled` INTEGER NOT NULL,
                    `createdDate` INTEGER NOT NULL,
                    `updatedDate` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_id` ON `reminders` (`id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_module` ON `reminders` (`module`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_repeatType` ON `reminders` (`repeatType`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_nextTrigger` ON `reminders` (`nextTrigger`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_isEnabled` ON `reminders` (`isEnabled`)")

            db.execSQL("DROP TABLE IF EXISTS `notification_preferences`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `notification_preferences` (
                    `key` TEXT NOT NULL PRIMARY KEY,
                    `title` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `isEnabled` INTEGER NOT NULL,
                    `updatedDate` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL("DROP TABLE IF EXISTS `scheduled_tasks`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `scheduled_tasks` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `taskName` TEXT NOT NULL,
                    `taskType` TEXT NOT NULL,
                    `cronOrFrequency` TEXT NOT NULL,
                    `lastRunTimestamp` INTEGER,
                    `nextRunTimestamp` INTEGER NOT NULL,
                    `isEnabled` INTEGER NOT NULL,
                    `status` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_tasks_id` ON `scheduled_tasks` (`id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_tasks_taskType` ON `scheduled_tasks` (`taskType`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_tasks_nextRunTimestamp` ON `scheduled_tasks` (`nextRunTimestamp`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_tasks_isEnabled` ON `scheduled_tasks` (`isEnabled`)")

            db.execSQL("DROP TABLE IF EXISTS `privacy_settings`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `privacy_settings` (
                    `userId` TEXT NOT NULL PRIMARY KEY,
                    `hideFinancialValues` INTEGER NOT NULL,
                    `hideDashboardAmounts` INTEGER NOT NULL,
                    `maskMobileNumbers` INTEGER NOT NULL,
                    `maskGstNumbers` INTEGER NOT NULL,
                    `maskEmailAddresses` INTEGER NOT NULL,
                    `blurSensitiveScreens` INTEGER NOT NULL,
                    `secureClipboard` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL("DROP TABLE IF EXISTS `session_policies`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `session_policies` (
                    `policyId` TEXT NOT NULL PRIMARY KEY,
                    `sessionTimeoutMinutes` INTEGER NOT NULL,
                    `idleTimeoutMinutes` INTEGER NOT NULL,
                    `maxConcurrentSessions` INTEGER NOT NULL,
                    `rememberDeviceDays` INTEGER NOT NULL,
                    `autoLogoutEnabled` INTEGER NOT NULL,
                    `forceReauthForSensitiveOps` INTEGER NOT NULL,
                    `enableAppLock` INTEGER NOT NULL,
                    `requireBiometric` INTEGER NOT NULL,
                    `requirePin` INTEGER NOT NULL,
                    `lockOnBackground` INTEGER NOT NULL,
                    `lockAfterIdleMinutes` INTEGER NOT NULL,
                    `lockAfterRestart` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL("DROP TABLE IF EXISTS `data_access_policies`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `data_access_policies` (
                    `policyId` TEXT NOT NULL PRIMARY KEY,
                    `roleId` TEXT NOT NULL,
                    `allowExport` INTEGER NOT NULL,
                    `allowBackup` INTEGER NOT NULL,
                    `allowRestore` INTEGER NOT NULL,
                    `allowScreenshot` INTEGER NOT NULL,
                    `allowPrinting` INTEGER NOT NULL,
                    `allowPdfSharing` INTEGER NOT NULL,
                    `rulesJson` TEXT NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL("DROP TABLE IF EXISTS `compliance_policies`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `compliance_policies` (
                    `policyId` TEXT NOT NULL PRIMARY KEY,
                    `framework` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `isEnforced` INTEGER NOT NULL,
                    `complianceRulesJson` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `lastEvaluatedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL("DROP TABLE IF EXISTS `realtime_sessions`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `realtime_sessions` (
                    `sessionId` TEXT NOT NULL PRIMARY KEY,
                    `userId` TEXT NOT NULL,
                    `userName` TEXT NOT NULL,
                    `businessId` TEXT NOT NULL,
                    `branchId` TEXT NOT NULL,
                    `connectionState` TEXT NOT NULL,
                    `transportType` TEXT NOT NULL,
                    `connectedAt` INTEGER NOT NULL,
                    `lastHeartbeatAt` INTEGER NOT NULL,
                    `ipAddress` TEXT NOT NULL,
                    `deviceInfo` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_realtime_sessions_userId` ON `realtime_sessions` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_realtime_sessions_businessId` ON `realtime_sessions` (`businessId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_realtime_sessions_connectionState` ON `realtime_sessions` (`connectionState`)")

            db.execSQL("DROP TABLE IF EXISTS `presence`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `presence` (
                    `userId` TEXT NOT NULL PRIMARY KEY,
                    `userName` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `customStatus` TEXT NOT NULL,
                    `lastSeenAt` INTEGER NOT NULL,
                    `currentDevice` TEXT NOT NULL,
                    `businessId` TEXT NOT NULL,
                    `branchId` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_presence_userId` ON `presence` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_presence_status` ON `presence` (`status`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_presence_businessId` ON `presence` (`businessId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_presence_lastSeenAt` ON `presence` (`lastSeenAt`)")

            db.execSQL("DROP TABLE IF EXISTS `realtime_events`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `realtime_events` (
                    `eventId` TEXT NOT NULL PRIMARY KEY,
                    `eventType` TEXT NOT NULL,
                    `module` TEXT NOT NULL,
                    `entityId` TEXT NOT NULL,
                    `payloadJson` TEXT NOT NULL,
                    `severity` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    `isProcessed` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_realtime_events_eventId` ON `realtime_events` (`eventId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_realtime_events_eventType` ON `realtime_events` (`eventType`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_realtime_events_module` ON `realtime_events` (`module`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_realtime_events_timestamp` ON `realtime_events` (`timestamp`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_realtime_events_isProcessed` ON `realtime_events` (`isProcessed`)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `locales` (
                    `code` TEXT NOT NULL PRIMARY KEY,
                    `name` TEXT NOT NULL,
                    `nativeName` TEXT NOT NULL,
                    `isRtl` INTEGER NOT NULL,
                    `isDefault` INTEGER NOT NULL,
                    `isActive` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `translations` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `localeCode` TEXT NOT NULL,
                    `key` TEXT NOT NULL,
                    `value` TEXT NOT NULL,
                    `category` TEXT NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `currency_settings` (
                    `code` TEXT NOT NULL PRIMARY KEY,
                    `name` TEXT NOT NULL,
                    `symbol` TEXT NOT NULL,
                    `decimalPrecision` INTEGER NOT NULL,
                    `exchangeRateToUsd` REAL NOT NULL,
                    `thousandsSeparator` TEXT NOT NULL,
                    `decimalSeparator` TEXT NOT NULL,
                    `isDefault` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `tax_profiles` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `countryCode` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `ratePercentage` REAL NOT NULL,
                    `taxType` TEXT NOT NULL,
                    `isDefault` INTEGER NOT NULL,
                    `rulesJson` TEXT NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `cache_metrics` (
                    `cacheRegion` TEXT NOT NULL PRIMARY KEY,
                    `hitCount` INTEGER NOT NULL,
                    `missCount` INTEGER NOT NULL,
                    `totalMemoryBytes` INTEGER NOT NULL,
                    `evictedKeysCount` INTEGER NOT NULL,
                    `avgLatencyMs` REAL NOT NULL,
                    `lastClearedTimestamp` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `queue_jobs` (
                    `jobId` TEXT NOT NULL PRIMARY KEY,
                    `queueName` TEXT NOT NULL,
                    `jobType` TEXT NOT NULL,
                    `payloadJson` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `retryCount` INTEGER NOT NULL,
                    `maxRetries` INTEGER NOT NULL,
                    `errorMessage` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `system_health_metrics` (
                    `serviceName` TEXT NOT NULL PRIMARY KEY,
                    `status` TEXT NOT NULL,
                    `latencyMs` INTEGER NOT NULL,
                    `successRatePercent` REAL NOT NULL,
                    `activeConnections` INTEGER NOT NULL,
                    `circuitBreakerState` TEXT NOT NULL,
                    `lastCheckTimestamp` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `performance_benchmarks` (
                    `metricName` TEXT NOT NULL PRIMARY KEY,
                    `budgetMs` INTEGER NOT NULL,
                    `actualMs` INTEGER NOT NULL,
                    `status` TEXT NOT NULL,
                    `sampleSize` INTEGER NOT NULL,
                    `lastUpdated` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL("DROP TABLE IF EXISTS `plugins`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `plugins` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `name` TEXT NOT NULL,
                    `version` TEXT NOT NULL,
                    `developer` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `pluginType` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `minApiVersion` INTEGER NOT NULL,
                    `licenseType` TEXT NOT NULL,
                    `licenseKey` TEXT NOT NULL,
                    `isLicenseValid` INTEGER NOT NULL,
                    `licenseExpiryDate` INTEGER NOT NULL,
                    `installedAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL("DROP TABLE IF EXISTS `plugin_settings`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `plugin_settings` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `pluginId` TEXT NOT NULL,
                    `key` TEXT NOT NULL,
                    `value` TEXT NOT NULL,
                    `valueType` TEXT NOT NULL DEFAULT 'STRING',
                    `updatedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL("DROP TABLE IF EXISTS `plugin_permissions`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `plugin_permissions` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `pluginId` TEXT NOT NULL,
                    `permissionName` TEXT NOT NULL,
                    `isGranted` INTEGER NOT NULL,
                    `grantedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL("DROP TABLE IF EXISTS `plugin_registry`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `plugin_registry` (
                    `pluginId` TEXT NOT NULL PRIMARY KEY,
                    `manifestJson` TEXT NOT NULL,
                    `entryPointClass` TEXT NOT NULL,
                    `isSandboxed` INTEGER NOT NULL DEFAULT 1
                )
                """.trimIndent()
            )

            db.execSQL("DROP TABLE IF EXISTS `dashboard_layouts`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `dashboard_layouts` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `layoutName` TEXT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `businessId` TEXT NOT NULL,
                    `branchId` TEXT NOT NULL,
                    `isDefault` INTEGER NOT NULL,
                    `widgetPositionsJson` TEXT NOT NULL,
                    `updatedDate` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_dashboard_layouts_id` ON `dashboard_layouts` (`id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_dashboard_layouts_userId` ON `dashboard_layouts` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_dashboard_layouts_businessId` ON `dashboard_layouts` (`businessId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_dashboard_layouts_branchId` ON `dashboard_layouts` (`branchId`)")

            db.execSQL("DROP TABLE IF EXISTS `dashboard_widgets`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `dashboard_widgets` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `widgetKey` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `isEnabled` INTEGER NOT NULL,
                    `isPinned` INTEGER NOT NULL,
                    `sortOrder` INTEGER NOT NULL,
                    `widthSpan` INTEGER NOT NULL,
                    `minRole` TEXT NOT NULL,
                    `requiredPermission` TEXT,
                    `configJson` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_dashboard_widgets_id` ON `dashboard_widgets` (`id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_dashboard_widgets_widgetKey` ON `dashboard_widgets` (`widgetKey`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_dashboard_widgets_category` ON `dashboard_widgets` (`category`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_dashboard_widgets_isEnabled` ON `dashboard_widgets` (`isEnabled`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_dashboard_widgets_isPinned` ON `dashboard_widgets` (`isPinned`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_dashboard_widgets_sortOrder` ON `dashboard_widgets` (`sortOrder`)")

            db.execSQL("DROP TABLE IF EXISTS `task_center`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `task_center` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `title` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `taskType` TEXT NOT NULL,
                    `severity` TEXT NOT NULL,
                    `priority` TEXT NOT NULL,
                    `isCompleted` INTEGER NOT NULL,
                    `actionUrl` TEXT,
                    `referenceId` TEXT,
                    `dueDate` INTEGER,
                    `createdDate` INTEGER NOT NULL,
                    `completedDate` INTEGER
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_center_id` ON `task_center` (`id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_center_taskType` ON `task_center` (`taskType`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_center_severity` ON `task_center` (`severity`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_center_priority` ON `task_center` (`priority`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_center_isCompleted` ON `task_center` (`isCompleted`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_center_createdDate` ON `task_center` (`createdDate`)")

            db.execSQL("DROP TABLE IF EXISTS `business_health`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `business_health` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `overallScore` INTEGER NOT NULL,
                    `revenueScore` INTEGER NOT NULL,
                    `cashFlowScore` INTEGER NOT NULL,
                    `inventoryScore` INTEGER NOT NULL,
                    `securityScore` INTEGER NOT NULL,
                    `backupScore` INTEGER NOT NULL,
                    `statusColor` TEXT NOT NULL,
                    `recommendationsJson` TEXT NOT NULL,
                    `calculatedDate` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_business_health_id` ON `business_health` (`id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_business_health_calculatedDate` ON `business_health` (`calculatedDate`)")
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureAllTablesExist(db)
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureAllTablesExist(db)
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureAllTablesExist(db)
            }
        }

        private val MIGRATION_10_12 = object : Migration(10, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureAllTablesExist(db)
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureAllTablesExist(db)
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureAllTablesExist(db)
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureAllTablesExist(db)
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureAllTablesExist(db)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    AppConstants.DATABASE_NAME
                )
                    .addMigrations(
                        MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10,
                        MIGRATION_10_11, MIGRATION_11_12, MIGRATION_10_12,
                        MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16
                    )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(true)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        RbacSeedHelper.seedRbacIfEmpty(
                            database.roleDao(),
                            database.permissionDao(),
                            database.rolePermissionDao(),
                            database.userRoleDao(),
                            database.userDao()
                        )
                    }
                }
            }
        }

        private suspend fun seedDatabase(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val dayMs = 86400000L

            // Seed Categories
            db.categoryDao().insertCategory(
                CategoryEntity(
                    name = "Hardware",
                    colorHex = "#2196F3",
                    iconName = "Computer",
                    description = "POS Equipment and Scanners"
                )
            )
            db.categoryDao().insertCategory(
                CategoryEntity(
                    name = "Supplies",
                    colorHex = "#4CAF50",
                    iconName = "LocalOffer",
                    description = "Paper rolls, labels and inks"
                )
            )
            db.categoryDao().insertCategory(
                CategoryEntity(
                    name = "Accessories",
                    colorHex = "#FF9800",
                    iconName = "Category",
                    description = "Drawers, stands and cables"
                )
            )

            // Seed Customers
            val custId1 = db.customerDao().insertCustomer(
                CustomerEntity(
                    customerCode = "CUST-1001",
                    name = "Acme Global Solutions",
                    company = "Acme Corp",
                    phone = "+91 98765 43210",
                    alternateNumber = "+91 98765 43211",
                    email = "billing@acmeglobal.com",
                    gstNumber = "27AAAAA0000A1Z5",
                    panNumber = "AAAAA0000A",
                    billingAddress = "Suite 404, Tech Park, Powai",
                    shippingAddress = "Warehouse 2, MIDC Industrial Area",
                    city = "Mumbai",
                    state = "Maharashtra",
                    pincode = "400076",
                    customerType = "Wholesale",
                    openingBalance = 500.0,
                    totalPurchases = 12450.00,
                    outstandingBalance = 1250.00,
                    creditLimit = 50000.0,
                    paymentTermsDays = 30,
                    notes = "Preferred customer. Always requires GST Tax Invoices.",
                    tags = "VIP, Wholesale, High Volume",
                    createdDate = now - (30 * dayMs)
                )
            )

            db.customerLedgerDao().insertLedger(
                CustomerLedgerEntity(
                    customerId = custId1,
                    transactionType = "OPENING_BALANCE",
                    referenceNumber = "OB-1001",
                    amount = 500.0,
                    balanceAfter = 500.0,
                    description = "Opening balance recorded",
                    transactionDate = now - (30 * dayMs)
                )
            )

            db.customerLedgerDao().insertLedger(
                CustomerLedgerEntity(
                    customerId = custId1,
                    transactionType = "INVOICE",
                    referenceNumber = "INV-2026-001",
                    amount = 750.0,
                    balanceAfter = 1250.0,
                    description = "POS Equipment Order",
                    transactionDate = now - (2 * dayMs)
                )
            )

            val custId2 = db.customerDao().insertCustomer(
                CustomerEntity(
                    customerCode = "CUST-1002",
                    name = "Apex Retail Traders",
                    company = "Apex LLC",
                    phone = "+91 98123 45678",
                    email = "orders@apexretail.com",
                    gstNumber = "07BBBBB1111B2Z3",
                    panNumber = "BBBBB1111B",
                    billingAddress = "Plot 12, Connaught Place",
                    city = "New Delhi",
                    state = "Delhi",
                    pincode = "110001",
                    customerType = "Retail",
                    openingBalance = 0.0,
                    totalPurchases = 8320.00,
                    outstandingBalance = 0.0,
                    creditLimit = 25000.0,
                    paymentTermsDays = 15,
                    notes = "Prompt payer via UPI / NEFT.",
                    tags = "Regular, Retail",
                    createdDate = now - (15 * dayMs)
                )
            )

            val custId3 = db.customerDao().insertCustomer(
                CustomerEntity(
                    customerCode = "CUST-1003",
                    name = "Vanguard Electronics",
                    company = "Vanguard Pvt Ltd",
                    phone = "+91 91234 56789",
                    email = "contact@vanguard.in",
                    gstNumber = "29CCCCC2222C3Z1",
                    billingAddress = "45 MG Road, Indiranagar",
                    city = "Bengaluru",
                    state = "Karnataka",
                    pincode = "560038",
                    customerType = "Distributor",
                    openingBalance = 3200.0,
                    totalPurchases = 24500.00,
                    outstandingBalance = 3200.00,
                    creditLimit = 100000.0,
                    paymentTermsDays = 45,
                    notes = "Regional distributor for South India territory.",
                    tags = "Distributor, Bulk",
                    createdDate = now - (5 * dayMs)
                )
            )

            db.customerLedgerDao().insertLedger(
                CustomerLedgerEntity(
                    customerId = custId3,
                    transactionType = "OPENING_BALANCE",
                    referenceNumber = "OB-1003",
                    amount = 3200.0,
                    balanceAfter = 3200.0,
                    description = "Opening balance imported from legacy system",
                    transactionDate = now - (5 * dayMs)
                )
            )

            // Seed Inventory Items
            db.inventoryDao().insertItem(
                InventoryItemEntity(
                    name = "Wireless Barcode Scanner Pro",
                    sku = "SKU-BCS-901",
                    barcode = "8901234567890",
                    category = "Hardware",
                    brand = "ScanTech",
                    description = "High-speed 2D omnidirectional wireless laser scanner.",
                    unit = "pcs",
                    purchasePrice = 85.00,
                    costPrice = 85.00,
                    unitPrice = 149.99,
                    gstPercentage = 18.0,
                    openingStock = 30,
                    stockQuantity = 24,
                    minStockThreshold = 5,
                    maxStock = 100,
                    location = "Shelf A-1",
                    createdDate = now - (20 * dayMs),
                    updatedDate = now
                )
            )

            db.inventoryDao().insertItem(
                InventoryItemEntity(
                    name = "Thermal Receipt Roll (80mm)",
                    sku = "SKU-TRR-80M",
                    barcode = "8901234567891",
                    category = "Supplies",
                    brand = "PaperMax",
                    description = "BPA-free high-density thermal receipt paper 50 rolls/box.",
                    unit = "box",
                    purchasePrice = 12.00,
                    costPrice = 12.00,
                    unitPrice = 29.50,
                    gstPercentage = 12.0,
                    openingStock = 20,
                    stockQuantity = 3, // Low stock demo
                    minStockThreshold = 10,
                    maxStock = 50,
                    location = "Shelf B-4",
                    createdDate = now - (18 * dayMs),
                    updatedDate = now
                )
            )

            db.inventoryDao().insertItem(
                InventoryItemEntity(
                    name = "Smart POS Terminal V2",
                    sku = "SKU-POS-V20",
                    barcode = "8901234567892",
                    category = "Hardware",
                    brand = "SmartBiz",
                    description = "Dual-screen Android POS with integrated receipt printer & NFC.",
                    unit = "pcs",
                    purchasePrice = 260.00,
                    costPrice = 260.00,
                    unitPrice = 399.00,
                    gstPercentage = 18.0,
                    openingStock = 15,
                    stockQuantity = 12,
                    minStockThreshold = 3,
                    maxStock = 30,
                    location = "Counter 1",
                    createdDate = now - (15 * dayMs),
                    updatedDate = now
                )
            )

            db.inventoryDao().insertItem(
                InventoryItemEntity(
                    name = "Heavy Duty Cash Drawer",
                    sku = "SKU-CDR-100",
                    barcode = "8901234567893",
                    category = "Accessories",
                    brand = "SafeLock",
                    description = "Steel construct cash drawer with 5 bill & 8 coin removable tray.",
                    unit = "pcs",
                    purchasePrice = 45.00,
                    costPrice = 45.00,
                    unitPrice = 89.00,
                    gstPercentage = 18.0,
                    openingStock = 10,
                    stockQuantity = 2, // Low stock demo
                    minStockThreshold = 4,
                    maxStock = 25,
                    location = "Shelf C-2",
                    createdDate = now - (10 * dayMs),
                    updatedDate = now
                )
            )

            db.inventoryDao().insertItem(
                InventoryItemEntity(
                    name = "Bluetooth Label Printer",
                    sku = "SKU-BLP-500",
                    barcode = "8901234567894",
                    category = "Hardware",
                    brand = "PrintMaster",
                    description = "Portable direct thermal barcode and sticker label printer.",
                    unit = "pcs",
                    purchasePrice = 65.00,
                    costPrice = 65.00,
                    unitPrice = 119.00,
                    gstPercentage = 18.0,
                    openingStock = 5,
                    stockQuantity = 0, // Out of stock demo!
                    minStockThreshold = 3,
                    maxStock = 20,
                    location = "Shelf A-3",
                    createdDate = now - (5 * dayMs),
                    updatedDate = now
                )
            )

            // Seed Invoices & Invoice Items
            val inv1Id = db.invoiceDao().insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2026-001",
                    date = now - (2 * dayMs),
                    dueDate = now + (13 * dayMs),
                    customerId = custId1,
                    customerName = "Acme Global Solutions",
                    customerPhone = "+91 98765 43210",
                    customerGst = "27AAAAA0000A1Z5",
                    billingAddress = "Suite 404, Tech Park, Powai, Mumbai",
                    status = "COMPLETED",
                    paymentStatus = "UNPAID",
                    subtotal = 1100.00,
                    discountType = "FLAT",
                    discountValue = 0.0,
                    discountAmount = 0.0,
                    taxAmount = 150.00,
                    roundOff = 0.0,
                    totalAmount = 1250.00,
                    paidAmount = 0.0,
                    balanceAmount = 1250.00,
                    itemsCount = 2,
                    notes = "POS Hardware Installation order",
                    terms = "Payment due in 15 days."
                )
            )

            db.invoiceDao().insertInvoiceItems(
                listOf(
                    InvoiceItemEntity(
                        invoiceId = inv1Id,
                        productId = 1,
                        productName = "Wireless Barcode Scanner Pro",
                        sku = "SKU-BCS-901",
                        quantity = 2.0,
                        unit = "pcs",
                        sellingPrice = 149.99,
                        discount = 0.0,
                        gstPercentage = 18.0,
                        taxAmount = 53.99,
                        lineTotal = 353.97
                    ),
                    InvoiceItemEntity(
                        invoiceId = inv1Id,
                        productId = 3,
                        productName = "Smart POS Terminal V2",
                        sku = "SKU-POS-V20",
                        quantity = 2.0,
                        unit = "pcs",
                        sellingPrice = 399.00,
                        discount = 0.0,
                        gstPercentage = 18.0,
                        taxAmount = 143.64,
                        lineTotal = 941.64
                    )
                )
            )

            val inv2Id = db.invoiceDao().insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2026-002",
                    date = now - (5 * dayMs),
                    dueDate = now + (10 * dayMs),
                    customerId = custId2,
                    customerName = "Apex Retail Traders",
                    customerPhone = "+91 98123 45678",
                    customerGst = "07BBBBB1111B2Z3",
                    billingAddress = "Plot 12, Connaught Place, New Delhi",
                    status = "COMPLETED",
                    paymentStatus = "PAID",
                    subtotal = 743.30,
                    discountType = "PERCENTAGE",
                    discountValue = 5.0,
                    discountAmount = 37.16,
                    taxAmount = 126.36,
                    roundOff = 0.0,
                    totalAmount = 832.50,
                    paidAmount = 832.50,
                    balanceAmount = 0.0,
                    itemsCount = 1,
                    notes = "Paid in full via NEFT",
                    terms = "Thank you for your business!"
                )
            )

            db.invoiceDao().insertInvoiceItems(
                listOf(
                    InvoiceItemEntity(
                        invoiceId = inv2Id,
                        productId = 2,
                        productName = "Thermal Receipt Roll (80mm)",
                        sku = "SKU-TRR-80M",
                        quantity = 25.0,
                        unit = "box",
                        sellingPrice = 29.50,
                        discount = 0.0,
                        gstPercentage = 12.0,
                        taxAmount = 88.50,
                        lineTotal = 826.00
                    )
                )
            )

            // Seed Suppliers
            val suppId1 = db.supplierDao().insertSupplier(
                SupplierEntity(
                    supplierCode = "SUP-1001",
                    supplierName = "ScanTech Electronics Corp",
                    businessName = "ScanTech Global",
                    phone = "+1 (800) 555-0199",
                    email = "supply@scantech.com",
                    gstNumber = "27AAACS1234F1Z9",
                    billingAddress = "450 Industrial Parkway, Building B",
                    city = "San Jose",
                    state = "California",
                    pincode = "95110",
                    openingBalance = 0.0,
                    outstandingBalance = 850.00,
                    paymentTerms = "Net 30",
                    notes = "Primary vendor for barcode scanners & readers."
                )
            )

            val suppId2 = db.supplierDao().insertSupplier(
                SupplierEntity(
                    supplierCode = "SUP-1002",
                    supplierName = "PaperMax Supplies Ltd",
                    businessName = "PaperMax Distribution",
                    phone = "+1 (800) 555-0244",
                    email = "orders@papermax.com",
                    gstNumber = "07AAACP9876E1Z2",
                    billingAddress = "12 Warehouse Way",
                    city = "Chicago",
                    state = "Illinois",
                    pincode = "60601",
                    openingBalance = 0.0,
                    outstandingBalance = 0.0,
                    paymentTerms = "Net 15",
                    notes = "Bulk thermal paper roll manufacturer."
                )
            )

            // Seed Purchase Orders
            val po1Id = db.purchaseDao().insertPurchase(
                PurchaseEntity(
                    purchaseNumber = "PO-2026-001",
                    supplierId = suppId1,
                    supplierName = "ScanTech Electronics Corp",
                    supplierPhone = "+1 (800) 555-0199",
                    purchaseDate = now - (10 * dayMs),
                    status = "RECEIVED",
                    paymentStatus = "PARTIAL",
                    subtotal = 1700.00,
                    discount = 0.0,
                    taxAmount = 306.00,
                    totalAmount = 2006.00,
                    paidAmount = 1156.00,
                    balanceAmount = 850.00,
                    itemsCount = 1,
                    notes = "Stock inward for Wireless Barcode Scanners"
                )
            )

            db.purchaseDao().insertPurchaseItems(
                listOf(
                    PurchaseItemEntity(
                        purchaseId = po1Id,
                        productId = 1,
                        productName = "Wireless Barcode Scanner Pro",
                        sku = "SKU-BCS-901",
                        quantity = 20.0,
                        unit = "pcs",
                        purchasePrice = 85.00,
                        taxPercentage = 18.0,
                        lineTotal = 2006.00
                    )
                )
            )

            val po2Id = db.purchaseDao().insertPurchase(
                PurchaseEntity(
                    purchaseNumber = "PO-2026-002",
                    supplierId = suppId2,
                    supplierName = "PaperMax Supplies Ltd",
                    supplierPhone = "+1 (800) 555-0244",
                    purchaseDate = now - (2 * dayMs),
                    status = "ORDERED",
                    paymentStatus = "UNPAID",
                    subtotal = 600.00,
                    discount = 0.0,
                    taxAmount = 72.00,
                    totalAmount = 672.00,
                    paidAmount = 0.0,
                    balanceAmount = 672.00,
                    itemsCount = 1,
                    notes = "Urgent reorder for Thermal Receipt Rolls"
                )
            )

            db.purchaseDao().insertPurchaseItems(
                listOf(
                    PurchaseItemEntity(
                        purchaseId = po2Id,
                        productId = 2,
                        productName = "Thermal Receipt Roll (80mm)",
                        sku = "SKU-TRR-80M",
                        quantity = 50.0,
                        unit = "box",
                        purchasePrice = 12.00,
                        taxPercentage = 12.0,
                        lineTotal = 672.00
                    )
                )
            )

            // Seed Expense Categories
            val catRent = db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Rent & Lease", colorHex = "#E91E63", iconName = "Business", description = "Storefront and warehouse lease payments", isSystemDefault = true))
            val catUtil = db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Utilities & Power", colorHex = "#FF9800", iconName = "Bolt", description = "Electricity, water, internet and phones", isSystemDefault = true))
            val catSal = db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Salaries & Payroll", colorHex = "#4CAF50", iconName = "People", description = "Employee compensation and bonuses", isSystemDefault = true))
            val catMaint = db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Maintenance & Repairs", colorHex = "#00BCD4", iconName = "Build", description = "Equipment servicing and store upkeep", isSystemDefault = true))
            val catMisc = db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Miscellaneous", colorHex = "#9C27B0", iconName = "MoreHoriz", description = "General operational overheads", isSystemDefault = true))

            // Seed Expenses
            val exp1Id = db.expenseDao().insertExpense(
                ExpenseEntity(
                    expenseNumber = "EXP-2026-001",
                    expenseDate = now - (12 * dayMs),
                    categoryId = catRent,
                    categoryName = "Rent & Lease",
                    amount = 1500.00,
                    taxAmount = 0.0,
                    totalAmount = 1500.00,
                    paymentMode = "BANK_TRANSFER",
                    paymentStatus = "PAID",
                    paidAmount = 1500.00,
                    referenceNumber = "FT-9832104",
                    payeeName = "Commercial Properties Ltd",
                    notes = "Monthly storefront rent for May 2026",
                    createdDate = now - (12 * dayMs)
                )
            )

            val exp2Id = db.expenseDao().insertExpense(
                ExpenseEntity(
                    expenseNumber = "EXP-2026-002",
                    expenseDate = now - (5 * dayMs),
                    categoryId = catUtil,
                    categoryName = "Utilities & Power",
                    amount = 240.00,
                    taxAmount = 24.00,
                    totalAmount = 264.00,
                    paymentMode = "UPI",
                    paymentStatus = "PAID",
                    paidAmount = 264.00,
                    referenceNumber = "UPI-771239",
                    payeeName = "City Power Corp",
                    notes = "Electricity & Fiber internet bill",
                    createdDate = now - (5 * dayMs)
                )
            )

            val exp3Id = db.expenseDao().insertExpense(
                ExpenseEntity(
                    expenseNumber = "EXP-2026-003",
                    expenseDate = now - (1 * dayMs),
                    categoryId = catMaint,
                    categoryName = "Maintenance & Repairs",
                    amount = 120.00,
                    taxAmount = 0.0,
                    totalAmount = 120.00,
                    paymentMode = "CASH",
                    paymentStatus = "PAID",
                    paidAmount = 120.00,
                    referenceNumber = "REC-4410",
                    payeeName = "QuickFix Electronics",
                    notes = "POS Printer thermal head replacement",
                    createdDate = now - (1 * dayMs)
                )
            )

            // Seed Income Entries
            val inc1Id = db.incomeDao().insertIncome(
                IncomeEntity(
                    incomeNumber = "INC-2026-001",
                    incomeDate = now - (8 * dayMs),
                    category = "Consulting Services",
                    customerId = custId1,
                    customerName = "Acme Global Solutions",
                    amount = 850.00,
                    paymentMode = "BANK_TRANSFER",
                    referenceNumber = "NEFT-004812",
                    notes = "POS Software Customization and Staff Onboarding",
                    createdDate = now - (8 * dayMs)
                )
            )

            val inc2Id = db.incomeDao().insertIncome(
                IncomeEntity(
                    incomeNumber = "INC-2026-002",
                    incomeDate = now - (3 * dayMs),
                    category = "Other Income",
                    customerId = null,
                    customerName = "Recycling Trade Corp",
                    amount = 180.00,
                    paymentMode = "CASH",
                    referenceNumber = "CSH-9921",
                    notes = "Sale of old packaging scrap and unused pallets",
                    createdDate = now - (3 * dayMs)
                )
            )

            // Seed Cash Book Entries
            db.cashBookDao().insertEntry(
                CashBookEntryEntity(
                    entryDate = now - (30 * dayMs),
                    entryType = "CASH_IN",
                    sourceType = "OPENING_BALANCE",
                    referenceNumber = "OB-CASH-001",
                    entityName = "Store Cash Drawer",
                    description = "Initial cash balance recorded",
                    amount = 5000.00,
                    paymentMode = "CASH",
                    balanceAfter = 5000.00,
                    createdDate = now - (30 * dayMs)
                )
            )

            db.cashBookDao().insertEntry(
                CashBookEntryEntity(
                    entryDate = now - (12 * dayMs),
                    entryType = "CASH_OUT",
                    sourceType = "EXPENSE",
                    referenceId = exp1Id,
                    referenceNumber = "EXP-2026-001",
                    entityName = "Commercial Properties Ltd",
                    description = "Rent & Lease: Monthly storefront rent",
                    amount = 1500.00,
                    paymentMode = "BANK_TRANSFER",
                    balanceAfter = 3500.00,
                    createdDate = now - (12 * dayMs)
                )
            )

            db.cashBookDao().insertEntry(
                CashBookEntryEntity(
                    entryDate = now - (8 * dayMs),
                    entryType = "CASH_IN",
                    sourceType = "INCOME",
                    referenceId = inc1Id,
                    referenceNumber = "INC-2026-001",
                    entityName = "Acme Global Solutions",
                    description = "Consulting Services: POS Software Customization",
                    amount = 850.00,
                    paymentMode = "BANK_TRANSFER",
                    balanceAfter = 4350.00,
                    createdDate = now - (8 * dayMs)
                )
            )

            db.cashBookDao().insertEntry(
                CashBookEntryEntity(
                    entryDate = now - (5 * dayMs),
                    entryType = "CASH_OUT",
                    sourceType = "EXPENSE",
                    referenceId = exp2Id,
                    referenceNumber = "EXP-2026-002",
                    entityName = "City Power Corp",
                    description = "Utilities & Power: Electricity & Fiber internet",
                    amount = 264.00,
                    paymentMode = "UPI",
                    balanceAfter = 4086.00,
                    createdDate = now - (5 * dayMs)
                )
            )

            db.cashBookDao().insertEntry(
                CashBookEntryEntity(
                    entryDate = now - (3 * dayMs),
                    entryType = "CASH_IN",
                    sourceType = "INCOME",
                    referenceId = inc2Id,
                    referenceNumber = "INC-2026-002",
                    entityName = "Recycling Trade Corp",
                    description = "Other Income: Sale of old packaging scrap",
                    amount = 180.00,
                    paymentMode = "CASH",
                    balanceAfter = 4266.00,
                    createdDate = now - (3 * dayMs)
                )
            )

            db.cashBookDao().insertEntry(
                CashBookEntryEntity(
                    entryDate = now - (1 * dayMs),
                    entryType = "CASH_OUT",
                    sourceType = "EXPENSE",
                    referenceId = exp3Id,
                    referenceNumber = "EXP-2026-003",
                    entityName = "QuickFix Electronics",
                    description = "Maintenance & Repairs: POS Printer thermal head replacement",
                    amount = 120.00,
                    paymentMode = "CASH",
                    balanceAfter = 4146.00,
                    createdDate = now - (1 * dayMs)
                )
            )
        }
    }
}
