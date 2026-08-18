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

public class PlayerInterfaceFile extends APluginFile {

    private static final List<String> NEW_CATEGORIES = List.of("good", "evil", "tech", "wildcard");
    private static final Set<String> SLOT_DONOR_CATEGORIES = Set.of("easy", "medium", "hard", "good", "evil", "tech", "wildcard");

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
        } catch (Exception e) {
            PluginLogger.error("An error occurred while loading the player interface file.");
            PluginLogger.error(e.getMessage());
        }
        PluginLogger.fine("Player interface file successfully loaded.");
    }

    /**
     * Upgrades an older category-based menu without replacing the administrator's theme/items.
     *
     * <p>Older interfaces commonly reserved several slots each for Easy/Medium/Hard. With the
     * maintained seven-category layout set to one quest per category, those unused quest slots
     * can be reassigned to the four new categories. If there are not enough old quest slots,
     * configured FILL slots are safe fallback positions because a quest item overlays the filler
     * only while that category is actually assigned.</p>
     */
    private void migrateSevenCategorySlots() throws IOException {
        final ConfigurationSection categories = config.getConfigurationSection("player_interface.quests.categories");
        if (categories == null) return;

        final List<String> missing = NEW_CATEGORIES.stream()
                .filter(category -> !categories.contains(category))
                .toList();
        if (missing.isEmpty()) return;

        final YamlConfiguration mainConfig = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "config.yml"));

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
        final ConfigurationSection items = config.getConfigurationSection("player_interface.items");
        if (items != null) {
            final List<Integer> fillSlots = new ArrayList<>();
            for (String key : items.getKeys(false)) {
                final ConfigurationSection item = items.getConfigurationSection(key);
                if (item == null || !"FILL".equalsIgnoreCase(item.getString("type", ""))) continue;
                final ConfigurationSection definition = item.getConfigurationSection("item");
                if (definition == null) continue;
                if (definition.isList("slot")) fillSlots.addAll(definition.getIntegerList("slot"));
                else if (definition.contains("slot")) fillSlots.add(definition.getInt("slot"));
            }
            fillSlots.stream()
                    .filter(slot -> slot > 0 && slot <= menuSize)
                    .filter(slot -> !reserved.contains(slot))
                    .sorted(Comparator.naturalOrder())
                    .forEach(candidates::add);
        }

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
}
