# SmartBiz — Installation & Build Guide

This guide provides step-by-step instructions for developers and release engineers to build, test, and deploy SmartBiz on Android devices and the Google Play Store.

---

## 💻 System Requirements

| Requirement | Minimum / Recommended |
|---|---|
| **OS** | macOS 13+, Windows 11, or Linux (Ubuntu 22.04 LTS+) |
| **JDK** | OpenJDK 17 or Java Development Kit 17 |
| **Android Studio** | Android Studio Jellyfish / Ladybug (2024.1+) |
| **Android SDK** | API 34 (Android 14) compiled, API 24 (Android 7.0) minimum |
| **Gradle** | 8.7+ with Kotlin 1.9.24+ and KSP 1.9.24-1.0.20 |

---

## 🛠️ Step-by-Step Setup

### 1. Project Import
1. Open Android Studio.
2. Select **Open** and navigate to the project root directory.
3. Allow Gradle to perform the initial project sync and dependency resolution.

### 2. Environment Configuration
The project uses standard Android Gradle configuration:
- Application ID: `com.aistudio.smartbiz.mvp`
- Target SDK: `34`
- Min SDK: `24`
- Version Code: `1`
- Version Name: `1.0.0`

No external API keys or third-party cloud service credentials are required for local build execution.

---

## 🧪 Testing Execution

Run local JVM unit tests:

```bash
# Run unit tests across all core utilities and viewmodel layers
gradle :app:testDebugUnitTest
```

---

## 📦 Generating Production Deliverables

### Debug APK Generation
To generate an unaligned debug build for developer testing:
```bash
gradle :app:assembleDebug
```
Output path: `app/build/outputs/apk/debug/app-debug.apk`

### Release Android App Bundle (AAB) for Google Play
To generate a signed, size-optimized Android App Bundle:
```bash
gradle :app:bundleRelease
```
Output path: `app/build/outputs/bundle/release/app-release.aab`

### Release APK Generation
To generate a standalone APK for direct sideloading or internal QA testing:
```bash
gradle :app:assembleRelease
```
Output path: `app/build/outputs/apk/release/app-release.apk`

---

## 🚀 Deployment Checklist

Before uploading `app-release.aab` to Google Play Console:
1. Verify `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Confirm release signing config in `build.gradle.kts` points to the valid production keystore.
3. Ensure R8 code shrinking (`isMinifyEnabled = true`) and resource shrinking (`isShrinkResources = true`) are configured.
4. Perform local backup and restore smoke test.
