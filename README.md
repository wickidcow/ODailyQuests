# ODailyQuests – Maintained 1.21.11+ Fork

A maintained fork of **O'DailyQuests by Ordwen**, focused on modern Paper/Purpur/Folia compatibility, stability, integrations, and expanded quest progression while preserving existing ODailyQuests configurations wherever possible.

> This project builds on the original work by Ordwen and the ODailyQuests contributors. Please give credit to the original project when redistributing or extending this fork.

## Downloads

Use the raw JAR attached to this fork's GitHub Releases:

- https://github.com/wickidcow/ODailyQuests/releases

The original documentation remains the best reference for standard quest configuration:

- https://ordwenplugins.gitbook.io/odailyquests/
- https://github.com/Ordwen/ODailyQuests

## 3.0.5 modernization

This maintained build adds:

- Paper/Purpur 1.21.11+ compatibility with Java 21 bytecode
- Java 25 build toolchain support
- Folia-aware scheduling and concurrent active-player state
- raw JAR GitHub release output
- aligned runtime/compile database libraries
- safer quest-type registration (no silent conflicting overrides)
- weighted quest selection
- permission-based quest pools
- optional per-quest difficulty/reward scaling
- quest chains
- weekly quest categories that survive daily resets until the ISO week changes
- configurable reroll costs with automatic refund on failed rerolls
- optional completion streak rewards
- optional server-wide/community goals
- automatic, toggleable Easy/Medium/Hard starter quest packs
- true Slimefun multiblock crafting quests plus addon-aware Slimefun quests
- Fable Quests with Good and Evil paths
- `/dqadmin doctor` diagnostics
- `%progressPercent%` placeholder
- automated unit tests in CI

## Default quest packs

The maintained fork adds a `default_quest_packs` section to `config.yml` automatically. Every shipped pack has an `enabled` toggle. Dependency-backed packs default to enabled but only become active when the matching plugin/addon is actually installed.

Setting `enabled: false` always wins, even if the dependency is present.

Fresh installations tag the bundled Easy/Medium/Hard Vanilla quests as the `vanilla` pack. Existing untagged server quest files are treated as administrator-owned custom content and are never silently disabled or overwritten.

Each active pack contributes an **Easy**, **Medium**, and **Hard** quest to the existing difficulty pools. Difficulty raises both the objective and the reward.

Built-in packs include:

- Vanilla Starter
- **Fable Quests - Good**
- **Fable Quests - Evil**
- Slimefun Core
- ValhallaMMO
- EvenMoreFish
- PyroFishingPro
- Networks
- Networks Expansion
- Infinity Expansion / InfinityExpansion2
- Fluffy Machines
- Foxy Machines
- Magic Expansion
- Military Arsenal
- Slimefun Warfare
- Mob Drops
- Lucky Blocks
- Alchimia Vitae
- Dank Tech
- Supreme
- Gastronomicon
- Exotic Garden
- Potion Expansion
- Flower Power
- Fast Machines
- Infernal Farm
- IDOE
- SlimeGlue

Slimefun addon detection is based on the addon that registered an item rather than a hard compile dependency, with aliases for common/forked addon names.

**MythicMobs is intentionally not included as a default quest pack.** Existing manual MythicMobs quest support is unchanged for servers that explicitly configure it.

### Slimefun difficulty examples

The Slimefun Core defaults deliberately scale with crafting complexity:

- Easy: craft **1 Common Talisman** — 500
- Medium: craft **8 Steel Ingots** — 1,100
- Hard: craft **2 Reinforced Alloy Ingots** — 2,500

`SLIMEFUN_CRAFT` listens to normal Bukkit crafting and Slimefun's multiblock craft event. `SLIMEFUN_ITEM` can additionally filter by the Slimefun addon that owns the item, which lets addon packs survive recipe and item-layout changes more gracefully.

## Extended quest keys

The following keys can be added directly to an individual quest entry:

```yaml
quests:
  10:
    name: "&bWeighted Challenge"
    quest_type: BREAK
    required: DIAMOND_ORE
    required_amount: 64

    # Relative chance inside the eligible category/pool.
    weight: 2.5

    # Optional pool controlled from config.yml.
    pool: veteran

    # Optional scaling without duplicating quest definitions.
    difficulty:
      required_multiplier: 1.5
      reward_multiplier: 2.0
```

### Quest chains

A chain-only quest is excluded from the normal random draw and replaces its predecessor when that predecessor is completed:

```yaml
quests:
  20:
    name: "&eChain Part 1"
    quest_type: BREAK
    required: IRON_ORE
    required_amount: 16

  21:
    name: "&6Chain Part 2"
    quest_type: BREAK
    required: GOLD_ORE
    required_amount: 32
    chain_after: "20"
```

You can also use a qualified ID such as `easy:20` in `chain_after`.

### Slimefun quests

Slimefun itself remains a soft dependency.

```yaml
quests:
  30:
    name: "&aSlimefun Engineer"
    quest_type: SLIMEFUN_CRAFT
    required_amount: 2
    slimefun_ids:
      - REINFORCED_ALLOY_INGOT
```

To target an addon family instead of exact IDs:

```yaml
quests:
  31:
    name: "&bNetworks Builder"
    quest_type: SLIMEFUN_ITEM
    required_amount: 5
    slimefun_addons:
      - Networks
      - Networks Expansion
```

## Optional config.yml additions

These sections can be added to an existing config. Missing sections retain legacy behavior. The `default_quest_packs` section does not need to be copied manually; the plugin creates missing pack toggles while preserving any values already set by the administrator.

```yaml
# Categories listed here keep their current assignment through daily resets
# and redraw when the ISO week changes.
weekly_categories:
  - weekly

# Pools can require permissions and can be switched off without editing quest files.
quest_pools:
  veteran:
    enabled: true
    permission: "odailyquests.pool.veteran"

# Cost is charged only when enabled. Supported types: MONEY, EXP_LEVELS.
# odailyquests.reroll.free bypasses the charge.
reroll_cost:
  enabled: false
  type: MONEY
  amount: 1000
  insufficient_message: "&cYou need %amount% %type% to reroll."
  charged_message: "&eReroll cost: &6%amount% %type%&e."

# Consecutive fully-completed quest periods.
streak_rewards:
  enabled: false
  milestones:
    3:
      commands:
        - "give %player% diamond 1"
    7:
      commands:
        - "eco give %player% 5000"

# Server-wide counters. Each matching completed quest adds one point.
community_quests:
  enabled: false
  goals:
    weekly_mining:
      enabled: true
      category: "*"
      quest_type: BREAK
      target: 500
      period: WEEKLY
      commands:
        - "broadcast The server completed %goal%!"
```

## Diagnostics

Run:

```text
/dqadmin doctor
```

The report includes the plugin/server/Java versions, runtime type, storage mode, loaded categories and quests, registered quest types, active player data, next reset, starter-pack status, active pack names, optional feature state, and detected integrations.

## Compatibility philosophy

Optional integrations are loaded only when their plugin is present. Integrations that do not publish stable compile-time artifacts are isolated behind reflection where practical so a missing optional plugin does not prevent ODailyQuests from starting.

Folia support requires more than a `folia-supported` flag; this fork routes timed player mutations through entity-aware scheduling and uses concurrent storage for active player assignments.

## License and attribution

This fork retains the repository's **GNU General Public License v3.0**. See [`LICENSE`](LICENSE) for the full terms.

Original project and concept: **Ordwen / ODailyQuests**. This maintained fork is independent and is not affiliated with Mojang Studios or Microsoft.
