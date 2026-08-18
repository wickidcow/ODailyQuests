package com.ordwen.odailyquests.events.listeners.integrations.slimefun;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.quests.player.progression.PlayerProgressor;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;

/** Progresses Slimefun item and true crafting quests without a hard Slimefun dependency. */
public class SlimefunItemListener extends PlayerProgressor implements Listener {

    private static final String MULTIBLOCK_CRAFT_EVENT = "io.github.thebusybiscuit.slimefun4.api.events.MultiBlockCraftEvent";

    public static void register(PluginManager pluginManager, ODailyQuests plugin) {
        SlimefunItemListener listener = new SlimefunItemListener();
        pluginManager.registerEvents(listener, plugin);

        Plugin slimefun = pluginManager.getPlugin("Slimefun");
        if (slimefun == null || !slimefun.isEnabled()) return;

        try {
            Class<?> rawEventClass = slimefun.getClass().getClassLoader().loadClass(MULTIBLOCK_CRAFT_EVENT);
            Class<? extends Event> eventClass = rawEventClass.asSubclass(Event.class);
            pluginManager.registerEvent(
                    eventClass,
                    listener,
                    EventPriority.MONITOR,
                    (registered, event) -> listener.onMultiBlockCraft(event),
                    plugin,
                    true
            );
            PluginLogger.info("Hooked Slimefun MultiBlockCraftEvent for crafting quests.");
        } catch (ClassNotFoundException | ClassCastException exception) {
            PluginLogger.warn("Slimefun crafting quest hook unavailable: " + exception.getMessage());
        }
    }

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
        setPlayerQuestProgression(event, player, amount, "SLIMEFUN_CRAFT");
    }

    private void onMultiBlockCraft(Event event) {
        try {
            Method getPlayer = event.getClass().getMethod("getPlayer");
            Method getOutput = event.getClass().getMethod("getOutput");
            Object playerObject = getPlayer.invoke(event);
            Object outputObject = getOutput.invoke(event);
            if (!(playerObject instanceof Player player) || !(outputObject instanceof ItemStack item)) return;
            if (SlimefunIntegration.getItemId(item) == null) return;
            int amount = Math.max(1, item.getAmount());
            setPlayerQuestProgression(event, player, amount, "SLIMEFUN_ITEM");
            setPlayerQuestProgression(event, player, amount, "SLIMEFUN_CRAFT");
        } catch (ReflectiveOperationException exception) {
            PluginLogger.warn("Unable to read Slimefun MultiBlockCraftEvent: " + exception.getMessage());
        }
    }
}
