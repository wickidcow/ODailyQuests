# ODailyQuests 3.0.11

Compatibility and quest-progression hardening for Paper 26.3.

## Fixed
- Uses Paper 26.3's post-craft `ItemCraftedEvent` when available so CRAFT quests count the item that was actually picked up instead of predicting shift-click results.
- Keeps the older `CraftItemEvent` path as a runtime fallback for supported pre-26.3 servers.
- Corrects shift-craft capacity math so partial inventory space can no longer round quest progress upward.
- Prevents the smithing quest listener from mutating the live smithing result stack.
- Corrects smithing capacity math and evaluates smithing at the final uncancelled event state.
- Applies final-event cancellation handling to other progression-only Bukkit quest listeners, reducing plugin-order races like the ENCHANT issue fixed in 3.0.10.

## Default quest rewards
- Freshly generated Fable Good/Evil quest files now use server-neutral vanilla XP rewards.
- Removed bundled assumptions about Albion-specific crate keys, ValhallaMMO skill rewards, custom economy commands, EliteMobs currency, quest-point commands, LuckPerms alignment metadata, and alignment scoreboards.
- Existing server `plugins/ODailyQuests/quests/good.yml` and `evil.yml` files are not overwritten, so established reward setups remain unchanged.

## Compatibility
- Built against Paper 26.3 / Java 25.
- `plugin.yml` remains at API 1.21.11.
