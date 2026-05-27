# Upstream Sources

Inspected on 2026-05-25 from a read-only shallow clone.

## Android Compose Samples: Jetsnack

- Repository: https://github.com/android/compose-samples
- Commit inspected: `d3ff757b289f7036815978a8f7b16706ee3423b0`
- License: Apache-2.0

- Source: https://github.com/android/compose-samples/blob/d3ff757b289f7036815978a8f7b16706ee3423b0/Jetsnack/app/src/main/java/com/example/jetsnack/ui/JetsnackApp.kt#L60-L75
- Local lesson: `SharedTransitionLayout` is placed above the navigation host so both source and destination routes can share a transition scope.

- Source: https://github.com/android/compose-samples/blob/d3ff757b289f7036815978a8f7b16706ee3423b0/Jetsnack/app/src/main/java/com/example/jetsnack/ui/snackdetail/SnackDetail.kt#L150-L225
- Local lesson: shared bounds use an `AnimatedVisibilityScope`, structured shared-content keys, matched clipping, and explicit enter/exit behavior.

## Android Compose Samples: Jetcaster

- Repository: https://github.com/android/compose-samples
- Commit inspected: `d3ff757b289f7036815978a8f7b16706ee3423b0`
- License: Apache-2.0

- Source: https://github.com/android/compose-samples/blob/d3ff757b289f7036815978a8f7b16706ee3423b0/Jetcaster/mobile/src/main/java/com/example/jetcaster/ui/JetcasterApp.kt#L47-L71
- Local lesson: the transition and visibility scopes can be provided through composition locals when multiple screens need access without pushing scope parameters through every layer.
