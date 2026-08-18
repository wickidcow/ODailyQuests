package com.ordwen.odailyquests.commands.player.handlers;

import com.ordwen.odailyquests.api.commands.player.PlayerCommandBase;
import com.ordwen.odailyquests.commands.interfaces.playerinterface.reroll.DailyRerollMenu;
import com.ordwen.odailyquests.enums.QuestsPermissions;
import com.ordwen.odailyquests.quests.features.RerollService;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        if (args.length == 1) {
            DailyRerollMenu.openChoice(player);
            return;
        }

        if (args.length != 2) {
            help(player);
            return;
        }

        if ("all".equalsIgnoreCase(args[1])) {
            RerollService.rerollAll(player);
            return;
        }

        final int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            help(player);
            return;
        }
        RerollService.rerollOne(player, index);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, String[] args) {
        if (args.length == 2 && sender instanceof Player player) {
            final PlayerQuests playerQuests = QuestsManager.getActiveQuests().get(player.getName());
            if (playerQuests == null) return Collections.emptyList();

            final List<String> choices = new ArrayList<>();
            choices.add("all");
            for (int i = 1; i <= playerQuests.getQuests().size(); i++) choices.add(String.valueOf(i));
            return choices;
        }
        return Collections.emptyList();
    }
}
