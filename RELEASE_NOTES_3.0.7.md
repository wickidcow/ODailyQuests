# ODailyQuests 3.0.7 — Reroll Performance Update

ODailyQuests 3.0.7 focuses on reducing server-thread work when players reroll quests, especially when using **reroll all** on servers with large quest pools or PlaceholderAPI-based requirements.

## Performance improvements

- Optimized **reroll all** to build the eligible quest pool only once per category during a reroll operation.
- Reuses the filtered category pool for additional quest slots instead of rescanning every quest repeatedly.
- Prevents repeated permission, quest-pool, and PlaceholderAPI condition evaluations for the same candidates during one reroll-all action.
- Already-active quests are excluded before more expensive eligibility checks.
- Selected replacements are removed from the reusable pool, preserving duplicate prevention.
- Weighted quest selection is preserved.
- Added debugger timing for single-reroll selection and reroll-all selection to make future performance reports easier to diagnose.

## Behavior preserved

This is a performance-focused update. Existing reroll behavior remains intact, including:

- reroll limits and bypass permissions
- reroll costs and refunds
- completed-quest restrictions
- category restrictions
- pool rules
- quest permissions
- PlaceholderAPI conditions
- weighted quest selection
- chain-only quest exclusion
- atomic reroll-all behavior

## Compatibility

- Minecraft / Spigot API: **1.21.11+**
- Build JDK: **Java 25**
- Runtime bytecode target: **Java 21+**
- Paper / Spigot compatible
- Folia-aware scheduling compatibility retained

## Upgrade

Replace the previous ODailyQuests JAR with the 3.0.7 JAR and restart the server. No configuration migration is required for this update.
