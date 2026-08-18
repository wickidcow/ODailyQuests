# 🚀 ODailyQuests — Wider Compatibility Expansion

**ODailyQuests is now maintained and developed by [wickidcow](https://github.com/wickidcow)**, carrying the project forward for modern Minecraft servers while preserving the spirit, flexibility, and daily-quest experience that made the original plugin successful.

The project was **originally created by Ordwen and the original ODailyQuests contributors**. Their work established the foundation this project continues to build on, and that contribution will always be credited here.

**ODailyQuests 3.0.5** targets Minecraft **1.21.11+**, uses **Java 21 bytecode**, and is built/tested with a **Java 25** CI toolchain.

[![Release](https://img.shields.io/badge/Release-v3.0.5-2ea44f?logo=github)](https://github.com/wickidcow/ODailyQuests/releases/tag/v3.0.5)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11%2B-62b47a)](https://github.com/wickidcow/ODailyQuests/releases)
[![Java](https://img.shields.io/badge/Java-21%20bytecode-orange?logo=openjdk)](https://github.com/wickidcow/ODailyQuests)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue)](LICENSE)

### 📥 [Download the latest ODailyQuests release](https://github.com/wickidcow/ODailyQuests/releases/latest)

> ❤️ **Project lineage:** Originally created by **Ordwen and contributors**. Current maintenance, modernization, compatibility work, integrations, and expansion are led by **wickidcow**. The project continues under the **GPL-3.0** license.

---

## ✨ 3.0.5 at a glance

- **7 maintained daily categories**
- dependency-aware **Tech** and **Wild Card** rotations
- Slimefun crafting and addon-aware objectives
- exact Pylon/Rebar item objectives
- ValhallaMMO, mcMMO, PyroFishingPro and EvenMoreFish progression
- MMOItems and ItemsAdder item recognition
- standalone **Fable Good** and **Fable Evil** paths
- a refreshed daily quest GUI with a **one-use-per-day reroll menu**
- optional **Jobs / JobsReborn** quest shortcut
- weighted quests, permission pools, scaling, chains, weekly categories, streaks and community goals
- `%progressPercent%`, `%totalQuests%`, and backward-compatible `%progression%`
- `/dqadmin doctor` integration diagnostics
- safe migration for older five-slot menus and existing administrator-owned YAML
- production-JAR packaging checks and a **raw versioned JAR** release asset

---

# 🗺️ Seven daily categories

A maintained default installation draws **one quest from each available category**:

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

Tech and Wild Card are dependency-aware. If a provider is unavailable, its maintained defaults are filtered instead of becoming impossible quests.

---

# 🖥️ Daily quest menu refresh

The 3.0.5 interface gives the quest board a cleaner workflow:

- **Tech** uses slot **23** and **Fable Evil** uses slot **25** in the maintained layout.
- The old **“Informations”** label is corrected to **“Information.”**
- The **top-right Daily Reroll icon is a real button**.
- Clicking reroll opens a chooser for **Reroll One Quest** or **Reroll All Quests**.
- **Reroll All** is atomic: the replacement set is selected before the current set is changed.
- A successful reroll-all counts as **one reroll action**, not one action per quest.
- Normal players get **one reroll action per daily quest set**. The existing admin bypass permission can still bypass the limit.
- If **Jobs** or **JobsReborn** is installed, the bottom-right corner becomes a **Jobs Quests** button that runs `/jobs quests` **as the clicking player**.
- The Jobs button hands off directly to the Jobs GUI.
- If Jobs is absent, that bottom-right position stays normal filler.

The upgrade code only adjusts recognizable maintained menu values and avoids replacing unrelated custom buttons, colors, heads, or styling.

---

# ⚙️ Tech — one technology category

Tech deliberately has **no Easy / Medium / Hard sub-tier**. The objective itself determines the workload.

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

The current build registers dedicated integration quest types instead of treating Slimefun items like ordinary vanilla items.

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

Pylon support uses Rebar/Pylon item recognition through reflection, keeping both integrations optional at runtime.

The Tech pool includes exact-item defaults such as:

- 🧲 **Craft a Shimmer Magnet** — `pylon:shimmer_magnet`
- 🔨 **Craft a Diamond Hammer** — `pylon:diamond_hammer`
- 🛗 **Craft an Elevator I** — `pylon:elevator_1`
- 💀 **Craft a Reactivated Wither Skull** — `pylon:reactivated_wither_skull`

These use exact Pylon namespaced keys. A random Pylon item does **not** satisfy the wrong exact-item objective.

The four maintained definitions are physically bundled in:

```text
quests/pylon-defaults.yml
```

They are merged additively into a maintained Pylon/Rebar Tech pool without wiping a custom Tech file.

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

Relevant maintained packs can be enabled under `default_quest_packs`:

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

If an optional category is missing, run:

```text
/dqadmin doctor
```

The doctor report shows loaded categories, active packs, registered quest types, **Tech available**, **Wild Card available**, and detected providers including Slimefun, Pylon, Rebar and mcMMO.

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

CI tests the maintained registry and verifies critical classes/resources exist in the final production JAR.

---

# 🛡️ Upgrade without wiping your configuration

Existing server files are treated as administrator-owned.

3.0.5 intentionally avoids “fixing” upgrades by deleting the plugin folder:

- Easy/Medium/Hard files are not blindly overwritten.
- Good/Evil files remain editable server content.
- missing maintained category files are created automatically.
- genuinely empty old Tech/Wild Card stubs can be replaced with populated defaults.
- Tech/Wild Card files containing real quests are preserved.
- exact Pylon defaults are added only to a managed Pylon/Rebar Tech pool.
- custom menu styling is preserved where migration can prove a slot is safe.
- old `%achieved%/5` counters migrate to `%achieved%/%totalQuests%`.
- `%progression%` remains a legacy alias for `%progress%`.
- older Jobs shortcut entries are migrated so `/jobs quests` runs as a player command.

Older numbered five-slot menus are expanded using safe filler positions rather than replacing the whole interface.

After upgrading, a saved old quest set can be regenerated for testing with:

```text
/dqadmin reset quests <player>
```

---

# 🔄 Rerolls

The maintained default is **one reroll action per daily quest set**:

```yaml
reroll_maximum: 1
```

Players can open the reroll chooser from the top-right menu icon or use the reroll command. Choosing **Reroll All** still consumes only one daily reroll action.

Failed rerolls do not consume configured reroll costs. The administrator bypass permission remains available for testing and moderation.

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

ODailyQuests also includes:

- weighted quest selection
- permission-controlled quest pools
- per-quest requirement/reward multipliers
- quest chains
- weekly categories
- reroll costs and safe refunds
- completion streaks
- community/server-wide goals

---

# 🧵 Paper / Purpur / Folia modernization

Modern compatibility work includes:

- Paper/Purpur 1.21.11+ targeting
- entity-aware and Folia-aware scheduling
- concurrent active-player quest state
- Java 21 bytecode
- Java 25 CI builds
- optional integrations isolated behind reflection where appropriate

Folia compatibility is treated as a threading/scheduling concern, not just a metadata flag.

---

# 📦 Install / upgrade

1. Stop the server.
2. Replace the ODailyQuests JAR.
3. **Keep your existing `plugins/ODailyQuests/` folder.**
4. Start the server normally.
5. Use `/dqadmin doctor` if an optional category or integration is not appearing.

Release asset:

```text
ODailyQuests3.0.5-Spigot-1.21.11+.jar
```

The GitHub release ships the plugin as a **raw JAR** — no unnecessary extraction step.

### 📥 [Go to GitHub Releases](https://github.com/wickidcow/ODailyQuests/releases)

---

# 🧪 Build and packaging safeguards

Build locally with:

```bash
./gradlew clean test shadowJar
```

GitHub Actions verifies the final JAR contains critical runtime classes and resources, including placeholder-condition classes, maintained integration classes, reroll GUI/service classes, all seven main quest YAML resources, `quests/pylon-defaults.yml`, and no missing internal `com.ordwen.odailyquests...` references detectable by `jdeps`.

---

# 📚 Project history & original documentation

ODailyQuests began with the work of **Ordwen and the original contributors**. This project would not exist without that foundation, and the original documentation and project pages remain valuable references:

- [Original ODailyQuests documentation](https://ordwenplugins.gitbook.io/odailyquests/)
- [Original Ordwen/ODailyQuests repository](https://github.com/Ordwen/ODailyQuests)
- [Original Spigot resource](https://www.spigotmc.org/resources/odailyquests-daily-quests-plugin-1-16-1-19.100990/)
- [Original Modrinth project](https://modrinth.com/plugin/odailyquests)

The current ODailyQuests project builds on that foundation with modern server compatibility, the seven-category model, dependency-backed integrations, expanded quest systems, and continued development under **wickidcow**.

---

# ☕ Support the developers

If ODailyQuests has been useful to you, you can support the original creator or the current project maintainer directly.

### ❤️ Support the original developer — Ordwen

Ordwen created the original ODailyQuests project and deserves full credit for the foundation that made the current project possible.

- [Ko-fi — Ordwen](https://ko-fi.com/I2I41CRIJI)
- [PayPal — Ordwen](https://www.paypal.com/paypalme/ordwen)

### 🛠️ Support current ODailyQuests development — wickidcow

Support ongoing maintenance, Minecraft version compatibility, testing, integrations, quest expansion, and future development:

- [PayPal — wickidcow](https://www.paypal.com/ncp/payment/ZYS8QH2FVV25U)

Thank you for supporting open-source Minecraft development and the developers who built ODailyQuests across both generations of the project.

---

# ❤️ Credits & license

**Original creator:** Ordwen  
**Original contributors:** the ODailyQuests contributors who helped build and improve the original project  
**Current maintainer / developer:** wickidcow

ODailyQuests continues under the **GNU General Public License v3.0**. See [`LICENSE`](LICENSE).

This project is independent and is not affiliated with Mojang Studios or Microsoft.

**The goal is to honor what made ODailyQuests great, keep it alive on modern Minecraft versions, and continue expanding what server owners can build with it.**
