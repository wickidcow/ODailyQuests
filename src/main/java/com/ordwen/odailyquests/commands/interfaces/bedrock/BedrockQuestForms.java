package com.ordwen.odailyquests.commands.interfaces.bedrock;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.functionalities.CompleteOnlyOnClick;
import com.ordwen.odailyquests.configuration.functionalities.DisabledWorlds;
import com.ordwen.odailyquests.enums.QuestsPermissions;
import com.ordwen.odailyquests.quests.features.RerollService;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.quests.player.progression.QuestLoaderUtils;
import com.ordwen.odailyquests.quests.player.progression.clickable.QuestContext;
import com.ordwen.odailyquests.quests.player.progression.clickable.commands.GetQuestCommand;
import com.ordwen.odailyquests.quests.player.progression.clickable.commands.LocationQuestCommand;
import com.ordwen.odailyquests.quests.player.progression.clickable.commands.ManualCompletionQuestCommand;
import com.ordwen.odailyquests.quests.player.progression.clickable.commands.PlaceholderQuestCommand;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.quests.types.inventory.GetQuest;
import com.ordwen.odailyquests.quests.types.inventory.LocationQuest;
import com.ordwen.odailyquests.quests.types.inventory.PlaceholderQuest;
import com.ordwen.odailyquests.rewards.Reward;
import com.ordwen.odailyquests.tools.QuestPlaceholders;
import com.ordwen.odailyquests.tools.TextFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Optional Floodgate/Cumulus view of the existing daily quest system.
 *
 * This class is only invoked after Floodgate has been enabled. The plugin's
 * default Java inventory UI is unchanged and no Floodgate classes are shaded.
 * Responses may arrive off-thread: all player/quest access runs on the
 * player's entity scheduler (Paper, Purpur, and Folia).
 */
public final class BedrockQuestForms {
    private BedrockQuestForms() {}

    private static final String USE = QuestsPermissions.QUESTS_PLAYER_USE.get();
    private static final String REROLL = QuestsPermissions.QUESTS_PLAYER_REROLL.get();

    public static boolean openIfAvailable(Player player) {
        if (!isBedrock(player)) return false;
        return showMain(player);
    }

    public static boolean openRerollIfAvailable(Player player) {
        if (!isBedrock(player)) return false;
        return showReroll(player);
    }

    private static boolean isBedrock(Player player) {
        final ODailyQuests plugin = ODailyQuests.INSTANCE;
        if (plugin == null || !plugin.isEnabled()
                || !plugin.getFilesManager().getConfigurationFile().getConfig().getBoolean("bedrock_forms.enabled", true)
                || !Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            return false;
        }
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (LinkageError | IllegalStateException exception) {
            plugin.getLogger().warning("Floodgate forms are unavailable: " + exception.getMessage());
            return false;
        }
    }

    private static PlayerQuests active(Player player) {
        final Map<String, PlayerQuests> all = QuestsManager.getActiveQuests();
        // Match the Java inventory's per-player renewal behavior before showing any form.
        QuestLoaderUtils.isTimeToRenew(player, all);
        return all.get(player.getName());
    }

    private static boolean showMain(Player player) {
        final PlayerQuests quests = active(player);
        if (quests == null) {
            player.sendMessage(ChatColor.RED + "Your daily quests are still loading. Try again shortly.");
            return false;
        }

        final List<Map.Entry<AbstractQuest, Progression>> entries =
                new ArrayList<>(quests.getQuests().entrySet());
        final SimpleForm.Builder form = SimpleForm.builder()
                .title("Daily Quests")
                .content("Completed: " + quests.getAchievedQuests() + "/" + entries.size()
                        + "\nChoose a quest for details and rewards.");
        final List<Runnable> actions = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            final AbstractQuest quest = entries.get(i).getKey();
            final Progression progress = entries.get(i).getValue();
            final String name = display(player, quest.getQuestName(), quest, progress, quests);
            final String status = progress.isAchieved() ? "Completed"
                    : Math.min(progress.getAdvancement(), progress.getRequiredAmount())
                    + "/" + progress.getRequiredAmount();
            form.button((i + 1) + ". " + name + "\n" + quest.getCategoryName() + " • " + status);
            actions.add(() -> showQuest(player, quest));
        }

