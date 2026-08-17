package com.ordwen.odailyquests.commands.player.handlers;

import com.ordwen.odailyquests.api.commands.player.PlayerCommandBase;
import com.ordwen.odailyquests.configuration.essentials.RerollMaximum;
import com.ordwen.odailyquests.enums.QuestsMessages;
import com.ordwen.odailyquests.enums.QuestsPermissions;
import com.ordwen.odailyquests.quests.features.RerollCostService;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PRerollCommand extends PlayerCommandBase {

    @Override
    public String getName() {
        return "reroll";
    }

    @Override
    public String getPermission() {
        return QuestsPermissions.QUESTS_PLAYER_REROLL.get();
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 2) {
            help(player);
            return;
        }

        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            help(player);
            return;
        }
        reroll(player, index);
    }

    private void reroll(Player player, int index) {
        final String playerName = player.getName();
        final Map<String, PlayerQuests> activeQuests = QuestsManager.getActiveQuests();
        final PlayerQuests playerQuests = activeQuests.get(playerName);
        if (playerQuests == null) {
            invalidQuest(player);
            return;
        }

        if (index < 1 || index > playerQuests.getQuests().size()) {
            invalidQuest(player);
            return;
        }

        final int previousRerolls = playerQuests.getRecentlyRolled();
        final boolean bypass = player.hasPermission(QuestsPermissions.QUESTS_PLAYER_BYPASS_REROLL_LIMIT.get());
        final RerollCostService.Payment payment = bypass ? RerollCostService.Payment.free() : RerollCostService.tryCharge(player);
        if (payment == null) return;

        if (playerQuests.rerollQuest(index - 1, player, bypass)) {
            RerollCostService.sendChargedMessage(player, payment);
            rerollConfirm(index, RerollMaximum.getMaxRerolls() - (previousRerolls + 1), player);
        } else {
            // A failed selection must never consume the player's money/levels.
            RerollCostService.refund(player, payment);
        }
    }

    private void rerollConfirm(int index, int remaining, Player target) {
        final String msg = QuestsMessages.QUEST_REROLLED.toString();
        if (msg != null) {
            target.sendMessage(msg.replace("%index%", String.valueOf(index))
                    .replace("%remaining%", String.valueOf(remaining)));
        }
    }

    protected void invalidQuest(Player player) {
        final String msg = QuestsMessages.INVALID_QUEST_INDEX.toString();
        if (msg != null) player.sendMessage(msg);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, String[] args) {
        if (args.length == 2 && sender instanceof Player player) {
            final PlayerQuests playerQuests = QuestsManager.getActiveQuests().get(player.getName());
            if (playerQuests == null) return Collections.emptyList();

            List<String> questNumbers = new ArrayList<>();
            for (int i = 1; i <= playerQuests.getQuests().size(); i++) questNumbers.add(String.valueOf(i));
            return questNumbers;
        }
        return Collections.emptyList();
    }
}
