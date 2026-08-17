package com.ordwen.odailyquests.quests.features;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.tools.PluginLogger;
import com.ordwen.odailyquests.tools.RenewSchedule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Optional 3.0.5 quest metadata. All fields are backward compatible: if a key is absent,
 * the original ODailyQuests behavior is preserved.
 */
public final class QuestFeatures {

    private QuestFeatures() {
    }

    private static YamlConfiguration mainConfig() {
        return YamlConfiguration.loadConfiguration(new File(ODailyQuests.INSTANCE.getDataFolder(), "config.yml"));
    }

    private static ConfigurationSection questSection(AbstractQuest quest) {
        String category = quest.getCategoryName();
        if (category == null || category.isBlank()) return null;

        String fileName = category.endsWith(".yml") ? category : category + ".yml";
        File questFile = new File(new File(ODailyQuests.INSTANCE.getDataFolder(), "quests"), fileName);
        if (!questFile.isFile()) {
            // Some loaders may expose a display/file path rather than the bare category name.
            questFile = new File(ODailyQuests.INSTANCE.getDataFolder(), fileName);
        }
        if (!questFile.isFile()) return null;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(questFile);
        return yaml.getConfigurationSection("quests." + quest.getFileIndex());
    }

    public static String qualifiedId(AbstractQuest quest) {
        return normalizeCategory(quest.getCategoryName()) + ":" + quest.getFileIndex();
    }

    private static String normalizeCategory(String category) {
        if (category == null) return "";
        String normalized = category.toLowerCase(Locale.ROOT);
        if (normalized.endsWith(".yml")) normalized = normalized.substring(0, normalized.length() - 4);
        return normalized;
    }

    public static double weight(AbstractQuest quest) {
        ConfigurationSection section = questSection(quest);
        if (section == null) return 1.0D;
        return Math.max(0.0D, section.getDouble("weight", 1.0D));
    }

    public static String pool(AbstractQuest quest) {
        ConfigurationSection section = questSection(quest);
        if (section == null) return "default";
        String pool = section.getString("pool", "default");
        return pool == null || pool.isBlank() ? "default" : pool.trim();
    }

    /**
     * A quest whose chain_after is set is reserved for chain advancement and is not placed
     * into the normal random draw.
     */
    public static boolean isChainOnly(AbstractQuest quest) {
        return chainAfter(quest) != null;
    }

    public static String chainAfter(AbstractQuest quest) {
        ConfigurationSection section = questSection(quest);
        if (section == null) return null;
        String value = section.getString("chain_after");
        if (value == null || value.isBlank()) return null;
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean chainMatches(AbstractQuest candidate, AbstractQuest completed) {
        String after = chainAfter(candidate);
        if (after == null) return false;
        String qualified = qualifiedId(completed).toLowerCase(Locale.ROOT);
        String local = completed.getFileIndex().toLowerCase(Locale.ROOT);
        return after.equals(qualified) || (after.equals(local)
                && normalizeCategory(candidate.getCategoryName()).equals(normalizeCategory(completed.getCategoryName())));
    }

    public static int scaleRequiredAmount(AbstractQuest quest, int baseAmount) {
        ConfigurationSection section = questSection(quest);
        if (section == null) return baseAmount;
        double multiplier = Math.max(0.01D, section.getDouble("difficulty.required_multiplier", 1.0D));
        return Math.max(1, (int) Math.round(baseAmount * multiplier));
    }

    public static double scaleRewardAmount(AbstractQuest quest, double baseAmount) {
        ConfigurationSection section = questSection(quest);
        if (section == null) return baseAmount;
        double multiplier = Math.max(0.0D, section.getDouble("difficulty.reward_multiplier", 1.0D));
        return baseAmount * multiplier;
    }

    public static List<String> slimefunIds(AbstractQuest quest) {
        ConfigurationSection section = questSection(quest);
        if (section == null) return Collections.emptyList();
        return section.getStringList("slimefun_ids").stream()
                .filter(id -> id != null && !id.isBlank())
                .map(id -> id.trim().toUpperCase(Locale.ROOT))
                .toList();
    }

    public static boolean isPoolAllowed(Player player, AbstractQuest quest) {
        String pool = pool(quest);
        ConfigurationSection poolSection = mainConfig().getConfigurationSection("quest_pools." + pool);
        if (poolSection == null) return true;
        if (!poolSection.getBoolean("enabled", true)) return false;
        String permission = poolSection.getString("permission", "");
        return permission == null || permission.isBlank() || player.hasPermission(permission);
    }

    public static boolean isWeeklyCategory(String category) {
        String normalized = normalizeCategory(category);
        return mainConfig().getStringList("weekly_categories").stream()
                .map(QuestFeatures::normalizeCategory)
                .anyMatch(normalized::equals);
    }

    /**
     * Weekly categories are preserved while the old assignment timestamp and current time
     * belong to the same ISO week in the configured renewal time zone.
     */
    public static boolean shouldPreserveWeekly(long previousTimestamp) {
        try {
            RenewSchedule.Settings settings = RenewSchedule.settings();
            if (!RenewSchedule.isValid(settings)) return false;
            ZonedDateTime oldTime = Instant.ofEpochMilli(previousTimestamp).atZone(settings.zone());
            ZonedDateTime now = ZonedDateTime.now(settings.zone());
            WeekFields wf = WeekFields.ISO;
            return oldTime.get(wf.weekBasedYear()) == now.get(wf.weekBasedYear())
                    && oldTime.get(wf.weekOfWeekBasedYear()) == now.get(wf.weekOfWeekBasedYear());
        } catch (RuntimeException exception) {
            PluginLogger.warn("Unable to evaluate weekly quest preservation: " + exception.getMessage());
            return false;
        }
    }
}
