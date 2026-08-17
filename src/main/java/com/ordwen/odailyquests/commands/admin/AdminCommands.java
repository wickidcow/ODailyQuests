package com.ordwen.odailyquests.commands.admin;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.api.commands.admin.AdminCommand;
import com.ordwen.odailyquests.api.commands.admin.AdminCommandRegistry;
import com.ordwen.odailyquests.enums.QuestsMessages;
import com.ordwen.odailyquests.enums.QuestsPermissions;
import com.ordwen.odailyquests.tools.DoctorReport;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AdminCommands implements CommandExecutor {

    private final ODailyQuests plugin;
    private final AdminCommandRegistry adminCommandRegistry;

    public AdminCommands(ODailyQuests plugin, AdminCommandRegistry adminCommandRegistry) {
        this.plugin = plugin;
        this.adminCommandRegistry = adminCommandRegistry;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(QuestsPermissions.QUESTS_ADMIN.get())) {
            noPermission(sender);
            return true;
        }
        if (args.length == 0) {
            help(sender);
            return true;
        }
        if (args.length == 1) return handleSingleArg(sender, args[0]);
        return handleRegisteredSubcommand(sender, args);
    }

    private boolean handleSingleArg(CommandSender sender, String subcommand) {
        if ("reload".equalsIgnoreCase(subcommand)) {
            plugin.getReloadService().reload();
            sendMessage(sender, QuestsMessages.PLUGIN_RELOADED);
            return true;
        }
        if ("doctor".equalsIgnoreCase(subcommand)) {
            DoctorReport.send(plugin, sender);
            return true;
        }
        help(sender);
        return true;
    }

    private boolean handleRegisteredSubcommand(CommandSender sender, String[] args) {
        final AdminCommand handler = adminCommandRegistry.getCommandHandler(args[0]);
        if (handler == null) {
            help(sender);
            return true;
        }
        if (!sender.hasPermission(handler.getPermission())) {
            noPermission(sender);
            return true;
        }
        handler.execute(sender, args);
        return true;
    }

    private void help(CommandSender sender) {
        sendMessage(sender, QuestsMessages.ADMIN_HELP);
    }

    private void noPermission(CommandSender sender) {
        sendMessage(sender, QuestsMessages.NO_PERMISSION);
    }

    private void sendMessage(CommandSender sender, QuestsMessages message) {
        final String msg = message.toString();
        if (msg != null) sender.sendMessage(msg);
    }
}
