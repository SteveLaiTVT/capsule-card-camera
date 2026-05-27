---
name: capsule-compose-animation
description: Use when adding or tuning Compose animations in capsule-card-camera, including pull gestures, animated visibility/content, list placement, transitions, and motion performance.
---

# Compose Animation

## When To Use

Use this skill for changes involving `Animatable`, `AnimatedVisibility`, `AnimatedContent`, `animateContentSize`, `animateItem`, `updateTransition`, `graphicsLayer`, gesture-driven motion, countdown/pull transitions, or photo-wall placement animation.

## Local Motion Model

- The pull camera island is driven by one `Animatable<Float>` named `expansion`; most dimensions and alpha values are derived from its progress.
- Drag uses `snapTo` while the gesture is active and `animateTo` on release.
- `PhotoWall` already uses stable item keys and `Modifier.animateItem()`.
- Camera preview alpha, scale, and panel controls are currently derived through `graphicsLayer` and simple interpolation.

## API Choice Rules

1. Keep a single source of truth for related motion. Derive size, alpha, scale, translation, and visibility from one state when they belong to the same interaction.
2. Use `Animatable` for interruptible gesture or physics-like motion, especially when drag, fling, cancel, or velocity matters.
3. Use `AnimatedVisibility` for a block entering or leaving the composition.
4. Use `AnimatedContent` when replacing related content for a target state, such as text field versus recording state.
5. Use `animateContentSize` for localized content expansion where layout should smoothly resize.
6. Use `updateTransition` when several animated properties must be coordinated from the same semantic state.
7. Use `Modifier.animateItem()` only with stable list/grid keys.
8. Prefer `graphicsLayer` for alpha, scale, translation, and shadow changes that should not force layout.
9. Add meaningful `label` values to animation APIs that support them.
10. Do not add long custom coroutine animation loops unless a Compose animation primitive cannot express the motion.

## Product Constraints

- Motion should clarify camera state: closed island, preview reveal, capture countdown, photo insertion, and frame settings.
- Do not animate live camera binding itself as a shared element or expensive layout resize. Animate the container, overlay, alpha, scale, or captured bitmap instead.
- Preserve readability and test tags. Animation should not make existing Compose tests flaky by delaying semantic availability without reason.

## Validation

- Run `git diff --check` for skill or doc changes.
- Run `./gradlew :app:testDebugUnitTest` for state or helper changes.
- Run `./gradlew :app:connectedDebugAndroidTest` when semantics, gestures, CameraX UI, or photo-wall behavior changes.
- For visual motion changes, inspect the screen on at least one phone-sized viewport/device before calling the work done.

## Source Discipline

Rules here are derived from Android's open-source Compose samples. If a requested motion is not covered by the listed sources, inspect another open-source Compose implementation before inventing a new pattern.
