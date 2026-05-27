# Upstream Sources

Inspected on 2026-05-27 from official Maestro documentation.

## Maestro CLI Overview

- Source: https://docs.maestro.dev/maestro-cli
- Local lesson: keep flows as declarative YAML files and execute them with `maestro test` only when runtime validation is requested.

## Core Selectors

- Source: https://docs.maestro.dev/reference/selectors/core-selectors
- Local lesson: use `id` selectors for dynamic or localized UI, and expose Android Compose test tags with `Modifier.semantics { testTagsAsResourceId = true }`.

## Command References

- `stopApp`: https://docs.maestro.dev/reference/commands-available/stopapp
- `openLink`: https://docs.maestro.dev/api-reference/commands/openlink
- `assertVisible`: https://docs.maestro.dev/reference/commands-available/assertvisible
- `tapOn`: https://docs.maestro.dev/reference/commands-available/tapon
- `scrollUntilVisible`: https://docs.maestro.dev/reference/commands-available/scrolluntilvisible
- Local lesson: for this app, combine debug deep-link launch with explicit visibility assertions and ID-based taps instead of relying on camera-screen settle heuristics.
