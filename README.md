# 🚀 ODailyQuests — Wider Compatibility Expansion

A maintained, modernized fork of **O'DailyQuests by Ordwen**, built to preserve the original daily-quest experience while expanding it for current Minecraft servers, RPG systems, Slimefun ecosystems, Pylon/Rebar, fishing plugins and custom-item stacks.

**ODailyQuests 3.0.5** targets Minecraft **1.21.11+**, with Paper as the primary platform, Purpur compatibility, Folia-aware scheduling, Java 21 bytecode and a Java 25 CI toolchain.

> ❤️ **Original project:** ODailyQuests by Ordwen and contributors. This fork preserves the original attribution and remains licensed under GPL-3.0.

[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-00457C?logo=paypal&logoColor=white)](https://www.paypal.com/ncp/payment/ZYS8QH2FVV25U)

If this maintained fork saves you time or helps your server, donations help support continued compatibility work, testing and addon integration.

---

## ✨ What changed in 3.0.5

**Wider Compatibility Expansion** is much more than a version bump. The maintained build now includes:

- **7 daily categories:** Easy, Medium, Hard, Fable Good, Fable Evil, Tech and Wild Card
- dependency-aware **Tech** and **Wild Card** categories
- toggleable built-in compatibility packs for Slimefun Core, many Slimefun addons, Pylon/Rebar and Wild Card integrations
- real `SLIMEFUN_CRAFT` and `SLIMEFUN_ITEM` quest types
- exact Pylon/Rebar item objectives
- ValhallaMMO, mcMMO, PyroFishingPro and EvenMoreFish progression support
- MMOItems and ItemsAdder item recognition
- Fable **Good** and **Evil** as standalone paths
- weighted quests, permission pools, quest chains, weekly categories, streaks and community goals
- migration support for older five-slot player menus
- legacy `%progression%` support plus `%progressPercent%` and `%totalQuests%`
- `/dqadmin doctor` integration/category diagnostics
- production-JAR verification and raw `.jar` GitHub Actions output

### 🖥️ Daily menu refresh

The maintained 3.0.5 interface also gets a usability overhaul:

- **Tech** now occupies the former Fable Evil position
- **Fable Evil** moves to the former Tech position
- **Information** replaces the old “Informations” wording
- the top-right **Daily Reroll** icon is now a working button
- reroll opens a chooser for **Reroll One Quest** or **Reroll All Quests**
- a normal player gets **one reroll action per daily quest set**
- reroll-all counts as one action and replaces the full set atomically
- a bottom-right **Jobs Quests** button opens `/jobs quests` when **Jobs** or **JobsReborn** is installed
- if Jobs/JobsReborn is absent, that slot remains normal filler

---

# 🗺️ Seven daily categories

A maintained default installation is designed to draw **one quest from each available category**:

| Category | Purpose |
|---|---|
| 🌱 **Easy** | approachable everyday objectives |
| ⚔️ **Medium** | more involved survival goals |
| 🔥 **Hard** | higher-effort challenges |
| 🌟 **Fable Good** | Good-aligned progression |
| 🩸 **Fable Evil** | Evil-aligned progression |
| ⚙️ **Tech** | Slimefun, supported addons, Pylon and Rebar |
| 🎲 **Wild Card** | installed RPG, fishing and custom-item integrations |

The category YAML files live in:

```text
plugins/ODailyQuests/quests/
```

Tech and Wild Card are **optional by design**. If no usable provider is available, dependency-tagged built-in quests are filtered rather than assigning an impossible objective.

---

# 🔌 Toggleable addon compatibility

ODailyQuests does **not** require every supported plugin. Compatibility is organized into built-in quest packs under:

```yaml
default_quest_packs:
```

Each maintained pack can be individually enabled or disabled.

A pack is active only when:

1. its `enabled` value is `true`, **and**
2. its required plugin is actually enabled, **and**
3. for Slimefun addon packs, the matching addon is actually detected.

That means you can install ODailyQuests on very different servers without stripping integrations from the JAR.

### Example

```yaml
default_quest_packs:
  slimefun-core:
    enabled: true

  networks:
    enabled: true

  infinity-expansion:
    enabled: false

  pylon-rebar:
    enabled: true

  valhallammo:
    enabled: true

  pyrofishingpro:
    enabled: false
```

Disabling a pack stops its **maintained built-in quests** from entering the rotation. It does not disable the external plugin itself and does not remove untagged administrator-created quests.

## ⚙️ Tech packs

The maintained Tech category can use these toggleable packs when their providers are present:

| Pack key | Integration |
|---|---|
| `slimefun-core` | Slimefun Core |
| `networks` | Networks |
| `networks-expansion` | Networks Expansion |
| `infinity-expansion` | Infinity Expansion / InfinityExpansion2 |
| `fluffy-machines` | Fluffy Machines |
| `foxy-machines` | Foxy Machines |
| `magic-expansion` | Magic Expansion |
| `military-arsenal` | Military Arsenal |
| `slimefun-warfare` | Slimefun Warfare |
| `mob-drops` | Mob Drops |
| `lucky-blocks` | Lucky Blocks |
| `alchimia-vitae` | Alchimia Vitae |
| `dank-tech` | Dank Tech |
| `supreme` | Supreme |
| `gastronomicon` | Gastronomicon |
| `exotic-garden` | Exotic Garden |
| `potion-expansion` | Potion Expansion |
| `flower-power` | Flower Power |
| `fast-machines` | Fast Machines |
| `infernal-farm` | Infernal Farm |
| `idoe` | IDOE / Illegal Dev Items |
| `slimeglue` | SlimeGlue |
| `pylon-rebar` | Pylon / Rebar |

New metadata for maintained packs is added automatically when missing, while existing administrator `enabled` choices are preserved.

## 🎲 Wild Card packs

| Pack key | Integration | Maintained direction |
|---|---|---|
| `valhallammo` | ValhallaMMO | skill progression + spendable skill-point rewards |
| `mcmmo` | mcMMO | skill XP objectives + mcMMO XP rewards |
| `pyrofishingpro` | PyroFishingPro | catches + Entropy/Fishing XP rewards |
| `evenmorefish` | EvenMoreFish | EMF catches + bait rewards |
| `mmoitems` | MMOItems | custom-item acquisition |
| `itemsadder` | ItemsAdder | custom-item acquisition |

**MythicMobs is not automatically injected into Wild Card.** Manual MythicMobs quests remain supported.

---

# ⚙️ Tech — one technology category

Tech deliberately has **no Easy / Medium / Hard label**. The objective itself determines the workload.

## 🧪 Slimefun

The maintained build registers dedicated Slimefun quest types instead of treating every Slimefun item like an ordinary vanilla item.

Exact craft example:

```yaml
quest_type: SLIMEFUN_CRAFT
slimefun_ids:
  - REINFORCED_ALLOY_INGOT
required_amount: 2
```

Addon-family example:

```yaml
quest_type: SLIMEFUN_ITEM
slimefun_addons:
  - Networks
required_amount: 5
```

`SLIMEFUN_CRAFT` supports normal Bukkit crafting plus Slimefun multiblock crafting paths. `SLIMEFUN_ITEM` can recognize which addon owns an item.

## 🔩 Pylon / Rebar

Pylon support uses Rebar item recognition through reflection, keeping Pylon/Rebar optional runtime dependencies.

The maintained Tech pool includes exact-item defaults such as:

- 🧲 **Craft a Shimmer Magnet** — `pylon:shimmer_magnet`
- 🔨 **Craft a Diamond Hammer** — `pylon:diamond_hammer`
- 🛗 **Craft an Elevator I** — `pylon:elevator_1`
- 💀 **Craft a Reactivated Wither Skull** — `pylon:reactivated_wither_skull`

These match the exact Pylon namespaced key. A different Pylon item cannot satisfy the wrong objective.

The four maintained definitions are physically bundled in:

```text
quests/pylon-defaults.yml
```

---

# 🎲 Wild Card — installed plugins join the rotation

Wild Card lets optional plugins contribute daily objectives without turning them into hard dependencies for ODailyQuests.

Provider-specific rewards can also stay inside the ecosystem that supplied the quest:

```text
ValhallaMMO    → spendable skill points
mcMMO          → mcMMO XP
PyroFishingPro → Entropy / Fishing XP
EvenMoreFish   → bait
```

MMOItems and ItemsAdder IDs are server-specific, so the stock release intentionally avoids inventing reward item IDs that may not exist on another server.

---

# 🧠 Maintained integration quest types

3.0.5 guarantees these maintained types are registered before quest YAML is parsed:

```text
SLIMEFUN_ITEM
SLIMEFUN_CRAFT
REBAR_ITEM
MCMMO_EXP
MMOITEM_ITEM
ITEMSADDER_ITEM
EMF_FISH
```

This matters because a YAML quest cannot load correctly if its type is missing from the runtime registry. CI now tests the registry and verifies critical runtime classes/resources in the final production JAR.

---

# 🔄 Daily reroll system

The top-right **Daily Reroll** button opens a dedicated GUI with two choices:

### Reroll One Quest
Choose one active quest and replace it with another quest from the same category.

### Reroll All Quests
Replace the entire active daily set at once.

For normal players, either choice consumes the same **one daily reroll action**. A player cannot reroll one quest and then reroll all again during the same daily set.

The existing administrator bypass permission still bypasses the normal reroll limit.

If reroll costs are configured, failed rerolls are refunded rather than charging the player for a replacement that could not be generated.

---

# 💼 Jobs / JobsReborn shortcut

If either **Jobs** or **JobsReborn** is enabled, the bottom-right corner of the daily quest menu becomes a **Jobs Quests** button and executes:

```text
/jobs quests
```

If neither plugin exists, ODailyQuests hides the button in memory and leaves the configured filler appearance intact.

This is a menu shortcut only; Jobs/JobsReborn does not become a hard dependency.

---

# 🛡️ Upgrade without wiping your configuration

Existing server files are treated as administrator-owned.

3.0.5 intentionally avoids “fixing” upgrades by deleting the plugin folder:

- Easy/Medium/Hard quest files are not blindly overwritten
- Good/Evil files remain editable server content
- missing maintained category files are created automatically
- genuinely empty old Tech/Wild Card stubs can be replaced with populated defaults
- real Tech/Wild Card files containing quests are preserved
- exact Pylon defaults are merged only into a managed Pylon/Rebar Tech pool
- custom menu styling is preserved where migration can prove a slot is safe
- old five-slot menus are expanded rather than replaced
- old `%achieved%/5` menu counters migrate to `%achieved%/%totalQuests%`
- recognized old interface values can receive the maintained Tech/Evil slot swap and menu-button updates without rewriting unrelated custom items

After upgrading from a saved five-quest set, you can force a fresh draw for testing with:

```text
/dqadmin reset quests <player>
```

---

# 🩺 Diagnostics

Run:

```text
/dqadmin doctor
```

The maintained doctor report helps diagnose optional compatibility and shows information such as:

- loaded categories
- registered quest types
- active/disabled/waiting default packs
- **Tech available: true/false**
- **Wild Card available: true/false**
- detected Slimefun, Pylon, Rebar, mcMMO and other integrations

If Tech or Wild Card is missing, this should be the first command to run.

---

# 🖥️ Placeholders

Maintained placeholders include:

```text
%progress%
%progression%      legacy alias
%required%
%progressBar%
%progressPercent%
%rewardAmount%
%displayName%
%achieved%
%totalQuests%
%drawIn%
%status%
```

`%progression%` remains supported as an alias so older interfaces do not suddenly print the token literally after upgrading.

---

# 🧩 Extended progression features

The maintained fork also includes:

- weighted quest selection
- permission-controlled quest pools
- per-quest requirement/reward multipliers
- quest chains
- weekly categories
- reroll costs and safe refunds
- completion streaks
- community/server-wide goals

These features remain configurable; missing extended settings preserve legacy-style behavior.

---

# 🧵 Paper / Purpur / Folia modernization

Modern compatibility work includes:

- current Paper APIs
- Paper/Purpur 1.21.11+ target
- Folia-aware scheduling
- entity-aware scheduling for player-owned mutations
- concurrent active-player quest state
- Java 21 bytecode
- Java 25 CI builds
- optional integrations isolated behind reflection where appropriate

Folia compatibility is treated as a real scheduling/threading concern, not just a metadata flag.

---

# 🧪 Build and packaging safeguards

Build locally with:

```bash
./gradlew clean test shadowJar
```

GitHub Actions verifies the packaged JAR before upload. CI checks include:

- placeholder-condition runtime classes
- maintained integration quest classes
- daily reroll GUI/service classes
- all seven category YAML resources
- `quests/pylon-defaults.yml`
- no missing internal `com.ordwen.odailyquests...` class references detectable by `jdeps`

The versioned plugin output is a **raw JAR**, not an extra ZIP wrapper:

```text
ODailyQuests3.0.5-Spigot-1.21.11+.jar
```

---

# 📥 Installation / upgrade

1. Stop the server.
2. Replace the old ODailyQuests JAR with the new JAR.
3. **Keep your existing `plugins/ODailyQuests/` folder.**
4. Start the server normally.
5. Run `/dqadmin doctor` to review detected integrations and optional categories.

For upgrade testing, do not delete your quest/config folder unless you intentionally want a fresh configuration.

---

# ❤️ Support development

If you enjoy the maintained fork and want to help support future compatibility work:

### [💙 Donate with PayPal](https://www.paypal.com/ncp/payment/ZYS8QH2FVV25U)

Donations are optional and help support testing, maintenance, new integrations and continued modernization work.

---

# 📚 Original documentation

For the original plugin's standard configuration concepts:

- https://ordwenplugins.gitbook.io/odailyquests/
- https://github.com/Ordwen/ODailyQuests

The maintained fork adds functionality beyond the original documentation, especially the seven-category model, dependency-backed integrations and modernized runtime behavior described here.

---

# ❤️ Credits & license

**Original ODailyQuests:** Ordwen and contributors  
**Maintained fork:** compatibility and expansion work for current Minecraft servers

Licensed under the **GNU General Public License v3.0**. See [`LICENSE`](LICENSE).

This project is independent and is not affiliated with Mojang Studios or Microsoft.

**Preserve the original daily-quest experience, modernize the runtime, and let every server choose exactly which ecosystems join the rotation.**
