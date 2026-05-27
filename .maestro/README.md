# Maestro Flows

These are mobile.dev Maestro CLI flows for `capsule-card-camera`.

They are seed smoke flows, not exhaustive UI tests. Keep detailed state coverage in `app/src/androidTest` and use Maestro for device-side checks that need the installed app, Android accessibility tree, deep links, or real navigation.

## Local App Contract

- App id: `com.example.capsulecardcamera`
- Debug deep link: `piccam://maestro`
- Expected CLI path: `/Users/stevelife/.maestro/bin/maestro`
- Flow directory: `.maestro/flows/`

The debug deep link is declared in `app/src/debug/AndroidManifest.xml`. The home and settings screens expose Compose `testTag` values as resource IDs with `testTagsAsResourceId`, so flows should prefer `id` selectors over visible text.

## Commands

These commands are examples for later validation. Do not run device flows unless the user asks for runtime verification.

```sh
MAESTRO_BIN="${MAESTRO_BIN:-/Users/stevelife/.maestro/bin/maestro}"
for flow in .maestro/flows/*.yaml; do "$MAESTRO_BIN" check-syntax "$flow" || exit 1; done
"$MAESTRO_BIN" test .maestro/flows/00-home-settings-smoke.yaml
```

Before runtime validation, install the debug app:

```sh
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
