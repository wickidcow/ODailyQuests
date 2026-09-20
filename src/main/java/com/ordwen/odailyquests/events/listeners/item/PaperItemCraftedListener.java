package com.ordwen.odailyquests.events.listeners.item;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.quests.player.progression.PlayerProgressor;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;

/**
 * Uses Paper's post-craft event when the runtime provides it.
 *
 * <p>Paper 26.3 added ItemCraftedEvent, which fires after the player actually
 * picks up a crafted result. Registering it reflectively keeps ODailyQuests
 * loadable on older supported runtimes while avoiding predictive click math
 * on 26.3+.</p>
 */
public final class PaperItemCraftedListener extends PlayerProgressor implements Listener, EventExecutor {

    private static final String EVENT_CLASS_NAME = "io.papermc.paper.event.inventory.ItemCraftedEvent";

    private final Method getPlayerMethod;
    private final Method getCraftedItemMethod;

    private PaperItemCraftedListener(Class<? extends Event> eventClass) throws NoSuchMethodException {
        this.getPlayerMethod = eventClass.getMethod("getPlayer");
        this.getCraftedItemMethod = eventClass.getMethod("getCraftedItem");
    }

    public static boolean register(PluginManager pluginManager, ODailyQuests plugin) {
        try {
            final Class<?> rawClass = Class.forName(
                    EVENT_CLASS_NAME,
                    false,
                    PaperItemCraftedListener.class.getClassLoader()
            );
            final Class<? extends Event> eventClass = rawClass.asSubclass(Event.class);
            final PaperItemCraftedListener listener = new PaperItemCraftedListener(eventClass);

            pluginManager.registerEvent(
                    eventClass,
                    listener,
                    EventPriority.MONITOR,
                    listener,
                    plugin,
                    true
            );

            PluginLogger.info("Using Paper ItemCraftedEvent for post-craft quest progression.");
            return true;
        } catch (ClassNotFoundException ignored) {
            Debugger.write("[PaperItemCraftedListener] ItemCraftedEvent is unavailable; using CraftItemEvent fallback.");
            return false;
        } catch (ClassCastException | ReflectiveOperationException | LinkageError exception) {
            PluginLogger.warn("Unable to use Paper ItemCraftedEvent; falling back to CraftItemEvent.");
            PluginLogger.warn("Details: " + exception.getMessage());
            return false;
        }
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        try {
            final Object rawPlayer = getPlayerMethod.invoke(event);
            final Object rawItem = getCraftedItemMethod.invoke(event);

            if (!(rawPlayer instanceof Player player) || !(rawItem instanceof ItemStack craftedItem)) {
                return;
            }

            final int amount = craftedItem.getAmount();
            if (amount <= 0) {
                return;
            }

            Debugger.write("PaperItemCraftedListener: successful craft by "
                    + player.getName() + " for " + craftedItem.getType() + " x" + amount + ".");
            setPlayerQuestProgression(event, player, amount, "CRAFT");
        } catch (ReflectiveOperationException exception) {
            throw new EventException(exception);
        }
    }
}
