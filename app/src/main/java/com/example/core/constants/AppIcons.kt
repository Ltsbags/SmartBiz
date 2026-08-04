package com.example.core.constants

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object AppIcons {
    // Navigation Icons
    val Dashboard: ImageVector = Icons.Outlined.Dashboard
    val Invoices: ImageVector = Icons.Outlined.ReceiptLong
    val Inventory: ImageVector = Icons.Outlined.Inventory2
    val Customers: ImageVector = Icons.Outlined.People
    val Settings: ImageVector = Icons.Outlined.Settings

    // Filled Navigation
    val DashboardFilled: ImageVector = androidx.compose.material.icons.Icons.Filled.Speed
    val InvoicesFilled: ImageVector = Icons.Filled.ReceiptLong
    val InventoryFilled: ImageVector = Icons.Filled.Inventory2
    val CustomersFilled: ImageVector = Icons.Filled.People
    val SettingsFilled: ImageVector = Icons.Filled.Settings

    // Common Actions
    val Add: ImageVector = Icons.Filled.Add
    val Edit: ImageVector = Icons.Filled.Edit
    val Delete: ImageVector = Icons.Filled.Delete
    val Search: ImageVector = Icons.Filled.Search
    val Filter: ImageVector = Icons.Filled.FilterList
    val Sort: ImageVector = Icons.Filled.Sort
    val Close: ImageVector = Icons.Filled.Close
    val Check: ImageVector = Icons.Filled.Check
    val Back: ImageVector = Icons.Filled.ArrowBack
    val ChevronRight: ImageVector = Icons.Filled.ChevronRight
    val More: ImageVector = Icons.Filled.MoreVert
    val Share: ImageVector = Icons.Filled.Share

    // Form & Input Icons
    val Calendar: ImageVector = Icons.Filled.CalendarToday
    val Currency: ImageVector = Icons.Filled.AttachMoney
    val TaxGST: ImageVector = Icons.Filled.Percent
    val Phone: ImageVector = Icons.Filled.Phone
    val Email: ImageVector = Icons.Filled.Email
    val Notes: ImageVector = Icons.Filled.Description
    val Number: ImageVector = Icons.Filled.Numbers
    val Business: ImageVector = Icons.Filled.Business
    val Store: ImageVector = Icons.Filled.Store

    // Metrics & Status
    val Growth: ImageVector = Icons.Filled.TrendingUp
    val Warning: ImageVector = Icons.Filled.Warning
    val Success: ImageVector = Icons.Filled.CheckCircle
    val Error: ImageVector = Icons.Filled.Error
    val Info: ImageVector = Icons.Filled.Info
    val Notification: ImageVector = Icons.Filled.Notifications
    val Star: ImageVector = Icons.Filled.Star

    // Quick Action & Dashboard Shortcuts
    val Sales: ImageVector = Icons.Filled.AttachMoney
    val Paid: ImageVector = Icons.Filled.AttachMoney
    val Pending: ImageVector = Icons.Filled.Description
    val LowStock: ImageVector = Icons.Filled.Warning
    val AddInvoice: ImageVector = Icons.Filled.Add
    val AddProduct: ImageVector = Icons.Filled.Inventory2
    val AddCustomer: ImageVector = Icons.Filled.People
    val Reports: ImageVector = Icons.Filled.TrendingUp
}
