# Verification Report — ODailyQuests 3.0.5

## Target

- Minecraft / Spigot API: 1.21.11+
- Primary runtime: Paper / Purpur
- Folia-aware scheduling: enabled
- Build JDK: 25
- Output bytecode: Java 21
- Output: raw shaded JAR

## CI verification

GitHub Actions runs the unit test suite before producing the shaded JAR:

```text
./gradlew clean test shadowJar --no-daemon
```

The workflow fails if the expected raw JAR cannot be staged.

## Runtime smoke-test checklist

Before promoting 3.0.5 to the main server, verify:

1. Server startup has no ODailyQuests stack traces.
2. `/dqadmin doctor` reports the expected runtime, storage mode, quest counts, and integrations.
3. Existing player progression loads without reset or corruption.
4. Daily renewal redraws normal categories and preserves configured weekly categories.
5. Normal quest progression, completion rewards, total rewards, and category rewards fire once.
6. A configured quest chain advances to the successor without awarding all-quests-complete early.
7. Reroll cost is charged only for a successful reroll and is refunded on failure.
8. Streak milestones execute only once per completed period.
9. Community goals increment once per matching completed quest and reset with their configured daily/weekly period.
10. If Slimefun is installed, configured `SLIMEFUN_ITEM` quests progress on matching crafted/picked-up items; without Slimefun, the plugin still starts normally.
11. Paper/Purpur test produces no asynchronous Bukkit access warnings.
12. Folia test produces no region ownership/thread-context errors during join, progress, completion, GUI use, or renewal.

## Notes

The previous verification report referred to an obsolete Albion/26.1.2 test JAR and Java 25 bytecode. It has been replaced so repository documentation reflects the current maintained build.
