package com.ordwen.odailyquests.quests.features;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.tools.PluginLogger;
import com.ordwen.odailyquests.tools.RenewSchedule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Optional server-wide daily/weekly completion goals. */
public final class CommunityQuestService {
    private record GoalState(String period, int progress, boolean completed) {}

    private static final Map<String, GoalState> STATES = new ConcurrentHashMap<>();
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "ODailyQuests-CommunityIO");
        thread.setDaemon(true);
        return thread;
    });
    private static volatile boolean loaded;

    private CommunityQuestService() {}

    private static File stateFile() {
        return new File(ODailyQuests.INSTANCE.getDataFolder(), "community-progress.yml");
    }

    private static YamlConfiguration config() {
        return YamlConfiguration.loadConfiguration(new File(ODailyQuests.INSTANCE.getDataFolder(), "config.yml"));
    }

    private static synchronized void ensureLoaded() {
        if (loaded) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(stateFile());
        ConfigurationSection goals = yaml.getConfigurationSection("goals");
        if (goals != null) {
            for (String id : goals.getKeys(false)) {
                STATES.put(id, new GoalState(
                        goals.getString(id + ".period", ""),
                        goals.getInt(id + ".progress", 0),
                        goals.getBoolean(id + ".completed", false)
                ));
            }
        }
        loaded = true;
    }

    public static synchronized void onQuestCompleted(Player player, AbstractQuest quest) {
        YamlConfiguration cfg = config();
        if (!cfg.getBoolean("community_quests.enabled", false)) return;
        ConfigurationSection goals = cfg.getConfigurationSection("community_quests.goals");
        if (goals == null) return;
        ensureLoaded();

        boolean changed = false;
        for (String id : goals.getKeys(false)) {
            ConfigurationSection goal = goals.getConfigurationSection(id);
            if (goal == null || !goal.getBoolean("enabled", true)) continue;

            String category = goal.getString("category", "*");
            if (category != null && !category.equals("*") && !category.equalsIgnoreCase(quest.getCategoryName())) continue;
            String questType = goal.getString("quest_type", "*");
            if (questType != null && !questType.equals("*") && !questType.equalsIgnoreCase(quest.getQuestType())) continue;

            int target = Math.max(1, goal.getInt("target", 1));
            String period = periodKey(goal.getString("period", "DAILY"));
            GoalState state = STATES.getOrDefault(id, new GoalState(period, 0, false));
            if (!period.equals(state.period())) state = new GoalState(period, 0, false);
            if (state.completed()) {
                STATES.put(id, state);
                continue;
            }

            int progress = Math.min(target, state.progress() + 1);
            boolean completed = progress >= target;
            STATES.put(id, new GoalState(period, progress, completed));
            changed = true;
            if (completed) giveReward(player, id, goal, progress, target);
        }
        if (changed) saveAsync();
    }

    public static int getProgress(String goalId) {
        ensureLoaded();
        GoalState state = STATES.get(goalId);
        return state == null ? 0 : state.progress();
    }

    private static String periodKey(String rawPeriod) {
        RenewSchedule.Settings settings = RenewSchedule.settings();
        ZoneId zone = RenewSchedule.isValid(settings) ? settings.zone() : ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zone);
        if ("WEEKLY".equalsIgnoreCase(rawPeriod)) {
            WeekFields wf = WeekFields.ISO;
            return now.get(wf.weekBasedYear()) + "-W" + now.get(wf.weekOfWeekBasedYear());
        }
        return now.toLocalDate().toString();
    }

    private static void giveReward(Player trigger, String goalId, ConfigurationSection goal, int progress, int target) {
        for (String raw : goal.getStringList("commands")) {
            if (raw == null || raw.isBlank()) continue;
            String command = raw.replace("%player%", trigger.getName())
                    .replace("%goal%", goalId)
                    .replace("%progress%", String.valueOf(progress))
                    .replace("%target%", String.valueOf(target));
            ODailyQuests.morePaperLib.scheduling().globalRegionalScheduler().run(() ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }
    }

    private static void saveAsync() {
        Map<String, GoalState> snapshot = Map.copyOf(STATES);
        IO.execute(() -> {
            YamlConfiguration yaml = new YamlConfiguration();
            for (Map.Entry<String, GoalState> entry : snapshot.entrySet()) {
                String base = "goals." + entry.getKey();
                yaml.set(base + ".period", entry.getValue().period());
                yaml.set(base + ".progress", entry.getValue().progress());
                yaml.set(base + ".completed", entry.getValue().completed());
            }
            try {
                yaml.save(stateFile());
            } catch (IOException exception) {
                PluginLogger.error("Unable to save community-progress.yml: " + exception.getMessage());
            }
        });
    }
}
