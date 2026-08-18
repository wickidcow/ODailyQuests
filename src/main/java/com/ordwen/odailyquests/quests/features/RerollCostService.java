package com.ordwen.odailyquests.quests.features;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.externs.hooks.eco.VaultHook;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Locale;

/** Optional reroll charging. Missing configuration means rerolls remain free. */
public final class RerollCostService {

    public record Payment(boolean charged, String type, double amount) {
        public static Payment free() {
            return new Payment(false, "NONE", 0.0D);
        }
    }

    private RerollCostService() {}

    private static YamlConfiguration config() {
        return YamlConfiguration.loadConfiguration(new File(ODailyQuests.INSTANCE.getDataFolder(), "config.yml"));
    }

    public static Payment tryCharge(Player player) {
        YamlConfiguration cfg = config();
        if (!cfg.getBoolean("reroll_cost.enabled", false) || player.hasPermission("odailyquests.reroll.free")) {
            return Payment.free();
        }

        String type = cfg.getString("reroll_cost.type", "NONE");
        type = type == null ? "NONE" : type.trim().toUpperCase(Locale.ROOT);
        double amount = Math.max(0.0D, cfg.getDouble("reroll_cost.amount", 0.0D));
        if (amount <= 0.0D || type.equals("NONE")) return Payment.free();

        switch (type) {
            case "MONEY" -> {
                if (VaultHook.getEconomy() == null) {
                    player.sendMessage(ChatColor.RED + "Reroll cost is set to MONEY, but Vault economy is unavailable.");
                    return null;
                }
                if (!VaultHook.getEconomy().has(player, amount)) {
                    sendInsufficient(player, cfg, amount, type);
                    return null;
                }
                if (!VaultHook.getEconomy().withdrawPlayer(player, amount).transactionSuccess()) {
                    player.sendMessage(ChatColor.RED + "Unable to charge the reroll cost. Please try again.");
                    return null;
                }
            }
            case "EXP_LEVELS" -> {
                int levels = (int) Math.ceil(amount);
                if (player.getLevel() < levels) {
                    sendInsufficient(player, cfg, levels, type);
                    return null;
                }
                player.giveExpLevels(-levels);
                amount = levels;
            }
            default -> {
                player.sendMessage(ChatColor.RED + "Unknown reroll cost type: " + type + ".");
                return null;
            }
        }

        return new Payment(true, type, amount);
    }

    public static void refund(Player player, Payment payment) {
        if (payment == null || !payment.charged()) return;
        switch (payment.type()) {
            case "MONEY" -> {
                if (VaultHook.getEconomy() != null) VaultHook.getEconomy().depositPlayer(player, payment.amount());
            }
            case "EXP_LEVELS" -> player.giveExpLevels((int) Math.round(payment.amount()));
            default -> { }
        }
    }

    public static void sendChargedMessage(Player player, Payment payment) {
        if (payment == null || !payment.charged()) return;
        YamlConfiguration cfg = config();
        String message = cfg.getString("reroll_cost.charged_message", "&eReroll cost: &6%amount% %type%&e.");
        if (message != null && !message.isBlank()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message
                    .replace("%amount%", format(payment.amount()))
                    .replace("%type%", payment.type())));
        }
    }

    private static void sendInsufficient(Player player, YamlConfiguration cfg, double amount, String type) {
        String message = cfg.getString("reroll_cost.insufficient_message", "&cYou need %amount% %type% to reroll a quest.");
        if (message != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message
                    .replace("%amount%", format(amount))
                    .replace("%type%", type)));
        }
    }

    private static String format(double amount) {
        if (amount == Math.rint(amount)) return Long.toString(Math.round(amount));
        return String.format(Locale.ROOT, "%.2f", amount);
    }
}
