package com.ordwen.odailyquests.quests.features;

import com.ordwen.odailyquests.configuration.essentials.RerollMaximum;
import com.ordwen.odailyquests.enums.QuestsMessages;
import com.ordwen.odailyquests.enums.QuestsPermissions;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/** Shared player reroll logic used by commands and the reroll GUI. */
public final class RerollService {

    private RerollService() {}

    public static boolean canRerollToday(Player player, boolean sendMessage) {
        final PlayerQuests playerQuests = QuestsManager.getActiveQuests().get(player.getName());
        if (playerQuests == null) return false;
        if (player.hasPermission(QuestsPermissions.QUESTS_PLAYER_BYPASS_REROLL_LIMIT.get())) return true;

        final int max = RerollMaximum.getMaxRerolls();
        final boolean allowed = max <= 0 || playerQuests.getRecentlyRolled() < max;
        if (!allowed && sendMessage) {
            final String msg = QuestsMessages.CANNOT_REROLL_IF_MAX.toString();
            if (msg != null) player.sendMessage(msg);
        }
        return allowed;
    }

    public static boolean rerollOne(Player player, int oneBasedIndex) {
        final PlayerQuests playerQuests = QuestsManager.getActiveQuests().get(player.getName());
        if (playerQuests == null || oneBasedIndex < 1 || oneBasedIndex > playerQuests.getQuests().size()) {
            final String msg = QuestsMessages.INVALID_QUEST_INDEX.toString();
            if (msg != null) player.sendMessage(msg);
            return false;
        }
        if (!canRerollToday(player, true)) return false;

        final boolean bypass = player.hasPermission(QuestsPermissions.QUESTS_PLAYER_BYPASS_REROLL_LIMIT.get());
        final int before = playerQuests.getRecentlyRolled();
        final RerollCostService.Payment payment = bypass ? RerollCostService.Payment.free() : RerollCostService.tryCharge(player);
        if (payment == null) return false;

        if (!playerQuests.rerollQuest(oneBasedIndex - 1, player, bypass)) {
            RerollCostService.refund(player, payment);
            return false;
        }

        RerollCostService.sendChargedMessage(player, payment);
        final String msg = QuestsMessages.QUEST_REROLLED.toString();
        if (msg != null) {
            final int max = RerollMaximum.getMaxRerolls();
            final int remaining = max <= 0 ? -1 : Math.max(0, max - (before + 1));
            player.sendMessage(msg.replace("%index%", String.valueOf(oneBasedIndex))
                    .replace("%remaining%", String.valueOf(remaining)));
        }
        return true;
    }

    public static boolean rerollAll(Player player) {
        final PlayerQuests playerQuests = QuestsManager.getActiveQuests().get(player.getName());
        if (playerQuests == null || playerQuests.getQuests().isEmpty()) return false;
        if (!canRerollToday(player, true)) return false;

        final boolean bypass = player.hasPermission(QuestsPermissions.QUESTS_PLAYER_BYPASS_REROLL_LIMIT.get());
        final RerollCostService.Payment payment = bypass ? RerollCostService.Payment.free() : RerollCostService.tryCharge(player);
        if (payment == null) return false;

        if (!playerQuests.rerollAll(player, bypass)) {
            RerollCostService.refund(player, payment);
            return false;
        }

        RerollCostService.sendChargedMessage(player, payment);
        player.sendMessage(ChatColor.AQUA + "All daily quests have been rerolled. "
                + ChatColor.GRAY + "Your daily reroll has been used.");
        return true;
    }
}
