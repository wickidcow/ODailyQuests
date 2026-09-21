# ODailyQuests 3.0.12

Paper 26.3 craft progression fix plus default basic quest reward updates.

## Paper 26.3 crafting compatibility
- Fixed normal `CRAFT` quests not progressing on Paper 26.3 when ODailyQuests uses Paper's post-craft `ItemCraftedEvent`.
- The quest matcher now reads the actual crafted `ItemStack` from the Paper event instead of rejecting the event type.
- Fixes crossbow crafting quests such as `CRAFT: CROSSBOW`.
- Also fixes the same regression for other normal crafting-table `CRAFT` objectives on Paper 26.3.
- Keeps the older `CraftItemEvent` fallback for runtimes where Paper's post-craft event is unavailable.
- The Paper event remains reflection-based so ODailyQuests does not gain a hard runtime dependency on a specific Paper event class.

## ValhallaMMO and modified-item compatibility
- Plain vanilla-material craft requirements now match by material when another plugin adds metadata to the crafted result.
- A quest configured simply as `CROSSBOW` will therefore count a crossbow even if ValhallaMMO adds lore, PDC data, quality data, attributes, or other metadata.
- Exact custom-item requirements remain strict. If the configured required item contains custom metadata/model data, ODailyQuests still requires that specific item rather than accepting every item of the same material.
- The same protection applies to other plugin-modified vanilla equipment craft results, not only bows and crossbows.

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
- The crafting compatibility fixes are code-side and apply immediately after replacing the plugin JAR.

## Compatibility
- Retains the Paper 26.3 compatibility and quest-event hardening from 3.0.11.
