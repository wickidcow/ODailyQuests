package com.ordwen.odailyquests.quests.player.progression;

import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.configuration.essentials.Logs;
import com.ordwen.odailyquests.configuration.essentials.QuestsPerCategory;
import com.ordwen.odailyquests.configuration.essentials.RenewInterval;
import com.ordwen.odailyquests.configuration.essentials.TimestampMode;
import com.ordwen.odailyquests.enums.QuestsMessages;
import com.ordwen.odailyquests.enums.QuestsPermissions;
import com.ordwen.odailyquests.quests.categories.CategoriesLoader;
import com.ordwen.odailyquests.quests.categories.Category;
import com.ordwen.odailyquests.quests.features.QuestFeatures;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.tools.PluginLogger;
import com.ordwen.odailyquests.tools.RenewSchedule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

public class QuestLoaderUtils {

    private QuestLoaderUtils() {}

    public static boolean checkTimestamp(long timestamp) {
        final int mode = TimestampMode.getTimestampMode();
        final Duration renewInterval = RenewInterval.getRenewInterval();

        switch (mode) {
            case 1 -> {
                final RenewSchedule.Settings settings = RenewSchedule.settings();
                if (!RenewSchedule.isValid(settings)) {
                    PluginLogger.error(ChatColor.RED + "Renew schedule is invalid.");
                    return false;
                }

                final ZonedDateTime lastRenew = Instant.ofEpochMilli(timestamp).atZone(settings.zone());
                final ZonedDateTime now = ZonedDateTime.now(settings.zone());
                return RenewSchedule.shouldRenewSince(lastRenew, now, settings);
            }
            case 2 -> {
                if (renewInterval != null) {
                    return System.currentTimeMillis() - timestamp >= renewInterval.toMillis();
                }
                PluginLogger.error(ChatColor.RED + "Impossible to check player quests timestamp. Renew interval is incorrect.");
            }
            default -> PluginLogger.error(ChatColor.RED + "Impossible to load player quests timestamp. The selected mode is incorrect.");
        }
        return false;
    }

    /**
     * Draws a new period while keeping configured weekly-category assignments during the
     * same ISO week. Historical completion totals are always retained.
     */
    public static void loadNewPlayerQuests(
            String playerName,
            Map<String, PlayerQuests> activeQuests,
            Map<String, Integer> totalAchievedQuestsByCategory,
            int totalAchievedQuests
    ) {
        Debugger.write("Entering loadNewPlayerQuests method for player " + playerName + ".");

        final Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            Debugger.write("Player " + playerName + " is null. Impossible to renew quests.");
            PluginLogger.warn("It seems that " + playerName + " disconnected before the end of the quest renewal.");
            return;
        }

        final PlayerQuests previous = activeQuests.get(playerName);
        final Map<AbstractQuest, Progression> preservedWeekly = new LinkedHashMap<>();
        if (previous != null && QuestFeatures.shouldPreserveWeekly(previous.getTimestamp())) {
            for (Map.Entry<AbstractQuest, Progression> entry : previous.getQuests().entrySet()) {
                if (QuestFeatures.isWeeklyCategory(entry.getKey().getCategoryName())) {
                    preservedWeekly.put(entry.getKey(), entry.getValue());
                }
            }
        }

        final Map<AbstractQuest, Progression> quests = QuestsManager.selectRandomQuests(player, preservedWeekly);
        final long timestamp = TimestampMode.getTimestampMode() == 1
                ? Calendar.getInstance().getTimeInMillis()
                : System.currentTimeMillis();
        final PlayerQuests playerQuests = new PlayerQuests(timestamp, quests);

        playerQuests.setTotalAchievedQuests(totalAchievedQuests);
        playerQuests.setTotalAchievedQuestsByCategory(totalAchievedQuestsByCategory);
        playerQuests.setRecentRerolls(0);

        activeQuests.put(playerName, playerQuests);

        final String msg = QuestsMessages.QUESTS_RENEWED.getMessage(player);
        if (msg != null && player.hasPermission(QuestsPermissions.QUESTS_PROGRESS.get())) {
            player.sendMessage(msg);
        }
        if (Logs.isEnabled()) PluginLogger.info(playerName + "'s quests have been renewed.");
        Debugger.write("Quests of player " + playerName + " have been renewed.");
    }

    public static boolean isTimeToRenew(Player player, Map<String, PlayerQuests> activeQuests) {
        if (TimestampMode.getTimestampMode() == 1) return false;
        final PlayerQuests playerQuests = activeQuests.get(player.getName());
        if (playerQuests == null) return false;

        if (checkTimestamp(playerQuests.getTimestamp())) {
            loadNewPlayerQuests(
                    player.getName(),
                    activeQuests,
                    playerQuests.getTotalAchievedQuestsByCategory(),
                    playerQuests.getTotalAchievedQuests()
            );
            return true;
        }
        return false;
    }

    public static AbstractQuest findQuest(String playerName, int questIndex, int id) {
        AbstractQuest quest = null;
        final Map<String, Category> categoryMap = CategoriesLoader.getAllCategories();
        int totalQuestsCount = 0;

        for (Map.Entry<String, Category> entry : categoryMap.entrySet()) {
            String categoryName = entry.getKey();
            Category category = entry.getValue();
            int categoryQuestsAmount = QuestsPerCategory.getAmountForCategory(categoryName);

            if (id <= totalQuestsCount + categoryQuestsAmount) {
                quest = getQuestAtIndex(category, questIndex, playerName);
                break;
            }
            totalQuestsCount += categoryQuestsAmount;
        }

        if (quest == null) {
            PluginLogger.warn("Quest ID " + id + " was not found. Player quests will be reset.");
            PluginLogger.warn("This can happen after a server reload or if the quest was deleted from the file.");
        }
        return quest;
    }

    public static AbstractQuest findQuest(String playerName, String categoryName, int questIndex, int id) {
        if (categoryName != null && !categoryName.isEmpty()) {
            final Category category = CategoriesLoader.getCategoryByName(categoryName);
            if (category == null) {
                PluginLogger.warn("Category '" + categoryName + "' referenced in player " + playerName
                        + " data no longer exists. New quests will be drawn for the player.");
                return null;
            }
            return getQuestAtIndex(category, questIndex, playerName);
        }
        return findQuest(playerName, questIndex, id);
    }

    public static AbstractQuest getQuestAtIndex(Category category, int index, String playerName) {
        try {
            return category.get(index);
        } catch (IndexOutOfBoundsException exception) {
            if (!category.isEmpty()) playerQuestMissing(playerName);
            else noQuestsAvailable();
            return null;
        }
    }

    private static void playerQuestMissing(String playerName) {
        PluginLogger.warn("A quest of the player " + playerName + " could not be loaded.");
        PluginLogger.warn("This happens when a previously loaded quest has been deleted from the file.");
        PluginLogger.warn("To avoid this problem, you should reset player progressions when you delete quests.");
        PluginLogger.warn("New quests will be drawn for the player.");
    }

    private static void noQuestsAvailable() {
        PluginLogger.error("There is no quest at all available!");
        PluginLogger.error("It can happen if IA/Oraxen/Nexo integration is enabled without the corresponding plugin.");
        PluginLogger.error("Please check your configuration. If the problem persists, contact the developer.");
    }
}
