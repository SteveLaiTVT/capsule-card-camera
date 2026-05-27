# Capsule Card Camera Agent Notes

This repository keeps its Codex skills project-local. Do not copy these skills into `~/.codex/skills`, `~/.agents/skills`, or any other global skill directory unless the user explicitly asks.

## Local Skills

- Use `.codex/skills/capsule-card-camera-project` for general Android project work, state modeling, tests, Gradle, and repository conventions.
- Use `.codex/skills/capsule-camerax-compose` for camera preview, CameraX binding, image capture, permissions, and photo persistence.
- Use `.codex/skills/capsule-compose-animation` for pull gestures, panel/list motion, `Animatable`, `AnimatedVisibility`, `AnimatedContent`, and related Compose animation work.
- Use `.codex/skills/capsule-shared-transition-animation` only when adding navigation-level or shared-element motion.
- Use `.codex/skills/capsule-maestro-ui` for Maestro CLI flow authoring, debug deep-link launches, device-side smoke validation, and Compose `testTag` selector conventions.

## Source Policy

The local skills were derived from inspected open-source Android repositories, not invented from scratch. Keep source-backed claims tied to the `references/upstream-sources.md` file inside each skill, or refresh against upstream open-source code before expanding the rules.

## Validation

For code changes, prefer the narrowest meaningful Gradle check first. Useful project gates are:

```sh
./gradlew :app:testDebugUnitTest
MAESTRO_BIN="${MAESTRO_BIN:-/Users/stevelife/.maestro/bin/maestro}"; for flow in .maestro/flows/*.yaml; do "$MAESTRO_BIN" check-syntax "$flow" || exit 1; done
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
"$MAESTRO_BIN" test .maestro/flows/00-home-settings-smoke.yaml
```

Prefer Maestro smoke flows for rendered UI and interaction validation. Run `./gradlew :app:connectedDebugAndroidTest` only when broader Compose instrumentation coverage is explicitly needed.

For Maestro skill/flow-only changes, do not execute device flows unless explicitly requested. Prefer syntax-only checks first.
