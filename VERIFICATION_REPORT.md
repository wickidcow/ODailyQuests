# ODailyQuests 3.0.5 Verification Report

This branch targets Minecraft/Paper 1.21.11+ and keeps Java 21 bytecode while building under Java 25.

## Automated verification

GitHub Actions runs:

```text
./gradlew clean test shadowJar --no-daemon
```

The raw shaded JAR is staged only after tests pass. Development artifacts are uploaded as the direct `.jar` file.

## Bundled quest-resource verification

The maintained JAR must physically contain these seven quest-category resources:

- `quests/easy.yml`
- `quests/medium.yml`
- `quests/hard.yml`
- `quests/good.yml`
- `quests/evil.yml`
- `quests/tech.yml`
- `quests/wildcard.yml`

`tech.yml` and `wildcard.yml` must contain actual quest entries and must not be empty shell files. Integration quests carry `default_pack` directly in YAML and are filtered when their dependency is unavailable.

## Runtime smoke-test checklist

1. Start the server and run `/dqadmin doctor`.
2. Confirm existing player quest data loads without reset or corruption.
3. Confirm the maintained daily layout is Easy / Medium / Hard / Fable Good / Fable Evil / Tech / Wild Card, one quest each by default when all category providers are available.
4. Complete one known-good Easy/Medium/Hard quest and verify progression/reward handling.
5. Complete one Fable Good and one Fable Evil quest and verify the correct Good/Evil alignment rewards.
6. Confirm rejected old Fable terminology does not appear in newly bundled Good/Evil content.
7. Open `plugins/ODailyQuests/quests/tech.yml` and confirm it is physically populated, readable and editable.
8. With Slimefun installed, confirm Tech rolls Slimefun objectives without an Easy/Medium/Hard Tech label.
9. Craft Common Talisman, Steel Ingots and Reinforced Alloy Ingots through their real crafting paths and verify `SLIMEFUN_CRAFT` progression.
10. With a detected Slimefun addon installed, confirm its tagged physical Tech quest survives filtering and can progress on addon-owned items.
11. With Pylon/Rebar installed, create or obtain a Pylon/Rebar item and verify `REBAR_ITEM` Tech progression.
12. Remove/disable all Tech providers and confirm Tech becomes empty after dependency filtering and is skipped cleanly rather than tripping `safety_mode`.
13. Open `plugins/ODailyQuests/quests/wildcard.yml` and confirm it is physically populated, readable and editable.
14. With ValhallaMMO installed, complete POWER, MINING and ARCHERY Wild Cards and verify the correct skill filter plus 1/2/3 spendable Valhalla skill-point rewards.
15. With mcMMO installed, complete Mining, Woodcutting and Swords Wild Cards and verify only the configured mcMMO skill progresses and the reward adds mcMMO XP.
16. With PyroFishingPro installed, complete Pyro Wild Cards and verify Pyro Entropy and Fishing XP rewards.
17. With EvenMoreFish installed, complete EMF Wild Cards and verify the bundled Shrimp bait reward.
18. With MMOItems installed, obtain MMOItems and verify the MMOItems Wild Card progression detector.
19. With ItemsAdder installed, obtain ItemsAdder custom items and verify the ItemsAdder Wild Card progression detector.
20. Disable/remove each Wild Card dependency and confirm only its tagged quests disappear from the loaded pool.
21. With no Wild Card providers and no custom Wild Card quests, confirm Wild Card is skipped safely.
22. Confirm MythicMobs has no automatic/default Wild Card pack; manually configured MythicMobs quests remain usable.
23. Reroll a quest and verify configured reroll behavior and failed-reroll refunds when enabled.
24. Verify the daily renewal path.
25. Enable and verify weekly, chain, streak, community, and reroll-cost features individually if used on the server.

## Upgrade / compatibility rules

- Existing quest YAML is administrator-owned and is not blindly overwritten on upgrade.
- Missing maintained category files are added automatically.
- Two short-lived development builds created exact empty Tech/Wild Card shell files. Only those recognizable empty stubs are replaced with the populated maintained resource; a file containing real quests is preserved.
- Fresh Easy/Medium/Hard files are tagged as the `vanilla` default pack after creation; fresh Good/Evil files are tagged with their Fable pack.
- Tech and Wild Card quests are now physical YAML, not runtime-only generated quests.
- Every integration quest is dependency-filtered through its `default_pack` tag.
- Existing Good/Evil files receive only the narrow Fable terminology migration.
- Slimefun, Pylon/Rebar, ValhallaMMO, mcMMO, MMOItems, ItemsAdder, PyroFishingPro and EvenMoreFish remain optional dependencies.
- `enabled: false` on a dependency pack always overrides automatic detection.
