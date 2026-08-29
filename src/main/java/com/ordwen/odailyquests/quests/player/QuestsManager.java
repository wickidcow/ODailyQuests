package com.ordwen.odailyquests.quests.player;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.configuration.essentials.QuestsPerCategory;
import com.ordwen.odailyquests.quests.categories.CategoriesLoader;
import com.ordwen.odailyquests.quests.categories.Category;
import com.ordwen.odailyquests.quests.conditions.placeholder.PlaceholderRuleSetEvaluator;
import com.ordwen.odailyquests.quests.features.QuestFeatures;
import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.quests.types.shared.EntityQuest;
import com.ordwen.odailyquests.quests.types.shared.ItemQuest;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Central manager for player quest lifecycle and quest selection.
 *
 * <p>The active map is concurrent because Folia can run different players on different
 * region threads at the same time.</p>
 */
public class QuestsManager implements Listener {

    private final ODailyQuests plugin;
    private static final Map<String, PlayerQuests> activeQuests = new ConcurrentHashMap<>();

    public QuestsManager(ODailyQuests oDailyQuests) {
        this.plugin = oDailyQuests;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Debugger.write("[EVENT START]");
        Debugger.write("PlayerJoinEvent triggered.");

        final Player player = event.getPlayer();
        final String playerName = player.getName();
        final UUID uuid = player.getUniqueId();

        Debugger.write("Player " + playerName + " joined the server.");
        Debugger.write("Player UUID is " + uuid);

        if (!activeQuests.containsKey(playerName)) {
            Debugger.write("Player " + playerName + " is not in the array.");
            plugin.getDatabaseManager().loadQuestsForPlayer(playerName);
        } else {
            Debugger.write("Player " + playerName + " is already in the array.");
            PluginLogger.warn(playerName + " detected into the array. This is not supposed to happen!");
            PluginLogger.warn("If the player can't make his quests progress, please contact the plugin developer.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Debugger.write("[EVENT START]");
        Debugger.write("PlayerQuitEvent triggered.");

        final Player player = event.getPlayer();
        final String playerName = player.getName();
        final String playerUUID = player.getUniqueId().toString();
        final PlayerQuests playerQuests = activeQuests.get(playerName);

        if (playerQuests == null) {
            Debugger.write("Player " + playerName + " not found in the array.");
            PluginLogger.warn("Player quests not found for player " + playerName);
            return;
        }

        plugin.getDatabaseManager().saveProgressionForPlayer(playerName, playerUUID, playerQuests);
        activeQuests.remove(playerName);
        Debugger.write("Player " + playerName + " removed from the array.");
    }

    public static Map<AbstractQuest, Progression> selectRandomQuests(Player player) {
        return selectRandomQuests(player, Map.of());
    }

    /**
     * Selects quests while optionally preserving existing weekly-category assignments.
     */
    public static Map<AbstractQuest, Progression> selectRandomQuests(
            Player player,
            Map<AbstractQuest, Progression> preservedQuests
    ) {
        final Map<AbstractQuest, Progression> quests = new LinkedHashMap<>();
        final Map<String, Category> categoryMap = CategoriesLoader.getAllCategories();
        final Map<String, Integer> resolvedAmounts = QuestsPerCategory.resolveAllFor(player);

        for (Map.Entry<String, Category> entry : categoryMap.entrySet()) {
            final String categoryName = entry.getKey();
            final Category category = entry.getValue();
            final int requiredAmount = resolvedAmounts.getOrDefault(categoryName, 0);
            if (requiredAmount <= 0) continue;

            int alreadyAssigned = 0;
            if (QuestFeatures.isWeeklyCategory(categoryName) && preservedQuests != null && !preservedQuests.isEmpty()) {
                for (Map.Entry<AbstractQuest, Progression> preserved : preservedQuests.entrySet()) {
                    if (alreadyAssigned >= requiredAmount) break;
                    if (!preserved.getKey().getCategoryName().equalsIgnoreCase(categoryName)) continue;
                    quests.put(preserved.getKey(), preserved.getValue());
                    alreadyAssigned++;
                }
            }

            for (int i = alreadyAssigned; i < requiredAmount; i++) {
                final AbstractQuest quest = getRandomQuestForPlayer(quests.keySet(), category, player);
                if (quest == null) {
                    Debugger.write("Not enough quests available to assign to " + player.getName()
                            + " in category " + categoryName + ".");
                    break;
                }
                quests.put(quest, createFreshProgression(quest));
            }
        }

        return quests;
    }

    public static int getDynamicRequiredAmount(String requiredAmountRaw) {
        if (requiredAmountRaw.contains("-")) {
            String[] parts = requiredAmountRaw.split("-", 2);
            int min = Integer.parseInt(parts[0].trim());
            int max = Integer.parseInt(parts[1].trim());
            if (min < 1) min = 1;
            if (max < min) max = min;
            return ThreadLocalRandom.current().nextInt(min, max + 1);
        }

        int amount = Integer.parseInt(requiredAmountRaw);
        return Math.max(amount, 1);
    }

    private static int getRandomIndexFrom(AbstractQuest quest) {
        if (quest instanceof EntityQuest eq && !eq.getRequiredEntities().isEmpty()) {
            return ThreadLocalRandom.current().nextInt(eq.getRequiredEntities().size());
        }
        if (quest instanceof ItemQuest iq && !iq.getRequiredItems().isEmpty()) {
            return ThreadLocalRandom.current().nextInt(iq.getRequiredItems().size());
        }
        return 0;
    }

    /**
     * Applies optional per-quest difficulty multipliers to both the required amount and reward.
     */
    public static Progression createFreshProgression(AbstractQuest quest) {
        final int baseRequired = getDynamicRequiredAmount(quest.getRequiredAmountRaw());
        final int requiredAmount = QuestFeatures.scaleRequiredAmount(quest, baseRequired);
        final double baseReward = quest.getReward().resolveRewardAmount();
        final double rewardAmount = QuestFeatures.scaleRewardAmount(quest, baseReward);
        final Progression progression = new Progression(requiredAmount, rewardAmount, 0, false);

        if (quest.isRandomRequired()) {
            progression.setSelectedRequiredIndex(getRandomIndexFrom(quest));
        }
        return progression;
    }

    public static AbstractQuest getRandomQuestForPlayer(
            Set<AbstractQuest> currentQuests,
            List<AbstractQuest> availableQuests,
            Player player
    ) {
        return weightedPick(getEligibleQuestsForPlayer(currentQuests, availableQuests, player));
    }

    /**
     * Builds the eligible quest pool once so callers that need several selections from the same
     * category can reuse it without repeating permission and PlaceholderAPI checks.
     */
    static List<AbstractQuest> getEligibleQuestsForPlayer(
            Set<AbstractQuest> excludedQuests,
            List<AbstractQuest> availableQuests,
            Player player
    ) {
        final List<AbstractQuest> filteredQuests = new ArrayList<>();
        for (AbstractQuest quest : availableQuests) {
            if (excludedQuests != null && excludedQuests.contains(quest)) continue;
            if (QuestFeatures.isChainOnly(quest)) continue;
            if (!QuestFeatures.isPoolAllowed(player, quest)) continue;
            if (!hasAllPermissions(player, quest.getRequiredPermissions())) continue;
            if (!PlaceholderRuleSetEvaluator.evaluate(player, quest.getPlaceholderConditions(), false)) continue;
            filteredQuests.add(quest);
        }
        return filteredQuests;
    }

    /**
     * Finds a configured chain successor for a completed quest. Chain-only quests are never
     * part of the normal random draw, but can be inserted into the same quest slot here.
     */
    public static AbstractQuest findChainSuccessor(
            AbstractQuest completed,
            Player player,
            Set<AbstractQuest> currentQuests
    ) {
        final Category category = CategoriesLoader.getCategoryByName(completed.getCategoryName());
        if (category == null) return null;

        final List<AbstractQuest> candidates = new ArrayList<>();
        for (AbstractQuest candidate : category) {
            if (currentQuests.contains(candidate)) continue;
            if (!QuestFeatures.chainMatches(candidate, completed)) continue;
            if (!QuestFeatures.isPoolAllowed(player, candidate)) continue;
            if (!hasAllPermissions(player, candidate.getRequiredPermissions())) continue;
            if (!PlaceholderRuleSetEvaluator.evaluate(player, candidate.getPlaceholderConditions(), false)) continue;
            candidates.add(candidate);
        }
        return weightedPick(candidates);
    }

    static AbstractQuest weightedPick(List<AbstractQuest> quests) {
        if (quests == null || quests.isEmpty()) return null;

        double totalWeight = 0.0D;
        for (AbstractQuest quest : quests) totalWeight += QuestFeatures.weight(quest);
        if (totalWeight <= 0.0D) {
            return quests.get(ThreadLocalRandom.current().nextInt(quests.size()));
        }

        double roll = ThreadLocalRandom.current().nextDouble(totalWeight);
        for (AbstractQuest quest : quests) {
            roll -= QuestFeatures.weight(quest);
            if (roll <= 0.0D) return quest;
        }
        return quests.get(quests.size() - 1);
    }

    private static boolean hasAllPermissions(Player player, List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) return true;
        for (String permission : permissions) {
            if (!player.hasPermission(permission)) return false;
        }
        return true;
    }

    public static Map<String, PlayerQuests> getActiveQuests() {
        return activeQuests;
    }
}
