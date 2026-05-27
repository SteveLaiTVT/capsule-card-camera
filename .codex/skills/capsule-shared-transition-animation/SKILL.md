---
name: capsule-shared-transition-animation
description: Use when adding shared-element, shared-bounds, or navigation-level transitions to capsule-card-camera Compose screens.
---

# Shared Transition Animation

## When To Use

Use this skill only for navigation-level motion or shared visual continuity, such as photo-wall thumbnail to frame editor, gallery item to detail, or future multi-screen flows.

Do not use this skill for simple pull, alpha, scale, countdown, or list placement animation. Use `capsule-compose-animation` for those.

## Working Rules

1. Add shared transitions only when two UI states share a recognizable visual object. Do not wrap the entire app in shared motion for decoration.
2. Place `SharedTransitionLayout` high enough that both source and destination composables share the same `SharedTransitionScope`.
3. Pass or provide the relevant `AnimatedVisibilityScope` where `sharedBounds`, `sharedElement`, or `animateEnterExit` requires it.
4. Use structured keys for matched content when there can be multiple photos or origins. Prefer a small data class over ad hoc string concatenation.
5. Match modifier order deliberately. Keep clipping, padding, shared bounds, and background order consistent between source and destination.
6. Do not shared-transition a live `PreviewView` camera surface unless fresh upstream evidence supports that exact case. Prefer animating a captured bitmap, thumbnail, frame shell, or overlay container.
7. Keep Navigation 3 usage intact. Do not migrate navigation libraries just to get a transition.
8. Provide a non-animated fallback if the transition scope is unavailable during previews or isolated tests.

## Validation

- Run `./gradlew :app:testDebugUnitTest` after state/model changes.
- Run `./gradlew :app:connectedDebugAndroidTest` when shared transition semantics, navigation, or frame settings behavior changes.
- Visually inspect source and destination states; mismatched bounds, clipping, or modifier order usually shows up only at runtime.

## Source Discipline

Rules here are based on open-source Compose samples that use `SharedTransitionLayout`, `sharedBounds`, `rememberSharedContentState`, and `AnimatedVisibilityScope`. Refresh `references/upstream-sources.md` before expanding this to new transition APIs.
