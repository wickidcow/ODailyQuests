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
 * Built-in, toggleable starter quest packs. Dependency-backed packs are enabled by default
 * but only become active when their dependency (and, for Slimefun addons, addon registration)
 * is actually present. Existing untagged server quests are always treated as custom content.
 */
public final class DefaultQuestPacks {

    private static final String ROOT = "default_quest_packs";

    private record PackDefinition(
            String displayName,
            String generator,
            List<String> pluginAny,
            List<String> slimefunAddons
    ) {}

    private static final Map<String, PackDefinition> DEFAULTS = new LinkedHashMap<>();

    static {
        add("vanilla", "Vanilla Starter", "vanilla", List.of(), List.of());
        add("fable-good", "Fable Quests - Good", "fable_good", List.of(), List.of());
        add("fable-evil", "Fable Quests - Evil", "fable_evil", List.of(), List.of());

        add("slimefun-core", "Slimefun Core", "slimefun_core", List.of("Slimefun"), List.of());
        add("valhallammo", "ValhallaMMO", "valhallammo", List.of("ValhallaMMO"), List.of());
        add("evenmorefish", "EvenMoreFish", "evenmorefish", List.of("EvenMoreFish"), List.of());
        add("pyrofishingpro", "PyroFishingPro", "pyrofishingpro", List.of("PyroFishingPro"), List.of());

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
    }

    private DefaultQuestPacks() {}

    private static void add(String key, String displayName, String generator, List<String> pluginAny, List<String> slimefunAddons) {
        DEFAULTS.put(key, new PackDefinition(displayName, generator, pluginAny, slimefunAddons));
    }

    private static void addSlimefunAddon(String key, String displayName, String... aliases) {
        add(key, displayName, "slimefun_addon", List.of("Slimefun"), List.of(aliases));
    }

    /** Adds new pack toggles without replacing an administrator's existing values. */
    public static boolean ensureConfig(FileConfiguration config, File file) {
        boolean changed = false;
        for (Map.Entry<String, PackDefinition> entry : DEFAULTS.entrySet()) {
            String base = ROOT + "." + entry.getKey();
            PackDefinition definition = entry.getValue();
            if (!config.contains(base + ".enabled")) {
                config.set(base + ".enabled", true);
                changed = true;
            }
            if (!config.contains(base + ".display_name")) {
                config.set(base + ".display_name", definition.displayName());
                changed = true;
            }
            if (!config.contains(base + ".generator")) {
                config.set(base + ".generator", definition.generator());
                changed = true;
            }
            if (!definition.pluginAny().isEmpty() && !config.contains(base + ".plugin_any")) {
                config.set(base + ".plugin_any", definition.pluginAny());
                changed = true;
            }
            if (!definition.slimefunAddons().isEmpty() && !config.contains(base + ".slimefun_addons")) {
                config.set(base + ".slimefun_addons", definition.slimefunAddons());
                changed = true;
            }
        }

        if (changed) {
            try {
                config.save(file);
                PluginLogger.info("Added default quest pack toggles to config.yml.");
            } catch (IOException exception) {
                PluginLogger.warn("Unable to save default quest pack settings: " + exception.getMessage());
                return false;
            }
        }
        return changed;
    }

    private static YamlConfiguration mainConfig() {
        return YamlConfiguration.loadConfiguration(new File(ODailyQuests.INSTANCE.getDataFolder(), "config.yml"));
    }

    public static boolean isPackActive(String packKey) {
        return isPackActive(mainConfig(), packKey);
    }

