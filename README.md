# Capsule Card Camera

Capsule Card Camera is an Android Compose camera prototype focused on playful card-style photo capture, on-device Gemini Nano photo analysis, and generated frame management.

## Features

- CameraX preview and capture in Jetpack Compose.
- Two home camera modes:
  - Pull-list camera with pull-down countdown capture.
  - Stage camera with a dedicated video-style shutter interface.
- Gemini Nano based on-device photo understanding and custom frame suggestion.
- Custom frame management UI for creating and setting default photo frames.
- Maestro smoke flows and Compose UI tests for camera/settings/frame flows.

## Requirements

- Android Studio or Android SDK command line tools.
- JDK 17.
- A device or emulator for CameraX and connected UI tests.

## Common Commands

```sh
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:installDebug
```

Connected tests require an available Android device or emulator.
