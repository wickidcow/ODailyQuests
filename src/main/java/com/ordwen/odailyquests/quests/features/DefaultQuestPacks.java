package com.ordwen.odailyquests.quests.features;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.events.listeners.integrations.slimefun.SlimefunIntegration;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Built-in, toggleable quest packs.
 *
 * Easy/Medium/Hard and Fable Good/Evil are real quest categories backed by YAML files.
 * Dependency-backed content is merged into two separate daily categories:
 * Tech (Slimefun + addons + Pylon/Rebar) and Wild Card (miscellaneous integrations).
 */
public final class DefaultQuestPacks {

    private static final String ROOT = "default_quest_packs";
    public static final String TECH_CATEGORY = "tech";
    public static final String WILDCARD_CATEGORY = "wildcard";

    private record PackDefinition(
            String displayName,
            String generator,
            String category,
            List<String> pluginAny,
            List<String> slimefunAddons
    ) {}

    private static final Map<String, PackDefinition> DEFAULTS = new LinkedHashMap<>();

    static {
        add("vanilla", "Vanilla Daily Quests", "none", "", List.of(), List.of());
        add("fable-good", "Fable Good", "none", "good", List.of(), List.of());
        add("fable-evil", "Fable Evil", "none", "evil", List.of(), List.of());

        // One Tech category. Difficulty is intentionally a property of the rolled objective,
        // not the category name.
        add("slimefun-core", "Slimefun Core", "slimefun_core", TECH_CATEGORY, List.of("Slimefun"), List.of());
        addSlimefunAddon("networks", "Networks", "Networks");
        addSlimefunAddon("networks-expansion", "Networks Expansion", "NetworksExpansion", "Networks Expansion", "NetworkExpansion");
        addSlimefunAddon("infinity-expansion", "Infinity Expansion", "InfinityExpansion", "InfinityExpansion2", "Infinity Expansion", "Infinity Expansion 2");
        addSlimefunAddon("fluffy-machines", "Fluffy Machines", "FluffyMachines", "Fluffy Machines");
        addSlimefunAddon("foxy-machines", "Foxy Machines", "FoxyMachines", "Foxy Machines");
        addSlimefunAddon("magic-expansion", "Magic Expansion", "MagicExpansion", "Magic Expansion");
        addSlimefunAddon("military-arsenal", "Military Arsenal", "MilitaryArsenal", "Military Arsenal");
        addSlimefunAddon("slimefun-warfare", "Slimefun Warfare", "SlimefunWarfare", "Slimefun Warfare");
        addSlimefunAddon("mob-drops", "Mob Drops", "MobDrops", "Mob Drops");
        addSlimefunAddon("lucky-blocks", "Lucky Blocks", "LuckyBlocks", "Lucky Blocks");
        addSlimefunAddon("alchimia-vitae", "Alchimia Vitae", "AlchimiaVitae", "Alchimia Vitae");
        addSlimefunAddon("dank-tech", "Dank Tech", "DankTech", "Dank Tech", "DankStorage");
        addSlimefunAddon("supreme", "Supreme", "Supreme");
        addSlimefunAddon("gastronomicon", "Gastronomicon", "Gastronomicon");
        addSlimefunAddon("exotic-garden", "Exotic Garden", "ExoticGarden", "Exotic Garden");
        addSlimefunAddon("potion-expansion", "Potion Expansion", "PotionExpansion", "Potion Expansion");
        addSlimefunAddon("flower-power", "Flower Power", "FlowerPower", "Flower Power");
        addSlimefunAddon("fast-machines", "Fast Machines", "FastMachines", "Fast Machines");
        addSlimefunAddon("infernal-farm", "Infernal Farm", "InfernalFarm", "Infernal Farm");
        addSlimefunAddon("idoe", "IDOE", "IDOE", "IllegalDevItems", "Illegal Dev Items");
        addSlimefunAddon("slimeglue", "SlimeGlue", "SlimeGlue", "Slime Glue");
        // Pylon depends on Rebar. Requiring Pylon prevents an empty Rebar framework install
        // from creating an impossible Tech quest.
        add("pylon-rebar", "Pylon / Rebar", "rebar_tech", TECH_CATEGORY, List.of("Pylon"), List.of());

        // One Wild Card category. Each pack only participates when its plugin is enabled.
        add("valhallammo", "ValhallaMMO", "valhallammo", WILDCARD_CATEGORY, List.of("ValhallaMMO"), List.of());
        add("evenmorefish", "EvenMoreFish", "evenmorefish", WILDCARD_CATEGORY, List.of("EvenMoreFish"), List.of());
        add("pyrofishingpro", "PyroFishingPro", "pyrofishingpro", WILDCARD_CATEGORY, List.of("PyroFishingPro"), List.of());
        add("mcmmo", "mcMMO", "mcmmo", WILDCARD_CATEGORY, List.of("mcMMO"), List.of());
        add("mmoitems", "MMOItems", "mmoitems", WILDCARD_CATEGORY, List.of("MMOItems"), List.of());
        add("itemsadder", "ItemsAdder", "itemsadder", WILDCARD_CATEGORY, List.of("ItemsAdder"), List.of());
    }

