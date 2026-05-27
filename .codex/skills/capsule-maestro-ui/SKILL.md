---
name: capsule-maestro-ui
description: Use when adding, editing, or running Maestro CLI UI flows for capsule-card-camera, especially debug deep-link launches, Compose testTag selectors, and device-side smoke checks.
---

# Capsule Maestro UI

## When To Use

Use this skill for mobile.dev Maestro CLI work in this repository: flow authoring, smoke-flow maintenance, selector strategy, and device-side verification planning.

Do not use this skill for the unrelated `.agents/skills/maestro` governance plugin. Keep all project-specific guidance inside this repository.

## Local Context

- App id: `com.example.capsulecardcamera`.
- Debug build exposes `piccam://maestro` in `app/src/debug/AndroidManifest.xml`.
- Starter Maestro flows live under `.maestro/flows/`.
- The previously reliable local binary path was `/Users/stevelife/.maestro/bin/maestro`; allow `MAESTRO_BIN` to override it.
- `MainScreen.kt` and `CameraSettingsScreen.kt` opt into `testTagsAsResourceId`, so stable Compose `testTag` values can be used as Maestro `id` selectors.

## Flow Authoring Rules

1. Start app flows with `stopApp`, then `openLink: piccam://maestro`, then assert `pull-island-camera-demo`.
2. Prefer `id` selectors backed by stable Compose `testTag` values. If adding a new screen root, make sure its subtree exposes tags through `semantics { testTagsAsResourceId = true }` before relying on IDs in Maestro.
3. Avoid `waitForAnimationToEnd` on the camera home screen. It is camera-heavy and animated; prefer explicit `assertVisible` checks against stable targets.
4. Keep Maestro flows at smoke-test level. Detailed state permutations should stay in Compose instrumentation tests under `app/src/androidTest`.
5. Camera expansion, image capture, permissions, and media persistence are device-sensitive. Run those flows only when a device or emulator is available and the user has asked for runtime validation.
6. If a provider panel is already visible, assert its content instead of forcing an extra tap. This reduces flaky retries on dynamic AI panels.

## Commands

Use these only when the user asks to validate with Maestro:

```sh
MAESTRO_BIN="${MAESTRO_BIN:-/Users/stevelife/.maestro/bin/maestro}"
"$MAESTRO_BIN" check-syntax .maestro/flows/*.yaml
"$MAESTRO_BIN" test .maestro/flows/00-home-settings-smoke.yaml
```

Use these for device setup before a runtime flow:

```sh
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Validation

- Skill or flow-only changes: parse YAML and run syntax-only checks first. Do not execute flows unless requested.
- Runtime UI verification: run the narrowest relevant Maestro flow first, then fall back to `./gradlew :app:connectedDebugAndroidTest` only when broader Compose coverage is needed.
- If no device or emulator is available, say that Maestro runtime behavior is unverified.

## Source Discipline

Maestro command syntax and Compose selector guidance are based on the official Maestro docs listed in `references/upstream-sources.md`. Refresh those docs before adding less common commands or cloud/device-matrix behavior.
