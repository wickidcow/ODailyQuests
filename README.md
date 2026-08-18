# ODailyQuests – Maintained 1.21.11+ Fork

A maintained fork of **O'DailyQuests by Ordwen**, focused on modern Paper/Purpur/Folia compatibility, stability, wider plugin integration, and richer daily progression while preserving administrator-owned configuration wherever possible.

> This project builds on the original work by Ordwen and the ODailyQuests contributors. Please give credit to the original project when redistributing or extending this fork.

## Downloads

Use the versioned raw JAR from this fork's GitHub Releases or GitHub Actions artifacts. Development artifacts are uploaded directly as `.jar` files rather than being wrapped in an extra ZIP.

The original documentation remains the best reference for standard ODailyQuests configuration:

- https://ordwenplugins.gitbook.io/odailyquests/
- https://github.com/Ordwen/ODailyQuests

## 3.0.5 — Wider Compatibility Expansion

This maintained build adds:

- Paper/Purpur 1.21.11+ compatibility with Java 21 bytecode
- Java 25 build toolchain support
- Folia-aware scheduling and concurrent active-player state
- raw JAR GitHub output
- weighted quest selection and permission-based pools
- optional per-quest requirement/reward scaling
- quest chains and weekly categories
- configurable reroll costs with failed-reroll refunds
- completion streak rewards
- server-wide/community goals
- seven maintained daily categories
- true Slimefun multiblock crafting progression
- addon-aware Slimefun objectives
- Pylon/Rebar Tech objectives
- Fable Good and Fable Evil daily paths
- dependency-aware Wild Card integrations
- provider-native rewards where a stable provider reward exists
- `/dqadmin doctor` diagnostics
- `%progressPercent%` placeholder
- automated CI tests before JAR staging

## Seven daily categories

The maintained defaults are designed around **one daily quest from each loaded category**:

1. **Easy** — `easy.yml`
2. **Medium** — `medium.yml`
3. **Hard** — `hard.yml`
4. **Fable Good** — `good.yml`
5. **Fable Evil** — `evil.yml`
6. **Tech** — `tech.yml`
7. **Wild Card** — `wildcard.yml`

The category files are real, readable YAML resources. **Tech and Wild Card are pre-generated in the JAR; they are not empty placeholder files and do not depend on invisible runtime-only quest definitions.** Every built-in integration quest stores a `default_pack` tag directly in YAML.

Existing server quest files are administrator-owned and are not blindly overwritten. Missing maintained category files are added automatically. Two short-lived development builds created empty Tech/Wild Card shell files; those exact empty managed stubs are migrated to the populated versions automatically, while any file containing real custom quests is preserved.

## ⚙️ Tech — one technology category

Tech is intentionally **not** split into Easy / Medium / Hard. The player simply receives a Daily Tech objective; the rolled objective itself determines the workload and reward.

The bundled `tech.yml` contains physical quest definitions for:

- Slimefun Core
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
- Pylon / Rebar

### Slimefun Core examples

The physical Tech pool includes objectives such as:

- craft **1 Common Talisman**
- craft **8 Steel Ingots**
- craft **2 Reinforced Alloy Ingots**

`SLIMEFUN_CRAFT` progresses from normal Bukkit crafting and Slimefun's multiblock craft event. `SLIMEFUN_ITEM` can match the addon that registered an item, allowing a single Tech pool to cover many maintained addon families without hard-linking ODailyQuests to every addon.

Pylon/Rebar objectives use optional item recognition and remain inactive when Pylon is not available.

If all Tech providers are absent or disabled and there are no custom Tech quests, the empty optional category is skipped cleanly instead of causing `safety_mode` to disable the plugin.

## 🎲 Wild Card — installed plugins join the rotation

The bundled `wildcard.yml` also contains physical quest definitions. A quest is eligible only while its `default_pack` dependency is enabled and detected.

Built-in providers are:

