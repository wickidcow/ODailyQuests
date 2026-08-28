# ODailyQuests 3.0.6 — Category & Custom Item Compatibility Update

ODailyQuests 3.0.6 is a focused compatibility update for server owners customizing the daily quest pool and GUI.

## Category disabling

Static `quests_per_category` values now accept `0`.

```yaml
quests_per_category:
  easy: 1
  medium: 1
  hard: 0
  good: 0
  evil: 0
  tech: 1
  wildcard: 0
```

A category set to `0` is treated as disabled for that startup. It is not loaded into the active category registry, does not require a quest file, is not checked by `safety_mode`, and does not contribute quests to players.

Negative values remain invalid. PlaceholderAPI-backed category amounts are unchanged and may still resolve to `0` on a per-player basis.

## Nexo GUI item fix

The configurable `player_interface.player_head.material` entry no longer assumes that every external item uses `SkullMeta`.

This fixes configurations such as:

```yaml
player_interface:
  player_head:
    enabled: true
    material: "nexo:odailyquests_head"
```

Nexo items may now use their normal underlying material without causing the player interface to fail. Player ownership is only applied when the resolved item is actually a player head.

## ItemsAdder retained

ItemsAdder support remains fully enabled and uses the same external-item resolver as before. The player information item can also use an ItemsAdder item, for example:

```yaml
material: "itemsadder:namespace:item_id"
```

Oraxen and MMOItems external interface-item support is preserved as well.

## Additional safety fixes

- Player information items are cloned before per-player placeholder rendering instead of mutating one shared item instance.
- Multi-slot `player_head.slot` lists now use the same 1-based slot numbering as single-slot configuration.
- Player-information slot bounds now correctly reject `slot == inventory size + 1`.
- Existing 3.0.5 configuration values are preserved while the stored config version is advanced to 3.0.6.
- CI release publishing now reads the release notes for the actual release version rather than being hard-coded to 3.0.5.

## Runtime / build targets

- Minecraft: 1.21.11+
- Java bytecode: 21
- CI/build toolchain: Java 25
- Paper/Spigot-compatible build

Thanks to the server owners reporting these edge cases and helping improve the maintained fork.
