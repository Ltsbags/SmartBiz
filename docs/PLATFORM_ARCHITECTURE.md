# SmartBiz Enterprise Cloud Platform - Architecture Specification

## 1. Executive System Overview
SmartBiz Enterprise is an offline-first, multi-tenant enterprise business management platform engineered for Android (Kotlin / Jetpack Compose / Room) with scalable cloud API integration capability (REST / WebSocket / PostgreSQL / Redis).

### Key Architectural Pillars
- **Clean Architecture & MVVM**: Unidirectional data flow via Kotlin Coroutines and StateFlow.
- **Offline-First Data Layer**: Room Database with pre-aggregated daily metrics cache, offline transaction queue, and delta sync mechanism.
- **Payment Engine Architecture**: Adapter pattern supporting dynamic multi-gateway switching (UPI, Razorpay, Stripe, Cash) with real-time settlement reconciliation and automated accounting ledger updates.
- **Business Intelligence & Forecasting**: Aggregated Daily Metrics cache, custom KPI threshold engine, and Holt-Winters exponential smoothing forecasting.
- **Zero-Trust Security & Compliance**: RBAC policy engine, PII masking, device trust verification, automated audit logging, and compliance policy enforcement (GDPR/HIPAA/PCI-DSS).

---

## 2. Module Dependency & Layer Diagram

```
+-------------------------------------------------------------------------+
|                          JETPACK COMPOSE UI LAYER                        |
|  [POS/Invoices] [Inventory] [CRM] [Payments] [BI Dashboard] [Security]  |
+------------------------------------+------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                            VIEWMODEL LAYER                              |
|   PaymentEngineViewModel | ExecutiveDashboardViewModel | SecurityVM ... |
+------------------------------------+------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                            REPOSITORY LAYER                             |
|    AppRepositoryProvider -> PaymentRepository | ReportingRepository ... |
+------------------------------------+------------------------------------+
                                     |
         +---------------------------+---------------------------+
         |                                                       |
         v                                                       v
+----------------------------------+           +----------------------------------+
|          SERVICES LAYER          |           |            DATA LAYER            |
|  • PaymentEngineService          |           |  • Room Database (AppDatabase)   |
|  • BIService & KPIEngine         | <-------> |  • SharedPreferences & Encrypted |
|  • CommunicationEngine           |           |  • Remote Gateway / WebSocket    |
|  • PolicyEngine (RBAC/Security)  |           |  • Offline Sync Queue            |
+----------------------------------+           +----------------------------------+
```

---

## 3. Core Engine Architecture

### 3.1 Payment Engine & Accounting Integration
- **Gateway Adapters**: `UpiAdapter`, `RazorpayAdapter`, `StripeAdapter`, `OfflineCashAdapter`.
- **Atomic Accounting Updates**: Executed inside `PaymentEngineService` and `RefundService`:
  1. `PaymentEntity` recorded with unique transaction reference.
  2. `CustomerEntity` outstanding balance reduced/increased.
  3. `CustomerLedgerEntity` entry logged (Credit/Debit).
  4. `CashBookEntryEntity` inflow/outflow recorded.
  5. `InvoiceEntity` paid amount, balance amount, and status (`PAID`, `PARTIAL`, `UNPAID`) updated.
- **Reconciliation Service**: Matches gateway settlement statements against local database transactions and flags `UNMATCHED_LOCAL`, `UNMATCHED_GATEWAY`, `AMOUNT_MISMATCH`, or `STATUS_MISMATCH`.

### 3.2 Executive BI & Forecasting Engine
- **Aggregated Daily Metrics Cache**: Pre-aggregates daily sales, expenses, purchases, income, and GST to prevent raw table scanning during dashboard loads.
- **KPI Evaluation Engine**: Evaluates current vs target values against `NORMAL`, `WARNING`, `CRITICAL`, and `TARGET_MET` thresholds.
- **AI Forecasting Engine**: Uses Holt-Winters Exponential Smoothing with trend ($\beta$) and level ($\alpha$) parameters and $95\%$ confidence bounds to predict 30-day revenue and expense trajectories.

### 3.3 Security & Compliance Policy Engine
- **RBAC Policy Evaluator**: Evaluates access rights based on user role (`ADMIN`, `MANAGER`, `STAFF`, `AUDITOR`).
- **Data Protection & PII Masking**: Automatically masks customer mobile numbers and emails unless explicit export permissions exist.
- **Device Trust & Audit Logging**: Validates device hardware signature and records every sensitive action to immutable Room audit log.