- **ValhallaMMO** — earn specific Valhalla skill experience; rewards include real spendable Valhalla skill points
- **mcMMO** — earn specific mcMMO skill experience; rewards include mcMMO skill XP
- **PyroFishingPro** — catch Pyro fish; rewards include Pyro Entropy and Pyro Fishing XP
- **EvenMoreFish** — catch EMF fish; rewards include bundled EvenMoreFish bait
- **MMOItems** — create or obtain MMOItems
- **ItemsAdder** — create or obtain ItemsAdder custom items

MMOItems and ItemsAdder content IDs are deliberately not invented in the generic defaults because those IDs belong to each server's own item packs. Their built-in quests therefore use safe economy/XP/Quest Point rewards until an administrator chooses a server-specific MMOItems/ItemsAdder item reward.

If no Wild Card provider is installed and no custom Wild Card quests exist, Wild Card is skipped for that startup. It does not become an impossible quest and does not break safety mode.

**MythicMobs is intentionally not injected automatically.** Existing manual MythicMobs quest support remains available for servers that explicitly configure it.

## 🌿 Fable Good / 🔥 Fable Evil

Fable is split into two real daily categories: **Good** and **Evil**. The bundled Good/Evil pools use the server-tested quest definitions and retain their Good/Evil scoreboard progression and configured rewards.

Only Good/Evil terminology is used by the maintained Fable system. Migration code cleans the obsolete short-lived terminology from older test configs without changing unrelated administrator content.

## Dependency packs and toggles

`config.yml` contains a `default_quest_packs` section. Dependency-backed packs default to enabled, but a tagged quest survives loading only when its provider is present. Setting a pack to `enabled: false` always wins.

This means `wildcard.yml` can safely contain ValhallaMMO, mcMMO, PyroFishingPro, EvenMoreFish, MMOItems, and ItemsAdder quests at the same time: a server only loads the subset it can actually complete.

The same model is used by Tech for Slimefun/addon/Pylon content.

## Extended quest keys

Individual quests can use maintained-fork features directly:

```yaml
quests:
  10:
    name: "&bWeighted Challenge"
    quest_type: BREAK
    required: DIAMOND_ORE
    required_amount: 64

    # Relative chance inside the eligible category/pool.
    weight: 2.5

    # Optional permission-controlled pool.
    pool: veteran

    # Optional scaling without duplicating the quest.
    difficulty:
      required_multiplier: 1.5
      reward_multiplier: 2.0
```

### Quest chains

A chain-only quest is excluded from the normal random draw and replaces its predecessor when that predecessor completes:

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

A qualified chain ID such as `easy:20` is also supported.

### Slimefun exact-item quest

```yaml
quests:
  30:
    name: "&aSlimefun Engineer"
    quest_type: SLIMEFUN_CRAFT
    required_amount: 2
    slimefun_ids:
      - REINFORCED_ALLOY_INGOT
```

### Slimefun addon-family quest

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

## Optional progression features

Missing extended sections retain legacy behavior. Examples include:

```yaml
weekly_categories:
  - weekly

quest_pools:
  veteran:
    enabled: true
    permission: "odailyquests.pool.veteran"

reroll_cost:
  enabled: false
  type: MONEY
  amount: 1000

streak_rewards:
  enabled: false
  milestones:
    3:
      commands:
        - "give %player% diamond 1"
    7:
      commands:
        - "eco give %player% 5000"

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

The report includes runtime/server/Java information, storage mode, loaded categories and quests, registered quest types, active player data, next reset, dependency-pack status, optional features, and detected integrations.

## Compatibility philosophy

Optional integrations activate only when their plugin is present. Integrations without a dependable compile-time API are isolated behind reflection where practical so missing optional plugins do not prevent ODailyQuests from starting.

Folia support requires more than a metadata flag; this fork routes timed player mutations through entity-aware scheduling and uses concurrent storage for active player assignments.

## License and attribution

This fork retains the repository's **GNU General Public License v3.0**. See [`LICENSE`](LICENSE) for the full terms.

Original project and concept: **Ordwen / ODailyQuests**. This maintained fork is independent and is not affiliated with Mojang Studios or Microsoft.
