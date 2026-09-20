package com.ordwen.odailyquests.events.listeners.item;

import com.ordwen.odailyquests.configuration.essentials.Debugger;

import com.ordwen.odailyquests.quests.player.progression.PlayerProgressor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

/**
 * Listener for smithing inventory events to track quest progression.
 * <p>
 * This listener monitors smithing actions performed by players
 * and updates their quest progression based on the items they smith.
 */
public class SmithItemListener extends PlayerProgressor implements Listener {

    /**
     * Handles smithing result clicks and updates quest progression accordingly.
     * Only processes non-canceled events with a non-null current item.
     *
     * @param event the smithing inventory click event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSmithItemEvent(SmithItemEvent event) {
        if (event.isCancelled() || event.getCurrentItem() == null) {
            return;
        }

        final ItemStack result = event.getCurrentItem().clone();
        final Player player = (Player) event.getWhoClicked();
        final ClickType click = event.getClick();

        int recipeAmount = result.getAmount();

        if (movingItem(result, recipeAmount, player, click)) {
            return;
        }

        recipeAmount = computeRecipeAmount(event, player, result, recipeAmount);

        if (recipeAmount == 0) {
            return;
        }

        result.setAmount(recipeAmount);

        Debugger.write("SmithItemListener: onSmithItemEvent summoned by " + player.getName() + " for " + result.getType() + ".");
        setPlayerQuestProgression(event, player, result.getAmount(), "CRAFT");
    }

    /**
     * Computes the effective amount to craft/move depending on the click type.
     * Returns 0 when the action should be ignored (blocked by cursor/hotbar/offhand constraints).
     *
     * @param event        the smithing event
     * @param player       the clicking player
     * @param result       the current result item
     * @param recipeAmount the initial result amount
     * @return the computed amount to apply, or 0 to abort processing
     */
    private int computeRecipeAmount(SmithItemEvent event, Player player, ItemStack result, int recipeAmount) {
        return switch (event.getClick()) {
            case NUMBER_KEY -> handleNumberKey(event, recipeAmount);
            case DROP, CONTROL_DROP -> handleDrop(event, recipeAmount);
            case SHIFT_RIGHT, SHIFT_LEFT -> handleShiftClick(event, player, result, recipeAmount);
            case SWAP_OFFHAND -> handleSwapOffhand(player);
            default -> recipeAmount;
        };
    }

    /**
     * Handles hotbar number key swaps on the result slot.
     *
     * @param event        the smithing event
     * @param recipeAmount the current amount
     * @return 0 if the hotbar target slot is occupied, otherwise the original amount
     */
    private int handleNumberKey(SmithItemEvent event, int recipeAmount) {
        return event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) != null ? 0 : recipeAmount;
    }

    /**
     * Handles drop clicks on the result slot.
     *
     * @param event        the smithing event
     * @param recipeAmount the current amount
     * @return 0 if the cursor is not empty (to avoid conflicts), otherwise the original amount
     */
    private int handleDrop(SmithItemEvent event, int recipeAmount) {
        final ItemStack cursor = event.getCursor();
        final boolean cursorNotEmpty = cursor != null && cursor.getType() != Material.AIR;
        return cursorNotEmpty ? 0 : recipeAmount;
    }

    /**
     * Handles shift-click crafting by computing how many results can fit in the player inventory.
     *
     * @param event        the smithing event
     * @param player       the clicking player
     * @param result       the current result item
     * @param recipeAmount the current amount
     * @return the maximum craftable amount adjusted to inventory capacity
     */
    private int handleShiftClick(SmithItemEvent event, Player player, ItemStack result, int recipeAmount) {
        if (recipeAmount == 0) {
            return 0;
        }

        int maxCraftable = getMaxSmithAmount(event.getInventory());
        final int capacity = fits(result, player.getInventory().getStorageContents());

        if (capacity < maxCraftable) {
            // Round up to the next multiple of recipeAmount (keeps batch consistency)
            maxCraftable = (capacity / recipeAmount) * recipeAmount;
        }

        return maxCraftable;
    }

    /**
     * Handles offhand swap attempts on the result slot.
     *
     * @param player the clicking player
     * @return 0 if offhand is occupied, otherwise 1
     */
    private int handleSwapOffhand(Player player) {
        final boolean offhandOccupied = player.getInventory().getItemInOffHand().getType() != Material.AIR;
        return offhandOccupied ? 0 : 1;
    }

    /**
     * Returns the maximum amount of items that can be smithed in the given inventory.
     *
     * @param inv the inventory to check.
     * @return the maximum.
     */
    private int getMaxSmithAmount(SmithingInventory inv) {
        if (inv.getResult() == null) return 0;

        int resultCount = inv.getResult().getAmount();
        int materialCount = Integer.MAX_VALUE;

        for (ItemStack is : inv.getContents())
            if (is != null && is.getAmount() < materialCount) materialCount = is.getAmount();

        return resultCount * materialCount;
    }
}
