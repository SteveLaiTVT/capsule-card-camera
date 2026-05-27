# Upstream Sources

Inspected on 2026-05-25 from a read-only shallow clone.

## Android Camera Samples: CameraXBasic

- Repository: https://github.com/android/camera-samples
- Commit inspected: `7f4e3b0a48d3b92468b72ef01a29363039874375`
- License: Apache-2.0
- Source: https://github.com/android/camera-samples/blob/7f4e3b0a48d3b92468b72ef01a29363039874375/CameraXBasic/app/src/main/java/com/android/example/cameraxbasic/fragments/CameraFragment.kt#L260-L324
- Local lesson: construct matching preview, capture, and optional analysis use cases with rotation/aspect settings, unbind before rebinding, bind to lifecycle, then attach the preview surface provider.

- Source: https://github.com/android/camera-samples/blob/7f4e3b0a48d3b92468b72ef01a29363039874375/CameraXBasic/app/src/main/java/com/android/example/cameraxbasic/fragments/CameraFragment.kt#L485-L500
- Local lesson: capture callbacks must handle both error and success paths; file/MediaStore output should be treated as asynchronous CameraX work, not a synchronous UI action.

- Source: https://github.com/android/camera-samples/blob/7f4e3b0a48d3b92468b72ef01a29363039874375/CameraXBasic/app/src/androidTest/java/com/android/example/cameraxbasic/CameraPreviewTest.kt
- Local lesson: CameraX behavior deserves device/emulator checks when touching provider availability, camera selectors, or lifecycle binding.