    private DefaultQuestPacks() {}

    private static void add(
            String key,
            String displayName,
            String generator,
            String category,
            List<String> pluginAny,
            List<String> slimefunAddons
    ) {
        DEFAULTS.put(key, new PackDefinition(displayName, generator, category, pluginAny, slimefunAddons));
    }

    private static void addSlimefunAddon(String key, String displayName, String... aliases) {
        add(key, displayName, "slimefun_addon", TECH_CATEGORY, List.of("Slimefun"), List.of(aliases));
    }

    /**
     * Adds new category/pack settings without replacing administrator choices.
     * Also removes obsolete pre-Good/Evil Fable pack keys from short-lived test builds.
     */
    public static boolean ensureConfig(FileConfiguration config, File file) {
        boolean changed = false;

        // Runtime construction lets old test configs migrate while keeping the rejected labels
        // out of this class/JAR's literal string table.
        final String legacyGoodKey = "fable-" + String.join("", "con", "cord");
        final String legacyEvilKey = "fable-" + String.join("", "dom", "inion");
        if (config.contains(ROOT + "." + legacyGoodKey)) {
            config.set(ROOT + "." + legacyGoodKey, null);
            changed = true;
        }
        if (config.contains(ROOT + "." + legacyEvilKey)) {
            config.set(ROOT + "." + legacyEvilKey, null);
            changed = true;
        }

        for (Map.Entry<String, PackDefinition> entry : DEFAULTS.entrySet()) {
            String base = ROOT + "." + entry.getKey();
            PackDefinition definition = entry.getValue();
            changed |= setIfMissing(config, base + ".enabled", true);
            changed |= setIfMissing(config, base + ".display_name", definition.displayName());
            changed |= setIfMissing(config, base + ".generator", definition.generator());
            if (!definition.category().isBlank()) {
                changed |= setIfMissing(config, base + ".category", definition.category());
            }
            if (!definition.pluginAny().isEmpty()) {
                changed |= setIfMissing(config, base + ".plugin_any", definition.pluginAny());
            }
            if (!definition.slimefunAddons().isEmpty()) {
                changed |= setIfMissing(config, base + ".slimefun_addons", definition.slimefunAddons());
            }
        }

        // Existing servers keep every configured amount they already chose. New built-in
        // categories are added at one quest per day, matching the tested category layout.
        changed |= setIfMissing(config, "quests_per_category.good", 1);
        changed |= setIfMissing(config, "quests_per_category.evil", 1);
        changed |= setIfMissing(config, "quests_per_category.tech", 1);
        changed |= setIfMissing(config, "quests_per_category.wildcard", 1);

        changed |= setIfMissing(config, "interfaces.good.inventory_name", "Quests - Fable Good");
        changed |= setIfMissing(config, "interfaces.good.empty_item", "LIME_STAINED_GLASS_PANE");
        changed |= setIfMissing(config, "interfaces.evil.inventory_name", "Quests - Fable Evil");
        changed |= setIfMissing(config, "interfaces.evil.empty_item", "RED_STAINED_GLASS_PANE");
        changed |= setIfMissing(config, "interfaces.tech.inventory_name", "Quests - Tech");
        changed |= setIfMissing(config, "interfaces.tech.empty_item", "CYAN_STAINED_GLASS_PANE");
        changed |= setIfMissing(config, "interfaces.wildcard.inventory_name", "Quests - Wild Card");
        changed |= setIfMissing(config, "interfaces.wildcard.empty_item", "PURPLE_STAINED_GLASS_PANE");

        changed |= setIfMissing(config, "npcs.good", "&a&lFable Good Quests");
        changed |= setIfMissing(config, "npcs.evil", "&4&lFable Evil Quests");
        changed |= setIfMissing(config, "npcs.tech", "&b&lTech Quests");
        changed |= setIfMissing(config, "npcs.wildcard", "&d&lWild Card Quests");

        if (changed) {
            try {
                config.save(file);
                PluginLogger.info("Updated built-in daily quest category and pack settings in config.yml.");
            } catch (IOException exception) {
                PluginLogger.warn("Unable to save default quest pack settings: " + exception.getMessage());
                return false;
            }
        }
        return changed;
    }

