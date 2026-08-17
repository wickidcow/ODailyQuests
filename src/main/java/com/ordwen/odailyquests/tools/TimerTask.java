package com.ordwen.odailyquests.tools;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.api.ODailyQuestsAPI;
import com.ordwen.odailyquests.enums.QuestsMessages;
import com.ordwen.odailyquests.quests.features.StreakService;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import com.ordwen.odailyquests.quests.player.progression.QuestLoaderUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wall-clock scheduler for global quest renewals.
 *
 * <p>The executor is intentionally used only for waiting. All Bukkit/player work is handed
 * back to MorePaperLib so Paper and Folia execute it on an appropriate server scheduler.</p>
 */
public class TimerTask {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "ODailyQuests-RenewClock");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private ScheduledFuture<?> scheduledTask;

    public TimerTask(LocalDateTime start) {
        scheduleNextExecution(start);
    }

    private synchronized void scheduleNextExecution(LocalDateTime start) {
        if (stopped.get() || scheduler.isShutdown()) return;

        final RenewSchedule.Settings settings = RenewSchedule.settings();
        if (!RenewSchedule.isValid(settings)) {
            PluginLogger.error("Invalid renew schedule. Task not scheduled.");
            return;
        }

        final ZonedDateTime now = start.atZone(ZoneId.systemDefault()).withZoneSameInstant(settings.zone());
        final ZonedDateTime next = RenewSchedule.nextExecutionAtOrAfter(now, settings);
        long initialDelayNanos = Math.max(0L, Duration.between(
                ZonedDateTime.now(ZoneId.systemDefault()),
                next.withZoneSameInstant(ZoneId.systemDefault())
        ).toNanos());

        scheduledTask = scheduler.schedule(this::dispatchRenewal, initialDelayNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Called by the private clock thread. It never directly touches Bukkit objects.
     */
    private void dispatchRenewal() {
        if (stopped.get() || !ODailyQuests.INSTANCE.isEnabled()) return;

        ODailyQuests.morePaperLib.scheduling().globalRegionalScheduler().run(() -> {
            if (stopped.get() || !ODailyQuests.INSTANCE.isEnabled()) return;

            PluginLogger.info("It's a new quest period. Online player quests are being reloaded.");
            final List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            for (Player player : players) {
                ODailyQuests.morePaperLib.scheduling().entitySpecificScheduler(player).run(
                        () -> renewPlayer(player),
                        () -> PluginLogger.fine("Skipped quest renewal for an unavailable player entity.")
                );
            }
        });

        scheduleNextExecution(LocalDateTime.now());
    }

    private void renewPlayer(Player player) {
        if (!player.isOnline()) return;

        final String playerName = player.getName();
        final PlayerQuests playerQuests = ODailyQuestsAPI.getPlayerQuests(playerName);
        if (playerQuests == null) {
            PluginLogger.warn("Cannot renew quests for " + playerName + ": progression is not loaded.");
            return;
        }

        final String msg = QuestsMessages.NEW_DAY.toString();
        if (msg != null) player.sendMessage(msg);

        StreakService.recordCompletedPeriod(player, playerQuests);

        final int totalAchievedQuests = playerQuests.getTotalAchievedQuests();
        final Map<String, Integer> totalAchievedQuestsByCategory = playerQuests.getTotalAchievedQuestsByCategory();
        QuestLoaderUtils.loadNewPlayerQuests(
                playerName,
                QuestsManager.getActiveQuests(),
                totalAchievedQuestsByCategory,
                totalAchievedQuests
        );
    }

    public void reload() {
        cancel();
        scheduleNextExecution(LocalDateTime.now());
    }

    private synchronized void cancel() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
    }

    public void stop() {
        stopped.set(true);
        cancel();
        scheduler.shutdownNow();
    }
}
