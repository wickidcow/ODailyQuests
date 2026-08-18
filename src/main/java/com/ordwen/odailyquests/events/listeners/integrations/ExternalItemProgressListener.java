package com.ordwen.odailyquests.events.listeners.integrations;

import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.quests.player.progression.PlayerProgressor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Progresses generic optional item quests from real item acquisition/crafting events.
 */
public final class ExternalItemProgressListener extends PlayerProgressor implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack stack = event.getItem().getItemStack();
        progress(event, player, stack, Math.max(1, stack.getAmount()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack result = event.getCurrentItem();
        if (result == null) return;
        // Bukkit supplies the recipe-result stack here. Existing native crafting progression
        // handles exact shift-click math; these integration quests deliberately count at least
        // the output of the recipe activation and never more than the actual result stack amount.
        progress(event, player, result, Math.max(1, result.getAmount()));
    }

    private void progress(org.bukkit.event.Event event, Player player, ItemStack stack, int amount) {
        if (ExternalItemIntegration.isPylonItem(stack)) {
            Debugger.write("ExternalItemProgressListener: Pylon/Rebar item acquired.");
            setPlayerQuestProgression(event, player, amount, "REBAR_ITEM");
        }
        if (ExternalItemIntegration.isMMOItem(stack)) {
            Debugger.write("ExternalItemProgressListener: MMOItems item acquired.");
            setPlayerQuestProgression(event, player, amount, "MMOITEM_ITEM");
        }
        if (ExternalItemIntegration.isItemsAdderItem(stack)) {
            Debugger.write("ExternalItemProgressListener: ItemsAdder item acquired.");
            setPlayerQuestProgression(event, player, amount, "ITEMSADDER_ITEM");
        }
    }
}
