# Verification Report - ODailyQuests 3.0.4 Albion Spigot 26.1.2

## Result
The uploaded `ODailyQuests-3.0.4-Albion-Spigot-26.1.2.jar` is not the fully patched jar.

## Found in uploaded jar
- Java class version: 69 for main ODailyQuests classes, which means Java 25 bytecode.
- `plugin.yml` parses successfully.
- `quests/examples.yml` parses successfully.
- No missing internal `com/ordwen/odailyquests/...` class references were found.
- `com/ordwen/odailyquests/quests/player/progression/PlayerProgressor.class` is present.

## Problems found in uploaded jar
- `plugin.yml` still has `api-version: 1.18` instead of `api-version: '26.1.2'`.
- `NMSHandler.class` still contains `compareTo`, meaning the old lexicographical version comparison is still compiled in.
- No built-in PyroFishingPro classes were found:
  - missing `com/ordwen/odailyquests/events/listeners/integrations/pyrofishingpro/PyroFishCatchListener.class`
  - missing `com/ordwen/odailyquests/quests/types/custom/items/PyroFishQuest.class`
  - no `PYRO_FISH` registration was found in the compiled main plugin.

## Corrected source included here
The corrected source package includes:
- Minecraft/Spigot API target: `26.1.2-R0.1-SNAPSHOT`
- Java target: `25`
- Gradle wrapper: `9.5.0`
- `plugin.yml` API version: `'26.1.2'`
- Numeric version comparison in `NMSHandler`
- Built-in `PYRO_FISH` quest type
- Built-in PyroFishingPro listener using reflection, so ODailyQuests can still load without PyroFishingPro installed

## Build command
Use Java 25 locally or in GitHub Actions:

```powershell
$env:JAVA_HOME="C:\\Program Files\\Amazon Corretto\\jdk25.0.2"
.\\gradlew.bat clean shadowJar --no-daemon
```

Expected output:

```text
build/libs/ODailyQuests-3.0.4-Albion-Spigot-26.1.2.jar
```
