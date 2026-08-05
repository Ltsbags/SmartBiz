# SmartBiz — Production Deployment & Release Guide

This document outlines the security policies, build optimizations, R8/ProGuard rules, and Google Play Store release guidelines for SmartBiz.

---

## 🔒 Production Security & Safeguards

- **Strict Input Validation**: All input fields (Customer Phone, Email, Pricing, Quantity, GSTIN) are validated using `Validators.kt` before persisting.
- **SQL Parametrized Binding**: All raw queries in Room DAOs use bound parameters to eliminate SQL injection vulnerabilities.
- **Local Data Protection**: User database files remain inside the private app sandbox directory (`/data/data/com.aistudio.smartbiz/databases/`).
- **Backup Verification**: Backup imports execute `DatabaseBackupService.kt` checksum verification to reject tampered or corrupted backup JSON files.

---

## 🚀 Release Optimizations

1. **R8 Code Shrinking & ProGuard**: Enable code obfuscation and dead-code removal in `app/build.gradle.kts`.
2. **Resource Shrinking**: Removes unused vector drawables and assets.
3. **Android App Bundle (AAB)**: Google Play generates optimized split APKs tailored to device screen density, language, and CPU architecture (arm64-v8a, armeabi-v7a, x86_64).
4. **Disabled Debug Logging**: `AppLogger.kt` conditionally strips debug logs (`d()`) in release mode (`BuildConfig.DEBUG == false`).

---

## 📋 Google Play Release Checklist

- [x] Application ID verified: `com.aistudio.smartbiz.mvp`
- [x] Target SDK set to 34 (Android 14) and Min SDK set to 24 (Android 7.0)
- [x] Version Code and Version Name configured (`1.0.0`)
- [x] Adaptive Launcher Icon and monochrome icon configured
- [x] All local unit tests executed (`gradle :app:testDebugUnitTest`)
- [x] Release build compiled successfully (`gradle :app:bundleRelease`)
- [x] Privacy Policy & Data Safety declaration: 100% offline app with zero user tracking or data collection
