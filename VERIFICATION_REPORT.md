# ODailyQuests 3.0.5 Verification Report

This branch targets Minecraft/Paper 1.21.11+ and keeps Java 21 bytecode while building under Java 25.

## Automated verification

GitHub Actions runs:

```text
./gradlew clean test shadowJar --no-daemon
```

The raw shaded JAR is staged only after tests pass. Development artifacts are uploaded as the direct `.jar` file.

CI also inspects the packaged production JAR for critical resources/classes and checks for missing internal `com.ordwen.odailyquests...` references with `jdeps`.

## Bundled quest-resource verification

The maintained JAR must physically contain:

- `quests/easy.yml`
- `quests/medium.yml`
- `quests/hard.yml`
- `quests/good.yml`
- `quests/evil.yml`
- `quests/tech.yml`
- `quests/wildcard.yml`
- `quests/pylon-defaults.yml`

`tech.yml` and `wildcard.yml` must contain actual quest entries and must not be empty shell files. Integration quests carry `default_pack` directly in YAML and are filtered when their dependency is unavailable.

`pylon-defaults.yml` must contain exactly the maintained exact-item defaults for:

- `pylon:shimmer_magnet`
- `pylon:diamond_hammer`
- `pylon:elevator_1`
- `pylon:reactivated_wither_skull`

## Maintained quest-type verification

The runtime must register these maintained types before category YAML is loaded:

- `SLIMEFUN_ITEM`
- `SLIMEFUN_CRAFT`
- `REBAR_ITEM`
- `MCMMO_EXP`
- `MMOITEM_ITEM`
- `ITEMSADDER_ITEM`
- `EMF_FISH`

A JUnit regression test verifies the registry mapping so Tech/Wild Card definitions cannot silently become invalid because their maintained quest types were forgotten.

## Runtime smoke-test checklist

1. Start the server and run `/dqadmin doctor`.
2. Confirm existing player quest data loads without reset or corruption.
3. Confirm the maintained daily layout is Easy / Medium / Hard / Fable Good / Fable Evil / Tech / Wild Card, one quest each by default when all category providers are available.
4. Confirm `/dqadmin doctor` shows loaded category names plus explicit `Tech available` and `Wild Card available` states.
5. Confirm `/dqadmin doctor` lists installed Slimefun, Pylon, Rebar, mcMMO and other relevant integrations.
6. On an older numbered five-slot `playerInterface.yml`, confirm startup adds safe positions for quest 6 and quest 7 without replacing custom menu styling.
7. Confirm an old `%achieved%/5` player-head counter migrates to `%achieved%/%totalQuests%`.
8. Confirm legacy `%progression%` still resolves to the current quest progress instead of appearing literally.
9. If the player already had a saved five-quest daily set before upgrading, run `/dqadmin reset quests <player>` (or wait for the next scheduled draw) and confirm a fresh eligible set is generated without a class-loading error.
10. Complete one known-good Easy/Medium/Hard quest and verify progression/reward handling.
11. Complete one Fable Good and one Fable Evil quest and verify the correct Good/Evil alignment rewards.
12. Confirm rejected old Fable terminology does not appear in newly bundled Good/Evil content.
13. Open `plugins/ODailyQuests/quests/tech.yml` and confirm it is physically populated, readable and editable.
14. With Slimefun installed, confirm Tech rolls Slimefun objectives without an Easy/Medium/Hard Tech label.
15. Craft Common Talisman, Steel Ingots and Reinforced Alloy Ingots through their real crafting paths and verify `SLIMEFUN_CRAFT` progression.
16. With a detected Slimefun addon installed, confirm its tagged physical Tech quest survives filtering and can progress on addon-owned items.
17. With Pylon/Rebar installed, confirm the generic `REBAR_ITEM` objectives progress from real Pylon item acquisition/crafting.
18. Confirm startup adds the four exact Pylon defaults to a managed `tech.yml` without removing or renumbering administrator quests.
19. Roll/assign **Shimmer Magnet**, obtain the exact `pylon:shimmer_magnet` item, and confirm progression.
20. While Shimmer Magnet is active, obtain a different Pylon item and confirm it does **not** progress that exact objective.
21. Repeat exact-key testing for `pylon:diamond_hammer`, `pylon:elevator_1` and `pylon:reactivated_wither_skull`.
22. Confirm a completely custom Tech file with no maintained `default_pack: pylon-rebar` quest does not receive the four seeded defaults.
23. Remove/disable all Tech providers and confirm Tech becomes empty after dependency filtering and is skipped cleanly rather than tripping `safety_mode`.
24. Open `plugins/ODailyQuests/quests/wildcard.yml` and confirm it is physically populated, readable and editable.
25. With ValhallaMMO installed, complete POWER, MINING and ARCHERY Wild Cards and verify the correct skill filter plus configured spendable skill-point rewards.
26. With mcMMO installed, complete Mining, Woodcutting and Swords Wild Cards and verify only the configured mcMMO skill progresses and the reward adds mcMMO XP.
27. With PyroFishingPro installed, complete Pyro Wild Cards and verify Pyro Entropy and Fishing XP rewards.
28. With EvenMoreFish installed, complete EMF Wild Cards and verify the bundled bait reward.
29. With MMOItems installed, obtain MMOItems and verify the MMOItems Wild Card progression detector.
30. With ItemsAdder installed, obtain ItemsAdder custom items and verify the ItemsAdder Wild Card progression detector.
31. Disable/remove each Wild Card dependency and confirm only its tagged quests disappear from the loaded pool.
32. With no Wild Card providers and no custom Wild Card quests, confirm Wild Card is skipped safely.
33. Confirm MythicMobs has no automatic/default Wild Card pack; manually configured MythicMobs quests remain usable.
34. Reroll a quest and verify configured reroll behavior and failed-reroll refunds when enabled.
35. Verify the daily renewal path.
36. Enable and verify weekly, chain, streak, community, and reroll-cost features individually if used on the server.

## Upgrade / compatibility rules

- Existing quest YAML is administrator-owned and is not blindly overwritten on upgrade.
- Missing maintained category files are added automatically.
- Exact empty Tech/Wild Card shell files from short-lived development builds can be replaced with populated maintained resources; a file containing real quests is preserved.
- Fresh Easy/Medium/Hard files are tagged as the `vanilla` default pack after creation; fresh Good/Evil files are tagged with their Fable pack.
- Tech and Wild Card quests are physical YAML, not runtime-only invisible definitions.
- Every integration quest is dependency-filtered through its `default_pack` tag.
- The Pylon exact-item resource is merged only into a Tech file that already contains the maintained `pylon-rebar` pack.
- Existing Good/Evil files receive only the narrow terminology migration.
- Older category-based interfaces are expanded for the maintained categories when safe.
- Older numbered interfaces are expanded by reusing only configured `FILL` positions; custom buttons and themed items are not selected as migration targets.
- Legacy hard-coded daily quest counters are migrated to `%totalQuests%` when the old `%achieved%/<number>` pattern is recognized.
- Existing pre-3.0.5 configs receive a 3.0.5 config-version migration marker after maintained compatibility settings are populated.
- Slimefun, Pylon/Rebar, ValhallaMMO, mcMMO, MMOItems, ItemsAdder, PyroFishingPro and EvenMoreFish remain optional dependencies.
- `enabled: false` on a dependency pack always overrides automatic detection.
