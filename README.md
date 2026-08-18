# 🚀 ODailyQuests — Wider Compatibility Expansion

A modern maintained fork of **O'DailyQuests by Ordwen**, focused on keeping the original daily-quest experience useful on current Minecraft servers while opening the quest board to RPG, technology, fishing and custom-item ecosystems.

**3.0.5** targets Minecraft **1.21.11+** with Paper as the primary platform, Purpur compatibility, Folia-aware scheduling, Java 21 bytecode and a Java 25 CI toolchain.

> ❤️ **Original project:** ODailyQuests by Ordwen and its contributors. This fork preserves that attribution and remains licensed under GPL-3.0.

---

## ✨ 3.0.5 at a glance

The Wider Compatibility Expansion adds:

- **7 maintained daily categories**
- dependency-aware **Tech** and **Wild Card** rotations
- real Slimefun crafting and addon-aware objectives
- exact Pylon/Rebar item objectives
- ValhallaMMO, mcMMO, PyroFishingPro and EvenMoreFish progression
- MMOItems and ItemsAdder item recognition
- Fable **Good** and **Evil** daily paths
- weighted quests, permission pools and quest chains
- weekly categories, streaks and community goals
- configurable reroll costs and safe failed-reroll refunds
- `%progressPercent%` and `%totalQuests%`
- backward-compatible `%progression%`
- `/dqadmin doctor` diagnostics
- migration support for older five-slot menus
- production-JAR packaging checks in CI
- a **raw versioned JAR** workflow artifact

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
| ⚙️ **Tech** | Slimefun, addons, Pylon and Rebar |
| 🎲 **Wild Card** | installed RPG, fishing and custom-item plugins |

The category YAML files live in:

```text
plugins/ODailyQuests/quests/
```

Tech and Wild Card are optional. If their providers are unavailable, their dependency-tagged defaults are filtered out rather than giving players impossible quests.

---

# ⚙️ Tech — one technology category

Tech deliberately has **no Easy / Medium / Hard label**. The objective itself determines the workload.

Built-in Tech coverage includes:

- Slimefun Core
- Networks / Networks Expansion
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

## 🧪 Slimefun

The maintained build registers dedicated integration quest types instead of treating Slimefun items like ordinary vanilla items.

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

`SLIMEFUN_CRAFT` supports normal Bukkit crafting and Slimefun multiblock crafting paths. `SLIMEFUN_ITEM` can recognize which addon owns an item.

## 🔩 Pylon / Rebar

Pylon support uses Rebar item recognition through reflection, so Pylon/Rebar remain optional runtime dependencies.

The maintained Tech pool keeps broad Pylon/Rebar objectives **and now includes exact-item defaults** such as:

- 🧲 **Craft a Shimmer Magnet** — `pylon:shimmer_magnet`
- 🔨 **Craft a Diamond Hammer** — `pylon:diamond_hammer`
- 🛗 **Craft an Elevator I** — `pylon:elevator_1`
- 💀 **Craft a Reactivated Wither Skull** — `pylon:reactivated_wither_skull`

These use exact Pylon namespaced keys. A random Pylon item does **not** satisfy an exact-item objective.

The four maintained definitions are physically bundled in:

```text
quests/pylon-defaults.yml
```

On startup they are merged additively into a managed `tech.yml`. A completely custom Tech file with no maintained Pylon pack is left alone.

---

# 🎲 Wild Card — installed plugins join the rotation

Wild Card lets optional plugins contribute daily objectives without making those plugins mandatory for ODailyQuests itself.

| Provider | Built-in direction | Provider-aware reward |
|---|---|---|
| **ValhallaMMO** | skill progression | spendable Valhalla skill points |
| **mcMMO** | skill XP objectives | mcMMO XP |
| **PyroFishingPro** | Pyro fish catches | Entropy + Fishing XP |
| **EvenMoreFish** | EMF catches | EvenMoreFish bait |
| **MMOItems** | MMOItems acquisition | safe generic defaults |
| **ItemsAdder** | custom item acquisition | safe generic defaults |

MMOItems and ItemsAdder IDs are server-specific, so the stock release intentionally does not invent item IDs for rewards.

**MythicMobs is not automatically injected into Wild Card.** Existing manual MythicMobs quest support remains available.

---

# 🔌 Enabling Tech and Wild Card

The category amounts should exist in `config.yml`:

```yaml
quests_per_category:
  easy: 1
  medium: 1
  hard: 1
  good: 1
  evil: 1
  tech: 1
  wildcard: 1
```

The relevant packs should also be enabled under `default_quest_packs`:

```yaml
default_quest_packs:
  slimefun-core:
    enabled: true

  pylon-rebar:
    enabled: true

  valhallammo:
    enabled: true

  mcmmo:
    enabled: true
```

