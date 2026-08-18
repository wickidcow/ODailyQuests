# 🚀 ODailyQuests 3.0.5 — Wider Compatibility Expansion

**Daily quests just got a much bigger world to play in.**

ODailyQuests 3.0.5 takes the familiar daily-quest loop and opens it up for modern RPG, technology, fishing, custom-item, and progression-heavy servers. This release brings **seven daily quest categories**, real pre-generated integration quest pools, true Slimefun crafting support, plugin-aware rewards, safer dependency handling, and a large set of progression features — all while keeping optional plugins optional.

> ❤️ This maintained fork builds on the original work of **Ordwen and the ODailyQuests contributors**. The goal is to preserve the spirit and configurability of ODailyQuests while carrying it forward for modern servers.

---

## 🎯 Seven Daily Quest Categories

A fresh maintained setup can now draw one daily quest from each loaded category:

- 🟢 **Easy** — approachable everyday objectives
- 🟠 **Medium** — a stronger daily challenge
- 🔴 **Hard** — the serious stuff
- 🌿 **Fable Good** — Good-aligned deeds and progression
- 🔥 **Fable Evil** — Evil-aligned deeds and progression
- ⚙️ **Tech** — Slimefun, Slimefun addons, Pylon and Rebar
- 🎲 **Wild Card** — installed RPG, fishing and custom-item plugins join the rotation

These are real category YAML files inside the plugin — not hidden placeholder categories.

---

## ⚙️ Tech Expansion — Slimefun + Pylon/Rebar

The new **Tech** category is one unified technology pool. It deliberately has **no Easy / Medium / Hard label**: one day may ask for a straightforward component, while another can send players after advanced alloys or addon technology.

### 🧪 Slimefun Core

ODailyQuests can now track real Slimefun crafting, including Bukkit crafting and Slimefun multiblock crafting paths.

The bundled Tech pool includes objectives such as:

- crafting a **Common Talisman**
- producing **Steel Ingots**
- producing **Reinforced Alloy Ingots**

### 🧰 Slimefun Addons

The physical `tech.yml` ships with addon-aware objectives for a broad collection of Slimefun ecosystems, including:

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

Addon quests can identify the addon that registered a Slimefun item, allowing the same Tech category to grow with the server's installed addon stack.

### 🔩 Pylon / Rebar

Pylon and Rebar technology can join that same Tech rotation through optional item detection. ODailyQuests does not hard-require either framework just to start.

**`tech.yml` now ships with 27 real, editable built-in quests.**

If every Tech provider is absent or disabled and there are no custom Tech quests, the category is simply skipped for that startup instead of becoming an impossible daily or failing safety mode.

---

## 🎲 Wild Card — Your Plugin Stack Becomes Gameplay

Wild Card is designed to make the day's final quest feel unpredictable without handing players objectives from plugins they do not have.

**`wildcard.yml` ships with 18 real built-in quests**, and every integration quest carries its provider tag directly in YAML. Only quests whose dependency is actually enabled survive loading.

### ⚔️ ValhallaMMO

Wild Card can target specific Valhalla skills such as **Power, Mining, and Archery**.

And the rewards speak Valhalla's language too:

- easier Valhalla objective → **+1 spendable Valhalla Skill Point**
- stronger objective → **+2 Skill Points**
- advanced objective → **+3 Skill Points**

### 🪓 mcMMO

Built-in mcMMO quests can track specific skills such as:

- Mining
- Woodcutting
- Swords

Rewards include **mcMMO skill XP** for the matching progression system.

### 🎣 PyroFishingPro

Catch PyroFishingPro fish and earn provider-native progression rewards such as:

- **Pyro Entropy**
- **Pyro Fishing XP**

Higher-effort catches pay more of both.

### 🐟 EvenMoreFish

EvenMoreFish catch objectives are built in and can reward **EvenMoreFish bait** in addition to the normal daily rewards.

### 🗡️ MMOItems + ✨ ItemsAdder

Wild Card can recognize MMOItems and ItemsAdder items without making either plugin mandatory.

Because MMOItems IDs and ItemsAdder namespaced IDs come from each server's own content packs, the generic release **does not invent fake item IDs** as rewards. The built-in objectives use safe general rewards until the server owner chooses real local item IDs.

### 👾 MythicMobs