    private static boolean setIfMissing(FileConfiguration config, String path, Object value) {
        if (config.contains(path)) return false;
        config.set(path, value);
        return true;
    }

    private static YamlConfiguration mainConfig() {
        return YamlConfiguration.loadConfiguration(new File(ODailyQuests.INSTANCE.getDataFolder(), "config.yml"));
    }

    public static boolean isPackActive(String packKey) {
        return isPackActive(mainConfig(), packKey);
    }

    private static boolean isPackActive(FileConfiguration config, String packKey) {
        PackDefinition definition = DEFAULTS.get(packKey);
        if (definition == null) return false;

        ConfigurationSection section = config.getConfigurationSection(ROOT + "." + packKey);
        if (section == null || !section.getBoolean("enabled", false)) return false;

        List<String> plugins = section.getStringList("plugin_any");
        if (!plugins.isEmpty()) {
            boolean found = plugins.stream().anyMatch(name -> Bukkit.getPluginManager().isPluginEnabled(name));
            if (!found) return false;
        }

        List<String> addonAliases = section.getStringList("slimefun_addons");
        return addonAliases.isEmpty() || SlimefunIntegration.isAnyAddonPresent(addonAliases);
    }

    /** Removes only explicitly tagged built-in quests; untagged server quests stay custom. */
    public static void filterConfiguredDefaults(FileConfiguration quests) {
        ConfigurationSection all = quests.getConfigurationSection("quests");
        if (all == null) return;

        FileConfiguration config = mainConfig();
        List<String> remove = new ArrayList<>();
        for (String key : all.getKeys(false)) {
            ConfigurationSection quest = all.getConfigurationSection(key);
            if (quest == null) continue;
            String pack = quest.getString("default_pack");
            if (pack != null && !pack.isBlank() && !isPackActive(config, pack.trim().toLowerCase(Locale.ROOT))) {
                remove.add(key);
            }
        }
        remove.forEach(key -> quests.set("quests." + key, null));
    }

    /**
     * Adds dependency-backed quests to Tech or Wild Card in memory.
     * Easy/Medium/Hard/Fable files are never polluted with integration quests.
     */
    public static void mergeGenerated(String category, FileConfiguration quests) {
        String target = normalizeCategory(category);
        if (!TECH_CATEGORY.equals(target) && !WILDCARD_CATEGORY.equals(target)) return;

        FileConfiguration config = mainConfig();
        for (Map.Entry<String, PackDefinition> entry : DEFAULTS.entrySet()) {
            String packKey = entry.getKey();
            PackDefinition definition = entry.getValue();
            if (!target.equals(definition.category()) || !isPackActive(config, packKey)) continue;

            ConfigurationSection pack = config.getConfigurationSection(ROOT + "." + packKey);
            String generator = pack == null
                    ? definition.generator()
                    : pack.getString("generator", definition.generator()).toLowerCase(Locale.ROOT);
            String displayName = pack == null
                    ? definition.displayName()
                    : pack.getString("display_name", definition.displayName());

            switch (generator) {
                case "slimefun_core" -> addSlimefunCore(quests, packKey);
                case "slimefun_addon" -> addSlimefunAddonQuests(quests, packKey, displayName,
                        pack == null ? definition.slimefunAddons() : pack.getStringList("slimefun_addons"));
                case "rebar_tech" -> addPylonRebar(quests, packKey);
                case "valhallammo" -> addValhalla(quests, packKey);
                case "evenmorefish" -> addFishingSet(quests, packKey, "EMF_FISH", "EvenMoreFish");
                case "pyrofishingpro" -> addFishingSet(quests, packKey, "PYRO_FISH", "PyroFishingPro");
                case "mcmmo" -> addMcMMO(quests, packKey);
                case "mmoitems" -> addExternalItemSet(quests, packKey, "MMOITEM_ITEM", "MMOItems", "IRON_SWORD");
                case "itemsadder" -> addExternalItemSet(quests, packKey, "ITEMSADDER_ITEM", "ItemsAdder", "PAPER");
                default -> { }
            }
        }
    }

