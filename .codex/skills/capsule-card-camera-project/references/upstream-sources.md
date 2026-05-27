# Upstream Sources

Inspected on 2026-05-25 from read-only shallow clones. These sources are open-source Android projects and are used as direction, not as copy-paste templates.

## Android Architecture Samples

- Repository: https://github.com/android/architecture-samples
- Commit inspected: `ee66e1526b84c026615df032c705842b7d2a521f`
- License: Apache-2.0
- Source: https://github.com/android/architecture-samples/blob/ee66e1526b84c026615df032c705842b7d2a521f/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksViewModel.kt#L58-L100
- Source: https://github.com/android/architecture-samples/blob/ee66e1526b84c026615df032c705842b7d2a521f/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksScreen.kt#L92-L107
- Local lesson: combine repository, filter, loading, and user-message streams into a single `StateFlow` UI state; collect it from Compose with lifecycle-aware collection.

## Now in Android

- Repository: https://github.com/android/nowinandroid
- Commit inspected: `7d45eae4f8720a0c77f507712ba2437ff974b6ed`
- License: Apache-2.0
- Source: https://github.com/android/nowinandroid/blob/7d45eae4f8720a0c77f507712ba2437ff974b6ed/docs/ArchitectureLearningJourney.md#L289-L305
- Local lesson: model screen data as a small UI-state hierarchy and convert cold repository/use-case flows into hot `StateFlow` values using `stateIn`.

## Android Compose Samples

- Repository: https://github.com/android/compose-samples
- Commit inspected: `d3ff757b289f7036815978a8f7b16706ee3423b0`
- License: Apache-2.0
- Source: https://github.com/android/compose-samples/tree/d3ff757b289f7036815978a8f7b16706ee3423b0
- Local lesson: Compose samples keep state, UI, animation, and tests close to feature-level surfaces; prefer that shape here over broad abstractions.

## Android Camera Samples

- Repository: https://github.com/android/camera-samples
- Commit inspected: `7f4e3b0a48d3b92468b72ef01a29363039874375`
- License: Apache-2.0
- Source: https://github.com/android/camera-samples/tree/7f4e3b0a48d3b92468b72ef01a29363039874375/CameraXBasic
- Local lesson: camera binding, capture, lifecycle, and device availability should be validated with real CameraX lifecycle behavior, not only static UI tests.
