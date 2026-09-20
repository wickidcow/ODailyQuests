# ODailyQuests 3.0.9 — Paper 26.3 Compatibility

ODailyQuests 3.0.9 updates the maintained build target to the current Paper 26.3 API while preserving the existing 1.21.11+ minimum plugin API level.

## Paper 26.3

- Build and test against pinned `io.papermc.paper:paper-api:26.3.build.25-alpha`.
- Continue using the Java 25 build toolchain required by the modern Paper development environment.
- Target Java 25 bytecode, matching the requirement of the Paper 26.3 API and Minecraft 26.3 servers.
- Keep `api-version: 1.21.11` so the plugin is not unnecessarily locked to 26.3-only servers.
- Preserve Paper, Purpur, and Folia-aware scheduling support.

## Packaging

The release artifact is the raw plugin JAR:

`ODailyQuests3.0.9.jar`

No ZIP extraction is required.

## Validation

Before release, GitHub Actions must pass:

- compilation against the Paper 26.3 API
- automated tests
- production Shadow JAR build
- packaged runtime-content verification
- internal dependency verification

## Upgrade

Replace the previous ODailyQuests JAR with `ODailyQuests3.0.9.jar` and restart the server. No configuration migration is required specifically for the 26.3 compatibility update.
