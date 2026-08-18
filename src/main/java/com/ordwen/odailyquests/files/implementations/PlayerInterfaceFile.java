package com.ordwen.odailyquests.files.implementations;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.files.APluginFile;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class PlayerInterfaceFile extends APluginFile {

    private static final List<String> NEW_CATEGORIES = List.of("good", "evil", "tech", "wildcard");
    private static final Set<String> SLOT_DONOR_CATEGORIES = Set.of("easy", "medium", "hard", "good", "evil", "tech", "wildcard");
    private static final Pattern LEGACY_DAILY_TOTAL = Pattern.compile(
            "(%achieved%[^/]*?/)((?:#[0-9A-Fa-f]{6}|&[0-9A-Fa-fK-Ok-oRr])*)\\d+\\s*$");

    public PlayerInterfaceFile(ODailyQuests plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        file = new File(plugin.getDataFolder(), "playerInterface.yml");

        if (!file.exists()) {
            plugin.saveResource("playerInterface.yml", false);
            PluginLogger.info("Player interface file created.");
        }

        config = new YamlConfiguration();

        try {
            config.load(file);
            migrateSevenCategorySlots();
            migrateLegacyDailyQuestTotal();
        } catch (Exception e) {
            PluginLogger.error("An error occurred while loading the player interface file.");
            PluginLogger.error(e.getMessage());
        }
        PluginLogger.fine("Player interface file successfully loaded.");
    }

    /**
     * Upgrades old player menus to the maintained seven-category layout without replacing
     * administrator styling. Both modern category-slot menus and older numbered quest-slot menus
     * are supported.
     */
    private void migrateSevenCategorySlots() throws IOException {
        final ConfigurationSection quests = config.getConfigurationSection("player_interface.quests");
        if (quests == null) return;

        final YamlConfiguration mainConfig = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "config.yml"));
        final ConfigurationSection categories = quests.getConfigurationSection("categories");

        if (categories == null) {
            migrateLegacyIndexedSlots(quests, mainConfig);
            return;
        }

        migrateCategorySlots(categories, mainConfig);
    }

    /**
     * Migrates the original numbered interface format, for example:
     * quests: { '1': 19, '2': 21, ... '5': 27 }.
     *
     * <p>The total number of required slots is derived from quests_per_category. New quest
     * positions are taken only from configured FILL locations, so custom buttons, heads, close
     * items and decorative category labels are never overwritten. Quest items are rendered after
     * the base inventory and naturally overlay the filler.</p>
     */
    private void migrateLegacyIndexedSlots(ConfigurationSection quests, YamlConfiguration mainConfig) throws IOException {
        final ConfigurationSection questCounts = mainConfig.getConfigurationSection("quests_per_category");
        if (questCounts == null) return;

        int requiredTotal = 0;
        for (String category : questCounts.getKeys(false)) {
            final Object raw = questCounts.get(category);
            if (!(raw instanceof Number number)) {
                PluginLogger.warn("Cannot automatically expand numbered player-interface quest slots: "
                        + "quests_per_category." + category + " is dynamic or invalid.");
                return;
            }
            requiredTotal += Math.max(0, number.intValue());
        }
        if (requiredTotal <= 0) return;

        final Map<Integer, Integer> existing = new LinkedHashMap<>();
        for (String key : quests.getKeys(false)) {
            if ("categories".equalsIgnoreCase(key)) continue;
            try {
                final int index = Integer.parseInt(key);
                if (index > 0) existing.put(index, quests.getInt(key));
            } catch (NumberFormatException ignored) {
                // Non-numeric custom keys are not part of the legacy numbered quest mapping.
            }
        }

        boolean missingAny = false;
        for (int index = 1; index <= requiredTotal; index++) {
            if (!existing.containsKey(index)) {
                missingAny = true;
                break;
            }
        }
        if (!missingAny) return;

        final int menuSize = config.getInt("player_interface.size", 54);
        final Set<Integer> reservedQuestSlots = new LinkedHashSet<>();
        existing.values().stream()
                .filter(slot -> slot > 0 && slot <= menuSize)
                .forEach(reservedQuestSlots::add);

        final List<Integer> fillSlots = getFillSlots(menuSize);
        if (fillSlots.isEmpty()) {
            PluginLogger.warn("Cannot automatically expand numbered player-interface quest slots: no FILL slots are available.");
            return;
        }

        // Prefer filler positions after the existing quest row/area. This keeps old custom menus
        // visually stable (for example a five-quest row at 19,21,23,25,27 grows into the next
        // available filler positions instead of taking decorative top-row panes).
        final int highestExisting = reservedQuestSlots.stream().mapToInt(Integer::intValue).max().orElse(0);
        fillSlots.sort(Comparator
                .comparingInt((Integer slot) -> slot > highestExisting ? 0 : 1)
                .thenComparingInt(slot -> slot > highestExisting ? slot : Math.abs(slot - highestExisting))
                .thenComparingInt(Integer::intValue));

        final LinkedHashSet<Integer> candidates = new LinkedHashSet<>();
        for (Integer slot : fillSlots) {
            if (!reservedQuestSlots.contains(slot)) candidates.add(slot);
        }

        final Map<Integer, Integer> assignments = new LinkedHashMap<>();
        final Set<Integer> allocated = new LinkedHashSet<>();
        for (int index = 1; index <= requiredTotal; index++) {
            if (existing.containsKey(index)) continue;

            Integer selected = null;
            for (Integer candidate : candidates) {
                if (allocated.add(candidate)) {
                    selected = candidate;
                    break;
                }
            }
            if (selected == null) {
                PluginLogger.warn("Cannot automatically expand numbered player-interface quest slots to "
                        + requiredTotal + ": not enough safe FILL positions are available. Existing layout was left unchanged.");
                return;
            }
            assignments.put(index, selected);
        }

        assignments.forEach((index, slot) -> quests.set(String.valueOf(index), slot));
        config.save(file);
        PluginLogger.info("Expanded legacy numbered playerInterface.yml from " + existing.size() + " to "
                + requiredTotal + " quest slots without replacing custom styling.");
    }

    private void migrateCategorySlots(ConfigurationSection categories, YamlConfiguration mainConfig) throws IOException {
        final List<String> missing = NEW_CATEGORIES.stream()
                .filter(category -> !categories.contains(category))
                .toList();
        if (missing.isEmpty()) return;

        final Map<String, Integer> requiredCounts = new LinkedHashMap<>();
        for (String category : categories.getKeys(false)) {
            final Object raw = mainConfig.get("quests_per_category." + category);
            if (raw instanceof Number number) {
                requiredCounts.put(category, Math.max(0, number.intValue()));
            } else {
                // Placeholder-driven/dynamic counts cannot be migrated safely because we do not
                // know how many of the administrator's configured slots are actually spare.
                requiredCounts.put(category, Integer.MAX_VALUE);
            }
        }
        for (String category : missing) {
            final Object raw = mainConfig.get("quests_per_category." + category);
            if (!(raw instanceof Number number)) {
                PluginLogger.warn("Cannot automatically add player-interface slots for " + category
                        + ": quests_per_category uses a dynamic or missing value.");
                return;
            }
            requiredCounts.put(category, Math.max(0, number.intValue()));
        }

        final int menuSize = config.getInt("player_interface.size", 54);
        final Set<Integer> reserved = new LinkedHashSet<>();
        final List<Integer> spareQuestSlots = new ArrayList<>();
        final Map<String, List<Integer>> originalSlots = new LinkedHashMap<>();

        for (String category : categories.getKeys(false)) {
            final List<Integer> slots = new ArrayList<>(categories.getIntegerList(category));
            originalSlots.put(category, slots);

            final int required = requiredCounts.getOrDefault(category, Integer.MAX_VALUE);
            if (!SLOT_DONOR_CATEGORIES.contains(category.toLowerCase())) {
                reserved.addAll(slots);
                continue;
            }

            final int keep = required == Integer.MAX_VALUE ? slots.size() : Math.min(required, slots.size());
            for (int i = 0; i < keep; i++) reserved.add(slots.get(i));
            for (int i = keep; i < slots.size(); i++) spareQuestSlots.add(slots.get(i));
        }

        final LinkedHashSet<Integer> candidates = new LinkedHashSet<>();
        spareQuestSlots.stream()
                .filter(slot -> slot > 0 && slot <= menuSize)
                .filter(slot -> !reserved.contains(slot))
                .sorted()
                .forEach(candidates::add);

        // Filler positions are a safe last resort and preserve custom colors/theme/configuration.
        getFillSlots(menuSize).stream()
                .filter(slot -> !reserved.contains(slot))
                .forEach(candidates::add);

        final Map<String, List<Integer>> assignments = new LinkedHashMap<>();
        final Set<Integer> allocated = new LinkedHashSet<>();
        for (String category : missing) {
            final int amount = requiredCounts.getOrDefault(category, 0);
            if (amount <= 0) continue;

            final List<Integer> slots = new ArrayList<>();
            for (Integer candidate : candidates) {
                if (allocated.contains(candidate)) continue;
                slots.add(candidate);
                allocated.add(candidate);
                if (slots.size() >= amount) break;
            }

            if (slots.size() < amount) {
                PluginLogger.warn("Cannot automatically add enough player-interface slots for " + category
                        + ". Your existing playerInterface.yml was left unchanged.");
                return;
            }
            assignments.put(category, slots);
        }

        if (assignments.isEmpty()) return;

        // Remove only slots that were genuinely spare from maintained donor categories. This
        // prevents duplicate slot ownership while keeping all currently-required legacy slots.
        for (Map.Entry<String, List<Integer>> entry : originalSlots.entrySet()) {
            final String category = entry.getKey();
            if (!SLOT_DONOR_CATEGORIES.contains(category.toLowerCase())) continue;
            final int required = requiredCounts.getOrDefault(category, Integer.MAX_VALUE);
            if (required == Integer.MAX_VALUE || entry.getValue().size() <= required) continue;

            final List<Integer> trimmed = new ArrayList<>();
            for (int i = 0; i < entry.getValue().size(); i++) {
                final int slot = entry.getValue().get(i);
                if (i < required || !allocated.contains(slot)) trimmed.add(slot);
            }
            categories.set(category, trimmed);
        }

        assignments.forEach(categories::set);
        config.save(file);
        PluginLogger.info("Upgraded playerInterface.yml with slots for the maintained seven-category layout without replacing custom styling.");
    }

    private List<Integer> getFillSlots(int menuSize) {
        final List<Integer> fillSlots = new ArrayList<>();
        final ConfigurationSection items = config.getConfigurationSection("player_interface.items");
        if (items == null) return fillSlots;

        for (String key : items.getKeys(false)) {
            final ConfigurationSection item = items.getConfigurationSection(key);
            if (item == null || !"FILL".equalsIgnoreCase(item.getString("type", ""))) continue;
            final ConfigurationSection definition = item.getConfigurationSection("item");
            if (definition == null) continue;
            if (definition.isList("slot")) fillSlots.addAll(definition.getIntegerList("slot"));
            else if (definition.contains("slot")) fillSlots.add(definition.getInt("slot"));
        }

        return fillSlots.stream()
                .filter(slot -> slot > 0 && slot <= menuSize)
                .distinct()
                .sorted()
                .toList();
    }

    /** Replaces the old hard-coded x/5 player-head counter with the maintained dynamic total. */
    private void migrateLegacyDailyQuestTotal() throws IOException {
        final String path = "player_interface.player_head.item_description";
        final List<String> description = config.getStringList(path);
        if (description.isEmpty()) return;

        boolean changed = false;
        final List<String> updated = new ArrayList<>(description.size());
        for (String line : description) {
            if (line != null && line.contains("%achieved%") && !line.contains("%totalQuests%")) {
                final String replacement = LEGACY_DAILY_TOTAL.matcher(line).replaceFirst("$1$2%totalQuests%");
                if (!replacement.equals(line)) changed = true;
                updated.add(replacement);
            } else {
                updated.add(line);
            }
        }

        if (changed) {
            config.set(path, updated);
            config.save(file);
            PluginLogger.info("Updated the legacy hard-coded daily quest counter in playerInterface.yml to %totalQuests%.");
        }
    }
}
