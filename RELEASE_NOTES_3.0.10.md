# ODailyQuests 3.0.10 — ENCHANT Quest Reliability Fix

ODailyQuests 3.0.10 fixes ENCHANT quests that could fail to register a successful enchant when other plugins also handled the enchant event.

## ENCHANT quest fix

- ENCHANT progression now observes `EnchantItemEvent` at Bukkit's `MONITOR` priority.
- Cancelled enchant events are ignored through `ignoreCancelled = true`.
- This removes an event-ordering race where ODailyQuests could see an enchant as cancelled at `HIGHEST` even though a later handler allowed the enchant to complete.
- Successful vanilla enchanting-table actions continue to progress ENCHANT quests by one.
- Existing ENCHANT quest configuration remains compatible; no YAML migration is required.

## Regression protection

- Added an automated test that verifies the ENCHANT listener remains registered at `MONITOR` priority with cancelled events ignored.
- The test protects this behavior from future listener refactors and Paper API updates.

## Compatibility

- Paper 26.3 build target retained.
- Minecraft / plugin API minimum remains 1.21.11.
- Java 25 build/runtime target retained.
- Folia-aware scheduling support retained.

## Packaging

The release artifact is the raw plugin JAR:

`ODailyQuests3.0.10.jar`

No ZIP extraction is required.

## Upgrade

Replace the previous ODailyQuests JAR with `ODailyQuests3.0.10.jar` and restart the server.

No configuration migration is required.
