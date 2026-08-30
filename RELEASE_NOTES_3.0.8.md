# ODailyQuests 3.0.8 — PyroFishingPro Compatibility Update

ODailyQuests 3.0.8 updates the maintained PyroFishingPro integration for current PyroAPI releases while preserving compatibility with older PyroFishingPro installations.

## PyroFishingPro / PyroAPI compatibility

- Added support for the current PyroAPI fish-catch event:
  - `me.PyroAPI.Events.PyroFishingPro.PyroFishCatchEvent`
- Retained compatibility with the legacy PyroFishingPro event:
  - `me.arsmagica.API.PyroFishCatchEvent`
- Current PyroAPI fish identifiers are read through `getFishId()`.
- Legacy installations continue to use `getFishNumber()`.
- `PYRO_FISH` quest filters now accept modern string fish IDs as well as legacy numeric IDs.
- Added `PyroAPI` as a soft dependency so ODailyQuests loads after it when installed.
- The integration remains reflection-based, so PyroAPI is not a hard compile-time dependency.

## Cleaner startup behavior

ODailyQuests no longer emits the obsolete-event warning simply because the old `me.arsmagica.API.PyroFishCatchEvent` class is unavailable.

When PyroFishingPro is installed, ODailyQuests now checks for the current PyroAPI event first, falls back to the legacy event when needed, and quietly skips the optional integration when neither supported API is available.

This removes the low-level compatibility warning while preserving support for older servers.

## Quest behavior

Filtered `PYRO_FISH` quests continue using the existing `<tier>:<id>` format. The ID portion may now be either a modern PyroAPI string identifier or a legacy numeric fish number.

Existing general PyroFishingPro quests that do not filter for a specific fish remain compatible.

## Compatibility

- Minecraft / Spigot API: **1.21.11+**
- Build JDK: **Java 25**
- Runtime bytecode target: **Java 21+**
- Paper / Spigot compatible
- Folia-aware scheduling compatibility retained
- Current PyroFishingPro / PyroAPI supported
- Legacy PyroFishingPro event package supported

## Validation

The 3.0.8 build passed the repository CI pipeline, including:

- compilation and automated tests
- production JAR build
- packaged runtime-content verification
- internal dependency verification
- raw JAR artifact staging

## Upgrade

Replace the previous ODailyQuests JAR with the **3.0.8** JAR and restart the server. No ODailyQuests configuration migration is required for this update.

For current PyroFishingPro installations, make sure the matching PyroAPI/PyroLib dependencies required by your PyroFishingPro version are installed.
