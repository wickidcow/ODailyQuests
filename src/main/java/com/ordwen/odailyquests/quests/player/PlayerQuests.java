package com.ordwen.odailyquests.quests.player;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.api.events.AllCategoryQuestsCompletedEvent;
import com.ordwen.odailyquests.api.events.AllQuestsCompletedEvent;
import com.ordwen.odailyquests.api.events.CategoryTotalRewardReachedEvent;
import com.ordwen.odailyquests.api.events.TotalRewardReachedEvent;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.configuration.essentials.RerollMaximum;
import com.ordwen.odailyquests.configuration.essentials.RerollNotAchieved;
import com.ordwen.odailyquests.configuration.functionalities.rewards.TotalRewards;
import com.ordwen.odailyquests.enums.QuestsMessages;
import com.ordwen.odailyquests.quests.categories.CategoriesLoader;
import com.ordwen.odailyquests.quests.categories.Category;
import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Player quest assignment and completion counters. */
public class PlayerQuests {

    public enum ReplaceResult {
        SUCCESS,
        INVALID_INDEX,
        ALREADY_PRESENT
    }

    private final Long timestamp;
    private int achievedQuests;
    private int totalAchievedQuests;
    private int recentRerolls;
    private final Map<AbstractQuest, Progression> quests;
    private final Map<String, Integer> achievedQuestsByCategory = new HashMap<>();
    private final Map<String, Integer> totalAchievedQuestsByCategory = new HashMap<>();

    public PlayerQuests(Long timestamp, Map<AbstractQuest, Progression> quests) {
        this.timestamp = timestamp;
        this.quests = quests;
        recalculateCurrentAchievements();
    }

