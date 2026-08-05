package com.example.core.database

import com.example.core.database.dao.PermissionDao
import com.example.core.database.dao.RoleDao
import com.example.core.database.dao.RolePermissionDao
import com.example.core.database.dao.UserRoleDao
import com.example.core.database.entity.PermissionEntity
import com.example.core.database.entity.RoleEntity
import com.example.core.database.entity.RolePermissionCrossRef
import com.example.core.database.entity.UserRoleCrossRef

object RbacSeedHelper {

    suspend fun seedRbacIfEmpty(
        roleDao: RoleDao,
        permissionDao: PermissionDao,
        rolePermissionDao: RolePermissionDao,
        userRoleDao: UserRoleDao,
        userDao: com.example.core.database.dao.UserDao
    ) {
        if (permissionDao.getPermissionCount() > 0 && roleDao.getRoleCount() > 0) {
            return
        }

        // 1. Seed Permissions
        val permissions = listOf(
            // Dashboard
            PermissionEntity("PERM_DASHBOARD_VIEW", "dashboard:view", "View Dashboard", "Dashboard", "Access the main metrics dashboard"),
            
            // Invoices
            PermissionEntity("PERM_INVOICE_VIEW", "invoices:view", "View Invoices", "Invoices", "View invoice listing and details"),
            PermissionEntity("PERM_INVOICE_CREATE", "invoices:create", "Create Invoices", "Invoices", "Generate new billing invoices"),
            PermissionEntity("PERM_INVOICE_EDIT", "invoices:edit", "Edit Invoices", "Invoices", "Update existing invoices"),
            PermissionEntity("PERM_INVOICE_DELETE", "invoices:delete", "Delete Invoices", "Invoices", "Void or remove invoices", isSensitive = true, requiresPinConfirmation = true),
            PermissionEntity("PERM_INVOICE_PRINT", "invoices:print", "Print/Export Invoices", "Invoices", "Print or export invoices to PDF"),

            // Inventory
            PermissionEntity("PERM_INVENTORY_VIEW", "inventory:view", "View Inventory", "Inventory", "View products and stock levels"),
            PermissionEntity("PERM_INVENTORY_CREATE", "inventory:create", "Add Product", "Inventory", "Add new items to inventory"),
            PermissionEntity("PERM_INVENTORY_EDIT", "inventory:edit", "Edit Product", "Inventory", "Update product pricing and details"),
            PermissionEntity("PERM_INVENTORY_DELETE", "inventory:delete", "Delete Product", "Inventory", "Delete inventory items", isSensitive = true),
            PermissionEntity("PERM_INVENTORY_ADJUST", "inventory:adjust_stock", "Adjust Stock", "Inventory", "Manually adjust stock counts", isSensitive = true),

            // Customers
            PermissionEntity("PERM_CUSTOMER_VIEW", "customers:view", "View Customers", "Customers", "View customer directory and ledgers"),
            PermissionEntity("PERM_CUSTOMER_CREATE", "customers:create", "Add Customer", "Customers", "Register new customers"),
            PermissionEntity("PERM_CUSTOMER_EDIT", "customers:edit", "Edit Customer", "Customers", "Update customer profile and credit limit"),
            PermissionEntity("PERM_CUSTOMER_DELETE", "customers:delete", "Delete Customer", "Customers", "Delete customer records", isSensitive = true),

            // Suppliers
            PermissionEntity("PERM_SUPPLIER_VIEW", "suppliers:view", "View Suppliers", "Suppliers", "View vendor directory"),
            PermissionEntity("PERM_SUPPLIER_CREATE", "suppliers:create", "Add Supplier", "Suppliers", "Register new vendors"),
            PermissionEntity("PERM_SUPPLIER_EDIT", "suppliers:edit", "Edit Supplier", "Suppliers", "Update supplier details"),
            PermissionEntity("PERM_SUPPLIER_DELETE", "suppliers:delete", "Delete Supplier", "Suppliers", "Delete supplier records", isSensitive = true),

            // Purchases
            PermissionEntity("PERM_PURCHASE_VIEW", "purchases:view", "View Purchases", "Purchases", "View purchase orders"),
            PermissionEntity("PERM_PURCHASE_CREATE", "purchases:create", "Create Purchase Order", "Purchases", "Create purchase orders"),
            PermissionEntity("PERM_PURCHASE_EDIT", "purchases:edit", "Edit Purchase Order", "Purchases", "Update purchase orders"),
            PermissionEntity("PERM_PURCHASE_DELETE", "purchases:delete", "Delete Purchase Order", "Purchases", "Cancel or delete purchase orders", isSensitive = true),

            // Expenses
            PermissionEntity("PERM_EXPENSE_VIEW", "expenses:view", "View Expenses", "Expenses", "View business expenses"),
            PermissionEntity("PERM_EXPENSE_CREATE", "expenses:create", "Record Expense", "Expenses", "Record new operational expenses"),
            PermissionEntity("PERM_EXPENSE_EDIT", "expenses:edit", "Edit Expense", "Expenses", "Update expense records"),
            PermissionEntity("PERM_EXPENSE_DELETE", "expenses:delete", "Delete Expense", "Expenses", "Delete expense records", isSensitive = true),

            // Income
            PermissionEntity("PERM_INCOME_VIEW", "income:view", "View Income", "Income", "View non-invoice income entries"),
            PermissionEntity("PERM_INCOME_CREATE", "income:create", "Record Income", "Income", "Record additional income"),
            PermissionEntity("PERM_INCOME_EDIT", "income:edit", "Edit Income", "Income", "Update income entries"),
            PermissionEntity("PERM_INCOME_DELETE", "income:delete", "Delete Income", "Income", "Delete income entries", isSensitive = true),

            // CashBook
            PermissionEntity("PERM_CASHBOOK_VIEW", "cashbook:view", "View Cash Book", "CashBook", "View daily cash register entries"),
            PermissionEntity("PERM_CASHBOOK_ENTRY", "cashbook:entry", "Add Cash Entry", "CashBook", "Add cash in/out entries"),

            // Reports
            PermissionEntity("PERM_REPORTS_VIEW", "reports:view", "View Reports", "Reports", "View standard business reports"),
            PermissionEntity("PERM_REPORTS_EXPORT", "reports:export", "Export Financials", "Reports", "Export financial profit/loss statements", isSensitive = true),

            // User Management
            PermissionEntity("PERM_USERS_VIEW", "users:view", "View Users", "UserManagement", "View staff directory and profiles"),
            PermissionEntity("PERM_USERS_MANAGE", "users:manage", "Manage Users", "UserManagement", "Create and edit user accounts", isSensitive = true),
            PermissionEntity("PERM_USERS_SECURITY", "users:security", "Manage Security", "UserManagement", "Manage device trust and active sessions", isSensitive = true),

            // Role Management
            PermissionEntity("PERM_ROLES_VIEW", "roles:view", "View Roles", "RoleManagement", "View role definitions and permissions"),
            PermissionEntity("PERM_ROLES_MANAGE", "roles:manage", "Manage Access Rights", "RoleManagement", "Create custom roles and assign permissions", isSensitive = true, requiresPinConfirmation = true),

            // Settings
            PermissionEntity("PERM_SETTINGS_VIEW", "settings:view", "View Settings", "Settings", "View app settings"),
            PermissionEntity("PERM_SETTINGS_MANAGE", "settings:manage", "Modify Settings", "Settings", "Change business profile and system configurations", isSensitive = true),

            // Audit
            PermissionEntity("PERM_AUDIT_VIEW", "audit:view", "View Audit Logs", "AuditLogs", "Access login history and security audit trail")
        )

        permissionDao.insertPermissions(permissions)

        // 2. Seed Default System Roles
        val roles = listOf(
            RoleEntity(
                roleId = "ROLE_SUPER_ADMIN",
                roleName = "Super Admin",
                roleCode = "SUPER_ADMIN",
                description = "Full unrestricted administrative access to all system capabilities, security controls, and audit logs.",
                isSystemRole = true,
                isCustomRole = false
            ),
            RoleEntity(
                roleId = "ROLE_ADMIN",
                roleName = "Admin",
                roleCode = "ADMIN",
                description = "Full operational and reporting access excluding root system modifications.",
                isSystemRole = true,
                isCustomRole = false
            ),
            RoleEntity(
                roleId = "ROLE_MANAGER",
                roleName = "Manager",
                roleCode = "MANAGER",
                description = "Manages daily operations, sales, inventory, customers, suppliers, and standard financial reports.",
                isSystemRole = true,
                isCustomRole = false
            ),
            RoleEntity(
                roleId = "ROLE_ACCOUNTANT",
                roleName = "Accountant",
                roleCode = "ACCOUNTANT",
                description = "Focuses on financial entries, invoices, expenses, income, cash book, and balance statements.",
                isSystemRole = true,
                isCustomRole = false
            ),
            RoleEntity(
                roleId = "ROLE_CASHIER",
                roleName = "Cashier",
                roleCode = "CASHIER",
                description = "Front-desk counter role for fast POS billing, collecting payments, and customer registration.",
                isSystemRole = true,
                isCustomRole = false
            ),
            RoleEntity(
                roleId = "ROLE_SALES_EXEC",
                roleName = "Sales Executive",
                roleCode = "SALES_EXEC",
                description = "Handles sales quotes, customer profiles, and invoice generation.",
                isSystemRole = true,
                isCustomRole = false
            ),
            RoleEntity(
                roleId = "ROLE_WAREHOUSE_STAFF",
                roleName = "Warehouse Staff",
                roleCode = "WAREHOUSE_STAFF",
                description = "Manages stock counts, inventory items, and purchase order receipts.",
                isSystemRole = true,
                isCustomRole = false
            ),
            RoleEntity(
                roleId = "ROLE_READ_ONLY",
                roleName = "Read Only User",
                roleCode = "READ_ONLY",
                description = "Auditor or view-only access without edit or create permissions.",
                isSystemRole = true,
                isCustomRole = false
            )
        )

        roleDao.insertRoles(roles)

        // 3. Map Permissions to Roles
        val allPermIds = permissions.map { it.permissionId }
        val crossRefs = mutableListOf<RolePermissionCrossRef>()

        // Super Admin -> All Permissions
        allPermIds.forEach { permId ->
            crossRefs.add(RolePermissionCrossRef("ROLE_SUPER_ADMIN", permId))
            crossRefs.add(RolePermissionCrossRef("ROLE_ADMIN", permId))
        }

        // Manager Permissions
        val managerPerms = permissions.filter { 
            !it.permissionCode.startsWith("roles:manage") && 
            !it.permissionCode.startsWith("users:security")
        }.map { it.permissionId }
        managerPerms.forEach { permId ->
            crossRefs.add(RolePermissionCrossRef("ROLE_MANAGER", permId))
        }

        // Accountant Permissions
        val accountantPerms = listOf(
            "PERM_DASHBOARD_VIEW", "PERM_INVOICE_VIEW", "PERM_INVOICE_CREATE", "PERM_INVOICE_PRINT",
            "PERM_CUSTOMER_VIEW", "PERM_SUPPLIER_VIEW", "PERM_PURCHASE_VIEW",
            "PERM_EXPENSE_VIEW", "PERM_EXPENSE_CREATE", "PERM_EXPENSE_EDIT",
            "PERM_INCOME_VIEW", "PERM_INCOME_CREATE", "PERM_INCOME_EDIT",
            "PERM_CASHBOOK_VIEW", "PERM_CASHBOOK_ENTRY",
            "PERM_REPORTS_VIEW", "PERM_REPORTS_EXPORT", "PERM_AUDIT_VIEW"
        )
        accountantPerms.forEach { permId ->
            crossRefs.add(RolePermissionCrossRef("ROLE_ACCOUNTANT", permId))
        }

        // Cashier Permissions
        val cashierPerms = listOf(
            "PERM_DASHBOARD_VIEW", "PERM_INVOICE_VIEW", "PERM_INVOICE_CREATE", "PERM_INVOICE_PRINT",
            "PERM_CUSTOMER_VIEW", "PERM_CUSTOMER_CREATE", "PERM_INVENTORY_VIEW", "PERM_CASHBOOK_VIEW", "PERM_CASHBOOK_ENTRY"
        )
        cashierPerms.forEach { permId ->
            crossRefs.add(RolePermissionCrossRef("ROLE_CASHIER", permId))
        }

        // Sales Exec Permissions
        val salesPerms = listOf(
            "PERM_DASHBOARD_VIEW", "PERM_INVOICE_VIEW", "PERM_INVOICE_CREATE", "PERM_INVOICE_PRINT",
            "PERM_CUSTOMER_VIEW", "PERM_CUSTOMER_CREATE", "PERM_CUSTOMER_EDIT", "PERM_INVENTORY_VIEW"
        )
        salesPerms.forEach { permId ->
            crossRefs.add(RolePermissionCrossRef("ROLE_SALES_EXEC", permId))
        }

        // Warehouse Staff Permissions
        val warehousePerms = listOf(
            "PERM_DASHBOARD_VIEW", "PERM_INVENTORY_VIEW", "PERM_INVENTORY_CREATE", "PERM_INVENTORY_EDIT",
            "PERM_INVENTORY_ADJUST", "PERM_PURCHASE_VIEW", "PERM_PURCHASE_CREATE", "PERM_SUPPLIER_VIEW"
        )
        warehousePerms.forEach { permId ->
            crossRefs.add(RolePermissionCrossRef("ROLE_WAREHOUSE_STAFF", permId))
        }

        // Read Only Permissions
        val readOnlyPerms = listOf(
            "PERM_DASHBOARD_VIEW", "PERM_INVOICE_VIEW", "PERM_INVENTORY_VIEW", "PERM_CUSTOMER_VIEW",
            "PERM_SUPPLIER_VIEW", "PERM_PURCHASE_VIEW", "PERM_EXPENSE_VIEW", "PERM_INCOME_VIEW",
            "PERM_CASHBOOK_VIEW", "PERM_REPORTS_VIEW"
        )
        readOnlyPerms.forEach { permId ->
            crossRefs.add(RolePermissionCrossRef("ROLE_READ_ONLY", permId))
        }

        rolePermissionDao.insertRolePermissionCrossRefs(crossRefs)

        // 4. Assign default Super Admin role to existing users if any
        val users = userDao.getAllUsersList()
        users.forEach { user ->
            userRoleDao.insertUserRoleCrossRef(
                UserRoleCrossRef(
                    userId = user.userId,
                    roleId = "ROLE_SUPER_ADMIN",
                    assignedBy = "SYSTEM"
                )
            )
        }
    }
}
