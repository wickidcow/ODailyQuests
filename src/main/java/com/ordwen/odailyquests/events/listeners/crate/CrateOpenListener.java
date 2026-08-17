package com.ordwen.odailyquests.events.listeners.crate;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.quests.player.progression.PlayerProgressor;
import com.ordwen.odailyquests.tools.PluginLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;

/**
 * Runtime ExcellentCrates hook that avoids a hard compile dependency on its API artifact.
 */
public final class CrateOpenListener extends PlayerProgressor implements Listener {

    private static final String CRATE_OPEN_EVENT = "su.nightexpress.excellentcrates.api.event.CrateOpenEvent";

    private CrateOpenListener() {
    }

    @SuppressWarnings("unchecked")
    public static void register(PluginManager pluginManager, ODailyQuests plugin) {
        try {
            final Class<?> rawEventClass = Class.forName(CRATE_OPEN_EVENT);
            if (!Event.class.isAssignableFrom(rawEventClass)) {
                PluginLogger.warn("ExcellentCrates CrateOpenEvent is not a Bukkit Event; crate quests are disabled.");
                return;
            }

            final Class<? extends Event> eventClass = (Class<? extends Event>) rawEventClass;
            final CrateOpenListener listener = new CrateOpenListener();
            pluginManager.registerEvent(
                    eventClass,
                    listener,
                    EventPriority.HIGHEST,
                    (ignored, event) -> listener.onCrateOpenEvent(event),
                    plugin,
                    false
            );
        } catch (ClassNotFoundException exception) {
            PluginLogger.warn("ExcellentCrates is enabled but CrateOpenEvent was not found; crate quests are disabled.");
        }
    }

    private void onCrateOpenEvent(Event event) {
        if (event instanceof Cancellable cancellable && cancellable.isCancelled()) {
            Debugger.write("CrateOpenListener: onCrateOpenEvent is cancelled.");
            return;
        }

        try {
            final Method getPlayer = event.getClass().getMethod("getPlayer");
            final Object playerObject = getPlayer.invoke(event);
            if (!(playerObject instanceof Player player)) {
                Debugger.write("CrateOpenListener: ExcellentCrates event did not return a Bukkit Player.");
                return;
            }

            Debugger.write("CrateOpenListener: onCrateOpenEvent summoned for " + player.getName() + ".");
            setPlayerQuestProgression(event, player, 1, "CRATE_OPEN");
        } catch (ReflectiveOperationException exception) {
            PluginLogger.warn("Could not read ExcellentCrates CrateOpenEvent: " + exception.getMessage());
        }
    }
}