You do **not** need every provider. One usable Tech provider can keep Tech active; one usable Wild Card provider can keep Wild Card active.

If one of the optional categories is missing, run:

```text
/dqadmin doctor
```

The doctor report now shows:

- loaded category names
- registered quest type count
- default-pack status
- active pack names
- **Tech available: true/false**
- **Wild Card available: true/false**
- detected Slimefun, Pylon, Rebar, mcMMO and other integrations

---

# 🧠 Maintained integration quest types

3.0.5 registers the quest types required by the Wider Compatibility Expansion before category YAML is loaded:

```text
SLIMEFUN_ITEM
SLIMEFUN_CRAFT
REBAR_ITEM
MCMMO_EXP
MMOITEM_ITEM
ITEMSADDER_ITEM
EMF_FISH
```

This registration is important: a YAML quest is not useful if its type disappears from the runtime registry. CI now tests the maintained registry and verifies critical classes/resources exist in the final production JAR.

---

# 🛡️ Upgrade without wiping your configuration

Existing server files are treated as administrator-owned.

3.0.5 intentionally avoids “fixing” upgrades by deleting the plugin folder:

- Easy/Medium/Hard files are not blindly overwritten.
- Good/Evil files remain editable server content.
- missing maintained category files are created automatically.
- genuinely empty old Tech/Wild Card stubs can be replaced with populated defaults.
- a Tech/Wild Card file containing real quests is preserved.
- exact Pylon defaults are added only to a Tech file that already contains the maintained `pylon-rebar` pack.
- custom menu styling is preserved where migration can prove a slot is safe.

## Older five-slot menus

Some older installations use a numbered interface similar to:

```yaml
quests:
  '1': 19
  '2': 21
  '3': 23
  '4': 25
  '5': 27
```

That can display only five assigned quests. 3.0.5 migrates this layout by finding safe unused/filler positions for quests 6 and 7 rather than replacing the whole interface.

Category-based interfaces are also supported.

A player-head line hardcoded to something like `%achieved%/5` is migrated to `%achieved%/%totalQuests%` so the displayed total follows the actual assigned set.

After upgrading from a five-quest saved set, you can force a fresh daily draw for testing with:

```text
/dqadmin reset quests <player>
```

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

The `%progression%` alias is intentionally retained so older interfaces do not suddenly print the token literally after an upgrade.

---

# 🎁 Provider-aware rewards

Integration quests can reward progression from the plugin that supplied the objective. Examples include:

```text
ValhallaMMO    → skill points
mcMMO          → mcMMO XP
PyroFishingPro → Entropy / Fishing XP
EvenMoreFish   → bait
```

Normal economy, vanilla XP and quest-point commands can still be combined with those rewards. Everything remains editable in YAML.

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

These features are optional; missing extended configuration keeps the legacy-style behavior.

---

# 🧵 Paper / Purpur / Folia modernization

Modern compatibility work includes:

- current Paper APIs
- entity-aware scheduling for player-owned mutations
- Folia-aware timed work
- concurrent active-player quest state
- Java 21 bytecode
- Java 25 CI builds
- optional integrations isolated behind reflection where appropriate

Folia compatibility is treated as a threading/scheduling concern, not just a metadata flag.

---

# 🧪 Build and packaging safeguards

Build locally with:

```bash
./gradlew clean test shadowJar
```

GitHub Actions then verifies the packaged JAR before upload. Among other checks, CI makes sure the production JAR contains:

- placeholder-condition runtime classes
- maintained integration quest classes
- all seven main quest YAML resources
- `quests/pylon-defaults.yml`
- no missing internal `com.ordwen.odailyquests...` class references detectable by `jdeps`

That packaging guard exists specifically to prevent a build where code references a class that was accidentally omitted from the released JAR.

The development artifact is uploaded as the raw versioned plugin file:

```text
ODailyQuests3.0.5-Spigot-1.21.11+.jar
```

Ordinary PR/test builds do **not** publish the official GitHub release.

---

# 📚 Original documentation

For the original plugin's standard configuration concepts, see:

- https://ordwenplugins.gitbook.io/odailyquests/
- https://github.com/Ordwen/ODailyQuests

The maintained fork adds features beyond that documentation, especially the seven-category model and dependency-backed integrations described here.

---

# ❤️ Credits & license

**Original ODailyQuests:** Ordwen and contributors  
**Maintained fork:** compatibility and expansion work for current Minecraft servers

Licensed under the **GNU General Public License v3.0**. See [`LICENSE`](LICENSE).

This project is independent and is not affiliated with Mojang Studios or Microsoft.

**The goal is simple: preserve the original daily-quest experience while giving modern servers a much larger world to build quests around.**
