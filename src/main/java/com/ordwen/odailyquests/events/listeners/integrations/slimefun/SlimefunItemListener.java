package com.ordwen.odailyquests.events.listeners.integrations.slimefun;

import com.ordwen.odailyquests.quests.player.progression.PlayerProgressor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

/** Progresses SLIMEFUN_ITEM quests from normal crafting and pickup events. */
public class SlimefunItemListener extends PlayerProgressor implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem().getItemStack();
        if (SlimefunIntegration.getItemId(item) == null) return;
        setPlayerQuestProgression(event, player, Math.max(1, item.getAmount()), "SLIMEFUN_ITEM");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack item = event.getCurrentItem();
        if (SlimefunIntegration.getItemId(item) == null) return;
        int amount = item == null ? 1 : Math.max(1, item.getAmount());
        setPlayerQuestProgression(event, player, amount, "SLIMEFUN_ITEM");
    }
}
