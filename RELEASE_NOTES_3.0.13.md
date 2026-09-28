# ODailyQuests 3.0.13

## Paper 26.3 primary support

- Promotes Paper/Minecraft 26.3 to the primary API/build target.
- Keeps Paper 26.2 as a tested backwards-compatibility line.
- Keeps Paper 1.21.11 as the minimum tested compatibility floor.
- Builds with a Java 25 toolchain while emitting Java 21 bytecode for the distributable plugin.
- Adds dedicated CI compile gates for Paper 26.3, Paper 26.2, and Paper 1.21.11.
- Uses a Java-25 dependency-selection attribute for Paper 26.x APIs without raising the plugin bytecode floor.
- Keeps `plugin.yml` at `api-version: 1.21.11` so the maintained support floor is not raised just because 26.3 is the primary development target.

No quest data, player progression, reroll state, rewards, database schema, or configured integrations are intentionally changed by this compatibility release.
