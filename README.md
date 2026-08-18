# 🚀 ODailyQuests — Wider Compatibility Expansion

A modern maintained fork of **O'DailyQuests by Ordwen**, built for current Minecraft servers that want daily quests to reach far beyond vanilla gameplay.

**ODailyQuests 3.0.5** keeps the familiar daily-quest experience while expanding it into Slimefun, Pylon/Rebar, ValhallaMMO, mcMMO, fishing plugins, custom-item ecosystems, Fable Good/Evil progression, modern Paper/Purpur/Folia scheduling, and a much safer upgrade path for existing servers.

> ❤️ **Original project:** ODailyQuests by Ordwen and its contributors. This fork exists because the original project deserves to keep living on modern servers. Please retain attribution when redistributing or extending it.

---

## ✨ What makes this build different?

Instead of treating every server like a vanilla-only installation, the Wider Compatibility Expansion lets installed plugins **join the daily quest rotation automatically**.

A normal day can now include:

- 🌱 **Easy** — approachable vanilla objectives
- ⚔️ **Medium** — more involved survival goals
- 🔥 **Hard** — demanding challenges
- 🌟 **Fable Good** — Good-aligned daily progression
- 🩸 **Fable Evil** — Evil-aligned daily progression
- ⚙️ **Tech** — Slimefun, Slimefun addons, Pylon and Rebar
- 🎲 **Wild Card** — optional plugin integrations such as ValhallaMMO, mcMMO and fishing/custom-item plugins

The maintained default is **one quest from each available category per day**.

If an optional provider is not installed, its quests are simply removed from eligibility. Players are not handed impossible objectives just because a YAML file contains them.

---

## 📦 Downloads

GitHub Actions and releases publish a **raw versioned JAR**:

```text
ODailyQuests3.0.5-Spigot-1.21.11+.jar
```

No extra artifact ZIP is required for the maintained build output.

### Runtime targets

- Minecraft **1.21.11+**
- Paper — primary target
- Purpur — supported
- Folia — scheduling-aware support
- Java **21+ runtime bytecode**
- CI builds with a Java 25 toolchain

---

# 🗺️ The Seven Daily Categories

## 🌱 Easy

A reliable pool of everyday survival objectives. The goal is quick participation without turning a daily quest into a grind.

File:

```text
plugins/ODailyQuests/quests/easy.yml
```

## ⚔️ Medium

Longer or more involved objectives for players who want something beyond the basic daily routine.

```text
plugins/ODailyQuests/quests/medium.yml
```

## 🔥 Hard

The high-effort daily pool. Hard quests are intended to take real preparation or playtime and can carry stronger rewards.

```text
plugins/ODailyQuests/quests/hard.yml
```

## 🌟 Fable Good

A dedicated Good-aligned category with its own quest pool and scoreboard/reward progression.

```text
plugins/ODailyQuests/quests/good.yml
```

## 🩸 Fable Evil

A separate Evil-aligned daily category, allowing players to progress down a competing path instead of mixing the two into generic difficulty tiers.

```text
plugins/ODailyQuests/quests/evil.yml
```

## ⚙️ Tech

Tech is one shared technology category. It is **not** split into Easy/Medium/Hard.

The actual rolled objective determines how difficult the Tech quest is.

```text
plugins/ODailyQuests/quests/tech.yml
```

Built-in Tech providers include:

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

### Slimefun progression

The maintained build adds dedicated quest types for Slimefun instead of pretending Slimefun items are ordinary vanilla items.

Examples include:

```yaml
quest_type: SLIMEFUN_CRAFT
slimefun_ids:
  - REINFORCED_ALLOY_INGOT
required_amount: 2
```

and addon-family objectives:

```yaml
quest_type: SLIMEFUN_ITEM
slimefun_addons:
  - Networks
required_amount: 5
```

`SLIMEFUN_CRAFT` supports normal Bukkit crafting and Slimefun multiblock crafting paths. `SLIMEFUN_ITEM` can recognize the addon that registered an item, allowing many addons to participate without making them hard dependencies of ODailyQuests.

