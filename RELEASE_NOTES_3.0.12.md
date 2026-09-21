# ODailyQuests 3.0.12

Paper 26.3 craft progression fix plus default basic quest reward updates.

## Paper 26.3 crafting compatibility
- Fixed normal `CRAFT` quests not progressing on Paper 26.3 when ODailyQuests uses Paper's post-craft `ItemCraftedEvent`.
- The quest matcher now reads the actual crafted `ItemStack` from the Paper event instead of rejecting the event type.
- Fixes crossbow crafting quests such as `CRAFT: CROSSBOW`.
- Also fixes the same regression for other normal crafting-table `CRAFT` objectives on Paper 26.3.
- Keeps the older `CraftItemEvent` fallback for runtimes where Paper's post-craft event is unavailable.
- The Paper event remains reflection-based so ODailyQuests does not gain a hard runtime dependency on a specific Paper event class.

## ValhallaMMO compatibility note
- ODailyQuests counts the actual material produced by the craft and does not require ValhallaMMO quality, tier, lore, or stat metadata for a normal `CRAFT` objective.
- This prevents ValhallaMMO item-quality configuration from making otherwise valid vanilla-material craft quests impossible.

## Easy / Medium / Hard defaults
- Easy quests now grant 1,000 Gold, 550 XP points, and 1 Quest Point.
- Medium quests now grant 2,000 Gold, 2,920 XP points, and 1 Quest Point.
- Hard quests now grant 4,000 Gold, 8,670 XP points, and 1 Quest Point.
- All 12 bundled quests in each category use the same reward bundle for that difficulty.
- Reward lore now shows Gold, XP, and 1 Quest Point consistently.
- Bundled vanilla quests remain tagged with `default_pack: vanilla`.

## Upgrade behavior
- The reward-file changes are fresh-install/default-file changes only.
- Existing server quest files are not overwritten by ODailyQuests, so customized Easy/Medium/Hard rewards stay intact unless the server owner replaces those files manually.
- The crafting compatibility fix is code-side and applies immediately after replacing the plugin JAR.

## Compatibility
- Retains the Paper 26.3 compatibility and quest-event hardening from 3.0.11.
