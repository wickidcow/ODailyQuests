package com.ordwen.odailyquests.tools;

import com.ordwen.odailyquests.configuration.functionalities.progression.ProgressBar;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class QuestPlaceholders {
    private static final String PROGRESS = "%progress%";
    private static final String LEGACY_PROGRESSION = "%progression%";
    private static final String PROGRESS_BAR = "%progressBar%";
    private static final String PROGRESS_PERCENT = "%progressPercent%";
    private static final String REQUIRED = "%required%";
    private static final String DISPLAY_NAME = "%displayName%";
    private static final String ACHIEVED = "%achieved%";
    private static final String TOTAL_QUESTS = "%totalQuests%";
    private static final String DRAW_IN = "%drawIn%";
    private static final String STATUS = "%status%";
    private static final String REWARD_AMOUNT = "%rewardAmount%";

    private QuestPlaceholders() {}

    public static String replaceProgressPlaceholders(String input, int progress, int required, double rewardAmount) {
        if (input == null) return null;
        int percent = required <= 0 ? 0 : Math.min(100, Math.max(0, (int) Math.round(progress * 100.0D / required)));
        return input.replace(REWARD_AMOUNT, String.valueOf(rewardAmount))
                .replace(PROGRESS, String.valueOf(progress))
                // Backward compatibility with older ODailyQuests configurations.
                .replace(LEGACY_PROGRESSION, String.valueOf(progress))
                .replace(REQUIRED, String.valueOf(required))
                .replace(PROGRESS_PERCENT, String.valueOf(percent))
                .replace(PROGRESS_BAR, ProgressBar.getProgressBar(progress, required));
    }

    public static String replaceQuestPlaceholders(String input, @Nullable Player player, @Nullable AbstractQuest quest,
            @Nullable Progression progression, @Nullable PlayerQuests playerQuests, @Nullable String status) {
        if (input == null) return null;
        String result = input;
        if (progression != null) {
            result = replaceProgressPlaceholders(result, progression.getAdvancement(), progression.getRequiredAmount(), progression.getRewardAmount());
        }
        if (quest != null && progression != null) {
            result = result.replace(DISPLAY_NAME, DisplayName.getDisplayName(quest, progression.getSelectedRequiredIndex()));
        }
        if (playerQuests != null) {
            result = result.replace(ACHIEVED, String.valueOf(playerQuests.getAchievedQuests()))
                    .replace(TOTAL_QUESTS, String.valueOf(playerQuests.getQuests().size()));
        }
        if (player != null) result = result.replace(DRAW_IN, TimeRemain.timeRemain(player.getName()));
        if (status != null) result = result.replace(STATUS, status);
        return result;
    }
}
