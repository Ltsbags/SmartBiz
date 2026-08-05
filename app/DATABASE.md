# SmartBiz — Database Schema & Optimization Documentation

SmartBiz relies on an optimized local **SQLite Room Database** (v2.6.1) with Write-Ahead Logging (WAL) enabled for high concurrent read performance.

---

## 🗄️ Database Tables & Entities

| Table Name | Primary Key | Description | Key Indexes / FK Constraints |
|---|---|---|---|
| `inventory_items` | `id` (Auto) | Product inventory catalog | `sku` (Unique index), `category` |
| `categories` | `id` (Auto) | Product & transaction categories | `name` (Unique index) |
| `customers` | `id` (Auto) | Customer profiles & ledger | `phone` (Index), `name` |
| `customer_ledger` | `id` (Auto) | Customer credit/debit transactions | Foreign Key `customerId` -> `customers(id)` ON DELETE CASCADE |
| `suppliers` | `id` (Auto) | Supplier contacts & payables | `supplierName` (Index), `phone` |
| `invoices` | `id` (Auto) | Sales invoices | `invoiceNumber` (Unique index), `date` |
| `invoice_items` | `id` (Auto) | Line items inside sales invoices | Foreign Key `invoiceId` -> `invoices(id)` ON DELETE CASCADE |
| `purchases` | `id` (Auto) | Purchase orders from suppliers | `purchaseNumber` (Unique index), `purchaseDate` |
| `expenses` | `id` (Auto) | Operational expense records | `expenseNumber` (Unique index), `expenseDate` |
| `income` | `id` (Auto) | Non-sale income entries | `incomeNumber` (Unique index), `incomeDate` |
| `cash_book_entries` | `id` (Auto) | Dual-entry cash & bank register | `date` (Index), `type` |

---

## ⚡ Database Performance & Integrity Features

1. **Foreign Key Constraints**: Enabled on all relational entities (`invoice_items`, `customer_ledger`) with cascade deletions to prevent orphaned records.
2. **Database Indexing**: Indexes applied on search columns (`invoiceNumber`, `name`, `sku`, `date`, `phone`) to ensure fast multi-table instant global search under 50ms.
3. **Write-Ahead Logging (WAL)**: Enabled by default in `DatabaseHelper` to allow non-blocking concurrent reads during database write operations.
4. **Automated Maintenance (`DatabaseMaintenance.kt`)**:
   - `PRAGMA wal_checkpoint(FULL)`: Flushes write-ahead log pages to main database file.
   - `ANALYZE`: Updates SQLite query planner statistics.
   - `VACUUM`: Reclaims unused database disk space and defragments pages.
