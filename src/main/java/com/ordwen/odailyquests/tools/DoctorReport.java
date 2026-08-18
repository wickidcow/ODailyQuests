package com.ordwen.odailyquests.tools;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.quests.categories.CategoriesLoader;
import com.ordwen.odailyquests.quests.features.DefaultQuestPacks;
import com.ordwen.odailyquests.quests.features.StreakService;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/** Human-readable runtime diagnostics for /dqadmin doctor. */
public final class DoctorReport {
    private DoctorReport() {}

    public static void send(ODailyQuests plugin, CommandSender sender) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        int questCount = CategoriesLoader.getAllCategories().values().stream().mapToInt(List::size).sum();
        RenewSchedule.Settings schedule = RenewSchedule.settings();
        String nextReset = "invalid";
        if (RenewSchedule.isValid(schedule)) {
            nextReset = formatDuration(RenewSchedule.millisUntilNext(ZonedDateTime.now(schedule.zone()), schedule));
        }

        sender.sendMessage(ChatColor.AQUA + "----- ODailyQuests Doctor -----");
        line(sender, "Plugin", plugin.getDescription().getVersion());
        line(sender, "Server", Bukkit.getName() + " / " + Bukkit.getVersion());
        line(sender, "Java", System.getProperty("java.version"));
        line(sender, "Folia runtime", Boolean.toString(isFoliaRuntime()));
        line(sender, "Storage", cfg.getString("storage_mode", "unknown"));
        line(sender, "Categories", Integer.toString(CategoriesLoader.getAllCategories().size()));
        line(sender, "Loaded category names", String.join(", ", CategoriesLoader.getAllCategories().keySet()));
        line(sender, "Loaded quests", Integer.toString(questCount));
        line(sender, "Quest types", Integer.toString(plugin.getAPI().getQuestTypeRegistry().size()));
        line(sender, "Active player data", Integer.toString(QuestsManager.getActiveQuests().size()));
        line(sender, "Next reset", nextReset);
        line(sender, "Weekly categories", String.join(", ", cfg.getStringList("weekly_categories")));
        line(sender, "Default quest packs", DefaultQuestPacks.statusSummary());
        line(sender, "Active packs", String.join(", ", DefaultQuestPacks.activePackNames()));
        line(sender, "Tech available", Boolean.toString(DefaultQuestPacks.isOptionalCategoryAvailable(DefaultQuestPacks.TECH_CATEGORY)));
        line(sender, "Wild Card available", Boolean.toString(DefaultQuestPacks.isOptionalCategoryAvailable(DefaultQuestPacks.WILDCARD_CATEGORY)));
        line(sender, "Streak rewards", Boolean.toString(cfg.getBoolean("streak_rewards.enabled", false)));
        line(sender, "Community quests", Boolean.toString(cfg.getBoolean("community_quests.enabled", false)));
        line(sender, "Reroll cost", Boolean.toString(cfg.getBoolean("reroll_cost.enabled", false)));
        line(sender, "Integrations", String.join(", ", enabledIntegrations()));
        if (sender instanceof org.bukkit.entity.Player player) {
            line(sender, "Your streak", Integer.toString(StreakService.getStreak(player.getUniqueId())));
        }
        sender.sendMessage(ChatColor.AQUA + "-------------------------------");
    }

    private static String formatDuration(long millis) {
        long seconds = Math.max(0L, millis / 1000L);
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        if (days > 0) return days + "d " + hours + "h " + minutes + "m";
        if (hours > 0) return hours + "h " + minutes + "m";
        if (minutes > 0) return minutes + "m " + secs + "s";
        return secs + "s";
    }

    private static void line(CommandSender sender, String label, String value) {
        sender.sendMessage(ChatColor.GRAY + label + ": " + ChatColor.WHITE
                + (value == null || value.isBlank() ? "none" : value));
    }

    private static boolean isFoliaRuntime() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static List<String> enabledIntegrations() {
        String[] names = {
                "Vault", "PlaceholderAPI", "Citizens", "FancyNpcs", "MythicMobs", "EliteMobs",
                "ItemsAdder", "Oraxen", "Nexo", "Towny", "Slimefun", "Pylon", "Rebar",
                "PyroFishingPro", "ValhallaMMO", "mcMMO", "MMOItems", "MMOCore",
                "EvenMoreFish", "CustomFishing"
        };
        List<String> enabled = new ArrayList<>();
        for (String name : names) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
            if (plugin != null && plugin.isEnabled()) enabled.add(name);
        }
        return enabled;
    }
}