    private static boolean isPackActive(FileConfiguration config, String packKey) {
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

    /** Removes only explicitly tagged built-in quests; untagged server quests are custom and untouched. */
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
     * Adds the active dependency/Fable packs to an in-memory difficulty category.
     * The administrator's quest YAML is never rewritten by this merge.
     */
    public static void mergeGenerated(String category, FileConfiguration quests) {
        String difficulty = category == null ? "" : category.toLowerCase(Locale.ROOT);
        if (!difficulty.equals("easy") && !difficulty.equals("medium") && !difficulty.equals("hard")) return;

        FileConfiguration config = mainConfig();
        ConfigurationSection packs = config.getConfigurationSection(ROOT);
        if (packs == null) return;

        for (String packKey : packs.getKeys(false)) {
            if ("vanilla".equalsIgnoreCase(packKey) || !isPackActive(config, packKey)) continue;
            ConfigurationSection pack = packs.getConfigurationSection(packKey);
            if (pack == null) continue;
            String generator = pack.getString("generator", "").toLowerCase(Locale.ROOT);
            String displayName = pack.getString("display_name", packKey);
            switch (generator) {
                case "slimefun_core" -> addSlimefunCore(quests, difficulty, packKey);
                case "slimefun_addon" -> addSlimefunAddonQuest(quests, difficulty, packKey, displayName, pack.getStringList("slimefun_addons"));
                case "valhallammo" -> addValhalla(quests, difficulty, packKey);
                case "evenmorefish" -> addFishing(quests, difficulty, packKey, "EMF_FISH", "EvenMoreFish");
                case "pyrofishingpro" -> addFishing(quests, difficulty, packKey, "PYRO_FISH", "PyroFishingPro");
                case "fable_good" -> addFableGood(quests, difficulty, packKey);
                case "fable_evil" -> addFableEvil(quests, difficulty, packKey);
                default -> { }
            }
        }
    }

    /** Tags only newly created bundled Vanilla quest files. */
    public static void tagVanillaDefaults(File file) {
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection quests = yaml.getConfigurationSection("quests");
            if (quests == null) return;
            for (String key : quests.getKeys(false)) {
                yaml.set("quests." + key + ".default_pack", "vanilla");
            }
            yaml.save(file);
        } catch (IOException exception) {
            PluginLogger.warn("Unable to tag fresh Vanilla starter quests: " + exception.getMessage());
        }
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

    private static void addSlimefunCore(FileConfiguration q, String d, String pack) {
        switch (d) {
            case "easy" -> addQuest(q, pack, "slimefun_core_easy", "&aSlimefun Apprentice", "EMERALD", "SLIMEFUN_CRAFT", 1, 500,
                    "Craft a Common Talisman", Map.of("slimefun_ids", List.of("COMMON_TALISMAN")));
            case "medium" -> addQuest(q, pack, "slimefun_core_medium", "&eSlimefun Metallurgist", "IRON_INGOT", "SLIMEFUN_CRAFT", 8, 1100,
                    "Craft %required% Steel Ingots", Map.of("slimefun_ids", List.of("STEEL_INGOT")));
            case "hard" -> addQuest(q, pack, "slimefun_core_hard", "&cSlimefun Alloy Master", "NETHERITE_INGOT", "SLIMEFUN_CRAFT", 2, 2500,
                    "Craft %required% Reinforced Alloy Ingots", Map.of("slimefun_ids", List.of("REINFORCED_ALLOY_INGOT")));
            default -> { }
        }
    }

    private static void addSlimefunAddonQuest(FileConfiguration q, String d, String pack, String display, List<String> aliases) {
        int amount = switch (d) { case "easy" -> 2; case "medium" -> 5; default -> 10; };
        int reward = switch (d) { case "easy" -> 450; case "medium" -> 950; default -> 1900; };
        addQuest(q, pack, "addon_" + safe(pack) + "_" + d, "&a" + display + " " + title(d), "SLIME_BALL", "SLIMEFUN_ITEM", amount, reward,
                "Create or obtain %required% " + display + " items", Map.of("slimefun_addons", aliases));
    }

    private static void addValhalla(FileConfiguration q, String d, String pack) {
        if ("hard".equals(d)) {
            addQuest(q, pack, "valhalla_hard", "&cValhallaMMO Mastery", "EXPERIENCE_BOTTLE", "VALHALLA_LEVEL_UP", 3, 2200,
                    "Gain %required% ValhallaMMO skill levels", Map.of("skill", "ANY"));
        } else {
            int amount = "easy".equals(d) ? 500 : 2500;
            int reward = "easy".equals(d) ? 450 : 1100;
            addQuest(q, pack, "valhalla_" + d, "&bValhallaMMO " + title(d), "EXPERIENCE_BOTTLE", "VALHALLA_EXP", amount, reward,
                    "Earn %required% ValhallaMMO skill experience", Map.of("skill", "ANY"));
        }
    }

    private static void addFishing(FileConfiguration q, String d, String pack, String type, String display) {
        int amount = switch (d) { case "easy" -> 3; case "medium" -> 8; default -> 15; };
        int reward = switch (d) { case "easy" -> 400; case "medium" -> 900; default -> 1800; };
        addQuest(q, pack, safe(pack) + "_" + d, "&b" + display + " " + title(d), "FISHING_ROD", type, amount, reward,
                "Catch %required% " + display + " fish", Map.of());
    }

    private static void addFableGood(FileConfiguration q, String d, String pack) {
        switch (d) {
            case "easy" -> addQuest(q, pack, "fable_good_easy", "&aFable Good: A Gentle Hand", "WHEAT", "FARMING", 32, 400,
                    "Harvest %required% wheat for a Good Quest", Map.of("required", "WHEAT"));
            case "medium" -> addQuest(q, pack, "fable_good_medium", "&aFable Good: Steward of Life", "WHEAT", "BREED", 12, 900,
                    "Breed %required% animals for a Good Quest", Map.of("required", List.of("COW", "SHEEP", "PIG", "CHICKEN")));
            case "hard" -> addQuest(q, pack, "fable_good_hard", "&aFable Good: Guardian's Oath", "GOLDEN_APPLE", "TAME", 5, 1800,
                    "Tame %required% loyal companions for a Good Quest", Map.of("required", List.of("WOLF", "CAT")));
            default -> { }
        }
    }

    private static void addFableEvil(FileConfiguration q, String d, String pack) {
        switch (d) {
            case "easy" -> addQuest(q, pack, "fable_evil_easy", "&4Fable Evil: Cull the Restless", "ROTTEN_FLESH", "KILL", 20, 400,
                    "Defeat %required% zombies for an Evil Quest", Map.of("required", "ZOMBIE"));
            case "medium" -> addQuest(q, pack, "fable_evil_medium", "&4Fable Evil: Trial by Flame", "BLAZE_ROD", "KILL", 15, 900,
                    "Defeat %required% blazes for an Evil Quest", Map.of("required", "BLAZE"));
            case "hard" -> addQuest(q, pack, "fable_evil_hard", "&4Fable Evil: Black Citadel", "WITHER_SKELETON_SKULL", "KILL", 25, 1800,
                    "Defeat %required% wither skeletons for an Evil Quest", Map.of("required", "WITHER_SKELETON"));
            default -> { }
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
            Map<String, Object> extras
    ) {
        String path = "quests.__pack_" + safe(key);
        if (q.contains(path)) return;
        q.set(path + ".name", name);
        q.set(path + ".menu_item", icon);
        q.set(path + ".description", List.of(
                " &7ᴅɪꜰꜰɪᴄᴜʟᴛʏ: &e" + title(categoryFromKey(key)),
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

    private static String categoryFromKey(String key) {
        if (key.endsWith("_easy")) return "easy";
        if (key.endsWith("_medium")) return "medium";
        return "hard";
    }

    private static String title(String value) {
        if (value == null || value.isBlank()) return "Quest";
        return Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
    }
}
