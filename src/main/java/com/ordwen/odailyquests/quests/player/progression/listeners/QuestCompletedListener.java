package com.ordwen.odailyquests.quests.player.progression.listeners;

import com.ordwen.odailyquests.api.ODailyQuestsAPI;
import com.ordwen.odailyquests.api.events.QuestCompletedEvent;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.quests.features.CommunityQuestService;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.rewards.RewardManager;
import com.ordwen.odailyquests.tools.PluginLogger;
import com.ordwen.odailyquests.tools.QuestPlaceholders;
import com.ordwen.odailyquests.tools.TextFormatter;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashSet;

public class QuestCompletedListener implements Listener {
    @EventHandler
    public void onQuestCompletedEvent(QuestCompletedEvent event) {
        final var player = event.getPlayer();
        final var progression = event.getProgression();
        final var quest = event.getAbstractQuest();
        Debugger.write("QuestCompletedListener: QuestCompletedEvent summoned by " + player.getName() + " for " + quest.getQuestName() + ".");
        if (progression.isAchieved()) return;

        final PlayerQuests playerQuests = ODailyQuestsAPI.getPlayerQuests(player.getName());
        if (playerQuests == null) return;
        final ArrayList<AbstractQuest> ordered = new ArrayList<>(playerQuests.getQuests().keySet());
        final int questIndex = ordered.indexOf(quest);
        final AbstractQuest successor = QuestsManager.findChainSuccessor(
                quest, player, new HashSet<>(playerQuests.getQuests().keySet()));

        progression.setAchieved();
        final String formattedQuestName = QuestPlaceholders.replaceQuestPlaceholders(
                TextFormatter.format(player, quest.getQuestName()), player, quest, progression, playerQuests, null);
        RewardManager.sendQuestRewardItems(formattedQuestName, player, quest.getReward(), progression);
        CommunityQuestService.onQuestCompleted(player, quest);

        if (successor != null && questIndex >= 0) {
            playerQuests.increaseCategoryAchievedQuests(quest.getCategoryName(), player, false);
            PlayerQuests.ReplaceResult result = playerQuests.setQuestAtIndex(questIndex, successor);
            if (result == PlayerQuests.ReplaceResult.SUCCESS) {
                player.sendMessage(ChatColor.GREEN + "Quest chain advanced: " + TextFormatter.format(player, successor.getQuestName()));
                return;
            }
            PluginLogger.warn("Unable to advance quest chain after " + quest.getFileIndex() + ": " + result
                    + ". Completion was recorded without final-set rewards.");
            return;
        }

        playerQuests.increaseCategoryAchievedQuests(quest.getCategoryName(), player);
    }
}
