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
3. Confirm the daily layout is Easy / Medium / Hard / Fable Good / Fable Evil / Tech / Wild Card, one quest each by default.
4. Complete one of the server-tested Easy/Medium/Hard quests and verify progression/reward handling.
5. Complete one Fable Good and one Fable Evil quest and verify the correct Good/Evil alignment reward commands.
6. Confirm no legacy Fable faction terminology remains in newly bundled or migrated Good/Evil files.
7. With Slimefun installed, confirm Tech is available and rolls Slimefun objectives without an Easy/Medium/Hard Tech label.
8. Craft Common Talisman, Steel Ingots, or Reinforced Alloy Ingots through the relevant Slimefun multiblock recipe and verify `SLIMEFUN_CRAFT` progression.
9. With a detected Slimefun addon installed, confirm its item objectives join the same Tech pool.
10. With Pylon/Rebar installed, obtain/craft a Pylon item and verify the Pylon/Rebar Tech objective progresses.
11. With no Slimefun/Pylon Tech provider installed, confirm an empty Tech category is skipped safely instead of triggering safety-mode shutdown.
12. Test Wild Card providers individually: PyroFishingPro, EvenMoreFish, ValhallaMMO, mcMMO, MMOItems, and ItemsAdder.
13. Disable/remove each Wild Card dependency and confirm it no longer contributes quests; with no providers, confirm Wild Card is safely skipped.
14. Confirm MythicMobs has no automatic/default Wild Card quest pack; manual MythicMobs support remains usable.
15. Reroll a quest and verify configured reroll behavior and failed-reroll refunds when enabled.
16. Verify the daily renewal path.
17. Enable and verify weekly, chain, streak, community, and reroll-cost features individually if they are used on the server.

## Compatibility rules

- Existing quest YAML is administrator-owned and is not overwritten on upgrade.
- Missing maintained category files are added automatically.
- Fresh bundled Easy/Medium/Hard files are tagged as the `vanilla` default pack; fresh Good/Evil files are tagged with their Fable pack.
- Tech and Wild Card dependency quests are merged in memory and do not rewrite administrator YAML.
- Existing Good/Evil files receive only a narrow migration of the rejected legacy Fable terminology.
- Slimefun, Pylon/Rebar, mcMMO, MMOItems, ItemsAdder and the fishing/MMO integrations remain optional dependencies.
- Dependency packs are enabled by default but active only while their dependency is present; `enabled: false` always overrides detection.