Pylon/Rebar support is optional and uses Rebar item recognition without requiring ODailyQuests to compile directly against the framework.

## 🎲 Wild Card

Wild Card is where the rest of the server gets to join the party.

```text
plugins/ODailyQuests/quests/wildcard.yml
```

The bundled pool currently supports:

| Provider | Example objective | Provider-aware reward |
|---|---|---|
| **ValhallaMMO** | Earn skill experience | Spendable Valhalla skill points |
| **mcMMO** | Earn skill XP | mcMMO XP |
| **PyroFishingPro** | Catch Pyro fish | Entropy / Fishing XP |
| **EvenMoreFish** | Catch EMF fish | EvenMoreFish bait |
| **MMOItems** | Obtain MMOItems content | Safe generic rewards by default |
| **ItemsAdder** | Obtain custom IA items | Safe generic rewards by default |

MMOItems and ItemsAdder item IDs are intentionally not invented in the stock pool because those IDs are server-specific. Server owners can replace the generic reward with their own custom item rewards once they know their actual IDs.

**MythicMobs is not injected automatically.** Manual MythicMobs quest support can still be configured when wanted.

---

# 🔌 Enabling Tech & Wild Card

For the maintained defaults, the two optional categories should be present in `config.yml`:

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

Then keep the relevant dependency pack enabled:

```yaml
default_quest_packs:
  slimefun-core:
    enabled: true

  valhallammo:
    enabled: true

  mcmmo:
    enabled: true

  itemsadder:
    enabled: true
```

You do **not** need every supported plugin. A single detected Tech provider can populate Tech, and a single detected Wild Card provider can populate Wild Card.

### Why might Tech or Wild Card not appear?

Check these in order:

1. `tech: 1` / `wildcard: 1` exists under `quests_per_category`.
2. At least one corresponding provider is installed and enabled.
3. Its `default_quest_packs.<provider>.enabled` setting is `true`.
4. The matching `tech.yml` or `wildcard.yml` contains quests.
5. Your player interface has enough quest slots.

### Upgrading an older five-slot interface

Older servers often have a custom `playerInterface.yml` that looks like this:

```yaml
quests:
  '1': 19
  '2': 21
  '3': 23
  '4': 25
  '5': 27
```

That layout can only display five assigned quests even when Tech and Wild Card are successfully loaded.

**3.0.5 now migrates this format automatically.** Missing numbered positions are assigned to safe configured `FILL` slots while preserving your existing custom buttons, heads, decorative items, colors and layout.

The newer category-based layout is also supported and migrated safely.

Do **not** delete your entire ODailyQuests configuration folder just to upgrade.

---

# 🧠 Smart dependency packs

Every maintained integration quest can carry a `default_pack` marker, for example:

```yaml
default_pack: valhallammo
```

or:

```yaml
default_pack: slimefun-core
```

At load time ODailyQuests checks whether that provider is actually usable.

That means the physical YAML remains easy to read and edit while dependency-specific quests can still disappear from the eligible pool when their plugin is not installed.

This is especially useful for distributing one complete `wildcard.yml` or `tech.yml` across different servers.

---

# 🛡️ Existing configs are treated as administrator-owned

The maintained fork avoids solving upgrades by erasing server customization.

Upgrade behavior is intentionally conservative:

- Existing Easy/Medium/Hard quest files are not blindly overwritten.
- Existing Good/Evil quest files remain yours.
- Missing maintained quest files are created automatically.
- An old empty managed Tech/Wild Card placeholder can be replaced with the populated built-in pool.
- A Tech/Wild Card file containing real quests is left alone.
- Older menu layouts are expanded instead of replaced.
- Custom colors, buttons, heads and filler items are preserved whenever migration can be completed safely.

If automatic migration cannot prove that a slot is safe to reuse, it logs a warning instead of destructively rewriting the interface.

---

# 🎁 Rewards that belong to the plugin

