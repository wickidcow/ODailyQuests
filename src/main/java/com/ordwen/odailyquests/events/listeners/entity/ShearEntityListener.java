package com.ordwen.odailyquests.events.listeners.entity;

import com.ordwen.odailyquests.configuration.essentials.Debugger;

import com.ordwen.odailyquests.configuration.integrations.RoseStackerEnabled;
import com.ordwen.odailyquests.quests.player.progression.PlayerProgressor;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;

public class ShearEntityListener extends PlayerProgressor implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShearEntityEvent(PlayerShearEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final Entity entity = event.getEntity();

        int shearedItemAmount = 1;

        if (RoseStackerEnabled.isEnabled()) {
            Debugger.write("ShearEntityListener: RoseStacker enabled, attempting to read stack size.");
            final RoseStackerAPI api = RoseStackerAPI.getInstance();
            final StackedEntity stacked = api.getStackedEntity((LivingEntity) entity);

            if (stacked != null) {
                final int stackSize = stacked.getStackSize();
                Debugger.write("ShearEntityListener: Stacked entity detected, stack size = " + stackSize);
                shearedItemAmount = stackSize;
            } else {
                Debugger.write("ShearEntityListener: No stacked entity found (null), using default amount = 1.");
            }
        }

        Debugger.write("=========================================================================================");
        Debugger.write("ShearEntityListener: Shear event by " + player.getName() + " on " + entity.getType());

        setPlayerQuestProgression(event, player, shearedItemAmount, "SHEAR");
    }
}
