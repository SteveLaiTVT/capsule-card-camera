# Capsule Card Camera

English | [中文](#中文)

Capsule Card Camera is a playful Android camera app built with Jetpack Compose. It turns the camera into a small "dynamic island" photo booth: capture a photo, print it out with a frame, keep the newest thumbnail in the album button, and use on-device AI when the device supports Gemini Nano.

## Features

- CameraX preview and capture in a Compose-first UI.
- Stage camera with shutter, curtain, photo-printing, album flip, and settings entry.
- Dynamic-island camera decoration that adapts to screen cutouts and top insets.
- Custom frame manager where users can write prompts and generate their own photo frame styles.
- Gemini Nano based on-device photo understanding and frame suggestion when the device supports ML Kit GenAI.
- Graceful fallback when on-device AI is unavailable: core camera and frame flows remain usable.
- Maestro smoke flows and unit tests for camera, settings, and frame-management paths.

## Requirements

- Android Studio or Android SDK command line tools.
- JDK 17.
- Android device or emulator for CameraX and Maestro runtime checks.
- A Gemini Nano capable device is optional. Devices without on-device AI still run the core camera experience.

## Common Commands

```sh
./gradlew :app:testDebugUnitTest
./gradlew :app:compileDebugAndroidTestKotlin
./gradlew :app:assembleDebug
```

Maestro syntax and smoke checks:

```sh
MAESTRO_BIN="${MAESTRO_BIN:-/Users/stevelife/.maestro/bin/maestro}"
for flow in .maestro/flows/*.yaml; do "$MAESTRO_BIN" check-syntax "$flow" || exit 1; done
"$MAESTRO_BIN" test .maestro/flows/00-home-settings-smoke.yaml
```

## Release Signing

Release signing is local-only. The keystore and passwords must not be committed.

Recommended local setup:

```properties
CAPSULE_RELEASE_STORE_FILE=/Users/stevelife/.android/capsule-card-camera-release.jks
CAPSULE_RELEASE_STORE_PASSWORD=local-secret
CAPSULE_RELEASE_KEY_ALIAS=capsule-card-camera
CAPSULE_RELEASE_KEY_PASSWORD=local-secret
```

The Gradle release build reads those values from environment variables first, then from the ignored root `local.properties`. The repository ignores `local.properties`, `.env*`, `*.jks`, `*.keystore`, `*.p12`, and `*.pem`.

Build signed release artifacts:

```sh
./gradlew :app:assembleRelease :app:bundleRelease
```

Expected outputs:

- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`

## Distribution Notes

Good low-friction paths for a toy/public preview:

- GitHub Releases: attach the signed APK and AAB. This is the primary public distribution path for this repository.
- Obtainium: users can add the GitHub release source and get update notifications from release pages.
- Pgyer or fir.im: fast QR-code beta distribution for small Android test groups in China.
- Firebase App Distribution: private, invite-based testing with tester management.
- IzzyOnDroid or F-Droid: not the first release target as-is. The current APK is larger than IzzyOnDroid's usual 30 MB rule of thumb, and the Google ML Kit / GenAI dependency would need review or a FLOSS/no-AI flavor before F-Droid-style submission.
- Uptodown or Aptoide Connect: broader third-party store listing after app metadata and ownership verification are ready.

Before wider store submission, prepare a stable icon, screenshots, privacy text, camera permission rationale, AI-availability note, and a non-`com.example` application id. The current release id is `io.github.stevelaitvt.capsulecardcamera`.

## 中文

[English](#capsule-card-camera) | 中文

Capsule Card Camera 是一个用 Jetpack Compose 做的趣味相机应用。它把相机做成类似灵动岛的小拍照台：拍照后像打印相片一样从灵动岛慢慢出来，带上相框，最后缩进左下角相册按钮；设备支持 Gemini Nano 时会使用端侧 AI，不支持时核心拍照功能仍然可用。

## 功能

- 基于 CameraX 的 Compose 相机预览和拍照。
- 拍照台模式支持快门、卷帘动画、相片打印动画、相册翻转和设置入口。
- 灵动岛样式会结合屏幕挖孔、安全区域和顶部间距做适配。
- 相框管理页面支持用户输入提示词，自由生成自己的相框样式。
- 设备支持 ML Kit GenAI / Gemini Nano 时，会使用端侧 AI 做图片理解和相框建议。
- 找不到端侧 AI 时会给用户提示，但拍照、相册、相框等核心流程持续可用。
- 使用 Maestro smoke flow 和单元测试覆盖拍照台、设置、相框管理等关键路径。

## 环境要求

- Android Studio 或 Android SDK 命令行工具。
- JDK 17。
- 用于 CameraX 和 Maestro 运行验证的 Android 真机或模拟器。
- Gemini Nano 设备不是必需条件；不支持端侧 AI 的设备仍可使用核心相机体验。

## 常用命令

```sh
./gradlew :app:testDebugUnitTest
./gradlew :app:compileDebugAndroidTestKotlin
./gradlew :app:assembleDebug
```

Maestro 语法检查和冒烟测试：

```sh
MAESTRO_BIN="${MAESTRO_BIN:-/Users/stevelife/.maestro/bin/maestro}"
for flow in .maestro/flows/*.yaml; do "$MAESTRO_BIN" check-syntax "$flow" || exit 1; done
"$MAESTRO_BIN" test .maestro/flows/00-home-settings-smoke.yaml
```

## Release 签名

Release 签名只放在本机。证书、密码不要提交到仓库。

推荐本地配置：

```properties
CAPSULE_RELEASE_STORE_FILE=/Users/stevelife/.android/capsule-card-camera-release.jks
CAPSULE_RELEASE_STORE_PASSWORD=local-secret
CAPSULE_RELEASE_KEY_ALIAS=capsule-card-camera
CAPSULE_RELEASE_KEY_PASSWORD=local-secret
```

Gradle 会优先读取环境变量，其次读取根目录下已被忽略的 `local.properties`。仓库已经忽略 `local.properties`、`.env*`、`*.jks`、`*.keystore`、`*.p12` 和 `*.pem`。

构建已签名 release 包：

```sh
./gradlew :app:assembleRelease :app:bundleRelease
```

产物位置：

- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`

## 分发建议

适合先当玩具给别人试用的低门槛路径：

- GitHub Releases：上传签名后的 APK 和 AAB，作为当前仓库的主要公开分发入口。
- Obtainium：用户可以把 GitHub Releases 加进去，后续通过 release 页面收到更新提示。
- 蒲公英 Pgyer 或 fir.im：适合小范围 Android 内测，二维码分发比较快。
- Firebase App Distribution：适合邀请制测试，可以管理测试用户。
- IzzyOnDroid 或 F-Droid：不建议作为第一波目标。当前 APK 已超过 IzzyOnDroid 常见的 30 MB 规则线，且 Google ML Kit / GenAI 依赖需要审核；如果要走 F-Droid 方向，最好后续做一个 FLOSS/no-AI flavor。
- Uptodown 或 Aptoide Connect：适合准备更公开的第三方商店列表，但需要补齐应用元数据并做归属验证。

更大范围上架前，建议补齐稳定图标、截图、隐私说明、相机权限说明、端侧 AI 不可用提示，并保持非 `com.example` 的正式包名。当前 release 包名是 `io.github.stevelaitvt.capsulecardcamera`。
