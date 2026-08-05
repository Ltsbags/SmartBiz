# SmartBiz — Mobile Business Management & POS Platform

SmartBiz is an enterprise-grade, offline-first mobile business management, billing, inventory control, and financial tracking platform built with modern **Android, Kotlin, Jetpack Compose, Clean Architecture, and SQLite Room**.

Designed for small-to-medium businesses, retail stores, traders, and service providers, SmartBiz provides a seamless offline experience for invoicing, customer credit ledger tracking, supplier accounts, inventory management, cash book tracking, and real-time business analytics.

---

## 🌟 Key Features

- **Offline-First Architecture**: 100% operational offline without remote cloud server dependencies or external tracking SDKs.
- **Dynamic Invoicing & Billing**: Create professional invoices, apply discounts and taxes, track payment status (Paid, Partial, Overdue), and export thermal/PDF receipts.
- **Inventory & Stock Tracking**: Real-time stock updates, low-stock alerts, product categories, SKU/barcode lookup, and batch price adjustments.
- **Customer Ledger & Credit**: Manage customer profiles, track outstanding balances, record cash/online payments, and view detailed account ledgers.
- **Supplier & Purchase Management**: Record purchases, manage supplier contacts, track payables, and maintain order histories.
- **Expenses & Income Cash Book**: Log daily operational expenses, non-sale income entries, and maintain an automated dual-entry Cash Book.
- **Global Instant Search**: Perform multi-entity searches across products, customers, invoices, suppliers, expenses, and purchases with instant filtering and search history.
- **Business Intelligence & Analytics**: Interactive charts, profit & loss summaries, top-selling products, sales velocity, and tax reports.
- **Data Backup & Restore**: Encrypted local database backup, checksum integrity verification, auto-backup, and CSV export for Excel/Tally.
- **Material 3 Design System**: Responsive edge-to-edge UI, dark/light theme switching, dynamic accent colors, and accessible components.

---

## 📐 Architecture Overview

SmartBiz strictly follows **Clean Architecture** principles and the **MVVM (Model-View-ViewModel)** design pattern:

```
SmartBiz Architecture
├── Presentation Layer (Jetpack Compose UI, ViewModels, UI States)
├── Domain Layer (Use cases, Repositories interfaces, Business Rules)
└── Data Layer (Room Database, DAOs, Entities, SharedPreferences, File Service)
```

- **UI Framework**: 100% Jetpack Compose with Material 3 components.
- **State Management**: Kotlin `StateFlow`, `MutableStateFlow`, and `collectAsStateWithLifecycle()`.
- **Local Database**: SQLite with Room ORM v2.6.1, KSP compiler, foreign key constraints, and performance indexes.
- **Asynchronous Operations**: Kotlin Coroutines and Flow pipelines.

---

## 📁 Repository Directory Structure

```
app/src/main/java/com/example/
├── core/
│   ├── constants/        # Global constants and app defaults
│   ├── database/         # Room Database, DAOs, Entities, Maintenance & Helpers
│   ├── services/         # SharedPreferences, Backup & CSV Export Services
│   └── utils/            # Formatters, Validators, Exception Handlers & AppLogger
├── features/
│   ├── cashbook/         # Cash Book entries & ledger screens
│   ├── customers/        # Customer profiles, ledger & balance management
│   ├── dashboard/        # Business performance summary & quick actions
│   ├── expenses/         # Operational expense tracking & categories
│   ├── income/           # Non-sale income entries & history
│   ├── inventory/        # Product catalog, stock alerts & pricing
│   ├── invoice/          # Invoice creation, view, payment & PDF/thermal print
│   ├── purchases/        # Supplier purchases & stock inward
│   ├── reports/          # Financial reports, charts & profit/loss analytics
│   ├── search/           # Global database instant search & filtering
│   ├── settings/         # Business profile, invoice terms, backup & theme
│   └── suppliers/        # Supplier directory & payables
├── repositories/         # Repository implementations (Clean Architecture Data Layer)
├── shared/
│   ├── components/       # AppShell, Navigation Drawer, Bottom Navigation Bar
│   └── widgets/          # Reusable UI widgets, PageHeaders, Skeleton loaders, Empty states
├── theme/                # Material 3 Color Schemes, Typography & Shapes
└── MainActivity.kt       # Application entry point & Jetpack Navigation Compose graph
```

---

## 🛠️ Getting Started & Build Instructions

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or IntelliJ IDEA
- JDK 17 or higher
- Android SDK 34 (Android 14 / UpsideDownCake)
- Minimum Supported SDK: Android 7.0 (API Level 24)

### Building Debug & Release Outputs
Execute standard Gradle commands:

```bash
# Build Debug APK
gradle :app:assembleDebug

# Build Release APK
gradle :app:assembleRelease

# Build Production Android App Bundle (AAB) for Google Play
gradle :app:bundleRelease

# Run Unit Tests
gradle :app:testDebugUnitTest
```

---

## 🔒 Security & Data Safety

- **Local-Only Storage**: All database records remain on the physical user device.
- **Zero Tracking**: No analytics, telemetry, crashlytics, or advertising SDKs.
- **Input Sanitization**: Regex validation for Phone, Email, GSTIN, PAN, and numeric values.
- **SQL Injection Prevention**: Room ORM compiled SQLite parametrized query binding.
- **Backup Safety**: Checksum validation prevents restoring corrupted or incomplete backup files.

---

## 📄 License & Terms

Commercial Enterprise Edition. All rights reserved.
