package com.ordwen.odailyquests.quests.features;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Adds maintained exact-item Pylon quests to an older managed Tech pool without touching custom quests. */
public final class PylonQuestDefaults {

    private static final String PACK = "pylon-rebar";
    private static final String RESOURCE = "quests/pylon-defaults.yml";

    private PylonQuestDefaults() {}

    /**
     * Adds the exact-item defaults only when the Tech file already contains the maintained
     * Pylon/Rebar pack. This makes the migration additive for managed defaults and avoids
     * inserting Pylon content into a completely custom Tech file.
     */
    public static void seed(ODailyQuests plugin, File techFile) {
        if (plugin == null || techFile == null || !techFile.isFile()) return;

        YamlConfiguration target = YamlConfiguration.loadConfiguration(techFile);
        ConfigurationSection targetQuests = target.getConfigurationSection("quests");
        if (targetQuests == null || !containsManagedPylonPack(targetQuests)) return;

        YamlConfiguration defaults = loadDefaults(plugin);
        if (defaults == null) return;
        ConfigurationSection defaultQuests = defaults.getConfigurationSection("quests");
        if (defaultQuests == null) {
            PluginLogger.warn("Bundled " + RESOURCE + " has no quests section.");
            return;
        }

        boolean changed = false;
        for (String key : defaultQuests.getKeys(false)) {
            String targetPath = "quests." + key;
            if (target.isConfigurationSection(targetPath)) continue;

            ConfigurationSection source = defaultQuests.getConfigurationSection(key);
            if (source == null) continue;
            for (Map.Entry<String, Object> entry : source.getValues(true).entrySet()) {
                if (entry.getValue() instanceof ConfigurationSection) continue;
                target.set(targetPath + "." + entry.getKey(), entry.getValue());
            }
            changed = true;
        }

        if (!changed) return;
        try {
            target.save(techFile);
            PluginLogger.info("Added exact-item Pylon/Rebar defaults to tech.yml.");
        } catch (IOException exception) {
            PluginLogger.warn("Unable to add exact-item Pylon/Rebar defaults to tech.yml: " + exception.getMessage());
        }
    }

    private static YamlConfiguration loadDefaults(ODailyQuests plugin) {
        try (InputStream stream = plugin.getResource(RESOURCE)) {
            if (stream == null) {
                PluginLogger.warn("Missing bundled Pylon quest resource: " + RESOURCE);
                return null;
            }
            return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            PluginLogger.warn("Unable to read bundled Pylon quest defaults: " + exception.getMessage());
            return null;
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
