package com.ordwen.odailyquests.rewards;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.configuration.functionalities.progression.ActionBar;
import com.ordwen.odailyquests.configuration.functionalities.progression.Title;
import com.ordwen.odailyquests.configuration.functionalities.progression.ToastNotification;
import com.ordwen.odailyquests.enums.QuestsMessages;
import com.ordwen.odailyquests.externs.hooks.eco.VaultHook;
import com.ordwen.odailyquests.externs.hooks.points.PlayerPointsHook;
import com.ordwen.odailyquests.externs.hooks.points.TokenManagerHook;
import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.tools.PluginLogger;
import com.ordwen.odailyquests.tools.PluginUtils;
import com.ordwen.odailyquests.tools.TextFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles quest rewards and optional economy/points integrations.
 */
public class RewardManager {

    private RewardManager() {
    }

    private static final String REWARD_AMOUNT = "%rewardAmount%";

    public static void sendQuestRewardItems(String questName, Player player, Reward reward, Progression progression) {
        Debugger.write("RewardManager: sendAllRewardItems summoned by " + player.getName() + " for " + questName + ".");

        final String msg = QuestsMessages.QUEST_ACHIEVED.getMessage(player, Map.of("%questName%", questName));
        if (msg != null) player.sendMessage(msg);

        Title.sendTitle(player, questName);
        ToastNotification.sendToastNotification(player, questName);
        ActionBar.sendActionbar(player, questName);

        final Map<String, String> placeholders = Map.of(
                "%required%", String.valueOf(progression.getRequiredAmount()),
                "%questName%", questName
        );

        final double rewardAmount = ensureProgressionRewardAmount(reward, progression);
        sendReward(player, reward, placeholders, rewardAmount);
    }

    public static void sendReward(Player player, Reward reward, Map<String, String> placeholders) {
        sendReward(player, reward, placeholders, null);
    }

    public static void sendReward(Player player, Reward reward, Map<String, String> placeholders, Double resolvedAmount) {
        if (reward.getRewardType() == RewardType.NONE) return;

        Debugger.write("RewardManager: sendQuestReward summoned by " + player.getName() + " for " + reward.getRewardType());

        final double rewardAmount = resolvedAmount != null ? resolvedAmount : reward.resolveRewardAmount();
        final Map<String, String> expandedPlaceholders = new HashMap<>();
        if (placeholders != null && !placeholders.isEmpty()) {
            expandedPlaceholders.putAll(placeholders);
        }
        expandedPlaceholders.put(REWARD_AMOUNT, String.valueOf(rewardAmount));

        switch (reward.getRewardType()) {
            case COMMAND -> handleCommandReward(player, reward, expandedPlaceholders);
            case EXP_LEVELS -> handleExpLevelsReward(player, rewardAmount);
            case EXP_POINTS -> handleExpPointsReward(player, rewardAmount);
            case MONEY -> handleMoneyReward(player, rewardAmount);
            case POINTS -> handlePointsReward(player, rewardAmount);
            case COINS_ENGINE -> handleCoinsEngineReward(player, reward, rewardAmount);
            default -> rewardTypeError(player, reward.getRewardType());
        }

        final String custom = reward.getMessage();
        if (custom != null && !custom.isEmpty()) {
            player.sendMessage(expandPlaceholders(player, custom, expandedPlaceholders));
        }
    }

    private static double ensureProgressionRewardAmount(Reward reward, Progression progression) {
        if (progression == null) {
            return reward.resolveRewardAmount();
        }
        if (!progression.hasRewardAmount()) {
            progression.setRewardAmount(reward.resolveRewardAmount());
        }
        return progression.getRewardAmount();
    }

    private static void handleCommandReward(Player player, Reward reward, Map<String, String> placeholders) {
        for (String raw : reward.getRewardCommands()) {
            final String cmd = expandPlaceholders(player, raw, placeholders);
            ODailyQuests.morePaperLib
                    .scheduling()
                    .globalRegionalScheduler()
                    .run(() -> {
                        try {
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
                            Debugger.write("[RewardCmd] Executed command: " + cmd);
                        } catch (Exception e) {
                            Debugger.write("[RewardCmd] Error while executing command: " + cmd);
                            if (e.getMessage() != null) Debugger.write(e.getMessage());
                            final String msg = QuestsMessages.REWARD_COMMAND_ERROR.toString();
                            if (msg != null) player.sendMessage(msg.replace("%command%", cmd));
                        }
                    });
        }
        sendMsg(player, QuestsMessages.REWARD_COMMAND);
    }