    private void recalculateCurrentAchievements() {
        achievedQuests = 0;
        achievedQuestsByCategory.clear();
        for (Map.Entry<AbstractQuest, Progression> entry : quests.entrySet()) {
            if (!entry.getValue().isAchieved()) continue;
            achievedQuests++;
            achievedQuestsByCategory.merge(entry.getKey().getCategoryName(), 1, Integer::sum);
        }
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void increaseCategoryAchievedQuests(String category, Player player) {
        increaseCategoryAchievedQuests(category, player, true);
    }

    public synchronized void increaseCategoryAchievedQuests(
            String category,
            Player player,
            boolean allowCompletionEvents
    ) {
        Debugger.write("PlayerQuests: recording completion for " + player.getName() + " in " + category + ".");

        achievedQuests++;
        totalAchievedQuests++;
        achievedQuestsByCategory.merge(category, 1, Integer::sum);
        totalAchievedQuestsByCategory.merge(category, 1, Integer::sum);

        if (allowCompletionEvents) {
            if (achievedQuestsByCategory.getOrDefault(category, 0) == countQuestsInCategory(category)) {
                ODailyQuests.INSTANCE.getServer().getPluginManager()
                        .callEvent(new AllCategoryQuestsCompletedEvent(player, category));
            }
            if (achievedQuests == quests.size()) {
                ODailyQuests.INSTANCE.getServer().getPluginManager()
                        .callEvent(new AllQuestsCompletedEvent(player));
            }
        }

        if (TotalRewards.isGlobalStep(totalAchievedQuests)) {
            ODailyQuests.INSTANCE.getServer().getPluginManager()
                    .callEvent(new TotalRewardReachedEvent(player, totalAchievedQuests));
        }
        if (TotalRewards.isCategoryStep(category, totalAchievedQuestsByCategory.get(category))) {
            ODailyQuests.INSTANCE.getServer().getPluginManager().callEvent(
                    new CategoryTotalRewardReachedEvent(player, category, totalAchievedQuestsByCategory.get(category))
            );
        }
    }

    public synchronized boolean rerollQuest(int index, Player player, boolean bypassMax) {
        final List<AbstractQuest> ordered = new ArrayList<>(quests.keySet());
        if (index < 0 || index >= ordered.size()) return false;

        final AbstractQuest oldQuest = ordered.get(index);
        final Progression oldProgression = quests.get(oldQuest);
        if (!isRerollAllowedProgression(oldProgression, player)) return false;
        if (!bypassMax && !isRerollAllowedMaximum(player)) return false;

        final String categoryName = oldQuest.getCategoryName();
        final Category category = CategoriesLoader.getCategoryByName(categoryName);
        if (category == null) {
            PluginLogger.error("An error occurred while rerolling a quest. The category is null.");
            return false;
        }

        final Set<AbstractQuest> currentWithoutOld = new HashSet<>(quests.keySet());
        currentWithoutOld.remove(oldQuest);
        final long selectionStarted = System.nanoTime();
        final AbstractQuest replacement = QuestsManager.getRandomQuestForPlayer(currentWithoutOld, category, player);
        Debugger.write("PlayerQuests: reroll selection for " + player.getName() + " in category " + categoryName
                + " completed in " + ((System.nanoTime() - selectionStarted) / 1_000_000.0D) + " ms.");
        if (replacement == null) {
            final String msg = QuestsMessages.NO_AVAILABLE_QUESTS_IN_CATEGORY.toString();
            if (msg != null) player.sendMessage(msg.replace("%category%", categoryName));
            return false;
        }

        replaceAtIndex(index, replacement);
        if (!bypassMax) recentRerolls++;

        if (oldProgression != null && oldProgression.isAchieved()) {
            decrementCurrentAchievement(categoryName);
        }
        return true;
    }

    /**
     * Replaces every currently assigned quest as one atomic reroll action. All replacements are
     * selected before the player's quest map is changed, so a missing replacement never leaves
     * the player with a partially rerolled daily set. One successful reroll-all consumes one
     * daily reroll, not one reroll per quest.
     *
     * <p>Eligibility is evaluated once per category and then reused for every replacement from
     * that category. This avoids repeating permission and PlaceholderAPI checks for every slot.</p>
     */
    public synchronized boolean rerollAll(Player player, boolean bypassMax) {
        if (quests.isEmpty()) return false;
        if (!bypassMax && !isRerollAllowedMaximum(player)) return false;

        final List<AbstractQuest> ordered = new ArrayList<>(quests.keySet());
        if (RerollNotAchieved.isRerollIfNotAchieved()) {
            for (AbstractQuest quest : ordered) {
                if (!isRerollAllowedProgression(quests.get(quest), player)) return false;
            }
        }

        final Set<AbstractQuest> currentAssignments = new HashSet<>(quests.keySet());
        final Map<String, List<AbstractQuest>> candidatesByCategory = new HashMap<>();
        final List<AbstractQuest> replacements = new ArrayList<>(ordered.size());
        final long selectionStarted = System.nanoTime();

        for (AbstractQuest oldQuest : ordered) {
            final String categoryName = oldQuest.getCategoryName();
            final Category category = CategoriesLoader.getCategoryByName(categoryName);
            if (category == null) {
                PluginLogger.error("An error occurred while rerolling all quests. Category " + categoryName + " is null.");
                return false;
            }

            List<AbstractQuest> candidates = candidatesByCategory.get(categoryName);
            if (candidates == null) {
                candidates = QuestsManager.getEligibleQuestsForPlayer(currentAssignments, category, player);
                candidatesByCategory.put(categoryName, candidates);
            }

            final AbstractQuest replacement = QuestsManager.weightedPick(candidates);
            if (replacement == null) {
                final String msg = QuestsMessages.NO_AVAILABLE_QUESTS_IN_CATEGORY.toString();
                if (msg != null) player.sendMessage(msg.replace("%category%", categoryName));
                return false;
            }

            replacements.add(replacement);
            candidates.remove(replacement);
        }

        Debugger.write("PlayerQuests: reroll-all selected " + replacements.size() + " replacements across "
                + candidatesByCategory.size() + " categories for " + player.getName() + " in "
                + ((System.nanoTime() - selectionStarted) / 1_000_000.0D) + " ms.");

        final LinkedHashMap<AbstractQuest, Progression> updated = new LinkedHashMap<>();
        for (AbstractQuest replacement : replacements) {
            updated.put(replacement, QuestsManager.createFreshProgression(replacement));
        }
        quests.clear();
        quests.putAll(updated);
        achievedQuests = 0;
        achievedQuestsByCategory.clear();
        if (!bypassMax) recentRerolls++;
        return true;
    }

    private boolean isRerollAllowedProgression(Progression progression, Player player) {
        if (progression != null && progression.isAchieved() && RerollNotAchieved.isRerollIfNotAchieved()) {
            final String msg = QuestsMessages.CANNOT_REROLL_IF_ACHIEVED.toString();
            if (msg != null) player.sendMessage(msg);
            return false;
        }
        return true;
    }

    private boolean isRerollAllowedMaximum(Player player) {
        int max = RerollMaximum.getMaxRerolls();
        if (max > 0 && recentRerolls >= max) {
            final String msg = QuestsMessages.CANNOT_REROLL_IF_MAX.toString();
            if (msg != null) player.sendMessage(msg);
            return false;
        }
        return true;
    }

    public synchronized ReplaceResult setQuestAtIndex(int index, AbstractQuest newQuest) {
        final List<AbstractQuest> ordered = new ArrayList<>(quests.keySet());
        if (index < 0 || index >= ordered.size()) return ReplaceResult.INVALID_INDEX;
        if (quests.containsKey(newQuest)) return ReplaceResult.ALREADY_PRESENT;

        final AbstractQuest oldQuest = ordered.get(index);
        final Progression oldProgression = quests.get(oldQuest);
        replaceAtIndex(index, newQuest);
        if (oldProgression != null && oldProgression.isAchieved()) {
            decrementCurrentAchievement(oldQuest.getCategoryName());
        }
        return ReplaceResult.SUCCESS;
    }

    private void replaceAtIndex(int index, AbstractQuest newQuest) {
        final List<AbstractQuest> ordered = new ArrayList<>(quests.keySet());
        final LinkedHashMap<AbstractQuest, Progression> updated = new LinkedHashMap<>();
        for (int i = 0; i < ordered.size(); i++) {
            AbstractQuest quest = ordered.get(i);
            if (i == index) updated.put(newQuest, QuestsManager.createFreshProgression(newQuest));
            else updated.put(quest, quests.get(quest));
        }
        quests.clear();
        quests.putAll(updated);
    }

    private void decrementCurrentAchievement(String category) {
        achievedQuests = Math.max(0, achievedQuests - 1);
        int categoryCount = achievedQuestsByCategory.getOrDefault(category, 0);
        if (categoryCount <= 1) achievedQuestsByCategory.remove(category);
        else achievedQuestsByCategory.put(category, categoryCount - 1);
    }

    private int countQuestsInCategory(String categoryName) {
        int total = 0;
        for (AbstractQuest quest : quests.keySet()) {
            if (quest.getCategoryName().equalsIgnoreCase(categoryName)) total++;
        }
        return total;
    }

    public void decreaseAchievedQuests() {
        achievedQuests = Math.max(0, achievedQuests - 1);
    }

    public void setAchievedQuests(int value) {
        achievedQuests = Math.max(0, value);
    }

    public void setTotalAchievedQuests(int value) {
        totalAchievedQuests = Math.max(0, value);
    }

    public void setRecentRerolls(int value) {
        recentRerolls = Math.max(0, value);
    }

    public void setTotalCategoryAchievedQuests(String category, int value) {
        totalAchievedQuestsByCategory.put(category, Math.max(0, value));
    }

    public void setTotalAchievedQuestsByCategory(Map<String, Integer> totals) {
        totalAchievedQuestsByCategory.clear();
        if (totals != null) totalAchievedQuestsByCategory.putAll(totals);
    }

    public void addTotalAchievedQuests(int amount) {
        totalAchievedQuests = Math.max(0, totalAchievedQuests + amount);
    }

    public void addRecentReroll(int amount) {
        recentRerolls = Math.max(0, recentRerolls + amount);
    }

    public void addTotalCategoryAchievedQuests(String category, int amount) {
        totalAchievedQuestsByCategory.merge(category, amount, Integer::sum);
        totalAchievedQuestsByCategory.computeIfPresent(category, (key, value) -> Math.max(0, value));
    }

    public void removeTotalAchievedQuests(int amount) {
        totalAchievedQuests = Math.max(totalAchievedQuests - amount, 0);
    }

    public void removeTotalCategoryAchievedQuests(String category, int amount) {
        totalAchievedQuestsByCategory.put(category,
                Math.max(totalAchievedQuestsByCategory.getOrDefault(category, 0) - amount, 0));
    }

    public int getAchievedQuests() {
        return achievedQuests;
    }

    public int getTotalAchievedQuests() {
        return totalAchievedQuests;
    }

    public int getRecentlyRolled() {
        return recentRerolls;
    }

    public Map<AbstractQuest, Progression> getQuests() {
        return quests;
    }

    public Map<String, Integer> getTotalAchievedQuestsByCategory() {
        return totalAchievedQuestsByCategory;
    }

    public int getTotalAchievedQuestsByCategory(String category) {
        return totalAchievedQuestsByCategory.getOrDefault(category, 0);
    }
}
