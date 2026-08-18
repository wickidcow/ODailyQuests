package com.ordwen.odailyquests.quests.player.progression;

import com.jeff_media.customblockdata.CustomBlockData;
import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.api.ODailyQuestsAPI;
import com.ordwen.odailyquests.api.events.QuestCompletedEvent;
import com.ordwen.odailyquests.api.events.QuestProgressEvent;
import com.ordwen.odailyquests.configuration.essentials.Antiglitch;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.configuration.essentials.Synchronization;
import com.ordwen.odailyquests.configuration.functionalities.CompleteOnlyOnClick;
import com.ordwen.odailyquests.configuration.functionalities.DisabledWorlds;
import com.ordwen.odailyquests.configuration.functionalities.progression.ProgressionMessage;
import com.ordwen.odailyquests.enums.QuestsMessages;
import com.ordwen.odailyquests.externs.hooks.Protection;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import com.ordwen.odailyquests.quests.types.item.FarmingQuest;
import com.ordwen.odailyquests.tools.QuestPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerProgressor {

    protected static final Set<Material> VERTICAL_PLANTS_UP = Set.of(
            Material.SUGAR_CANE, Material.CACTUS, Material.BAMBOO, Material.KELP_PLANT, Material.TWISTING_VINES_PLANT);
    protected static final Set<Material> VERTICAL_PLANTS_DOWN = Set.of(
            Material.WEEPING_VINES_PLANT, Material.CAVE_VINES_PLANT);

    protected static boolean isVerticalPlant(Material m) {
        return VERTICAL_PLANTS_UP.contains(m) || VERTICAL_PLANTS_DOWN.contains(m);
    }

    public void setPlayerQuestProgression(Event event, Player player, int amount, String questType) {
        if (QuestsManager.getActiveQuests().containsKey(player.getName())) {
            Debugger.write("Active quests contain " + player.getName() + ".");
            checkForProgress(event, player, amount, questType);
        }
    }

    private void checkForProgress(Event event, Player player, int amount, String questType) {
        final var data = ODailyQuestsAPI.getPlayerQuests(player.getName());
        if (data == null) return;

        for (Map.Entry<AbstractQuest, Progression> entry : data.getQuests().entrySet()) {
            final AbstractQuest quest = entry.getKey();
            final Progression progression = entry.getValue();
            if (quest.getQuestType().equals(questType)
                    && !progression.isAchieved()
                    && quest.canProgress(event, progression)) {
                actionQuest(player, progression, quest, amount);
                if (!Synchronization.isSynchronised()) break;
            }
        }
    }

    public void actionQuest(Player player, Progression progression, AbstractQuest quest, int amount) {
        Debugger.write("QuestProgressUtils: actionQuest summoned by " + player.getName()
                + " for " + quest.getQuestName() + " with amount " + amount + ".");
        final QuestProgressEvent event = new QuestProgressEvent(player, progression, quest, amount);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) runProgress(player, progression, quest, amount);
    }

    private void runProgress(Player player, Progression progression, AbstractQuest quest, int amount) {
        if (QuestLoaderUtils.isTimeToRenew(player, QuestsManager.getActiveQuests())) return;
        if (!isAllowedToProgress(player, quest)) return;

        final String questName = QuestPlaceholders.replaceQuestPlaceholders(
                quest.getQuestName(), player, quest, progression, null, null);
        final int required = progression.getRequiredAmount();
        final int remaining = required - progression.getAdvancement();
        final int toAdd = Math.min(amount, remaining);

        Debugger.write("QuestProgressUtils: increasing progression for " + questName + " by " + toAdd + ".");
        for (int i = 0; i < toAdd; i++) progression.increaseAdvancement();

        if (progression.getAdvancement() >= required) {
            if (CompleteOnlyOnClick.isEnabled()) return;

            // Completion mutates player-owned quest state and may send rewards/UI, so it
            // must remain attached to the player's entity scheduler under Folia.
            ODailyQuests.morePaperLib.scheduling().entitySpecificScheduler(player).runDelayed(
                    () -> {
                        if (!player.isOnline()) return;
                        Debugger.write("QuestProgressUtils: QuestCompletedEvent is called.");
                        Bukkit.getPluginManager().callEvent(new QuestCompletedEvent(player, progression, quest));
                    },
                    () -> Debugger.write("QuestProgressUtils: completion skipped because the player entity retired."),
                    1L
            );
            return;
        }

        ProgressionMessage.sendProgressionMessage(
                player, questName, progression.getAdvancement(), progression.getRequiredAmount(), progression.getRewardAmount());
    }

    public boolean isAllowedToProgress(Player player, AbstractQuest quest) {
        if (!player.hasPermission("odailyquests.progress")) {
            Debugger.write("PlayerProgressor: isAllowedToProgress cancelled due to missing permission.");
            return false;
        }
        if (DisabledWorlds.isWorldDisabled(player.getWorld().getName())) {
            Debugger.write("PlayerProgressor: isAllowedToProgress cancelled due to disabled world.");
            return false;
        }
        if (!quest.getRequiredWorlds().isEmpty() && !quest.getRequiredWorlds().contains(player.getWorld().getName())) {
            final String msg = QuestsMessages.NOT_REQUIRED_WORLD.getMessage(player);
            if (msg != null) player.sendMessage(msg);
            return false;
        }
        if (!quest.getRequiredRegions().isEmpty() && !Protection.checkRegion(player, quest.getRequiredRegions())) {
            final String msg = QuestsMessages.NOT_REQUIRED_REGION.getMessage(player);
            if (msg != null) player.sendMessage(msg);
            return false;
        }
        return true;
    }

    public int fits(ItemStack stack, ItemStack[] contents) {
        int result = 0;
        for (ItemStack is : contents) {
            if (is == null) result += stack.getMaxStackSize();
            else if (is.isSimilar(stack)) result += Math.max(stack.getMaxStackSize() - is.getAmount(), 0);
        }
        return result;
    }

    public boolean movingItem(ItemStack result, int recipeAmount, Player player, ClickType click) {
        final ItemStack cursorItem = player.getItemOnCursor();
        if (cursorItem.getType() != Material.AIR) {
            if (cursorItem.getType() == result.getType()) {
                if (cursorItem.getAmount() + recipeAmount > cursorItem.getMaxStackSize()) {
                    return click == ClickType.LEFT || click == ClickType.RIGHT;
                }
            } else return true;
        }
        return false;
    }

    protected boolean isPlayerPlacedBlock(Block block, Material material) {
        if (material.isBlock() && Antiglitch.isStorePlacedBlocks()) {
            final PersistentDataContainer pdc = new CustomBlockData(block, ODailyQuests.INSTANCE);
            if (!pdc.has(Antiglitch.PLACED_KEY, PersistentDataType.STRING)) {
                Debugger.write("PlayerProgressor: isPlayerPlacedBlock no PLACED_KEY, not a placed block.");
                return false;
            }
            final String previousType = pdc.get(Antiglitch.PLACED_KEY, PersistentDataType.STRING);
            if (previousType != null && previousType.equals(material.name())) {
                Debugger.write("PlayerProgressor: isPlayerPlacedBlock cancelled, block was placed (type=" + previousType + ").");
                return true;
            }
            Debugger.write("PlayerProgressor: isPlayerPlacedBlock type changed (" + previousType
                    + " -> " + material.name() + "), allow.");
            return false;
        }
        Debugger.write("PlayerProgressor: isPlayerPlacedBlock not storing placed blocks, or material is not a block.");
        return false;
    }

    protected void handleDrops(Event event, Player player, List<Item> drops) {
        Debugger.write("PlayerProgressor: handleDrops summoned.");
        for (Item item : drops) {
            final ItemStack droppedItem = item.getItemStack();
            FarmingQuest.setCurrent(new ItemStack(droppedItem.getType()));
            setPlayerQuestProgression(event, player, droppedItem.getAmount(), "FARMING");
        }
    }

    protected void storeBrokenBlockMetadata(Collection<? extends ItemStack> drops, Player player) {
        for (ItemStack drop : drops) {
            final ItemMeta dropMeta = drop.getItemMeta();
            if (dropMeta == null) continue;
            dropMeta.getPersistentDataContainer().set(
                    Antiglitch.BROKEN_KEY, PersistentDataType.STRING, player.getUniqueId().toString());
            drop.setItemMeta(dropMeta);
        }
    }
}
