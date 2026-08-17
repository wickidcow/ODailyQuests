# ODailyQuests 3.0.5 Verification Report

This branch targets Minecraft/Paper 1.21.11+ and keeps Java 21 bytecode while building under Java 25.

## Automated verification

GitHub Actions runs:

```text
./gradlew clean test shadowJar --no-daemon
```

The raw shaded JAR is staged only after tests pass.

Current automated coverage includes renewal scheduling/time math and a full production compile/shadow build.

## Runtime smoke-test checklist

1. Start the server and run `/dqadmin doctor`.
2. Confirm existing player quest data loads without reset or corruption.
3. Complete one normal Vanilla quest and verify progression/reward handling.
4. Reroll a quest and verify configured reroll behavior.
5. Verify the daily renewal path.
6. Check `default_quest_packs` in `config.yml` and confirm disabled packs do not enter the quest pool.
7. With Slimefun installed, verify Easy/Medium/Hard Slimefun Core quests and at least one detected addon-family pack.
8. Craft a Slimefun multiblock recipe and verify `SLIMEFUN_CRAFT` progression.
9. With ValhallaMMO/EvenMoreFish/PyroFishingPro installed, verify their packs appear automatically; remove/disable the dependency and verify they disappear from the generated pool.
10. Verify Fable Quests - Concord and Fable Quests - Dominion appear in Easy/Medium/Hard and can each be disabled independently.
11. Confirm MythicMobs has no automatic/default quest pack.
12. Enable and verify weekly, chain, streak, community, and reroll-cost features individually if they are used on the server.

## Compatibility rules

- Existing untagged quest YAML is treated as administrator-owned custom content and is never removed by a default-pack toggle.
- Fresh bundled Vanilla quest files are tagged as the `vanilla` default pack.
- Generated dependency/Fable quests are merged in memory and do not rewrite administrator quest YAML.
- Slimefun remains a soft dependency; item IDs, addon ownership, and multiblock crafting events are accessed reflectively.
- Dependency packs are enabled by default but active only while their dependency is present; `enabled: false` always overrides detection.