The wider integration system is designed so plugin-specific objectives can feel like part of that plugin rather than an unrelated economy task.

Examples from the maintained Wild Card defaults include:

```text
ValhallaMMO quest → Valhalla skill point reward
mcMMO quest       → mcMMO XP reward
PyroFishingPro    → Pyro Entropy / Fishing XP
EvenMoreFish      → EvenMoreFish bait
```

Regular money, experience and quest-point rewards can still be combined with provider-native rewards.

Everything remains editable in YAML.

---

# 🧩 Extended quest features

The fork also adds several progression tools for more advanced servers.

## Weighted selection

```yaml
weight: 2.5
```

Higher weight means the quest is more likely to be selected relative to other eligible quests in the same pool.

## Permission-controlled pools

```yaml
pool: veteran
```

Useful for donor, rank, class or progression-specific quest pools.

## Difficulty/reward multipliers

```yaml
difficulty:
  required_multiplier: 1.5
  reward_multiplier: 2.0
```

## Quest chains

```yaml
quests:
  20:
    name: '&eChain Part 1'
    quest_type: BREAK
    required: IRON_ORE
    required_amount: 16

  21:
    name: '&6Chain Part 2'
    quest_type: BREAK
    required: GOLD_ORE
    required_amount: 32
    chain_after: '20'
```

Chain-only quests are kept out of the normal random draw until their predecessor is completed.

## Weekly categories

Servers can maintain longer-period quest pools alongside daily categories.

## Reroll economy

Rerolls can use configurable costs, with refund protection if a replacement quest cannot actually be produced.

## Completion streaks

Reward players for repeatedly finishing their daily set.

## Community goals

Server-wide progress can be tracked toward shared weekly or custom goals.

---

# 🖥️ Player interface placeholders

Maintained placeholders include:

```text
%progress%
%progression%      (legacy alias)
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

The older `%progression%` token is kept as a compatibility alias so existing interfaces do not suddenly print the placeholder literally after upgrading.

`%totalQuests%` is useful for interfaces where optional categories can change the size of the player's daily set.

---

# 🩺 Diagnostics

Run:

```text
/dqadmin doctor
```

The diagnostic report helps inspect:

- server and Java runtime
- storage mode
- loaded quest categories
- registered quest types
- active quest data
- reset timing
- integration pack status
- optional feature status
- detected dependencies

When an optional category is missing, this is the first command to run before deleting configuration files.

---

# 🧵 Paper, Purpur and Folia modernization

Modern compatibility is more than changing `api-version`.

This fork includes work around:

- entity-aware scheduling for player mutations
- safer timed tasks
- concurrent active-player quest storage
- modern Java compilation
- current Paper APIs
- optional integrations isolated so absent plugins do not break startup

Folia support remains an ongoing compatibility target as Minecraft and plugin APIs evolve.

---

# 🏗️ Building

The repository uses Gradle.

```bash
./gradlew clean test shadowJar
```

CI:

1. checks out the source
2. sets up Java 25
3. runs tests
4. builds the shadow JAR
5. stages the versioned raw JAR
6. uploads it directly as the workflow artifact

Official releases are intentionally separated from ordinary feature-branch builds.

---

# 📚 Original documentation

For standard ODailyQuests concepts and configuration, the original project documentation remains an excellent reference:

- https://ordwenplugins.gitbook.io/odailyquests/
- https://github.com/Ordwen/ODailyQuests

The maintained fork adds features beyond the original documentation, especially the seven-category system and dependency-backed integrations described here.

---

# ❤️ Credits, license & project status

**Original ODailyQuests:** Ordwen and contributors  
**Maintained fork:** community modernization and compatibility work for current servers

This repository remains licensed under the **GNU General Public License v3.0**. See [`LICENSE`](LICENSE) for the complete license text and redistribution requirements.

This fork is independent and is **not affiliated with Mojang Studios or Microsoft**.

ODailyQuests is maintained with one goal in mind: **keep the original daily-quest experience alive while giving modern servers a much bigger world to build quests around.**
