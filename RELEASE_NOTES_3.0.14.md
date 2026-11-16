# ODailyQuests 3.0.14 — Native Bedrock Daily Quest Forms

## New: native Bedrock forms for Floodgate players (#12)
- Bedrock clients connecting through Floodgate can use native, touch-friendly daily quest forms using `/dailyquests` or `/dailyquests me`.
- A quest overview shows category, progress, and completion status; individual quest pages show objectives and reward information.
- Check/claim actions reuse the existing validation and reward pathways for GET, LOCATION, PLACEHOLDER, and manual-completion objectives.
- `/dailyquests reroll` now opens a native selection flow, including reroll-one, confirmation for reroll-all, and the existing daily limits and costs.
- Form callbacks run on the player's region-safe scheduler, guarding against player disconnects and changed quests between form opening and selection.
- Java players keep the existing inventory menus. Floodgate is optional; servers without it remain supported.
- Admins can disable native forms by adding `bedrock_forms.enabled: false` to `plugins/ODailyQuests/config.yml`. Existing server configurations and player data remain intact.

## Compatibility
- Primary: Paper 26.3 (Java 25 server); compatibility compile checks for Paper 26.2 and Minecraft 1.21.11.
- Java 21 distributable bytecode target is unchanged.
- No database, quest storage, or reward changes. No additional JAR required on servers without Floodgate.
- The `/dailyquests show <category>` category browser remains an inventory-based interface in this release.

**Testing:** Build/unit and compatibility compilation are automated. Native Bedrock/Geyser in-game testing must still be performed on a live server; this was not verified by CI.

**Documentation:** [Bedrock Forms setup and test checklist](BEDROCK_FORMS.md)
