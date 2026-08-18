package com.ordwen.odailyquests.quests.features;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.tools.PluginLogger;
import com.ordwen.odailyquests.tools.RenewSchedule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Optional consecutive-completion streaks stored outside the main progression schema. */
public final class StreakService {

    private record State(int streak, String lastPeriod) {}

    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "ODailyQuests-StreakIO");
        thread.setDaemon(true);
        return thread;
    });
    private static volatile boolean loaded;

    private StreakService() {}

    private static File stateFile() {
        return new File(ODailyQuests.INSTANCE.getDataFolder(), "streaks.yml");
    }

    private static YamlConfiguration config() {
        return YamlConfiguration.loadConfiguration(new File(ODailyQuests.INSTANCE.getDataFolder(), "config.yml"));
    }

    private static synchronized void ensureLoaded() {
        if (loaded) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(stateFile());
        ConfigurationSection players = yaml.getConfigurationSection("players");
        if (players != null) {
            for (String rawUuid : players.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(rawUuid);
                    STATES.put(uuid, new State(
                            players.getInt(rawUuid + ".streak", 0),
                            players.getString(rawUuid + ".last_period", "")
                    ));
                } catch (IllegalArgumentException ignored) {
                    PluginLogger.warn("Ignoring invalid UUID in streaks.yml: " + rawUuid);
                }
            }
        }
        loaded = true;
    }

    public static void recordCompletedPeriod(Player player, PlayerQuests playerQuests) {
        YamlConfiguration cfg = config();
        if (!cfg.getBoolean("streak_rewards.enabled", false)) return;
        if (playerQuests == null || playerQuests.getQuests().isEmpty()) return;

        ensureLoaded();
        RenewSchedule.Settings settings = RenewSchedule.settings();
        ZoneId zone = RenewSchedule.isValid(settings) ? settings.zone() : ZoneId.systemDefault();
        String period = Instant.ofEpochMilli(playerQuests.getTimestamp()).atZone(zone).toLocalDate().toString();

        UUID uuid = player.getUniqueId();
        State old = STATES.getOrDefault(uuid, new State(0, ""));
        if (period.equals(old.lastPeriod())) return;

        boolean completed = playerQuests.getAchievedQuests() >= playerQuests.getQuests().size();
        int nextStreak = completed ? old.streak() + 1 : 0;
        STATES.put(uuid, new State(nextStreak, period));
        saveAsync();

        if (completed && nextStreak > 0) giveMilestoneReward(player, nextStreak, cfg);
    }

    public static int getStreak(UUID uuid) {
        ensureLoaded();
        return STATES.getOrDefault(uuid, new State(0, "")).streak();
    }

    private static void giveMilestoneReward(Player player, int streak, YamlConfiguration cfg) {
        for (String raw : cfg.getStringList("streak_rewards.milestones." + streak + ".commands")) {
            if (raw == null || raw.isBlank()) continue;
            String command = raw.replace("%player%", player.getName()).replace("%streak%", String.valueOf(streak));
            ODailyQuests.morePaperLib.scheduling().globalRegionalScheduler().run(() ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }
    }

    private static void saveAsync() {
        Map<UUID, State> snapshot = Map.copyOf(STATES);
        IO.execute(() -> {
            YamlConfiguration yaml = new YamlConfiguration();
            for (Map.Entry<UUID, State> entry : snapshot.entrySet()) {
                String base = "players." + entry.getKey();
                yaml.set(base + ".streak", entry.getValue().streak());
                yaml.set(base + ".last_period", entry.getValue().lastPeriod());
            }
            try {
                yaml.save(stateFile());
            } catch (IOException exception) {
                PluginLogger.error("Unable to save streaks.yml: " + exception.getMessage());
            }
        });
    }
}
