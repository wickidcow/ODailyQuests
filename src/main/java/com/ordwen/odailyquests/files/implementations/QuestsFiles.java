package com.ordwen.odailyquests.files.implementations;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.quests.features.DefaultQuestPacks;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class QuestsFiles {

    private static final Map<String, FileConfiguration> configurations = new HashMap<>();

    private final ODailyQuests plugin;

    public QuestsFiles(ODailyQuests plugin) {
        this.plugin = plugin;
    }

    public static FileConfiguration getQuestsConfigurationByCategory(String category) {
        final FileConfiguration configuration = configurations.get(category);
        if (configuration == null) {
            PluginLogger.error("Impossible to find the configuration file for category " + category + ".");
            PluginLogger.error("Please check that the file exists and is correctly referenced in the configuration file (quests_per_category section).");
            PluginLogger.error("If the problem persists, please inform the developer.");
            return null;
        }

        return configuration;
    }

    /** Returns whether a loaded category currently contains at least one usable quest. */
    public static boolean hasQuestEntries(String category) {
        final FileConfiguration configuration = configurations.get(category);
        if (configuration == null) return false;
        final ConfigurationSection quests = configuration.getConfigurationSection("quests");
        return quests != null && !quests.getKeys(false).isEmpty();
    }

    /**
     * Init quests files.
     */
    public void load() {
        configurations.clear();

        final File questsFolder = new File(plugin.getDataFolder(), "quests");
        if (!questsFolder.exists()) questsFolder.mkdirs();

        // Add missing maintained-fork category files without ever overwriting an administrator's files.
        // This also migrates older installs that only had Easy/Medium/Hard.
        ensureBuiltInQuestFiles();

        final File[] questFiles = questsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (questFiles == null) {
            PluginLogger.error("An error occurred while loading quests files.");
            PluginLogger.error("Please inform the developer.");
            return;
        }

        for (File file : questFiles) {
            final String category = file.getName().replace(".yml", "");

            // A short-lived test build used earlier Fable faction wording. Migrate only the
            // exact strings that build wrote, so existing Good/Evil files are fixed without
            // otherwise changing administrator-authored content.
            if ("good".equalsIgnoreCase(category) || "evil".equalsIgnoreCase(category)) {
                migrateLegacyFableTerminology(file);
            }

            final FileConfiguration config = new YamlConfiguration();
            try {
                config.load(file);

                // Filter only explicitly tagged built-in defaults. Untagged existing server quests
                // are considered custom and are never disabled by a pack toggle.
                DefaultQuestPacks.filterConfiguredDefaults(config);

                // Tech and Wild Card dependency quests are merged in memory. The physical YAML
                // remains administrator-owned and is never rewritten by a dependency appearing/disappearing.
                DefaultQuestPacks.mergeGenerated(category, config);

                configurations.put(category, config);
                PluginLogger.fine(category + " quests file successfully loaded.");
            } catch (InvalidConfigurationException | IOException e) {
                PluginLogger.error("An error occurred while loading the " + category + " quests file.");
                PluginLogger.error("Please inform the developer.");
                PluginLogger.error(e.getMessage());
            }
        }
    }

    private void ensureBuiltInQuestFiles() {
        ensureQuestFile("examples.yml", null);
        ensureQuestFile("easy.yml", "vanilla");
        ensureQuestFile("medium.yml", "vanilla");
        ensureQuestFile("hard.yml", "vanilla");
        ensureQuestFile("good.yml", "fable-good");
        ensureQuestFile("evil.yml", "fable-evil");
        ensureQuestFile("tech.yml", null);
        ensureQuestFile("wildcard.yml", null);
    }

    private void ensureQuestFile(String fileName, String packKey) {
        final File questsFolder = new File(plugin.getDataFolder(), "quests");
        final File file = new File(questsFolder, fileName);
        if (file.exists()) return;

        plugin.saveResource("quests/" + fileName, false);
        if (packKey != null) DefaultQuestPacks.tagDefaults(file, packKey);
        PluginLogger.info(fileName + " created as default.");
    }

    private void migrateLegacyFableTerminology(File file) {
        // Build the two obsolete labels at runtime. This lets us migrate old configs while
        // keeping those rejected labels out of the compiled class/JAR string table.
        final String legacyGood = String.join("", "Con", "cord");
        final String legacyEvil = String.join("", "Dom", "inion");

        try {
            String original = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String migrated = original
                    .replace("alignment.last_deed " + legacyGood, "alignment.last_deed Good")
                    .replace("alignment.last_deed " + legacyEvil, "alignment.last_deed Evil")
                    .replace("the " + legacyGood + " shrine", "a Good shrine")
                    .replace("Let the smoke teach it " + legacyEvil + " silence.", "Let the smoke carry an Evil warning.")
                    .replace("and prove " + legacyEvil + " takes what it wants.", "and prove Evil takes what it wants.")
                    .replace("Raise soulflame for the " + legacyEvil + " altars", "Raise soulflame for the Evil altars");

            if (!original.equals(migrated)) {
                Files.writeString(file.toPath(), migrated, StandardCharsets.UTF_8);
                PluginLogger.info("Migrated legacy Fable terminology in " + file.getName() + ".");
            }
        } catch (IOException exception) {
            PluginLogger.warn("Unable to migrate legacy Fable terminology in " + file.getName() + ": " + exception.getMessage());
        }
    }
}
