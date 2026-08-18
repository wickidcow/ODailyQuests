package com.ordwen.odailyquests.quests.features;

import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Adds maintained exact-item Pylon quests to an older managed Tech pool without touching custom quests. */
public final class PylonQuestDefaults {

    private static final String PACK = "pylon-rebar";

    private record Definition(
            String id,
            String name,
            String icon,
            String task,
            String key,
            int gold,
            int xp,
            int points
    ) {}

    private static final List<Definition> DEFINITIONS = List.of(
            new Definition("__pylon_shimmer_magnet", "&#55FFFFDaily Tech: Shimmer Magnet", "COMPASS",
                    "Craft a Shimmer Magnet", "pylon:shimmer_magnet", 2200, 900, 3),
            new Definition("__pylon_diamond_hammer", "&#55FFFFDaily Tech: Diamond Hammer", "DIAMOND_PICKAXE",
                    "Craft a Diamond Hammer", "pylon:diamond_hammer", 1400, 600, 2),
            new Definition("__pylon_elevator_1", "&#55FFFFDaily Tech: Elevator I", "IRON_BLOCK",
                    "Craft an Elevator I", "pylon:elevator_1", 1700, 700, 2),
            new Definition("__pylon_reactivated_wither_skull", "&#55FFFFDaily Tech: Reactivated Wither Skull", "WITHER_SKELETON_SKULL",
                    "Craft a Reactivated Wither Skull", "pylon:reactivated_wither_skull", 3200, 1200, 3)
    );

    private PylonQuestDefaults() {}

    /**
     * Adds the exact-item defaults only when the Tech file already contains the maintained
     * Pylon/Rebar pack. This makes the migration additive for managed defaults and avoids
     * inserting Pylon content into a completely custom Tech file.
     */
    public static void seed(File techFile) {
        if (techFile == null || !techFile.isFile()) return;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(techFile);
        ConfigurationSection quests = yaml.getConfigurationSection("quests");
        if (quests == null || !containsManagedPylonPack(quests)) return;

        boolean changed = false;
        for (Definition definition : DEFINITIONS) {
            String path = "quests." + definition.id();
            if (yaml.isConfigurationSection(path)) continue;

            yaml.set(path + ".name", definition.name());
            yaml.set(path + ".menu_item", definition.icon());
            yaml.set(path + ".description", List.of(
                    " &7Category: &fTech",
                    "",
                    "&3Task:",
                    " &3&l| &7" + definition.task(),
                    "",
                    "&3Progress:",
                    " &3&l| %status%",
                    "",
                    "&3Rewards:",
                    " &3&l| &7" + definition.gold() + " Gold",
                    " &3&l| &7" + definition.xp() + " XP",
                    " &3&l| &7" + definition.points() + (definition.points() == 1 ? " Quest Point" : " Quest Points")
            ));
            yaml.set(path + ".quest_type", "REBAR_ITEM");
            yaml.set(path + ".required_amount", 1);
            yaml.set(path + ".pylon_keys", List.of(definition.key()));
            yaml.set(path + ".reward.reward_type", "COMMAND");
            yaml.set(path + ".reward.commands", List.of(
                    "eco give %player_name% " + definition.gold(),
                    "xp give %player_name% " + definition.xp(),
                    "questadmin givepoints %player_name% " + definition.points()
            ));
            yaml.set(path + ".default_pack", PACK);
            yaml.set(path + ".weight", 1.0D);
            changed = true;
        }

        if (!changed) return;
        try {
            yaml.save(techFile);
            PluginLogger.info("Added exact-item Pylon/Rebar defaults to tech.yml.");
        } catch (IOException exception) {
            PluginLogger.warn("Unable to add exact-item Pylon/Rebar defaults to tech.yml: " + exception.getMessage());
        }
    }

    private static boolean containsManagedPylonPack(ConfigurationSection quests) {
        for (String key : quests.getKeys(false)) {
            ConfigurationSection quest = quests.getConfigurationSection(key);
            if (quest != null && PACK.equalsIgnoreCase(quest.getString("default_pack", ""))) return true;
        }
        return false;
    }
}
