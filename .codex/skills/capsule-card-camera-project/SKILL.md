---
name: capsule-card-camera-project
description: Use when working in the capsule-card-camera Android project, especially Compose UI, ViewModel state, tests, Gradle, or repository conventions.
---

# Capsule Card Camera Project

## When To Use

Use this skill for general work in this repository: UI changes, ViewModel state, repository boundaries, Gradle configuration, tests, and project documentation.

Use the more focused local skills when the task is specifically about CameraX or animation.

## Project Shape

- Single Android app module: `app`.
- Kotlin, Jetpack Compose, Material 3, CameraX, Navigation 3.
- Main UI lives under `app/src/main/java/com/example/capsulecardcamera/ui/main/`.
- Current camera experience is a one-screen pull-down island camera demo with frame selection and photo-wall UI.
- Unit tests live under `app/src/test`; Compose instrumented tests live under `app/src/androidTest`.

## Working Rules

1. Read the relevant local file before changing behavior. This app is compact enough that nearby state, UI, and camera code usually matter.
2. Keep scope narrow. Do not introduce multi-module architecture, DI, database, network, or app-wide framework changes unless the user asks.
3. Model screen state explicitly. When state grows beyond local-only interaction state, prefer a ViewModel-owned `StateFlow` UI state shape, following the open-source architecture samples listed in `references/upstream-sources.md`.
4. Keep Compose content testable. Preserve stable `testTag` and semantics where tests or expected UI flows depend on them.
5. Treat `MainScreen.kt` camera and gesture code as lifecycle-sensitive. For camera-specific changes, switch to the `capsule-camerax-compose` skill.
6. Treat motion as product behavior, not decoration. For animation-specific changes, switch to the `capsule-compose-animation` skill.

## Validation

- Documentation or skill-only changes: run `git diff --check`.
- Pure ViewModel/state logic: run `./gradlew :app:testDebugUnitTest`.
- UI semantics, CameraX, permissions, or pull interaction changes: run `./gradlew :app:connectedDebugAndroidTest` when a device or emulator is available.

## Source Discipline

This skill intentionally depends on open-source Android samples. If a future change needs a new pattern, inspect an open-source source file first and add the source to `references/upstream-sources.md`.
