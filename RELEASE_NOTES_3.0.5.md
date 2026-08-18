# 🚀 ODailyQuests 3.0.5 — Wider Compatibility Expansion

**Daily quests just got a much bigger world to play in.**

ODailyQuests 3.0.5 carries the original Ordwen project forward for modern Paper/Purpur/Folia servers and opens the daily board to technology, RPG progression, fishing systems and custom items — while keeping optional plugins optional.

> ❤️ Built on the original work of **Ordwen and the ODailyQuests contributors**. This maintained fork preserves the original project's attribution and GPL-3.0 license.

---

## 🎯 Seven Daily Categories

A maintained setup can draw one quest from each available category:

- 🟢 **Easy**
- 🟠 **Medium**
- 🔴 **Hard**
- 🌿 **Fable Good**
- 🔥 **Fable Evil**
- ⚙️ **Tech**
- 🎲 **Wild Card**

Tech and Wild Card are dependency-aware: unavailable provider defaults are filtered instead of becoming impossible quests.

---

## ⚙️ Tech Expansion

Tech is one unified technology category — **no separate Easy/Medium/Hard Tech tiers**.

### 🧪 Slimefun

3.0.5 adds real maintained quest types for Slimefun crafting and item ownership. The Tech pool includes objectives such as:

- craft a **Common Talisman**
- produce **Steel Ingots**
- produce **Reinforced Alloy Ingots**
- create or obtain items from supported Slimefun addons

Addon coverage includes Networks, Networks Expansion, Infinity Expansion, Fluffy Machines, Foxy Machines, Magic Expansion, Military Arsenal, Slimefun Warfare, Mob Drops, Lucky Blocks, Alchimia Vitae, Dank Tech, Supreme, Gastronomicon, Exotic Garden, Potion Expansion, Flower Power, Fast Machines, Infernal Farm, IDOE and SlimeGlue.

### 🔩 Pylon / Rebar — now with exact-item quests

The generic Pylon/Rebar Tech objectives remain, and 3.0.5 now bundles four exact Pylon challenges using verified Pylon namespaced keys:

- 🧲 **Craft a Shimmer Magnet** — `pylon:shimmer_magnet`
- 🔨 **Craft a Diamond Hammer** — `pylon:diamond_hammer`
- 🛗 **Craft an Elevator I** — `pylon:elevator_1`
- 💀 **Craft a Reactivated Wither Skull** — `pylon:reactivated_wither_skull`

These are not display-name guesses. The quest matcher reads the Rebar/Pylon item key and requires the exact configured key, so another Pylon item does not complete an exact-item objective.

The four definitions are physically bundled in `quests/pylon-defaults.yml` and are added to an existing **managed** Pylon/Rebar Tech pool without wiping custom Tech files.

---

## 🎲 Wild Card

Wild Card lets installed plugins join the daily rotation:

- **ValhallaMMO** — skill progression with spendable skill-point rewards
- **mcMMO** — skill XP objectives and mcMMO XP rewards
- **PyroFishingPro** — fish catches with Entropy/Fishing XP rewards
- **EvenMoreFish** — EMF catches with bait rewards
- **MMOItems** — custom-item acquisition
- **ItemsAdder** — custom-item acquisition

MMOItems and ItemsAdder reward IDs are intentionally not invented because those IDs belong to each server's content packs.

MythicMobs remains available for manual quests but is not automatically injected into Wild Card.

---

## 🧠 Integration quest types are now guaranteed at startup

The maintained runtime registers its integration quest types **before quest YAML is loaded**:

```text
SLIMEFUN_ITEM
SLIMEFUN_CRAFT
REBAR_ITEM
MCMMO_EXP
MMOITEM_ITEM
ITEMSADDER_ITEM
EMF_FISH
```

This closes an important failure mode where Tech/Wild Card YAML could exist and be enabled, but its quest types were not in the registry and therefore never became usable daily quests.

---

## 🌿 Fable Good / 🔥 Fable Evil

Fable is represented by two proper standalone categories:

- **Fable Good**
- **Fable Evil**

The bundled pools contain 16 Good and 16 Evil quests with their alignment progression and rewards preserved.

---

## 🔄 Upgrade without deleting your config

3.0.5 is designed to migrate existing servers rather than force a clean folder.

Upgrade compatibility includes:

- older five-position numeric quest menus are expanded for quests 6 and 7 using safe filler positions
- newer category-based menus are also supported
- custom buttons, heads, colors and filler styling are preserved where safe
- old `%achieved%/5` counters migrate to `%achieved%/%totalQuests%`
- `%progression%` remains a legacy alias for `%progress%`
- empty old Tech/Wild Card managed stubs can be replaced with populated defaults
- real administrator-owned quest YAML is not blindly overwritten
- exact Pylon defaults are added only when the Tech file already contains the maintained `pylon-rebar` pack

If a player still has a saved five-quest set after upgrading, wait for the next draw or regenerate it with:

```text
/dqadmin reset quests <player>
```

---

## 🩺 Better diagnostics

`/dqadmin doctor` now surfaces the information needed to troubleshoot optional categories, including:

- loaded category names
- active/default pack state
- **Tech available: true/false**
- **Wild Card available: true/false**
- detected Slimefun, Pylon, Rebar, mcMMO and other providers

---

## 🧠 More progression features

3.0.5 also includes:

- ⚖️ weighted quest selection
- 🔐 permission-controlled pools
- 📈 per-quest requirement/reward scaling
- 🔗 quest chains
- 📅 weekly categories
- 🎲 reroll costs and failed-reroll refunds
- 🔥 completion streak rewards
- 🌍 community/server-wide goals
- 📊 `%progressPercent%`

---

## 🧵 Modern server support

- Paper / Purpur **1.21.11+**
- Folia-aware scheduling
- Java **21 bytecode**
- Java **25 CI toolchain**
- concurrent active-player quest state
- reflection-isolated optional integrations where appropriate

---

## 🧪 Production-JAR safeguards

GitHub Actions runs tests and the production Shadow JAR build before staging the raw artifact.

CI verifies critical runtime classes and resources are physically in the JAR, including:

- placeholder-condition classes used by quest selection/reset
- maintained integration quest classes
- all seven category YAML resources
- `quests/pylon-defaults.yml`
- no missing internal `com.ordwen.odailyquests...` references detectable by `jdeps`

This specifically protects against the old class-packaging failure that could break `/dqadmin reset quests` at runtime.

---

## 📦 Raw JAR

The release plugin asset is:

`ODailyQuests3.0.5-Spigot-1.21.11+.jar`

No unnecessary extraction step for the plugin file.

---

## ❤️ Credits

**Original project and concept:** Ordwen / ODailyQuests and contributors.

This maintained fork exists to extend compatibility while respecting the original project, license and configurable design.

Independent project; not affiliated with Mojang Studios or Microsoft.

**Have fun with the new rotation — vanilla survival, Fable paths, Slimefun technology, Pylon machinery, RPG skills and fishing systems can finally share the same daily quest board.** 🎉
