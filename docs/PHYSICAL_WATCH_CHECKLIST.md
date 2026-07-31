# OnePlus Watch 3 physical validation

Record the watch firmware, Wear OS version, API level, Health Services version, and app version before each test cycle.

## Capability and permissions

- Confirm the home-screen diagnostic reports running, location, distance, heart rate, and the expected hardware-button count.
- Record unsupported Health Services data types.
- Test precise versus approximate location. Approximate-only must not start a normal run.
- Test location and activity-recognition denial, then grant from system settings.
- Test heart-rate denial; the run must continue as GPS-only.
- Test foreground and background heart-rate permission flows on the installed API level.
- Test notification denial on API 33+ and verify the foreground-service disclosure remains available.

## Session behavior

- Cold-start GPS acquisition and the 20-second Start Anyway action.
- Start, pause, resume, and end by touch.
- Pause/resume with each assignable stem button; record emitted keycodes using `adb logcat` if mappings differ.
- Repeatedly press controls and verify no double transition or accidental end.
- Put the screen to sleep and return to the watch face; verify the Ongoing Activity returns to the active session.
- Kill the app process without force-stop and reopen; verify Health Services reconnection is reported without claiming to restore discarded metrics.
- Force-stop or reboot and verify the app does not fabricate or persist a workout record.

## Sensor and route tests

- Open sky, trees, urban obstruction, and tunnel entry/exit.
- Confirm pace becomes stale after five seconds without accepted fixes and unavailable after ten.
- Confirm no distance or pace is bridged over pauses and route gaps.
- Compare against a known measured route or trusted recorder; target at most 2% distance and 3% average-pace error.
- Compare heart-rate samples with the native Health Services stream. Do not interpret results as medical measurements.

## Reliability and power

- Run at least one two-hour recording with the display mostly off.
- Repeat short start/end cycles and rapid pause/resume cycles.
- Test OnePlus normal smart mode and each available battery/power saver mode.
- Record whether RTOS/power switching changes callbacks, GPS accuracy, notifications, or service survival.
- Record battery percentage before and after standardized 60-minute runs.
- Verify active duration has no missing intervals after a 60-minute screen-off test.

## Display-only behavior and upgrade

- End a run, dismiss the in-memory summary, and verify no recent-run history is shown.
- Install a newer APK with `adb install -r` and verify user display settings remain intact.
- Verify the release APK with `apksigner verify --verbose` before sideloading.
