# Native Bedrock quest forms (Floodgate)

ODailyQuests can present native Bedrock menus to Floodgate players. This is an optional view of the existing quests, **not** a separate progression or reward system.

- Install a compatible Floodgate 2.x build (and Geyser, as appropriate for the server).
- Run `/dailyquests`, `/dailyquests me`, or `/dailyquests reroll` as a Bedrock player.
- Java players and servers without Floodgate continue to use the existing inventory GUI.
- Turn the integration off with `bedrock_forms.enabled: false` in `plugins/ODailyQuests/config.yml`, then restart the server.
- If using Floodgate on a proxy, configure `send-floodgate-data: true` and matching Floodgate keys on the backend.

The native menu shows your current categories, progress, descriptions, and configured reward summaries. It supports claiming GET, LOCATION, PLACEHOLDER and manual-completion quests, plus choosing one quest or the entire set to reroll. These actions use the **same** underlying validation, costs, permissions, limits, and reward logic as the Java UI. Closing a form takes no action; Reroll All has a confirmation page.

## Compatibility and test plan

The Floodgate API is compile-only, and Floodgate is a soft dependency. No database migration or player quest data changes are needed.

Verify on Paper 26.3 / Java 25 with Geyser and Floodgate:
1. Java player sees the unchanged inventory menu; server without Floodgate starts cleanly.
2. Bedrock player sees native forms via `/dailyquests` and `/dailyquests reroll`.
3. Quest details show the right progress, after progression/renewal and a re-open.
4. Check GET quest with and without required inventory; confirm item deduction is unchanged.
5. Check LOCATION, PLACEHOLDER and `complete_only_on_click` quest behavior.
6. Reroll one and all, including costs, failed payment/refund, bypass permissions, daily cap and cancellation.
7. Keep an old quest form open across a quest renewal/reroll: stale actions must not touch the wrong quest.
8. Repeat a form response after disconnect, plugin reload and on Folia: no off-thread access/errors.

`/dailyquests show <category>` remains the existing category-browser inventory, rather than a native form in this first implementation.
