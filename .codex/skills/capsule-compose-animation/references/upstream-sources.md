# Upstream Sources

Inspected on 2026-05-25 from a read-only shallow clone.

## Android Compose Samples

- Repository: https://github.com/android/compose-samples
- Commit inspected: `d3ff757b289f7036815978a8f7b16706ee3423b0`
- License: Apache-2.0

### Visibility And Content Replacement

- Source: https://github.com/android/compose-samples/blob/d3ff757b289f7036815978a8f7b16706ee3423b0/Jetchat/app/src/main/java/com/example/compose/jetchat/conversation/UserInput.kt#L238-L261
- Local lesson: `AnimatedVisibility` is appropriate for a panel that enters and exits as a block.

- Source: https://github.com/android/compose-samples/blob/d3ff757b289f7036815978a8f7b16706ee3423b0/Jetchat/app/src/main/java/com/example/compose/jetchat/conversation/UserInput.kt#L409-L420
- Local lesson: `AnimatedContent` is appropriate when target state swaps related content inside the same layout slot.

### Coordinated State Motion

- Source: https://github.com/android/compose-samples/blob/d3ff757b289f7036815978a8f7b16706ee3423b0/Jetchat/app/src/main/java/com/example/compose/jetchat/components/AnimatingFabContent.kt#L43-L89
- Local lesson: `updateTransition` coordinates multiple animated properties from one semantic state.

- Source: https://github.com/android/compose-samples/blob/d3ff757b289f7036815978a8f7b16706ee3423b0/JetLagged/app/src/main/java/com/example/jetlagged/JetLaggedDrawer.kt#L66-L115
- Local lesson: `Animatable` works well for gesture-driven state with snapping, velocity, and cancel recovery.

- Source: https://github.com/android/compose-samples/blob/d3ff757b289f7036815978a8f7b16706ee3423b0/Jetsnack/app/src/main/java/com/example/jetsnack/ui/home/Home.kt#L240-L270
- Local lesson: multiple `Animatable` values can derive item selection and indicator movement from stable selection state.

### Layout And List Motion

- Source: https://github.com/android/compose-samples/blob/d3ff757b289f7036815978a8f7b16706ee3423b0/Jetcaster/mobile/src/main/java/com/example/jetcaster/ui/podcast/PodcastDetailsScreen.kt#L248-L264
- Local lesson: `animateContentSize` is appropriate for localized text/content expansion.

- Source: https://github.com/android/compose-samples/blob/d3ff757b289f7036815978a8f7b16706ee3423b0/Jetsnack/app/src/main/java/com/example/jetsnack/ui/home/cart/Cart.kt#L172-L208
- Local lesson: `Modifier.animateItem()` belongs on keyed lazy-list items where insert/remove/reorder placement should animate.
