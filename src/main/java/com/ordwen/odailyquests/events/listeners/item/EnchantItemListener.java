package com.ordwen.odailyquests.events.listeners.item;

import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.quests.player.progression.PlayerProgressor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class EnchantItemListener extends PlayerProgressor implements Listener {

    /**
     * Progress ENCHANT quests only after every normal plugin handler has had a chance
     * to accept, cancel, or un-cancel the enchant.
     *
     * <p>Using MONITOR with ignoreCancelled avoids an ordering race at HIGHEST where
     * ODailyQuests could observe the event as cancelled even though a later handler
     * allowed the enchant to complete.</p>
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchantItemEvent(EnchantItemEvent event) {
        Debugger.write("EnchantItemListener: successful enchant by "
                + event.getEnchanter().getName() + " for " + event.getItem().getType() + ".");
        setPlayerQuestProgression(event, event.getEnchanter(), 1, "ENCHANT");
    }
}
