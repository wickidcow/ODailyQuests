
package com.ordwen.odailyquests.events.listeners.integrations.pyrofishingpro;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.quests.player.progression.PlayerProgressor;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;

public final class PyroFishCatchListener extends PlayerProgressor implements Listener, EventExecutor {

    public static final String EVENT_CLASS_NAME = "me.arsmagica.API.PyroFishCatchEvent";

    private final Method getPlayerMethod;

    public static void register(final PluginManager pluginManager, final ODailyQuests plugin) {
        final Plugin pyroFishingPro = pluginManager.getPlugin("PyroFishingPro");
        if (pyroFishingPro == null || !pyroFishingPro.isEnabled()) {
            return;
        }

        try {
            final Class<?> rawEventClass = pyroFishingPro.getClass().getClassLoader().loadClass(EVENT_CLASS_NAME);
            final Class<? extends Event> eventClass = rawEventClass.asSubclass(Event.class);
            final PyroFishCatchListener listener = new PyroFishCatchListener(eventClass);

            pluginManager.registerEvent(
                    eventClass,
                    listener,
                    EventPriority.NORMAL,
                    listener,
                    plugin,
                    true
            );

            PluginLogger.info("Hooked into PyroFishingPro for PYRO_FISH quests.");
        } catch (ClassNotFoundException exception) {
            PluginLogger.warn("Cannot hook into PyroFishingPro: PyroFishCatchEvent was not found.");
            PluginLogger.warn("Your PyroFishingPro version may not expose me.arsmagica.API.PyroFishCatchEvent.");
        } catch (ClassCastException exception) {
            PluginLogger.warn("Cannot hook into PyroFishingPro: PyroFishCatchEvent is not a Bukkit Event.");
        } catch (ReflectiveOperationException exception) {
            PluginLogger.warn("Cannot hook into PyroFishingPro: required event methods were not found.");
        }
    }

    private PyroFishCatchListener(final Class<? extends Event> eventClass) throws NoSuchMethodException {
        this.getPlayerMethod = eventClass.getMethod("getPlayer");
    }

    @Override
    public void execute(final Listener listener, final Event event) throws EventException {
        try {
            Debugger.write("[PyroFishCatchListener] Caught a fish via PyroFishingPro.");

            final Object player = getPlayerMethod.invoke(event);
            if (!(player instanceof Player bukkitPlayer)) {
                Debugger.write("[PyroFishCatchListener] Event player was not a Bukkit Player.");
                return;
            }

            setPlayerQuestProgression(event, bukkitPlayer, 1, "PYRO_FISH");
        } catch (ReflectiveOperationException exception) {
            throw new EventException(exception);
        }
    }
}