    private static void handleExpLevelsReward(Player player, double amount) {
        ODailyQuests.morePaperLib.scheduling().entitySpecificScheduler(player)
                .run(() -> {
                    player.giveExpLevels((int) amount);
                    Debugger.write("RewardManager: Given " + amount + " EXP levels to " + player.getName() + ".");
                }, null);
        sendMsgAmount(player, QuestsMessages.REWARD_EXP_LEVELS, amount);
    }

    private static void handleExpPointsReward(Player player, double amount) {
        ODailyQuests.morePaperLib.scheduling().entitySpecificScheduler(player)
                .run(() -> {
                    player.giveExp((int) amount);
                    Debugger.write("RewardManager: Given " + amount + " EXP points to " + player.getName() + ".");
                }, null);
        sendMsgAmount(player, QuestsMessages.REWARD_EXP_POINTS, amount);
    }

    private static void handleMoneyReward(Player player, double amount) {
        if (VaultHook.getEconomy() == null) {
            rewardTypeErrorWithVault(player, RewardType.MONEY);
            return;
        }
        VaultHook.getEconomy().depositPlayer(player, amount);
        Debugger.write("RewardManager: Given " + amount + " money to " + player.getName() + ".");
        sendMsgAmount(player, QuestsMessages.REWARD_MONEY, amount);
    }

    private static void handlePointsReward(Player player, double amount) {
        if (TokenManagerHook.getTokenManagerAPI() != null) {
            TokenManagerHook.getTokenManagerAPI().addTokens(player, (int) amount);
            Debugger.write("RewardManager: Given " + amount + " points to " + player.getName() + " via TokenManager.");
            sendMsgAmount(player, QuestsMessages.REWARD_POINTS, amount);
            return;
        }
        if (PlayerPointsHook.isPlayerPointsSetup()) {
            PlayerPointsHook.getPlayerPointsAPI().give(player.getUniqueId(), (int) amount);
            Debugger.write("RewardManager: Given " + amount + " points to " + player.getName() + " via PlayerPoints.");
            sendMsgAmount(player, QuestsMessages.REWARD_POINTS, amount);
            return;
        }
        rewardTypeErrorNoPoints(player, RewardType.POINTS);
    }

    /**
     * Keeps the existing COINS_ENGINE configuration key compatible with both the modern
     * ExcellentEconomy replacement and older CoinsEngine installations. Both integrations are
     * reflection-based because NightExpress currently does not publish these API artifacts to a
     * public Maven repository used by this project.
     */
    private static void handleCoinsEngineReward(Player player, Reward reward, double amount) {
        if (PluginUtils.isPluginEnabled("ExcellentEconomy") && tryExcellentEconomyReward(player, reward, amount)) {
            return;
        }
        if (PluginUtils.isPluginEnabled("CoinsEngine") && tryLegacyCoinsEngineReward(player, reward, amount)) {
            return;
        }
        rewardTypeError(player, reward.getRewardType());
    }

    @SuppressWarnings("unchecked")
    private static boolean tryExcellentEconomyReward(Player player, Reward reward, double amount) {
        try {
            final Class<?> apiClass = Class.forName("su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI");
            final Object api = Bukkit.getServicesManager().load((Class<Object>) apiClass);
            if (api == null) {
                PluginLogger.error("ExcellentEconomy is enabled but its Bukkit service is not registered.");
                return false;
            }

            final String currencyId = reward.getRewardCurrency();
            final Method hasCurrency = apiClass.getMethod("hasCurrency", String.class);
            if (!Boolean.TRUE.equals(hasCurrency.invoke(api, currencyId))) {
                currencyError(player, currencyId);
                return true;
            }

            final Method deposit = apiClass.getMethod("deposit", Player.class, String.class, double.class);
            if (!Boolean.TRUE.equals(deposit.invoke(api, player, currencyId, amount))) {
                PluginLogger.error("ExcellentEconomy rejected the reward deposit for " + player.getName() + ".");
                return false;
            }

            Debugger.write("RewardManager: Given " + amount + " " + currencyId + " to " + player.getName() + " via ExcellentEconomy.");
            sendMsgAmountAndCurrency(
                    player,
                    QuestsMessages.REWARD_COINS_ENGINE,
                    amount,
                    TextFormatter.format(reward.getRewardCurrencyDisplayName())
            );
            return true;
        } catch (ReflectiveOperationException exception) {
            PluginLogger.error("Unable to use ExcellentEconomy API: " + exception.getMessage());
            return false;
        }
    }

