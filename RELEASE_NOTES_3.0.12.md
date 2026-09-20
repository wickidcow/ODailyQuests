# ODailyQuests 3.0.12

Default basic quest reward update.

## Easy / Medium / Hard defaults
- Easy quests now grant 1,000 Gold, 550 XP points, and 1 Quest Point.
- Medium quests now grant 2,000 Gold, 2,920 XP points, and 1 Quest Point.
- Hard quests now grant 4,000 Gold, 8,670 XP points, and 1 Quest Point.
- All 12 bundled quests in each category use the same reward bundle for that difficulty.
- Reward lore now shows Gold, XP, and 1 Quest Point consistently.
- Bundled vanilla quests remain tagged with `default_pack: vanilla`.

## Upgrade behavior
- These are fresh-install/default-file changes only.
- Existing server quest files are not overwritten by ODailyQuests, so customized Easy/Medium/Hard rewards stay intact unless the server owner replaces those files manually.

## Compatibility
- Retains the Paper 26.3 compatibility and quest-event hardening from 3.0.11.
