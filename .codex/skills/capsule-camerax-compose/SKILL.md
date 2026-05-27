---
name: capsule-camerax-compose
description: Use when modifying CameraX preview, image capture, camera permissions, PreviewView interop, ImageProxy handling, or photo saving in capsule-card-camera.
---

# CameraX And Compose

## When To Use

Use this skill for changes touching `PreviewView`, `AndroidView`, `ProcessCameraProvider`, `ImageCapture`, `ImageProxy`, camera permissions, capture errors, rotation, lens facing, or saving captured photos.

## Local Context

- `MainScreen.kt` embeds `PreviewView` through `AndroidView`.
- `BindCameraPreview` owns `ProcessCameraProvider` binding and clears `ImageCapture` on dispose.
- `capturePhoto` uses `ImageCapture.takePicture` with `OnImageCapturedCallback`, converts the returned `ImageProxy` to a scaled bitmap, and closes the proxy in `finally`.
- `PhotoFrames.kt` handles framed bitmap rendering and MediaStore/public Pictures persistence.

## Working Rules

1. Keep CameraX lifecycle ownership explicit. Bind use cases only after `PreviewView` and `LifecycleOwner` are available, and unbind on disposal or rebinding.
2. Keep `PreviewView` interop isolated behind a composable boundary. Do not leak `PreviewView` into ViewModel or repository state.
3. Pass the ready `ImageCapture?` through callbacks or a small local holder. Do not store CameraX use cases in persistent UI state objects.
4. Always handle capture error and cleanup paths. Any `ImageProxy` must be closed exactly once, including on conversion failure.
5. If adding rotation or aspect-ratio behavior, align preview and capture configuration instead of tuning only one use case.
6. If changing persistence, preserve Android Q+ MediaStore behavior and pre-Q public Pictures fallback unless the user asks to drop old API support.
7. Camera permission changes must preserve the current user flow: request on first meaningful camera expansion, then render a non-crashing UI for denied permission.

## Validation

- For capture conversion or save helpers, add or run local unit tests where feasible.
- For preview, permission, or capture lifecycle changes, run `./gradlew :app:connectedDebugAndroidTest` on a device/emulator when available.
- If connected tests cannot run, at minimum run `./gradlew :app:testDebugUnitTest` and call out that CameraX runtime behavior still needs device verification.

## Source Discipline

The CameraX lifecycle rules are based on Android's open-source CameraX sample. Refresh `references/upstream-sources.md` before introducing new CameraX patterns such as analysis, video, extensions, or custom resolution negotiation.
