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

    private static final String PYRO_API_PLUGIN_NAME = "PyroAPI";
    private static final String CURRENT_EVENT_CLASS_NAME = "me.PyroAPI.Events.PyroFishingPro.PyroFishCatchEvent";
    private static final String LEGACY_EVENT_CLASS_NAME = "me.arsmagica.API.PyroFishCatchEvent";

    private final Method getPlayerMethod;

    public static void register(final PluginManager pluginManager, final ODailyQuests plugin) {
        final Plugin pyroFishingPro = pluginManager.getPlugin("PyroFishingPro");
        if (pyroFishingPro == null || !pyroFishingPro.isEnabled()) {
            return;
        }

        try {
            final EventBinding binding = findSupportedEvent(pluginManager, pyroFishingPro);
            if (binding == null) {
                Debugger.write("[PyroFishCatchListener] PyroFishingPro is enabled, but no supported fish-catch event is available. PYRO_FISH integration was skipped.");
                return;
            }

            final PyroFishCatchListener listener = new PyroFishCatchListener(binding.eventClass());

            pluginManager.registerEvent(
                    binding.eventClass(),
                    listener,
                    EventPriority.NORMAL,
                    listener,
                    plugin,
                    true
            );

            PluginLogger.info("Hooked into PyroFishingPro for PYRO_FISH quests using " + binding.eventClassName() + ".");
        } catch (ClassCastException exception) {
            PluginLogger.warn("Cannot hook into PyroFishingPro: the detected fish-catch event is not a Bukkit Event.");
        } catch (ReflectiveOperationException exception) {
            PluginLogger.warn("Cannot hook into PyroFishingPro: required event methods were not found.");
        }
    }

    private static EventBinding findSupportedEvent(final PluginManager pluginManager, final Plugin pyroFishingPro) {
        final Plugin pyroApi = pluginManager.getPlugin(PYRO_API_PLUGIN_NAME);
        if (pyroApi != null && pyroApi.isEnabled()) {
            final Class<? extends Event> currentEvent = loadEventClass(pyroApi, CURRENT_EVENT_CLASS_NAME);
            if (currentEvent != null) {
                return new EventBinding(CURRENT_EVENT_CLASS_NAME, currentEvent);
            }
        }

        // Some PyroFishingPro builds can expose dependency classes through their own class loader.
        // Try that path too before falling back to the pre-PyroAPI event package.
        final Class<? extends Event> currentEventFromPyroFishing = loadEventClass(pyroFishingPro, CURRENT_EVENT_CLASS_NAME);
        if (currentEventFromPyroFishing != null) {
            return new EventBinding(CURRENT_EVENT_CLASS_NAME, currentEventFromPyroFishing);
        }

        final Class<? extends Event> legacyEvent = loadEventClass(pyroFishingPro, LEGACY_EVENT_CLASS_NAME);
        if (legacyEvent != null) {
            return new EventBinding(LEGACY_EVENT_CLASS_NAME, legacyEvent);
        }

        return null;
    }

    private static Class<? extends Event> loadEventClass(final Plugin owner, final String className) {
        try {
            return owner.getClass().getClassLoader().loadClass(className).asSubclass(Event.class);
        } catch (ClassNotFoundException ignored) {
            return null;
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

    private record EventBinding(String eventClassName, Class<? extends Event> eventClass) {
    }
}
