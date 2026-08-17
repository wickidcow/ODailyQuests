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
- built-in optional Slimefun item quest integration
- `/dqadmin doctor` diagnostics
- `%progressPercent%` placeholder
- automated unit tests in CI

All new progression systems are opt-in unless explicitly configured.

## Extended quest keys

The following keys can be added directly to an individual quest entry:

```yaml
quests:
  10:
    name: "&bWeighted Challenge"
    type: BREAK
    required: 64
    required_item: DIAMOND_ORE

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
    type: BREAK
    required: 16
    required_item: IRON_ORE

  21:
    name: "&6Chain Part 2"
    type: BREAK
    required: 32
    required_item: GOLD_ORE
    chain_after: "20"
```

You can also use a qualified ID such as `easy:20` in `chain_after`.

### Slimefun item quests

`SLIMEFUN_ITEM` is built into this fork as an optional quest type. Slimefun itself remains a soft dependency.

```yaml
quests:
  30:
    name: "&aSlimefun Engineer"
    type: SLIMEFUN_ITEM
    required: 4
    slimefun_ids:
      - ELECTRIC_MOTOR
      - ELECTRIC_MOTOR_2
```

The quest progresses when matching Slimefun items are crafted or picked up. If `slimefun_ids` is omitted, any recognized Slimefun item can progress the quest.

## Optional config.yml additions

These sections can be added to an existing config. Missing sections retain legacy behavior.

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

The report includes the plugin/server/Java versions, runtime type, storage mode, loaded categories and quests, registered quest types, active player data, next reset, optional feature state, and detected integrations.

## Compatibility philosophy

Optional integrations are loaded only when their plugin is present. Integrations that do not publish stable compile-time artifacts are isolated behind reflection where practical so a missing optional plugin does not prevent ODailyQuests from starting.

Folia support requires more than a `folia-supported` flag; this fork routes timed player mutations through entity-aware scheduling and uses concurrent storage for active player assignments.

## License and attribution

This fork retains the repository's **GNU General Public License v3.0**. See [`LICENSE`](LICENSE) for the full terms.

Original project and concept: **Ordwen / ODailyQuests**. This maintained fork is independent and is not affiliated with Mojang Studios or Microsoft.