    public static boolean isOptionalCategory(String category) {
        String normalized = normalizeCategory(category);
        return TECH_CATEGORY.equals(normalized) || WILDCARD_CATEGORY.equals(normalized);
    }

    /** True when at least one dependency pack can populate this optional category. */
    public static boolean isOptionalCategoryAvailable(String category) {
        String normalized = normalizeCategory(category);
        if (!isOptionalCategory(normalized)) return true;
        FileConfiguration config = mainConfig();
        for (Map.Entry<String, PackDefinition> entry : DEFAULTS.entrySet()) {
            if (normalized.equals(entry.getValue().category()) && isPackActive(config, entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    /** Tags newly created bundled quest files with the pack that owns them. */
    public static void tagDefaults(File file, String packKey) {
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection quests = yaml.getConfigurationSection("quests");
            if (quests == null) return;
            for (String key : quests.getKeys(false)) {
                yaml.set("quests." + key + ".default_pack", packKey);
            }
            yaml.save(file);
        } catch (IOException exception) {
            PluginLogger.warn("Unable to tag fresh default quests in " + file.getName() + ": " + exception.getMessage());
        }
    }

    public static void tagVanillaDefaults(File file) {
        tagDefaults(file, "vanilla");
    }

    public static String statusSummary() {
        FileConfiguration config = mainConfig();
        int active = 0;
        int disabled = 0;
        int waiting = 0;
        for (String key : DEFAULTS.keySet()) {
            ConfigurationSection section = config.getConfigurationSection(ROOT + "." + key);
            if (section == null || !section.getBoolean("enabled", false)) disabled++;
            else if (isPackActive(config, key)) active++;
            else waiting++;
        }
        return active + " active, " + waiting + " waiting for dependency, " + disabled + " disabled";
    }

    public static List<String> activePackNames() {
        FileConfiguration config = mainConfig();
        List<String> names = new ArrayList<>();
        for (String key : DEFAULTS.keySet()) {
            if (!isPackActive(config, key)) continue;
            names.add(config.getString(ROOT + "." + key + ".display_name", DEFAULTS.get(key).displayName()));
        }
        return names;
    }

    private static void addSlimefunCore(FileConfiguration q, String pack) {
        addQuest(q, pack, "slimefun_common_talisman", "&bDaily Tech: Slimefun", "EMERALD", "SLIMEFUN_CRAFT", 1, 500,
                "Craft a Common Talisman", Map.of("slimefun_ids", List.of("COMMON_TALISMAN")), "Tech");
        addQuest(q, pack, "slimefun_steel_ingots", "&bDaily Tech: Slimefun", "IRON_INGOT", "SLIMEFUN_CRAFT", 8, 1100,
                "Craft %required% Steel Ingots", Map.of("slimefun_ids", List.of("STEEL_INGOT")), "Tech");
        addQuest(q, pack, "slimefun_reinforced_alloy", "&bDaily Tech: Slimefun", "NETHERITE_INGOT", "SLIMEFUN_CRAFT", 2, 2500,
                "Craft %required% Reinforced Alloy Ingots", Map.of("slimefun_ids", List.of("REINFORCED_ALLOY_INGOT")), "Tech");
    }

    private static void addSlimefunAddonQuests(FileConfiguration q, String pack, String display, List<String> aliases) {
        addQuest(q, pack, "addon_" + safe(pack) + "_2", "&bDaily Tech: " + display, "SLIME_BALL", "SLIMEFUN_ITEM", 2, 450,
                "Create or obtain %required% " + display + " items", Map.of("slimefun_addons", aliases), "Tech");
        addQuest(q, pack, "addon_" + safe(pack) + "_5", "&bDaily Tech: " + display, "SLIME_BALL", "SLIMEFUN_ITEM", 5, 950,
                "Create or obtain %required% " + display + " items", Map.of("slimefun_addons", aliases), "Tech");
        addQuest(q, pack, "addon_" + safe(pack) + "_10", "&bDaily Tech: " + display, "SLIME_BALL", "SLIMEFUN_ITEM", 10, 1900,
                "Create or obtain %required% " + display + " items", Map.of("slimefun_addons", aliases), "Tech");
    }

    private static void addPylonRebar(FileConfiguration q, String pack) {
        addQuest(q, pack, "pylon_rebar_2", "&bDaily Tech: Pylon / Rebar", "COPPER_INGOT", "REBAR_ITEM", 2, 500,
                "Create or obtain %required% Pylon/Rebar items", Map.of(), "Tech");
        addQuest(q, pack, "pylon_rebar_6", "&bDaily Tech: Pylon / Rebar", "IRON_INGOT", "REBAR_ITEM", 6, 1200,
                "Create or obtain %required% Pylon/Rebar items", Map.of(), "Tech");
        addQuest(q, pack, "pylon_rebar_12", "&bDaily Tech: Pylon / Rebar", "NETHERITE_SCRAP", "REBAR_ITEM", 12, 2400,
                "Create or obtain %required% Pylon/Rebar items", Map.of(), "Tech");
    }

    private static void addValhalla(FileConfiguration q, String pack) {
        addQuest(q, pack, "valhalla_xp_500", "&dWild Card: ValhallaMMO", "EXPERIENCE_BOTTLE", "VALHALLA_EXP", 500, 450,
                "Earn %required% ValhallaMMO skill experience", Map.of("skill", "ANY"), "Wild Card");
        addQuest(q, pack, "valhalla_xp_2500", "&dWild Card: ValhallaMMO", "EXPERIENCE_BOTTLE", "VALHALLA_EXP", 2500, 1100,
                "Earn %required% ValhallaMMO skill experience", Map.of("skill", "ANY"), "Wild Card");
        addQuest(q, pack, "valhalla_levels_3", "&dWild Card: ValhallaMMO", "EXPERIENCE_BOTTLE", "VALHALLA_LEVEL_UP", 3, 2200,
                "Gain %required% ValhallaMMO skill levels", Map.of("skill", "ANY"), "Wild Card");
    }

    private static void addFishingSet(FileConfiguration q, String pack, String type, String display) {
        int[] amounts = {3, 8, 15};
        int[] rewards = {400, 900, 1800};
        for (int i = 0; i < amounts.length; i++) {
            addQuest(q, pack, safe(pack) + "_fish_" + amounts[i], "&dWild Card: " + display, "FISHING_ROD", type,
                    amounts[i], rewards[i], "Catch %required% " + display + " fish", Map.of(), "Wild Card");
        }
    }

    private static void addMcMMO(FileConfiguration q, String pack) {
        addQuest(q, pack, "mcmmo_xp_250", "&dWild Card: mcMMO", "IRON_PICKAXE", "MCMMO_EXP", 250, 400,
                "Earn %required% mcMMO skill experience", Map.of(), "Wild Card");
        addQuest(q, pack, "mcmmo_xp_1000", "&dWild Card: mcMMO", "DIAMOND_AXE", "MCMMO_EXP", 1000, 950,
                "Earn %required% mcMMO skill experience", Map.of(), "Wild Card");
        addQuest(q, pack, "mcmmo_xp_2500", "&dWild Card: mcMMO", "DIAMOND_SWORD", "MCMMO_EXP", 2500, 1900,
                "Earn %required% mcMMO skill experience", Map.of(), "Wild Card");
    }

    private static void addExternalItemSet(FileConfiguration q, String pack, String type, String display, String icon) {
        int[] amounts = {1, 3, 6};
        int[] rewards = {400, 900, 1800};
        for (int i = 0; i < amounts.length; i++) {
            addQuest(q, pack, safe(pack) + "_items_" + amounts[i], "&dWild Card: " + display, icon, type,
                    amounts[i], rewards[i], "Create or obtain %required% " + display + " items", Map.of(), "Wild Card");
        }
    }

    private static void addQuest(
            FileConfiguration q,
            String pack,
            String key,
            String name,
            String icon,
            String type,
            int requiredAmount,
            int reward,
            String task,
            Map<String, Object> extras,
            String categoryLabel
    ) {
        String path = "quests.__pack_" + safe(key);
        if (q.contains(path)) return;
        q.set(path + ".name", name);
        q.set(path + ".menu_item", icon);
        q.set(path + ".description", List.of(
                " &7Category: &f" + categoryLabel,
                "",
                "&3Task:",
                " &3&l| &7" + task,
                "",
                "&3Progress:",
                " &3&l| %status%",
                "",
                "&3Reward:",
                " &3&l| &7" + reward + "$"
        ));
        q.set(path + ".quest_type", type);
        q.set(path + ".required_amount", requiredAmount);
        q.set(path + ".reward.reward_type", "MONEY");
        q.set(path + ".reward.amount", reward);
        q.set(path + ".default_pack", pack);
        q.set(path + ".weight", 1.0D);
        for (Map.Entry<String, Object> extra : extras.entrySet()) {
            q.set(path + "." + extra.getKey(), extra.getValue());
        }
    }

    private static String normalizeCategory(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
    }

    private static String safe(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
    }
}