        if (player.hasPermission(REROLL)) {
            form.button("Reroll Quests");
            actions.add(() -> showReroll(player));
        }
        form.button("Refresh");
        actions.add(() -> showMain(player));
        form.button("Close");
        actions.add(() -> {});
        return send(player, form, actions);
    }

    private static void showQuest(Player player, AbstractQuest quest) {
        final PlayerQuests quests = active(player);
        if (quests == null || !quests.getQuests().containsKey(quest)) {
            showMain(player);
            return;
        }
        final Progression progress = quests.getQuests().get(quest);
        final StringBuilder content = new StringBuilder()
                .append("Category: ").append(quest.getCategoryName())
                .append("\nType: ").append(quest.getQuestType())
                .append("\nProgress: ").append(Math.min(progress.getAdvancement(), progress.getRequiredAmount()))
                .append("/").append(progress.getRequiredAmount())
                .append("\nStatus: ").append(progress.isAchieved() ? "Completed" : "In progress");

        for (String description : quest.getQuestDesc()) {
            if (description != null && !description.isBlank()) {
                content.append("\n").append(display(player, description, quest, progress, quests));
            }
        }
        content.append("\n\nReward: ").append(rewardSummary(player, quest, progress, quests));

        final SimpleForm.Builder form = SimpleForm.builder()
                .title(display(player, quest.getQuestName(), quest, progress, quests))
                .content(content.toString());
        final List<Runnable> actions = new ArrayList<>();
        if (!progress.isAchieved() && isCheckable(quest, progress)) {
            form.button("Check / Claim Quest");
            actions.add(() -> claim(player, quest));
        }
        form.button("Back to Daily Quests");
        actions.add(() -> showMain(player));
        form.button("Close");
        actions.add(() -> {});
        send(player, form, actions);
    }

    private static boolean isCheckable(AbstractQuest quest, Progression progress) {
        return quest instanceof GetQuest || quest instanceof LocationQuest || quest instanceof PlaceholderQuest
                || (CompleteOnlyOnClick.isEnabled()
                    && progress.getAdvancement() >= progress.getRequiredAmount());
    }

    private static void claim(Player player, AbstractQuest quest) {
        final PlayerQuests quests = active(player);
        if (quests == null) return;
        // A response from an old form must never act on a quest after a renewal/reroll.
        final Progression progress = quests.getQuests().get(quest);
        if (progress == null || progress.isAchieved()) {
            showMain(player);
            return;
        }

        // Match the Java menu's permission and disabled-world gates for clickable quests.
        if (!player.hasPermission("odailyquests.progress")
                || DisabledWorlds.isWorldDisabled(player.getWorld().getName())) return;

        final QuestContext context = new QuestContext.Builder(player).clickedItem(quest.getMenuItem()).build();
        if (quest instanceof GetQuest get) {
            new GetQuestCommand(context, progress, get).execute();
        } else if (quest instanceof LocationQuest location) {
            new LocationQuestCommand(context, progress, location).execute();
        } else if (quest instanceof PlaceholderQuest placeholder) {
            new PlaceholderQuestCommand(context, progress, placeholder).execute();
        } else if (CompleteOnlyOnClick.isEnabled()
                && progress.getAdvancement() >= progress.getRequiredAmount()) {
            new ManualCompletionQuestCommand(context, progress, quest).execute();
        }
        showMain(player);
    }

    private static boolean showReroll(Player player) {
        if (!player.hasPermission(REROLL)) return false;
        if (!RerollService.canRerollToday(player, true)) return true;

        final SimpleForm.Builder form = SimpleForm.builder()
                .title("Daily Reroll")
                .content("Replace one quest or your entire current set.\n"
                        + "Reroll All counts as one action and uses the configured cost.")
                .button("Reroll One Quest")
                .button("Reroll All Quests")
                .button("Back")
                .button("Close");
        return send(player, form, List.of(
                () -> showRerollSelection(player),
                () -> confirmRerollAll(player),
                () -> showMain(player),
                () -> {}
        ));
    }

    private static void showRerollSelection(Player player) {
        if (!player.hasPermission(REROLL) || !RerollService.canRerollToday(player, true)) return;
        final PlayerQuests quests = active(player);
        if (quests == null) return;

        final List<AbstractQuest> snapshot = new ArrayList<>(quests.getQuests().keySet());
        final SimpleForm.Builder form = SimpleForm.builder()
                .title("Choose Quest to Reroll")
                .content("Select a quest to replace (uses one reroll action).");
        final List<Runnable> actions = new ArrayList<>();
        for (int i = 0; i < snapshot.size(); i++) {
            final int index = i;
            final AbstractQuest quest = snapshot.get(i);
            final Progression progress = quests.getQuests().get(quest);
            form.button((i + 1) + ". " + display(player, quest.getQuestName(), quest, progress, quests));
            actions.add(() -> {
                final PlayerQuests current = active(player);
                if (current == null) return;
                final List<AbstractQuest> now = new ArrayList<>(current.getQuests().keySet());
                // Reject stale forms rather than rerolling a different quest by index.
                if (current != quests || index >= now.size() || now.get(index) != quest) {
                    player.sendMessage(ChatColor.YELLOW + "Your quests changed. Please choose again.");
                    showMain(player);
                    return;
                }
                RerollService.rerollOne(player, index + 1);
                showMain(player);
            });
        }
        form.button("Back");
        actions.add(() -> showReroll(player));
        form.button("Close");
        actions.add(() -> {});
        send(player, form, actions);
    }

    private static void confirmRerollAll(Player player) {
        if (!player.hasPermission(REROLL) || !RerollService.canRerollToday(player, true)) return;
        // Capture the assignment: a pending form must not reroll a newly renewed set.
        final PlayerQuests atConfirmation = active(player);
        if (atConfirmation == null) return;
        final List<AbstractQuest> originalAssignments = new ArrayList<>(atConfirmation.getQuests().keySet());
        final SimpleForm.Builder form = SimpleForm.builder()
                .title("Confirm Reroll All")
                .content("Replace every current daily quest? This spends one reroll action and any configured cost.")
                .button("Confirm Reroll All")
                .button("Cancel");
        send(player, form, List.of(
                () -> {
                    if (!player.hasPermission(REROLL)) return;
                    final PlayerQuests current = active(player);
                    if (current == null) return;
                    if (current != atConfirmation || !sameAssignments(originalAssignments,
                            new ArrayList<>(current.getQuests().keySet()))) {
                        player.sendMessage(ChatColor.YELLOW + "Your quests changed. Please confirm a new reroll.");
                        showMain(player);
                        return;
                    }
                    RerollService.rerollAll(player);
                    showMain(player);
                },
                () -> showReroll(player)
        ));
    }

    /** Preserve both assignment order and identity across a pending Bedrock response. */
    static boolean sameAssignments(List<?> previous, List<?> current) {
        if (previous.size() != current.size()) return false;
        for (int i = 0; i < previous.size(); i++) {
            if (previous.get(i) != current.get(i)) return false;
        }
        return true;
    }

    private static String rewardSummary(Player player, AbstractQuest quest,
                                        Progression progress, PlayerQuests quests) {
        final Reward reward = quest.getReward();
        if (reward == null || reward.getRewardType() == null) return "None";
        final double amount = progress.hasRewardAmount()
                ? progress.getRewardAmount() : reward.getRewardAmount();
        final String summary = switch (reward.getRewardType()) {
            case COMMAND -> "Configured server rewards";
            case EXP_POINTS -> amount + " experience";
            case EXP_LEVELS -> amount + " experience levels";
            case MONEY -> amount + " money";
            case POINTS -> amount + " points";
            case COINS_ENGINE -> amount + " " + reward.getRewardCurrencyDisplayName();
            case NONE -> "None";
        };
        final String message = reward.getMessage();
        return message == null || message.isBlank() ? summary
                : summary + "\n" + display(player, message, quest, progress, quests);
    }

    private static String display(Player player, String raw, AbstractQuest quest,
                                  Progression progress, PlayerQuests quests) {
        if (raw == null) return "";
        final String replaced = QuestPlaceholders.replaceQuestPlaceholders(raw, player, quest, progress, quests, null);
        final String formatted = TextFormatter.format(player, replaced);
        return ChatColor.stripColor(formatted).replace("\r", "").trim();
    }

    private static boolean send(Player player, SimpleForm.Builder form, List<Runnable> actions) {
        // The Floodgate 2.2.x Cumulus API uses a validated form response handler.
        final List<Runnable> handlers = List.copyOf(actions);
        form.validResultHandler(response -> {
            final int index = response.clickedButtonId();
            if (index >= 0 && index < handlers.size()) {
                schedule(player, handlers.get(index));
            }
        });
        try {
            return FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
        } catch (LinkageError | IllegalStateException exception) {
            ODailyQuests.INSTANCE.getLogger().warning("Cannot send Bedrock quest form: " + exception.getMessage());
            return false;
        }
    }

    private static void schedule(Player player, Runnable action) {
        final ODailyQuests plugin = ODailyQuests.INSTANCE;
        if (plugin == null || !plugin.isEnabled()) return;
        // Cumulus response handlers are not guaranteed to execute on the server thread.
        ODailyQuests.morePaperLib.scheduling().entitySpecificScheduler(player).run(
                () -> {
                    if (player.isOnline() && plugin.isEnabled() && player.hasPermission(USE)
                            && isBedrock(player)) action.run();
                },
                () -> { /* player retired or disconnected */ }
        );
    }
}