    private static boolean tryLegacyCoinsEngineReward(Player player, Reward reward, double amount) {
        try {
            final Class<?> apiClass = Class.forName("su.nightexpress.coinsengine.api.CoinsEngineAPI");
            final Class<?> currencyClass = Class.forName("su.nightexpress.coinsengine.api.currency.Currency");
            final Method getCurrency = apiClass.getMethod("getCurrency", String.class);
            final Object currency = getCurrency.invoke(null, reward.getRewardCurrency());
            if (currency == null) {
                currencyError(player, reward.getRewardCurrency());
                return true;
            }

            final Method addBalance = apiClass.getMethod("addBalance", Player.class, currencyClass, double.class);
            addBalance.invoke(null, player, currency, amount);
            Debugger.write("RewardManager: Given " + amount + " " + reward.getRewardCurrency() + " to " + player.getName() + " via legacy CoinsEngine.");
            sendMsgAmountAndCurrency(
                    player,
                    QuestsMessages.REWARD_COINS_ENGINE,
                    amount,
                    TextFormatter.format(reward.getRewardCurrencyDisplayName())
            );
            return true;
        } catch (ReflectiveOperationException exception) {
            PluginLogger.error("Unable to use the legacy CoinsEngine API: " + exception.getMessage());
            return false;
        }
    }

    private static String expandPlaceholders(Player player, String raw, Map<String, String> placeholders) {
        String s = TextFormatter.format(TextFormatter.format(player, raw)).replace("%player%", player.getName());
        if (placeholders == null || placeholders.isEmpty()) return s;
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            final String k = e.getKey();
            final String v = e.getValue();
            if (k != null && v != null) s = s.replace(k, v);
        }
        return s;
    }

    private static void sendMsg(Player player, QuestsMessages qm) {
        final String msg = qm.getMessage(player);
        if (msg != null) player.sendMessage(msg);
    }

    private static void sendMsgAmount(Player player, QuestsMessages qm, double amount) {
        final String msg = qm.getMessage(player);
        if (msg != null) player.sendMessage(msg.replace(REWARD_AMOUNT, String.valueOf(amount)));
    }

    private static void sendMsgAmountAndCurrency(Player player, QuestsMessages qm, double amount, String currencyName) {
        final String msg = qm.getMessage(player);
        if (msg != null) {
            player.sendMessage(msg.replace(REWARD_AMOUNT, String.valueOf(amount)).replace("%currencyName%", currencyName));
        }
    }

    private static void rewardTypeError(Player player, RewardType type) {
        PluginLogger.error("Impossible to give the reward to " + player.getName() + ".");
        PluginLogger.error("Reward type is " + type + " but required plugin is not hooked.");
        player.sendMessage(ChatColor.RED + "Impossible to give you your reward. Please contact an administrator.");
    }

    private static void rewardTypeErrorWithVault(Player player, RewardType type) {
        PluginLogger.error("Impossible to give the reward to " + player.getName() + ".");
        PluginLogger.error("Reward type is " + type + " but Vault is not hooked.");
        player.sendMessage(ChatColor.RED + "Impossible to give you your reward. Please contact an administrator.");
    }

    private static void rewardTypeErrorNoPoints(Player player, RewardType type) {
        PluginLogger.error("Impossible to give the reward to " + player.getName() + ".");
        PluginLogger.error("Reward type is " + type + " but no points plugin is hooked.");
        player.sendMessage(ChatColor.RED + "Impossible to give you your reward. Please contact an administrator.");
    }

    private static void currencyError(Player player, String currency) {
        PluginLogger.error("Impossible to give the reward to " + player.getName() + ".");
        PluginLogger.error("Economy currency '" + currency + "' not found.");
        player.sendMessage(ChatColor.RED + "Impossible to give you your reward. Please contact an administrator.");
    }
}