MythicMobs remains available for manually configured quests, but it is **intentionally not injected automatically** into the Wild Card pool.

---

## 🌿 Fable Good / 🔥 Fable Evil

Fable now has two proper standalone daily paths:

- **Fable Good**
- **Fable Evil**

The bundled pools contain **16 Good quests and 16 Evil quests**, with their alignment progression and rewards preserved.

The maintained Fable system uses only **Good / Evil** terminology.

---

## 🧠 More Than Just New Integrations

3.0.5 also expands the quest engine itself:

- ⚖️ weighted quest selection
- 🔐 permission-controlled quest pools
- 📈 per-quest requirement and reward scaling
- 🔗 quest chains
- 📅 weekly categories that can survive daily resets
- 🎲 reroll costs with failed-reroll refunds
- 🔥 completion streak rewards
- 🌍 server-wide/community goals
- 📊 `%progressPercent%` support
- 🩺 `/dqadmin doctor` diagnostics

---

## 🛡️ Dependency-Safe by Design

Optional integrations stay optional.

Built-in integration quests carry a `default_pack` directly in their physical YAML. If the corresponding plugin or Slimefun addon is missing or disabled, those built-ins are filtered out before the daily pool is used.

Existing administrator-owned quest files are not blindly overwritten. Missing maintained categories can be created automatically, and the short-lived empty Tech/Wild Card development stubs are migrated only when they are still recognizable empty managed files.

---

## 🔄 Upgrade Without Wiping Your Configs

3.0.5 is designed to **upgrade existing servers instead of making them start over**.

Older ODailyQuests installations frequently have a five-position player menu that only knows about Easy, Medium, Hard, Good and Evil. Even with Tech/Wild Card enabled in `config.yml`, that old menu has nowhere to draw quest 6 or quest 7.

The maintained migration now recognizes both interface formats:

- newer category-based quest slots
- older numbered quest slots such as `'1': 19` through `'5': 27`

For numbered menus, missing quest positions are assigned only to configured **FILL** slots, so custom heads, buttons, close items, category decorations, colors and other themed content are preserved.

Other compatibility upgrades include:

- `%progression%` remains supported as a legacy alias for `%progress%`
- old `%achieved%/5` player-head counters migrate to `%achieved%/%totalQuests%`
- pre-3.0.5 configs receive the 3.0.5 config migration marker
- existing custom quest YAML is not blindly replaced

If a player already has an old five-quest daily set saved from before the upgrade, either wait for the next scheduled draw or regenerate the player's active set with:

```text
/dqadmin reset quests <player>
```

**You should not need to delete the entire `plugins/ODailyQuests/` folder to upgrade.**

---

## 🧵 Modern Server Support

This maintained release is focused on the modern server ecosystem:

- **Paper / Purpur 1.21.11+**
- **Folia-aware scheduling**
- Java **21 bytecode**
- Java **25 build toolchain**
- concurrent active-player quest state where modern threading requires it

Folia support is treated as a scheduling/threading concern, not just a metadata flag.

---

## 🧪 Built and Checked Before Packaging

GitHub Actions runs the test suite and production Shadow JAR build before staging the artifact.

A dedicated regression test verifies that:

- all seven maintained category resources are bundled
- `tech.yml` never goes back to an empty `quests: {}` shell
- `wildcard.yml` never goes back to an empty shell
- Tech contains its Slimefun/Pylon provider tags
- Wild Card contains its integration provider tags
- provider-native reward commands remain present

The server smoke-test checklist additionally covers legacy five-slot menu migration, dynamic total-quest counters and immediate quest redraw testing.

---

## 📦 Download

The release asset is the **raw plugin JAR**:

`ODailyQuests3.0.5-Spigot-1.21.11+.jar`

No extra source archive or unnecessary ZIP extraction step is required for the plugin file — grab the JAR and place it in your server's `plugins` folder.

---

## ❤️ Credits

**Original project and concept:** Ordwen / ODailyQuests and its contributors.

This maintained fork exists to extend compatibility and keep the project useful on modern Minecraft server software while respecting the original work and license.

This project is independent and is not affiliated with Mojang Studios or Microsoft.

**Have fun with the new daily rotation — your technology stack, RPG progression, fishing systems, Fable paths, and custom content can finally share the same quest board.** 🎉
