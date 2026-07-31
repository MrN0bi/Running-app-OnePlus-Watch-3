# Watch Running

A standalone, watch-only Wear OS running display built for the OnePlus Watch 3. Workout metrics are held in memory for the current display session and are not saved. The app has no phone, account, network, cloud, map, or Health Connect components.

## Implemented

- Health Services capability probe for running, location, distance, speed, pace, heart rate, statistics, auto-pause support, and hardware-button count.
- Capability-filtered `ExerciseClient` preparation and workout configuration.
- Hybrid location keeps direct watch GNSS (`GPS_PROVIDER`), fused watch/phone/Wi-Fi location, and Health Services watch/phone location active together. Only fresh fixes with reported horizontal accuracy of 25 m or better are considered; the most accurate stable source drives pace and distance with hysteresis to prevent rapid source oscillation.
- Same-process `START_STICKY` foreground service with `health|location` types, private workout notification, and Wear Ongoing Activity.
- Health Services-authoritative prepare/start/pause/resume/end state handling with serialized, debounced commands.
- Runtime permission flows for API 30–35 sensors and API 36 health permissions; heart-rate denial degrades to GPS-only.
- Five-second GPS pace by default, with 3/5/10-second settings, point validation, weighted regression, residual filtering, flicker suppression, and stale/unavailable states.
- Health-rate zones from age or manual maximum HR, zone indicator, and active-only zone-time accumulation.
- Preferences DataStore for age, maximum-HR mode, manual maximum HR, and smoothing window.
- Round Compose UI for start, acquisition, active workout, paused controls, end confirmation, an in-memory end summary, and settings.
- Acquisition diagnostics show Health Services availability, selected location source, horizontal accuracy, and the 25 m readiness threshold.
- Foreground stem-key pause/resume handling and touch fallback.
- Backup disabled and no Internet, storage, Bluetooth, phone, wake-lock, or background-location permission.

## Build

Requirements:

- JDK 17
- Android SDK Platform 36 and Build Tools 36.0.0
- Internet access for the first dependency resolution

The Gradle wrapper is included:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The app keeps `compileSdk` and `targetSdk` at 36. `androidx.core` is pinned to 1.17.0 and Lifecycle to 2.10.0 because the later versions named in the original plan now require compile SDK 37; API 37 is outside the requested Wear OS 6 compile boundary. Other planned dependency versions remain pinned.

## Sideload

Enable ADB and wireless debugging on the watch, then:

```powershell

$adb = "$env:TEMP\watch-running-tools\android-sdk\platform-tools\adb.exe"
& $adb version

& $adb pair <watch-ip>:<pairing-port>

$watchAddress = Read-Host "Enter the IP address and port shown on the main Wireless debugging screen"
& $adb connect $watchAddress

& $adb devices
& $adb install -r "D:\Computer\Documents\VSCode Projects\Watch-running\app\build\outputs\apk\debug\app-debug.apk"
```

`-r` preserves display settings when the application ID and signing certificate are unchanged.

## Physical validation still required

Emulator/build validation cannot answer OnePlus firmware-specific questions. Before treating this as a release APK, complete [the physical watch checklist](docs/PHYSICAL_WATCH_CHECKLIST.md), especially permissions, screen-off delivery, exercise reconnection, button mapping, OnePlus power modes, long-run survival, accuracy, and battery tests.

Ambient rendering is intentionally not enabled in this build: Android's current `AmbientLifecycleObserver` documentation requires the `WAKE_LOCK` permission, while the requested permission policy explicitly forbids it. The foreground service and Ongoing Activity continue the workout when the activity is not visible.
