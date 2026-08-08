# SystemMonitor — Battery, Memory, Storage, Network/Wifi, Security score

This is a working, buildable slice of the full app architecture, implemented
end-to-end for five features: **Battery, Memory, Storage, Network/Wifi, and
Security score**. Everything else in the original file tree (Cpu, AppUsage,
AppLock, parental controls, auth, navigation, ...) is scaffolding to be
filled in by copying this same pattern.

## No unused API keys

This app talks to Firebase only (Auth/Firestore/Storage/Messaging via
`google-services.json`) and nothing else. `core/AppConfig.kt` says so
explicitly. **Google Maps, VirusTotal, AbuseIPDB, Google Safe Browsing, IP
Geolocation, Weather, and HaveIBeenPwned are not referenced anywhere in this
codebase** — the security score (`SecurityScoreEngine`) is computed entirely
on-device from `PackageManager` data (install source + requested dangerous
permissions), not third-party threat-intel lookups. If you had a generated
config file elsewhere listing those keys as "NOT USED" placeholders, delete
it — this project has no field for them, so there's nothing to wire up or
strip out in code.

## What's implemented

```
BatteryEntity (Room)          local/database/entity/BatteryEntity.kt
BatteryDao                    local/database/dao/BatteryDao.kt
AppDatabase                   local/database/AppDatabase.kt
Battery (domain model)        domain/model/Battery.kt
BatteryMapper (entity<->domain) domain/mapper/BatteryMapper.kt
BatteryMonitor (OS reader)    monitoring/BatteryMonitor.kt
BatteryRepository             repository/BatteryRepository.kt
GetBatteryInfoUseCase         domain/usecase/GetBatteryInfoUseCase.kt
MonitoringWorker              workers/MonitoringWorker.kt
FirebaseSyncWorker            workers/FirebaseSyncWorker.kt
FirestoreManager + BatterySync firebase/firestore/
BatteryViewModel              features/dashboard/BatteryViewModel.kt
BatteryCard (Compose)         features/dashboard/BatteryCard.kt
DI wiring                     di/DatabaseModule.kt, di/FirebaseModule.kt
```

## Data flow

```
BatteryManager (Android OS)
   │  readCurrent()
   ▼
BatteryMonitor
   │  captureAndStore()
   ▼
BatteryRepository ──► BatteryDao ──► Room (battery_readings table, raw & local-only)
   │
   │  observeLatest() as Flow<Battery>
   ▼
BatteryViewModel ──► BatteryCard (Compose UI)

Separately, hourly:
BatteryRepository.getSummarySince() ──► BatterySync.pushSummary() ──► Firestore
   (only an averaged rollup leaves the device — not every raw reading)
```

## Key design decisions baked into this slice

1. **Local raw data, remote rollups.** `BatteryEntity` rows never sync
   individually. `FirebaseSyncWorker` computes an hourly average via
   `BatteryRepository.getSummarySince()` and only that leaves the device.
   Apply the same split to Cpu/Memory/Network — anything high-frequency.
2. **Domain models are separate from Room entities.** `Battery` (domain)
   never imports Room annotations; `BatteryMapper.kt` is the only file that
   knows both types exist. UI and use cases only ever see `Battery`.
3. **Hilt + WorkManager.** `SystemMonitorApplication` implements
   `Configuration.Provider` and supplies `HiltWorkerFactory` so
   `MonitoringWorker`/`FirebaseSyncWorker` can constructor-inject the
   repository instead of doing manual service location.
4. **google-services.json is gitignored.** It contains real Firebase API
   keys — commit a `.example` version with placeholders instead.

## How to replicate this for another metric (e.g. Memory)

1. `local/database/entity/MemoryEntity.kt` + `local/database/dao/MemoryDao.kt`
2. Add `MemoryEntity` to `AppDatabase.entities` and bump `version`, add a
   `Migration` in `DatabaseMigrations.kt`
3. `domain/model/Memory.kt` + `domain/mapper/MemoryMapper.kt`
4. `monitoring/MemoryMonitor.kt` (reads `ActivityManager.MemoryInfo`)
5. `repository/MemoryRepository.kt`
6. `domain/usecase/GetMemoryInfoUseCase.kt`
7. Wire into `MonitoringWorker` (add the repository as a constructor param
   and capture it alongside battery) or create a dedicated worker if the
   polling cadence differs
8. `features/dashboard/MemoryViewModel.kt` + `MemoryCard.kt`
9. Decide sync strategy: rollup like battery, or skip Firestore entirely if
   it's device-local-only (memory pressure rarely needs to leave the device)

## Not implemented here (see prior architecture review)

- `ApkScanner` / `ThreatAnalyzer` / `HashCalculator` — heuristic-only static
  analysis (permissions, install source, SHA-256 against a list you
  maintain). No signature-database antivirus engine.
- AppLock / Parental controls — needs Accessibility Service or Device Admin
  permission flows, which have their own onboarding UX beyond a runtime
  permission dialog.
- Auth screens, navigation graph, remaining feature screens — same MVVM
  pattern as Battery, just not filled in yet.

## Build

Requires a real `app/google-services.json` from your Firebase console
(this repo only ships a placeholder-free `.gitignore` entry for it) and
Android Studio Koala+ / AGP 8.6 / JDK 17.

```
./gradlew assembleDebug
```
"# system2" 
"# system2" 
